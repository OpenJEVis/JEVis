package org.jevis.jedatacollector.vpn;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class ThrottlingAlertNotifierTest {

    private static final long INTERVAL = 3600_000;

    private final List<String> subjects = new ArrayList<>();
    private final List<Boolean> priorities = new ArrayList<>();
    private final AtomicLong now = new AtomicLong(0);
    private ThrottlingAlertNotifier notifier;

    @Before
    public void setUp() {
        notifier = new ThrottlingAlertNotifier((subject, body, priority, config) -> {
            subjects.add(subject);
            priorities.add(priority);
        }, INTERVAL, now::get);
    }

    @Test
    public void sendsFirstAlertThenRemindersPerInterval() {
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        assertEquals(1, subjects.size());
        assertTrue(priorities.get(0));

        now.addAndGet(INTERVAL);
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        assertEquals(2, subjects.size());
        assertTrue(subjects.get(1).contains("REMINDER"));
    }

    @Test
    public void sendsResolvedOnlyForActiveConditions() {
        notifier.resolve(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "ok");
        assertTrue(subjects.isEmpty());

        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        notifier.resolve(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "ok");
        assertEquals(2, subjects.size());
        assertTrue(subjects.get(1).contains("RESOLVED"));
        assertFalse(priorities.get(1));

        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        assertEquals("new incident after resolve is sent immediately", 3, subjects.size());
    }

    @Test
    public void profilesAndTypesAreThrottledSeparately() {
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "a", null, "x");
        notifier.raise(VpnAlertNotifier.AlertType.TEARDOWN_FAILED, "b", null, "x");
        notifier.raise(VpnAlertNotifier.AlertType.SESSION_TIMEOUT, "a", null, "x");
        assertEquals(3, subjects.size());
    }

    @Test
    public void eventsAreSentAgainAfterInterval() {
        notifier.raise(VpnAlertNotifier.AlertType.SESSION_TIMEOUT, "a", null, "x");
        notifier.raise(VpnAlertNotifier.AlertType.SESSION_TIMEOUT, "a", null, "x");
        now.addAndGet(INTERVAL);
        notifier.raise(VpnAlertNotifier.AlertType.SESSION_TIMEOUT, "a", null, "x");
        assertEquals(2, subjects.size());
        assertFalse(subjects.get(1).contains("REMINDER"));
    }
}
