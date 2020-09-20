/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.JEVisClasses;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class InitialContact {
    private long ID;
    private String name;
    private Long year;

    private String Comment;
    private String DateOfContact;
    private File ContactFile;

    public InitialContact(SQLDataSource input, JEVisClasses jc) throws Exception {
        ID = 0L;
        name = "";
        year = 0L;
        Comment = "";
        DateOfContact = "";

        for (JsonObject obj : input.getObjects(jc.getInitialContact().getName(), false)) {
            this.setID(obj.getId());
            this.setName(obj.getName());
        }

        for (JsonAttribute att : input.getAttributes(ID)) {

            final String attFile = "Contact File";
            final String attDate = "Contact Date";
            final String attComment = "Comment";
            switch (att.getType()) {
                case attComment:
                    this.setComment(getValueString(att, ""));
                    break;
                case attDate:
                    String s = getValueString(att, "");
                    if (!"".equals(s)) {
                        DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                        DateTime dt = format.parseDateTime(s);
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setDateOfContact(s);
                    break;
                case attFile:

                    break;
                default:
                    break;
            }
        }

    }

    public InitialContact() {

        ID = 0L;
        name = "";
        year = 0L;
        Comment = "";
        DateOfContact = "";
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String Comment) {
        this.Comment = Comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfContact() {
        return DateOfContact;
    }

    public void setDateOfContact(String DateOfContact) {
        this.DateOfContact = DateOfContact;
    }

    @Override
    public String toString() {
        return "InitialContact{" + "ID=" + ID + ", name=" + name + ", Comment=" + Comment + ", DateOfContact=" + DateOfContact + ", ContactFile=" + ContactFile + '}';
    }

    public File getContactFile() {
        return ContactFile;
    }

    public void setContactFile(File ContactFile) {
        this.ContactFile = ContactFile;
    }

}
