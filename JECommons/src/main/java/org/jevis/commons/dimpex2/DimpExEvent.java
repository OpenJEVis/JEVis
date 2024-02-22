package org.jevis.commons.dimpex2;

import java.util.EventObject;

public class DimpExEvent extends EventObject {

    private final DimpExEvent.TYPE type;
    private final Object object;

    private final String message;

    public DimpExEvent(Object source, DimpExEvent.TYPE type, Object object, String message) {
        super(source);
        this.type = type;
        this.object = object;
        this.message = message;
    }

    public DimpExEvent.TYPE getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    public String getMessage() {
        return message;
    }

    public enum TYPE {
        OBJECT_TMPFILE_CREATE
    }
}

