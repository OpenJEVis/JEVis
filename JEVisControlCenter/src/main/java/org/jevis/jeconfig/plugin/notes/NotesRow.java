package org.jevis.jeconfig.plugin.notes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class NotesRow {
    private final UUID uuid;
    private final DateTime timeStamp;
    private final StringProperty noteProperty = new SimpleStringProperty();
    private final JEVisObject object;
    private String note;
    private String user;
    private String tag;
    private List<NoteTag> noteTags;
    private boolean hasChanged = false;

    public NotesRow(DateTime timeStamp, JEVisObject object) {
        this.timeStamp = timeStamp;
        this.object = object;
        this.uuid = UUID.randomUUID();

        try {
            tag = object.getAttribute("Tag").getSamples(timeStamp, timeStamp).get(0).getValueAsString();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        try {
            user = object.getAttribute("User").getSamples(timeStamp, timeStamp).get(0).getValueAsString();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

        try {
            user = object.getAttribute("Value").getSamples(timeStamp, timeStamp).get(0).getValueAsString();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

    }

    public NotesRow(DateTime timeStamp, String note, JEVisObject object, String tag, String user) {
        this.timeStamp = timeStamp;
        this.object = object;
        this.note = note;
        this.uuid = UUID.randomUUID();
        noteProperty.setValue(note);
        noteProperty.addListener((observable, oldValue, newValue) -> {
            hasChanged = true;
        });
        this.tag = tag;
        this.noteTags = NoteTag.parseTags(tag);
        this.user = user;

        noteTags.forEach(noteTag -> {
        });
    }

    public void commit() {
        try {
            JEVisSample noteSample = object.getAttribute("Value").getSamples(timeStamp, timeStamp).get(0);
            noteSample.setValue(noteProperty.get());
            noteSample.commit();


            //JEVisSample tagSample = object.getAttribute("Tag").getSamples(timeStamp, timeStamp).get(0);
            //tagSample.setValue(tag);
            //tagSample.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DateTime getTimeStamp() {
        return timeStamp;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public StringProperty getNoteProperty() {
        return noteProperty;
    }

    public StringProperty notePropertyProperty() {
        return noteProperty;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public JEVisObject getObject() {
        return object;
    }

    public String getTag() {
        return tag;
    }

    public List<NoteTag> getTags() {
        return noteTags;
    }

    public String getUser() {
        return user;
    }

    public void delete() {
        try {
            JEVisAttribute noteSample = object.getAttribute("Value");
            noteSample.deleteSamplesBetween(getTimeStamp(), getTimeStamp());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            JEVisAttribute tagSample = object.getAttribute("Tag");
            tagSample.deleteSamplesBetween(getTimeStamp(), getTimeStamp());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            JEVisAttribute userSample = object.getAttribute("User");
            userSample.deleteSamplesBetween(getTimeStamp(), getTimeStamp());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NotesRow) {
            NotesRow otherRow = (NotesRow) obj;
            return otherRow.getUuid().equals(this.getUuid());
        }

        return false;
    }

    @Override
    public String toString() {
        return "NotesRow{" +
                "uuid=" + uuid.toString() +
                "timeStamp=" + timeStamp +
                ", note='" + note + '\'' +
                ", object=" + object +
                ", user='" + user + '\'' +
                ", tag='" + tag + '\'' +
                //", noteTags=" + noteTags.size() +
                '}';
    }
}
