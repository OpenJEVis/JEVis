package org.jevis.jecc.plugin.dashboard.datahandler;

import java.util.EventObject;

public class SampleHandlerEvent extends EventObject {

    private final TYPE type;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @param type
     * @throws IllegalArgumentException if source is null.
     */
    public SampleHandlerEvent(Object source, TYPE type) {

        super(source);
        this.type = type;
    }

    @Override
    public String toString() {
        return "SampleHandlerEvent{" +
                "type=" + type +
                ", source=" + source +
                '}';
    }

    public enum TYPE {
        UPDATE
    }


}
