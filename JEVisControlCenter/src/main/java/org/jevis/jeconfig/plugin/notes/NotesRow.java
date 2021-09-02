package org.jevis.jeconfig.plugin.notes;

import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

public class NotesRow {
    private final DateTime timeStamp;
    private final String note;
    private final JEVisObject object;

    public NotesRow(DateTime timeStamp, String note, JEVisObject object) {
        this.timeStamp = timeStamp;
        this.object = object;
        this.note = note;
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public String getNote() {
        return note;
    }

    public JEVisObject getObject() {
        return object;
    }
}
