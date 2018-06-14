/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ManagementReview {
    private long ID;
    private String name;
    private Long year;

    private String Content;
    private String reviewdate;
    private File ManagementReviewFile;
    private File ManagementReviewPDF;
    private String Participants;

    public ManagementReview(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        year = 0L;
        Content = "";
        reviewdate = "";
        Participants = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listManagementReviewAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listManagementReviewAttributes) {
            String name = att.getType();

            final String attParticipants = "Participants";
            final String attManagementReviewPDF = "Management Review PDF";
            final String attManagementReviewFile = "Management Review File";
            final String attReviewDate = "Review Date";
            final String attContent = "Content";
            switch (name) {
                case attContent:
                    this.setContent(getValueString(att, ""));
                    break;
                case attManagementReviewFile:

                    break;
                case attManagementReviewPDF:

                    break;
                case attParticipants:
                    this.setParticipants(getValueString(att, ""));
                    break;
                case attReviewDate:
                    DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                    if (!"".equals(getValueString(att, ""))) {
                        DateTime dt = format.parseDateTime(getValueString(att, ""));
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setReviewdate(getValueString(att, ""));
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

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReviewdate() {
        return reviewdate;
    }

    public void setReviewdate(String reviewdate) {
        this.reviewdate = reviewdate;
    }

    public File getManagementReviewFile() {
        return ManagementReviewFile;
    }

    public void setManagementReviewFile(File ManagementReviewFile) {
        this.ManagementReviewFile = ManagementReviewFile;
    }

    public File getManagementReviewPDF() {
        return ManagementReviewPDF;
    }

    public void setManagementReviewPDF(File ManagementReviewPDF) {
        this.ManagementReviewPDF = ManagementReviewPDF;
    }

    public String getParticipants() {
        return Participants;
    }

    public void setParticipants(String Participants) {
        this.Participants = Participants;
    }

    @Override
    public String toString() {
        return "ManagementReview{" + "ID=" + ID + ", name=" + name + ", Content=" + Content + ", ReviewDate=" + reviewdate + ", ManagementReviewFile=" + ManagementReviewFile + ", ManagementReviewPDF=" + ManagementReviewPDF + ", Participants=" + Participants + '}';
    }

}
