package org.jevis.jedatacollector.vpn;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class ManagementClientTest {

    private PipedOutputStream server;
    private ByteArrayOutputStream commands;
    private ManagementClient client;

    @Before
    public void setUp() throws IOException {
        server = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream(server);
        commands = new ByteArrayOutputStream();
        client = new ManagementClient(in, commands, in);
    }

    @After
    public void tearDown() {
        client.close();
    }

    private void serverSends(String line) throws Exception {
        server.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
        server.flush();
    }

    private void awaitCondition(java.util.function.BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            client.awaitEvent(100);
        }
        assertTrue(condition.getAsBoolean());
    }

    @Test
    public void parsesStateNotificationsAndReplies() throws Exception {
        serverSends(">INFO:OpenVPN Management Interface Version 3 -- type 'help' for more info");
        serverSends(">STATE:1700000000,WAIT,,,,,,");
        awaitCondition(() -> "WAIT".equals(client.getState()));

        serverSends(">STATE:1700000005,CONNECTED,SUCCESS,10.8.0.6,203.0.113.1,1194,,");
        awaitCondition(() -> "CONNECTED".equals(client.getState()));
        assertEquals("SUCCESS", client.getStateDetail());

        serverSends("1700000010,EXITING,SIGTERM,,,,,");
        awaitCondition(() -> "EXITING".equals(client.getState()));
    }

    @Test
    public void detectsPasswordRequestsAndAuthFailure() throws Exception {
        serverSends(">PASSWORD:Need 'Auth' username/password");
        awaitCondition(() -> client.getRecentLines().size() == 1);
        assertEquals("Auth", client.takePasswordRequest());
        assertNull(client.takePasswordRequest());

        serverSends(">PASSWORD:Verification Failed: 'Auth'");
        awaitCondition(client::isAuthFailed);
    }

    @Test
    public void tracksByteCountAndFatalErrors() throws Exception {
        serverSends(">BYTECOUNT:1234,5678");
        awaitCondition(() -> client.getStats().getBytesIn() == 1234);
        assertEquals(5678, client.getStats().getBytesOut());

        serverSends(">FATAL:Cannot open TUN/TAP dev /dev/net/tun");
        awaitCondition(() -> client.getFatal() != null);
    }

    @Test
    public void detectsClosedConnection() throws Exception {
        server.close();
        assertTrue(client.awaitClosedUninterruptibly(3000));
        try {
            client.send("state");
            fail("send on closed connection must fail");
        } catch (IOException expected) {
        }
    }

    @Test
    public void sendsCommandsLineByLine() throws Exception {
        client.send("hold release");
        assertEquals("hold release\n", new String(commands.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void quotesCredentials() {
        assertEquals("\"pa\\\"ss\\\\word\"", ManagementClient.quote("pa\"ss\\word"));
    }
}
