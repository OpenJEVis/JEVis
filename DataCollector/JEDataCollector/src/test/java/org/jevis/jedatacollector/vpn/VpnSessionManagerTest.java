package org.jevis.jedatacollector.vpn;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class VpnSessionManagerTest {

    private static final String PROFILE = "site-a";

    private FakeBackend backend;
    private RecordingNotifier notifier;
    private AtomicLong now;
    private VpnSessionManager manager;

    private static VpnConfig config(long idleTimeout) throws VpnException {
        return new VpnConfig(null, "test", PROFILE).setTimeouts(5, 2, 0, idleTimeout, 600);
    }

    @Before
    public void setUp() {
        backend = new FakeBackend();
        notifier = new RecordingNotifier();
        now = new AtomicLong(1_000_000);
        manager = new VpnSessionManager(backend, notifier, new VpnStatusWriter(), 0, now::get);
    }

    private void awaitState(VpnSessionManager.State expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (manager.stateOf(PROFILE) != expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertEquals(expected, manager.stateOf(PROFILE));
    }

    @Test
    public void sharesTunnelBetweenDataSourcesAndClosesAfterLastRelease() throws Exception {
        VpnSessionManager.Lease first = manager.acquire(config(0), "ds1");
        VpnSessionManager.Lease second = manager.acquire(config(0), "ds2");
        assertEquals(1, backend.connects.get());

        first.close();
        first.close();
        assertEquals(VpnSessionManager.State.UP, manager.stateOf(PROFILE));

        second.close();
        awaitState(VpnSessionManager.State.DOWN);
        assertEquals(1, backend.disconnects.get());
    }

    @Test
    public void reusesTunnelWithinIdleTimeout() throws Exception {
        manager.acquire(config(60), "ds1").close();
        VpnSessionManager.Lease lease = manager.acquire(config(60), "ds2");
        assertEquals(1, backend.connects.get());
        assertEquals(0, backend.disconnects.get());
        lease.close();
    }

    @Test
    public void concurrentAcquiresConnectOnce() throws Exception {
        backend.connectDelay = 200;
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Future<VpnSessionManager.Lease>> leases = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String user = "ds" + i;
            leases.add(pool.submit(() -> manager.acquire(config(0), user)));
        }
        for (Future<VpnSessionManager.Lease> lease : leases) lease.get(5, TimeUnit.SECONDS).close();
        pool.shutdown();

        awaitState(VpnSessionManager.State.DOWN);
        assertEquals(1, backend.connects.get());
        assertEquals(1, backend.disconnects.get());
    }

    @Test
    public void retriesFailedConnect() throws Exception {
        backend.failingConnects = 1;
        VpnSessionManager.Lease lease = manager.acquire(config(0), "ds1");
        assertEquals(2, backend.connects.get());
        assertEquals("failed attempt must be cleaned up", 1, backend.disconnects.get());
        lease.close();
    }

    @Test
    public void givesUpAfterRetriesAndHoldsOff() throws Exception {
        backend.failingConnects = 100;
        VpnConfig config = config(0).setNotifyOnConnectError(true);
        try {
            manager.acquire(config, "ds1");
            fail("expected VpnException");
        } catch (VpnException expected) {
        }
        assertEquals(3, backend.connects.get());
        assertEquals(3, backend.disconnects.get());
        assertEquals(VpnSessionManager.State.DOWN, manager.stateOf(PROFILE));
        assertTrue(notifier.raised.contains("CONNECT_FAILED"));

        try {
            manager.acquire(config, "ds2");
            fail("expected hold-off");
        } catch (VpnException expected) {
            assertTrue(expected.getMessage().contains("not retrying"));
        }
        assertEquals("no new attempt during hold-off", 3, backend.connects.get());

        now.addAndGet(VpnSessionManager.FAILURE_HOLD_OFF + 1);
        backend.failingConnects = 0;
        manager.acquire(config, "ds3").close();
        assertTrue(notifier.resolved.contains("CONNECT_FAILED"));
    }

    @Test
    public void failedTeardownRaisesAlertAndBlocksUntilStopped() throws Exception {
        backend.disconnectWorks = false;
        backend.forceStopWorks = false;
        manager.acquire(config(0), "ds1").close();

        awaitState(VpnSessionManager.State.BROKEN);
        assertTrue(notifier.raised.contains("TEARDOWN_FAILED"));
        try {
            manager.acquire(config(0), "ds2");
            fail("broken tunnel must not be reused");
        } catch (VpnException expected) {
        }

        manager.watchdog();
        assertEquals(VpnSessionManager.State.BROKEN, manager.stateOf(PROFILE));
        assertEquals("reminder is raised (throttled by the notifier)", 2, Collections.frequency(notifier.raised, "TEARDOWN_FAILED"));

        backend.forceStopWorks = true;
        manager.watchdog();
        assertEquals(VpnSessionManager.State.DOWN, manager.stateOf(PROFILE));
        assertTrue(notifier.resolved.contains("TEARDOWN_FAILED"));
    }

    @Test
    public void watchdogClosesSessionExceedingMaxDuration() throws Exception {
        VpnSessionManager.Lease lease = manager.acquire(config(0), "hanging-ds");
        now.addAndGet(601_000);
        manager.watchdog();

        awaitState(VpnSessionManager.State.DOWN);
        assertTrue(notifier.raised.contains("SESSION_TIMEOUT"));
        assertEquals(1, backend.disconnects.get());

        lease.close();
        VpnSessionManager.Lease next = manager.acquire(config(0), "ds2");
        assertEquals("stale lease must not affect the new session", VpnSessionManager.State.UP, manager.stateOf(PROFILE));
        next.close();
    }

    @Test
    public void reconnectsWhenTunnelDropped() throws Exception {
        VpnSessionManager.Lease lease = manager.acquire(config(60), "ds1");
        lease.close();
        backend.lastConnector.alive = false;

        manager.acquire(config(60), "ds2").close();
        assertEquals(2, backend.connects.get());
    }

    @Test
    public void startupCleanupStopsOrphans() {
        backend.orphans.add(PROFILE);
        backend.running = true;
        manager.startupCleanup();

        assertTrue(notifier.raised.contains("ORPHAN_FOUND"));
        assertEquals(1, backend.forceStops.get());
        assertTrue(backend.clearedMarkers.contains(PROFILE));
    }

    @Test
    public void startupCleanupIgnoresStaleMarkerWithoutProcess() {
        backend.orphans.add(PROFILE);
        backend.running = false;
        manager.startupCleanup();

        assertTrue(notifier.raised.isEmpty());
        assertTrue(backend.clearedMarkers.contains(PROFILE));
    }

    @Test
    public void interruptedConnectClosesTunnelInBackground() throws Exception {
        backend.connectDelay = 5000;
        CountDownLatch started = new CountDownLatch(1);
        backend.connectStarted = started;
        Thread worker = new Thread(() -> {
            try {
                manager.acquire(config(0), "ds1");
            } catch (Exception ignored) {
            }
        });
        worker.start();
        assertTrue(started.await(2, TimeUnit.SECONDS));
        worker.interrupt();
        worker.join(2000);

        awaitState(VpnSessionManager.State.DOWN);
        assertEquals(1, backend.disconnects.get());
    }

    @Test
    public void shutdownClosesOpenTunnels() throws Exception {
        manager.acquire(config(60), "ds1");
        manager.shutdown();
        assertEquals(1, backend.disconnects.get());
        assertFalse(notifier.raised.contains("SHUTDOWN_FAILED"));
    }

    @Test
    public void limitsParallelSessions() throws Exception {
        manager = new VpnSessionManager(backend, notifier, new VpnStatusWriter(), 1, now::get);
        VpnSessionManager.Lease a = manager.acquire(config(0), "ds1");

        VpnConfig other = new VpnConfig(null, "other", "site-b").setTimeouts(1, 0, 0, 0, 600);
        long start = System.currentTimeMillis();
        try {
            manager.acquire(other, "ds2");
            fail("second session must wait for a free slot");
        } catch (VpnException expected) {
            assertTrue(System.currentTimeMillis() - start >= 900);
        }

        a.close();
        long deadline = System.currentTimeMillis() + 5000;
        while (manager.stateOf(PROFILE) != VpnSessionManager.State.DOWN && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        now.addAndGet(VpnSessionManager.FAILURE_HOLD_OFF + 1);
        manager.acquire(other, "ds3").close();
    }

    static class RecordingNotifier implements VpnAlertNotifier {
        final List<String> raised = Collections.synchronizedList(new ArrayList<>());
        final List<String> resolved = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void raise(AlertType type, String profile, VpnConfig config, String details) {
            raised.add(type.name());
        }

        @Override
        public void resolve(AlertType type, String profile, VpnConfig config, String details) {
            resolved.add(type.name());
        }
    }

    static class FakeBackend implements VpnBackend {
        final AtomicInteger connects = new AtomicInteger();
        final AtomicInteger disconnects = new AtomicInteger();
        final AtomicInteger forceStops = new AtomicInteger();
        final List<String> orphans = new ArrayList<>();
        final List<String> clearedMarkers = Collections.synchronizedList(new ArrayList<>());
        volatile int failingConnects;
        volatile long connectDelay;
        volatile boolean disconnectWorks = true;
        volatile boolean forceStopWorks = true;
        volatile boolean running;
        volatile CountDownLatch connectStarted;
        volatile FakeConnector lastConnector;

        @Override
        public VpnConnector createConnector(String profile) {
            lastConnector = new FakeConnector(this);
            return lastConnector;
        }

        @Override
        public List<String> findOrphanedSessions() {
            return orphans;
        }

        @Override
        public TunnelStatus status(String profile) {
            return running ? TunnelStatus.RUNNING : TunnelStatus.STOPPED;
        }

        @Override
        public boolean forceStop(String profile) {
            forceStops.incrementAndGet();
            return forceStopWorks;
        }

        @Override
        public void clearMarker(String profile) {
            clearedMarkers.add(profile);
        }
    }

    static class FakeConnector implements VpnConnector {
        private final FakeBackend backend;
        volatile boolean alive;

        FakeConnector(FakeBackend backend) {
            this.backend = backend;
        }

        @Override
        public void connect(VpnConfig config) throws VpnException, InterruptedException {
            backend.connects.incrementAndGet();
            if (backend.connectStarted != null) backend.connectStarted.countDown();
            if (backend.connectDelay > 0) Thread.sleep(backend.connectDelay);
            synchronized (backend) {
                if (backend.failingConnects > 0) {
                    backend.failingConnects--;
                    throw new VpnException("simulated connect failure");
                }
            }
            alive = true;
        }

        @Override
        public boolean disconnect() {
            backend.disconnects.incrementAndGet();
            alive = false;
            return backend.disconnectWorks;
        }

        @Override
        public boolean isAlive() {
            return alive;
        }

        @Override
        public boolean isExternal() {
            return false;
        }

        @Override
        public VpnStats getStats() {
            return new VpnStats(100, 200);
        }

        @Override
        public String getDiagnostics() {
            return "fake";
        }
    }
}
