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

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ManagementDocument {

    static final String AttDateOfDecontrol = "Release Date";

    long ID;
    String name;
    Long year;

    String content;
    JsonObject object;
    SQLDataSource ds;

    String createdby;
    String dateofcreation;

    String dateofdecontrol;
    String decontrolby;
    String number;
    String title;
    String version;

    public ManagementDocument() {

        ID = 0L;
        name = "";
        year = 0L;
        content = "";
        createdby = "";
        dateofcreation = "";
        dateofdecontrol = "";
        decontrolby = "";
        number = "";
        title = "";
        version = "";
    }

    public ManagementDocument(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        year = 0L;
        content = "";
        createdby = "";
        dateofcreation = "";
        dateofdecontrol = "";
        decontrolby = "";
        number = "";
        title = "";
        version = "";
        this.ID = input.getId();
        this.name = input.getName();

        this.object = input;
        this.ds = ds;

        for (JsonAttribute att : ds.getAttributes(input.getId())) {
            final String AttContent = "Content";
            final String AttCreatedBy = "Created by";
            final String AttDateOfCreation = "Date of Creation";
            final String AttDecontrolBy = "Released by";
            final String AttNumber = "Document Number";
            final String AttTitle = "Title";
            final String AttVersion = "Version";

            String name = att.getType();

            switch (name) {
                case AttContent:
                    this.setContent(getValueString(att, ""));
                    break;
                case AttCreatedBy:
                    this.setCreatedby(getValueString(att, ""));
                    break;
                case AttDateOfCreation: {
                    String s = getValueString(att, "");
                    this.setDateofcreation(s);
                    break;
                }
                case AttDateOfDecontrol: {
                    String s = getValueString(att, "");
                    DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                    if (!"".equals(s)) {
                        DateTime dt = format.parseDateTime(s);
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setDateofdecontrol(s);
                    break;
                }
                case AttDecontrolBy:
                    this.setDecontrolby(getValueString(att, ""));
                    break;
                case AttNumber:
                    this.setNumber(getValueString(att, ""));
                    break;
                case AttTitle:
                    this.setTitle(getValueString(att, ""));
                    break;
                case AttVersion:
                    this.setVersion(getValueString(att, ""));
                    break;
                default:
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public JsonObject getObject() {
        return object;
    }

    public void setObject(JsonObject object) {
        this.object = object;
    }

    public String getCreatedby() {
        return createdby;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public String getDateofcreation() {
        return dateofcreation;
    }

    public void setDateofcreation(String dateofcreation) {
        this.dateofcreation = dateofcreation;
    }

    public String getDateofdecontrol() {
        return dateofdecontrol;
    }

    public void setDateofdecontrol(String dateofdecontrol) {
        this.dateofdecontrol = dateofdecontrol;
    }

    public String getDecontrolby() {
        return decontrolby;
    }

    public void setDecontrolby(String decontrolby) {
        this.decontrolby = decontrolby;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ManagementDocument{" + "ID=" + ID + ", name=" + name + ", content=" + content + ", object=" + object + ", createdby=" + createdby + ", dateofcreation=" + dateofcreation + ", dateofdecontrol=" + dateofdecontrol + ", decontrolby=" + decontrolby + ", number=" + number + ", title=" + title + ", version=" + version + '}';
    }

}
