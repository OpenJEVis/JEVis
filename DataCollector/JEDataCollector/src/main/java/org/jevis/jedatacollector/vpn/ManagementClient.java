package org.jevis.jedatacollector.vpn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client for the OpenVPN management interface protocol (line based, see openvpn management-notes.txt).
 * A background thread parses all incoming lines and keeps the latest tunnel state.
 */
public class ManagementClient implements Closeable {

    private static final Logger logger = LogManager.getLogger(ManagementClient.class);
    private static final int MAX_RECENT_LINES = 30;
    private static final Pattern STATE_LINE = Pattern.compile("^(?:>STATE:)?\\d+,([A-Z_]+),([^,]*)(?:,.*)?$");
    private static final Pattern PASSWORD_NEED = Pattern.compile("^>PASSWORD:Need '([^']+)'.*$");
    private static final Pattern BYTECOUNT = Pattern.compile("^>BYTECOUNT:(\\d+),(\\d+)$");

    private final InputStream in;
    private final OutputStream out;
    private final Closeable connection;
    private final Deque<String> recentLines = new ArrayDeque<>();

    private String state;
    private String stateDetail = "";
    private String pendingPasswordRequest;
    private boolean authFailed;
    private String fatal;
    private boolean closed;
    private long bytesIn;
    private long bytesOut;

    public ManagementClient(InputStream in, OutputStream out, Closeable connection) {
        this.in = in;
        this.out = out;
        this.connection = connection;
        Thread reader = new Thread(this::readLoop, "vpn-management-reader");
        reader.setDaemon(true);
        reader.start();
    }

    /**
     * Wraps a value in double quotes as required for username/password commands.
     */
    static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private void readLoop() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                handleLine(line.trim());
            }
        } catch (IOException e) {
            logger.debug("Management connection closed: {}", e.getMessage());
        } finally {
            synchronized (this) {
                closed = true;
                notifyAll();
            }
        }
    }

    synchronized void handleLine(String line) {
        if (line.isEmpty()) return;
        logger.trace("management: {}", line);
        if (!line.startsWith(">BYTECOUNT:")) {
            recentLines.addLast(line);
            if (recentLines.size() > MAX_RECENT_LINES) recentLines.removeFirst();
        }

        Matcher matcher;
        if ((matcher = STATE_LINE.matcher(line)).matches()) {
            state = matcher.group(1);
            stateDetail = matcher.group(2);
        } else if ((matcher = BYTECOUNT.matcher(line)).matches()) {
            bytesIn = Long.parseLong(matcher.group(1));
            bytesOut = Long.parseLong(matcher.group(2));
        } else if ((matcher = PASSWORD_NEED.matcher(line)).matches()) {
            pendingPasswordRequest = matcher.group(1);
        } else if (line.startsWith(">PASSWORD:Verification Failed")) {
            authFailed = true;
        } else if (line.startsWith(">FATAL:")) {
            fatal = line.substring(">FATAL:".length());
        }
        notifyAll();
    }

    public synchronized void send(String command) throws IOException {
        if (closed) throw new IOException("management connection is closed");
        out.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * Waits for the next incoming event or until the timeout expires.
     */
    public synchronized void awaitEvent(long timeoutMillis) throws InterruptedException {
        if (timeoutMillis > 0 && !closed) wait(timeoutMillis);
    }

    /**
     * Waits until the connection is closed by OpenVPN. Keeps waiting if the thread is interrupted.
     */
    public synchronized boolean awaitClosedUninterruptibly(long timeoutMillis) {
        boolean interrupted = false;
        long deadline = System.currentTimeMillis() + timeoutMillis;
        try {
            while (!closed) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) break;
                try {
                    wait(remaining);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            return closed;
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    /**
     * @return the pending password request type ("Auth", "Private Key", ...) and clears it
     */
    public synchronized String takePasswordRequest() {
        String request = pendingPasswordRequest;
        pendingPasswordRequest = null;
        return request;
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized String getStateDetail() {
        return stateDetail;
    }

    public synchronized boolean isAuthFailed() {
        return authFailed;
    }

    public synchronized String getFatal() {
        return fatal;
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    public synchronized VpnStats getStats() {
        return new VpnStats(bytesIn, bytesOut);
    }

    public synchronized List<String> getRecentLines() {
        return new ArrayList<>(recentLines);
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            logger.debug("Could not close management connection", e);
        }
    }
}
