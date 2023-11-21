package org.jevis.jecc.plugin.meters.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisEventListener;

import javax.swing.event.EventListenerList;

public class MeterEventHandler {
    private static final Logger logger = LogManager.getLogger(MeterEventHandler.class);
    private final EventListenerList listeners = new EventListenerList();

    public void addEventListener(MeterPlanEventListener listener) {

        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
        }

        this.listeners.add(MeterPlanEventListener.class, listener);
    }

    public void removeEventListener(MeterPlanEventListener listener) {
        this.listeners.remove(MeterPlanEventListener.class, listener);
    }

    public MeterPlanEventListener[] getEventListener() {
        return this.listeners.getListeners(MeterPlanEventListener.class);
    }

    private synchronized void notifyListeners(MeterPlanEvent event) {
        logger.error("SampleHandlerEvent: {}", event);
        for (MeterPlanEventListener l : this.listeners.getListeners(MeterPlanEventListener.class)) {
            l.fireEvent(event);
        }
    }

    public void fireEvent(MeterPlanEvent event) {
        notifyListeners(event);
    }
}
