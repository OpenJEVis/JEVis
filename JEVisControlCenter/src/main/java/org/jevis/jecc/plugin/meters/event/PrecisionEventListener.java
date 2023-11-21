package org.jevis.jecc.plugin.meters.event;

import java.util.EventListener;

public interface PrecisionEventListener extends EventListener {
    void fireEvent(PrecisionEvent event);
}
