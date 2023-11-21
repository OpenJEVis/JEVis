package org.jevis.jecc.plugin.meters.event;

import java.util.EventListener;

public interface MeterPlanEventListener extends EventListener {
    void fireEvent(MeterPlanEvent event);
}
