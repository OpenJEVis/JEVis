package org.jevis.jedatacollector.vpn;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OpenVpnConnectorTest {

    private final List<List<String>> commands = new ArrayList<>();
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static VpnConfig config() throws VpnException {
        return new VpnConfig(null, "test", "site-a").setTimeouts(1, 0, 0, 0, 60);
    }

    private OpenVpnBackend backend(int statusExit, int startExit) throws Exception {
        CommandRunner runner = (command, timeout) -> {
            commands.add(command);
            String action = command.get(3);
            if ("status".equals(action)) return new CommandRunner.Result(statusExit, "");
            if ("start".equals(action)) return new CommandRunner.Result(startExit, "profile not found");
            return new CommandRunner.Result(0, "");
        };
        return new OpenVpnBackend(runner, "/usr/local/sbin/jevis-vpn", tmp.newFolder("run"), tmp.newFolder("log"));
    }

    @Test
    public void callsWrapperViaSudoWithProfile() throws Exception {
        backend(OpenVpnBackend.EXIT_NOT_RUNNING, 0).status("site-a");
        assertEquals(java.util.Arrays.asList("sudo", "-n", "/usr/local/sbin/jevis-vpn", "status", "site-a"), commands.get(0));
    }

    @Test
    public void missingSudoSetupFailsWithoutStopping() throws Exception {
        VpnConnector connector = backend(1, 0).createConnector("site-a");
        try {
            connector.connect(config());
            fail("expected VpnException");
        } catch (VpnException expected) {
            assertTrue(expected.getMessage().contains("sudoers"));
        }
        assertTrue("nothing was started, so disconnect succeeds", connector.disconnect());
        assertTrue(commands.stream().noneMatch(c -> "stop".equals(c.get(3))));
    }

    @Test
    public void failedStartKeepsMarkerUntilVerifiedStopped() throws Exception {
        OpenVpnBackend backend = backend(OpenVpnBackend.EXIT_NOT_RUNNING, 4);
        VpnConnector connector = backend.createConnector("site-a");
        try {
            connector.connect(config());
            fail("expected VpnException");
        } catch (VpnException expected) {
            assertTrue(expected.getMessage().contains("profile not found"));
        }
        assertEquals(Collections.singletonList("site-a"), backend.findOrphanedSessions());

        assertTrue(connector.disconnect());
        assertTrue(backend.findOrphanedSessions().isEmpty());
    }

    @Test
    public void ignoresInvalidMarkerNames() throws Exception {
        OpenVpnBackend backend = backend(0, 0);
        File runDir = new File(tmp.getRoot(), "run");
        assertTrue(new File(runDir, "bad name" + OpenVpnBackend.MARKER_SUFFIX).createNewFile());
        assertTrue(backend.findOrphanedSessions().isEmpty());
    }

    @Test(expected = VpnException.class)
    public void rejectsUnsafeProfileNames() throws Exception {
        new VpnConfig(null, "test", "../../etc/passwd");
    }
}
