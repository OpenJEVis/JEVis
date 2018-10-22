package org.jevis.commons.task;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TaskStep {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private final DateTime time;
    private String message = "";
    private String type = "";

    public TaskStep(String type, Object message) {
        this.type = type;
        this.time = new DateTime();

        if (message instanceof DateTime) {
            this.message = FORMATTER.print((DateTime) message);
        } else {
            this.message = message.toString();
        }


    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public DateTime getTime() {
        return time;
    }
}
