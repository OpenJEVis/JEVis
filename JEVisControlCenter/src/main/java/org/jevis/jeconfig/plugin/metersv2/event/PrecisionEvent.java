package org.jevis.jeconfig.plugin.metersv2.event;

import java.util.EventObject;

public class PrecisionEvent extends EventObject{

    public TYPE getType() {
        return type;
    }

    private TYPE type;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public PrecisionEvent(Object source, TYPE type) {
        super(source);
        this.type = type;
    }

    public enum TYPE {
        INCREASE,
        DECREASE,
    }
}
