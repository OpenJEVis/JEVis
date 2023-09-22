package org.jevis.jeconfig.plugin.metersv2.event;

import org.jevis.jeconfig.plugin.dashboard.datahandler.SampleHandlerEvent;

import java.util.EventListener;

public interface MeterPlanEventListener extends EventListener {
    void fireEvent(MeterPlanEvent event);
}
