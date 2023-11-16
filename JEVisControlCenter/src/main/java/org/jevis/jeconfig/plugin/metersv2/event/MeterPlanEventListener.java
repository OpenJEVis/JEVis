package org.jevis.jeconfig.plugin.metersv2.event;

import java.util.EventListener;

public interface MeterPlanEventListener extends EventListener {
    void fireEvent(MeterPlanEvent event);
}
