package org.jevis.jeconfig.plugin.metersv2.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisEventListener;

import javax.swing.event.EventListenerList;

public class PrecisionEventHandler {
    private final EventListenerList listeners = new EventListenerList();
    private static final Logger logger = LogManager.getLogger(PrecisionEventHandler.class);

    public void addEventListener(PrecisionEventListener listener) {

        if (this.listeners.getListeners(JEVisEventListener.class).length > 0) {
        }

        this.listeners.add(PrecisionEventListener.class, listener);
    }

    public void removeEventListener(PrecisionEventListener listener) {
        this.listeners.remove(PrecisionEventListener.class, listener);
    }

    public PrecisionEventListener[] getEventListener() {
        return this.listeners.getListeners(PrecisionEventListener.class);
    }

    private synchronized void notifyListeners(PrecisionEvent event) {
        logger.error("PrecisionEvent: {}", event);
        for (PrecisionEventListener l : this.listeners.getListeners(PrecisionEventListener.class)) {
            l.fireEvent(event);
        }
    }

    public void fireEvent(PrecisionEvent event){
        notifyListeners(event);
    }
}
