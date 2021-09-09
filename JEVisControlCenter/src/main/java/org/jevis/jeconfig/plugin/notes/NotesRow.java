package org.jevis.jeconfig.plugin.notes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.util.List;

public class NotesRow {
    private final DateTime timeStamp;
    private String note;
    private StringProperty noteProperty = new SimpleStringProperty();
    private final JEVisObject object;
    private String user;
    private String tag;
    private List<NoteTag> noteTags;
    private boolean hasChanged = false;

    public NotesRow(DateTime timeStamp, JEVisObject object) {
        this.timeStamp = timeStamp;
        this.object = object;

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
            user = object.getAttribute("User Notes").getSamples(timeStamp, timeStamp).get(0).getValueAsString();
        } catch (Exception ex) {
            //ex.printStackTrace();
        }

    }

    public NotesRow(DateTime timeStamp, String note, JEVisObject object, String tag, String user) {
        this.timeStamp = timeStamp;
        this.object = object;
        this.note = note;
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
            JEVisSample noteSample = object.getAttribute("User Notes").getSamples(timeStamp, timeStamp).get(0);
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

    public StringProperty getNoteProperty() {
        return noteProperty;
    }

    public StringProperty notePropertyProperty() {
        return noteProperty;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setNode(String note) {
        this.note = note;
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
            JEVisAttribute noteSample = object.getAttribute("User Notes");
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

    @Override
    public String toString() {
        return "NotesRow{" +
                "timeStamp=" + timeStamp +
                ", note='" + note + '\'' +
                ", object=" + object +
                ", user='" + user + '\'' +
                ", tag='" + tag + '\'' +
                ", noteTags=" + noteTags.size() +
                '}';
    }
}
