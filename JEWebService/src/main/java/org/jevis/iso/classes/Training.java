/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Training {
    private long ID;
    private String name;
    private Long year;

    private String TrainingName;
    private String TrainingDate;
    private String Participants;
    private String TrainingTime;
    private String Trainer;
    private long TrainingCourse;

    public Training(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        TrainingName = "";
        year = 0L;
        TrainingDate = "";
        Participants = "";
        Trainer = "";
        TrainingTime = "";
        TrainingCourse = 0L;
        this.ID = input.getId();
        this.name = input.getName();
        this.TrainingName = input.getName();

        List<JsonAttribute> listTrainingAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listTrainingAttributes) {
            String name = att.getType();

            final String attTrainingCourse = "Training Course";
            final String attTrainer = "Trainer";
            final String attTrainingTime = "Training Time";
            final String attParticipants = "Training Participants";
            final String attTrainingDate = "Training Date";
            switch (name) {
                case attParticipants:
                    this.setParticipants(getValueString(att, ""));
                    break;
                case attTrainer:
                    this.setTrainer(getValueString(att, ""));
                    break;
                case attTrainingCourse:
                    if (getValueString(att, "") != "") {
                        this.setTrainingCourse(Long.parseLong(getValueString(att, "")));
                    } else this.setTrainingCourse(0L);
                    break;
                case attTrainingDate:
                    DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                    if (!"".equals(getValueString(att, ""))) {
                        DateTime dt = format.parseDateTime(getValueString(att, ""));
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setTrainingDate(getValueString(att, ""));
                    break;
                case attTrainingTime:
                    this.setTrainingTime(getValueString(att, ""));
                    break;
            }
        }

    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getTrainingName() {
        return TrainingName;
    }

    public void setTrainingName(String TrainingName) {
        this.TrainingName = TrainingName;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public String getTrainingDate() {
        return TrainingDate;
    }

    public void setTrainingDate(String TrainingDate) {
        this.TrainingDate = TrainingDate;
    }

    public String getParticipants() {
        return Participants;
    }

    public void setParticipants(String Participants) {
        this.Participants = Participants;
    }

    public String getTrainingTime() {
        return TrainingTime;
    }

    public void setTrainingTime(String TrainingTime) {
        this.TrainingTime = TrainingTime;
    }

    public String getTrainer() {
        return Trainer;
    }

    public void setTrainer(String Trainer) {
        this.Trainer = Trainer;
    }

    public long getTrainingCourse() {
        return TrainingCourse;
    }

    public void setTrainingCourse(long TrainingCourse) {
        this.TrainingCourse = TrainingCourse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
