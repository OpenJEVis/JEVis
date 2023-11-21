package org.jevis.jecc.plugin.dashboard.datahandler;

import java.util.EventListener;

public interface SampleHandlerEventListener extends EventListener {
    void fireEvent(SampleHandlerEvent event);
}
