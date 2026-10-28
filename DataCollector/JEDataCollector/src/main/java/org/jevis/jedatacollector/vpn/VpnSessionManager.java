package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

/**
 * Opens VPN tunnels on demand for data sources and closes them as soon as no data source needs them.
 * <p>
 * Data sources using the same profile share one tunnel (reference counting). After the last data source
 * released it, the tunnel stays open for the configured idle timeout so that queued data sources of the
 * same site reuse it. A watchdog closes sessions exceeding their maximum duration and keeps retrying to
 * stop tunnels that could not be closed. Every incident that may leave a tunnel open raises an alert.
 */
public class VpnSessionManager {

    static final long FAILURE_HOLD_OFF = 5 * 60 * 1000L;
    private static final Logger logger = LogManager.getLogger(VpnSessionManager.class);
    private static final long WATCHDOG_INTERVAL = 30;
    private static final long SHUTDOWN_TIMEOUT = 30000;
    private static final long WAIT_SLACK = 60000;
    private final VpnBackend backend;
    private final VpnAlertNotifier notifier;
    private final VpnStatusWriter statusWriter;
    private final Semaphore slots;
    private final LongSupplier clock;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread thread = new Thread(r, "vpn-manager");
        thread.setDaemon(true);
        return thread;
    });
    private volatile boolean shuttingDown;
    /**
     * @param maxParallelSessions maximum number of tunnels open at the same time, 0 for unlimited
     *                            (use 1 if the site networks overlap)
     */
    public VpnSessionManager(VpnBackend backend, VpnAlertNotifier notifier, VpnStatusWriter statusWriter, int maxParallelSessions) {
        this(backend, notifier, statusWriter, maxParallelSessions, System::currentTimeMillis);
    }
    VpnSessionManager(VpnBackend backend, VpnAlertNotifier notifier, VpnStatusWriter statusWriter, int maxParallelSessions, LongSupplier clock) {
        this.backend = backend;
        this.notifier = notifier;
        this.statusWriter = statusWriter;
        this.slots = maxParallelSessions > 0 ? new Semaphore(maxParallelSessions, true) : null;
        this.clock = clock;
    }

    /**
     * Orders data sources so that all data sources of one VPN profile follow each other. They are then
     * executed close together and share one tunnel session. Data sources without VPN come first.
     */
    public static List<JEVisObject> groupByVpn(List<JEVisObject> dataSources) {
        List<JEVisObject> result = new ArrayList<>();
        Map<String, List<JEVisObject>> groups = new LinkedHashMap<>();
        for (JEVisObject dataSource : dataSources) {
            String profile = null;
            try {
                VpnConfig config = VpnConfig.forDataSource(dataSource);
                if (config != null) profile = config.getProfile();
            } catch (VpnException e) {
                logger.debug("Could not resolve VPN of data source {}: {}", dataSource.getID(), e.getMessage());
            }
            if (profile == null) {
                result.add(dataSource);
            } else {
                groups.computeIfAbsent(profile, key -> new ArrayList<>()).add(dataSource);
            }
        }
        groups.values().forEach(result::addAll);
        return result;
    }

    private static long maxConnectDuration(VpnConfig config) {
        long retries = config.getMaxRetries();
        return (config.getConnectTimeout() * (retries + 1) + config.getRetryDelay() * retries * (retries + 1) / 2) * 1000;
    }

    /**
     * Cleans up sessions left by a previous run and starts the watchdog.
     */
    public void start() {
        startupCleanup();
        scheduler.scheduleWithFixedDelay(this::watchdog, WATCHDOG_INTERVAL, WATCHDOG_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Makes sure the tunnel of the data source is up. Returns {@link Lease#NONE} if the data source uses no VPN.
     *
     * @throws VpnException if the tunnel could not be established; the data source must not be read then
     */
    public Lease acquire(JEVisObject dataSource) throws VpnException, InterruptedException {
        VpnConfig config = VpnConfig.forDataSource(dataSource);
        if (config == null) return Lease.NONE;
        return acquire(config, dataSource.getName() + ":" + dataSource.getID());
    }

    Lease acquire(VpnConfig config, String user) throws VpnException, InterruptedException {
        if (shuttingDown) throw new VpnException("Collector is shutting down");
        Session s = sessions.computeIfAbsent(config.getProfile(), Session::new);
        long waitDeadline = clock.getAsLong() + maxConnectDuration(config) + WAIT_SLACK;

        synchronized (s) {
            while (true) {
                if (s.state == State.UP) {
                    if (s.connector.isAlive()) return join(s, user);
                    logger.warn("Tunnel of VPN profile {} dropped, reconnecting", s.profile);
                    beginTeardown(s);
                } else if (s.state == State.BROKEN) {
                    throw new VpnException("Tunnel of profile '" + s.profile + "' could not be closed before and is blocked until it is stopped");
                } else if (s.state == State.DOWN) {
                    long sinceFailure = clock.getAsLong() - s.lastFailureTime;
                    if (s.lastFailureTime > 0 && sinceFailure < FAILURE_HOLD_OFF) {
                        throw new VpnException("Connecting profile '" + s.profile + "' failed " + sinceFailure / 1000
                                + " s ago (" + s.lastFailure + "), not retrying before " + FAILURE_HOLD_OFF / 1000 + " s");
                    }
                    s.state = State.CONNECTING;
                    s.config = config;
                    s.generation++;
                    break;
                }

                long remaining = waitDeadline - clock.getAsLong();
                if (remaining <= 0) {
                    throw new VpnException("Timeout while waiting for tunnel of profile '" + s.profile + "' (state " + s.state + ")");
                }
                s.wait(Math.min(remaining, 1000));
            }
        }
        return connect(s, config, user);
    }

    private Lease connect(Session s, VpnConfig config, String user) throws VpnException, InterruptedException {
        boolean slot = false;
        VpnConnector connector = null;
        try {
            if (slots != null) {
                if (!slots.tryAcquire(maxConnectDuration(config), TimeUnit.MILLISECONDS)) {
                    throw new VpnException("No free VPN slot, the maximum number of parallel VPN sessions is reached");
                }
                slot = true;
            }

            VpnException last = null;
            for (int attempt = 0; attempt <= config.getMaxRetries(); attempt++) {
                if (attempt > 0) {
                    logger.info("Retrying VPN profile {} in {} s (attempt {}/{})", s.profile, config.getRetryDelay() * attempt, attempt + 1, config.getMaxRetries() + 1);
                    Thread.sleep(config.getRetryDelay() * 1000 * attempt);
                }
                connector = backend.createConnector(s.profile);
                synchronized (s) {
                    s.connector = connector;
                }
                try {
                    logger.info("Connecting VPN profile {} for {}", s.profile, user);
                    connector.connect(config);
                } catch (VpnException e) {
                    last = e;
                    logger.warn("VPN profile {} attempt {} failed: {}", s.profile, attempt + 1, e.getMessage());
                    VpnConnector failed = connector;
                    connector = null;
                    if (!failed.disconnect() && !backend.forceStop(s.profile)) {
                        markBroken(s, failed, config, slot, "Tunnel could not be stopped after failed connect: " + e.getMessage());
                        slot = false;
                        throw new VpnException("Connect failed and the tunnel could not be stopped: " + e.getMessage(), e);
                    }
                    continue;
                }

                Lease lease;
                synchronized (s) {
                    s.state = State.UP;
                    s.upSince = clock.getAsLong();
                    s.holdsSlot = slot;
                    s.lastFailureTime = 0;
                    s.refs = 0;
                    s.users.clear();
                    lease = join(s, user);
                    s.notifyAll();
                }
                slot = false;
                statusWriter.connected(config);
                notifier.resolve(VpnAlertNotifier.AlertType.CONNECT_FAILED, s.profile, config, "Connected successfully.");
                return lease;
            }
            throw last != null ? last : new VpnException("No connect attempt for profile '" + s.profile + "'");
        } catch (VpnException | InterruptedException | RuntimeException e) {
            if (connector != null) {
                // interrupted (job cancelled) or unexpected error while connecting: close in background
                VpnConnector pending = connector;
                synchronized (s) {
                    if (s.state == State.CONNECTING) {
                        s.connector = pending;
                        s.holdsSlot = slot;
                        slot = false;
                        beginTeardown(s);
                    }
                }
            } else {
                synchronized (s) {
                    if (s.state == State.CONNECTING) {
                        s.state = State.DOWN;
                        s.connector = null;
                        s.notifyAll();
                    }
                }
            }
            if (slot) slots.release();

            if (!(e instanceof InterruptedException)) {
                synchronized (s) {
                    s.lastFailureTime = clock.getAsLong();
                    s.lastFailure = e.getMessage();
                }
                statusWriter.error(config, e.getMessage());
                if (config.isNotifyOnConnectError() && e instanceof VpnException) {
                    notifier.raise(VpnAlertNotifier.AlertType.CONNECT_FAILED, s.profile, config, e.getMessage());
                }
            }
            throw e;
        }
    }

    private Lease join(Session s, String user) {
        cancelIdleClose(s);
        s.refs++;
        s.users.add(user);
        return new Lease(this, s, s.generation);
    }

    void release(Lease lease) {
        Session s = lease.session;
        synchronized (s) {
            if (lease.generation != s.generation || s.state != State.UP) return;
            s.refs = Math.max(0, s.refs - 1);
            if (s.refs > 0) return;

            long idle = s.config.getIdleTimeout();
            if (idle <= 0 || shuttingDown) {
                beginTeardown(s);
            } else {
                long generation = s.generation;
                try {
                    s.idleClose = scheduler.schedule(() -> closeIfIdle(s, generation), idle, TimeUnit.SECONDS);
                } catch (RejectedExecutionException e) {
                    beginTeardown(s);
                }
            }
        }
    }

    private void closeIfIdle(Session s, long generation) {
        synchronized (s) {
            if (s.generation == generation && s.state == State.UP && s.refs == 0) {
                beginTeardown(s);
            }
        }
    }

    private void cancelIdleClose(Session s) {
        if (s.idleClose != null) {
            s.idleClose.cancel(false);
            s.idleClose = null;
        }
    }

    /**
     * Must be called while holding the session monitor. Invalidates all leases of the session and closes
     * the tunnel on a manager thread, so it also works when the calling job thread is interrupted.
     */
    private void beginTeardown(Session s) {
        cancelIdleClose(s);
        s.state = State.CLOSING;
        s.refs = 0;
        s.generation++;
        try {
            scheduler.execute(() -> teardown(s));
        } catch (RejectedExecutionException e) {
            Thread thread = new Thread(() -> teardown(s), "vpn-teardown-" + s.profile);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void teardown(Session s) {
        VpnConnector connector;
        VpnConfig config;
        String users;
        synchronized (s) {
            if (s.state != State.CLOSING) return;
            connector = s.connector;
            config = s.config;
            users = String.join(", ", s.users);
        }

        VpnStats stats = connector != null ? connector.getStats() : VpnStats.EMPTY;
        boolean down = connector == null || connector.disconnect() || backend.forceStop(s.profile);
        statusWriter.disconnected(config, stats);

        if (down) {
            synchronized (s) {
                s.state = State.DOWN;
                s.connector = null;
                s.users.clear();
                releaseSlot(s);
                s.notifyAll();
            }
            logger.info("VPN profile {} disconnected ({})", s.profile, stats);
        } else {
            markBroken(s, connector, config, false, "Tunnel could not be closed after use by: " + users);
        }
    }

    private void markBroken(Session s, VpnConnector connector, VpnConfig config, boolean holdsSlot, String reason) {
        synchronized (s) {
            s.state = State.BROKEN;
            s.connector = connector;
            s.config = config;
            if (holdsSlot) s.holdsSlot = true;
            s.notifyAll();
        }
        String diagnostics = connector != null ? connector.getDiagnostics() : "";
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, s.profile, config, reason + "\n\n" + diagnostics);
    }

    private void releaseSlot(Session s) {
        if (s.holdsSlot) {
            s.holdsSlot = false;
            slots.release();
        }
    }

    void watchdog() {
        try {
            for (Session s : sessions.values()) {
                String timeoutDetails = null;
                boolean broken;
                VpnConfig config;
                synchronized (s) {
                    config = s.config;
                    broken = s.state == State.BROKEN;
                    if (s.state == State.UP && !s.connector.isExternal()
                            && clock.getAsLong() - s.upSince > config.getMaxSessionTime() * 1000) {
                        timeoutDetails = "Session open since " + new DateTime(s.upSince) + " (limit " + config.getMaxSessionTime()
                                + " s), active users: " + s.refs + " (" + String.join(", ", s.users) + "). The tunnel is closed now.\n\n"
                                + s.connector.getDiagnostics();
                        beginTeardown(s);
                    }
                }
                if (timeoutDetails != null) {
                    notifier.raise(VpnAlertNotifier.AlertType.SESSION_TIMEOUT, s.profile, config, timeoutDetails);
                }
                if (broken) retryBroken(s, config);
            }
        } catch (RuntimeException e) {
            logger.error("VPN watchdog failed", e);
        }
    }

    private void retryBroken(Session s, VpnConfig config) {
        boolean down = backend.forceStop(s.profile);
        synchronized (s) {
            if (s.state != State.BROKEN) return;
            if (down) {
                s.state = State.DOWN;
                s.connector = null;
                releaseSlot(s);
                s.notifyAll();
            }
        }
        if (down) {
            backend.clearMarker(s.profile);
            notifier.resolve(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, s.profile, config, "The tunnel process is stopped now.");
        } else {
            notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, s.profile, config,
                    "The tunnel process is still running, the collector keeps trying to stop it every " + WATCHDOG_INTERVAL + " s.");
        }
    }

    void startupCleanup() {
        for (String profile : backend.findOrphanedSessions()) {
            VpnBackend.TunnelStatus status = backend.status(profile);
            if (status == VpnBackend.TunnelStatus.STOPPED) {
                logger.info("Removing stale session marker of VPN profile {}, tunnel is not running", profile);
                backend.clearMarker(profile);
                continue;
            }

            boolean stopped = backend.forceStop(profile);
            if (stopped) backend.clearMarker(profile);
            notifier.raise(VpnAlertNotifier.AlertType.ORPHAN_FOUND, profile, null,
                    "A tunnel started by a previous collector run was not closed (crash or kill). Status: " + status
                            + ". Stopped now: " + (stopped ? "yes" : "NO - the collector keeps trying"));
            if (!stopped) {
                Session s = sessions.computeIfAbsent(profile, Session::new);
                markBroken(s, null, null, false, "Leftover tunnel could not be stopped at startup.");
            }
        }
    }

    /**
     * Closes all tunnels, used by the JVM shutdown hook.
     */
    public void shutdown() {
        shuttingDown = true;
        scheduler.shutdownNow();

        ExecutorService closer = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "vpn-shutdown");
            thread.setDaemon(true);
            return thread;
        });
        Map<Session, Future<Boolean>> results = new LinkedHashMap<>();
        for (Session s : sessions.values()) {
            VpnConnector connector;
            synchronized (s) {
                if (s.state == State.DOWN) continue;
                connector = s.connector;
                s.state = State.CLOSING;
                s.generation++;
            }
            results.put(s, closer.submit(() -> (connector != null && connector.disconnect()) || backend.forceStop(s.profile)));
        }

        long deadline = System.currentTimeMillis() + SHUTDOWN_TIMEOUT;
        results.forEach((s, result) -> {
            boolean down;
            try {
                down = result.get(Math.max(1, deadline - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                down = false;
            }
            if (down) {
                logger.info("VPN profile {} closed on shutdown", s.profile);
            } else {
                notifier.raise(VpnAlertNotifier.AlertType.SHUTDOWN_FAILED, s.profile, s.config,
                        "The collector is shutting down and could not close this tunnel. It may still be running.");
            }
        });
        closer.shutdownNow();
        notifier.flush(10000);
    }

    State stateOf(String profile) {
        Session s = sessions.get(profile);
        if (s == null) return State.DOWN;
        synchronized (s) {
            return s.state;
        }
    }

    enum State {
        DOWN, CONNECTING, UP, CLOSING,
        /**
         * Closing failed, the tunnel process may still be running. The watchdog keeps trying to stop it.
         */
        BROKEN
    }

    static final class Session {
        final String profile;
        final Set<String> users = new LinkedHashSet<>();
        State state = State.DOWN;
        int refs;
        long generation;
        VpnConnector connector;
        VpnConfig config;
        long upSince;
        ScheduledFuture<?> idleClose;
        long lastFailureTime;
        String lastFailure;
        boolean holdsSlot;

        Session(String profile) {
            this.profile = profile;
        }
    }

    /**
     * Handle for a data source using a tunnel. Closing it releases the tunnel; closing twice is harmless.
     */
    public static final class Lease implements AutoCloseable {
        public static final Lease NONE = new Lease(null, null, 0);

        private final VpnSessionManager manager;
        private final Session session;
        private final long generation;
        private final AtomicBoolean released = new AtomicBoolean();

        private Lease(VpnSessionManager manager, Session session, long generation) {
            this.manager = manager;
            this.session = session;
            this.generation = generation;
        }

        public boolean isVpn() {
            return session != null;
        }

        @Override
        public void close() {
            if (manager != null && released.compareAndSet(false, true)) {
                manager.release(this);
            }
        }
    }
}
