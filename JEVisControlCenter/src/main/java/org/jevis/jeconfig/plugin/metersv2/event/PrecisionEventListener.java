package org.jevis.jeconfig.plugin.metersv2.event;

import java.util.EventListener;

public interface PrecisionEventListener extends EventListener {
    void fireEvent(PrecisionEvent event);
}
