package org.jevis.jecc.plugin.meters.event;

import java.util.EventObject;

public class PrecisionEvent extends EventObject {

    private final TYPE type;

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

    public TYPE getType() {
        return type;
    }

    public enum TYPE {
        INCREASE,
        DECREASE,
    }
}
