package org.jevis.jeconfig.plugin.meters.event;

import java.util.EventObject;

public class MeterPlanEvent extends EventObject {
    private final TYPE type;

    public MeterPlanEvent(Object source, TYPE type) {
        super(source);
        this.type = type;
    }

    @Override
    public String toString() {
        return "MeterPlanEvent{" +
                "type=" + type +
                ", source=" + source +
                '}';
    }

    public TYPE getType() {
        return type;
    }


    public enum TYPE {
        UPDATE,
        ADD,
        REMOVE,
        FILTER
    }
}
