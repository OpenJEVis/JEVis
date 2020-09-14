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

import java.io.File;
import java.util.List;

import static org.jevis.iso.add.Snippets.getValueString;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class Meeting {
    private long ID;
    private String name;
    private Long year;

    private String contentofmeetingandresults;
    private String meetingdate;
    private String meetingparticipants;
    private String meetingtime;
    private File minutesofthemeeting;

    public Meeting(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0;
        name = "";
        year = 0L;
        contentofmeetingandresults = "";
        meetingdate = "";
        meetingparticipants = "";
        meetingtime = "";
        this.ID = input.getId();
        this.name = input.getName();

        List<JsonAttribute> listMeetingAttributes = ds.getAttributes(input.getId());

        for (JsonAttribute att : listMeetingAttributes) {
            String name = att.getType();

            final String attMinutesOfTheMeeting = "Minutes of Meeting";
            final String attMeetingTime = "Meeting Time";
            final String attMeetingParticipants = "Meeting Participants";
            final String attMeetingDate = "Meeting Date";
            final String attContentOfMeetingAndResults = "Content of Meeting and Results";
            switch (name) {
                case attContentOfMeetingAndResults:
                    this.setcontentofmeetingandresults(getValueString(att, ""));
                    break;
                case attMeetingDate:
                    //      DateFormat format = new SimpleDateFormat("MM.dd.yyyy", Locale.ENGLISH);
                    //      format.parse(att.getLatestSample().getValueAsString();
                    String s = getValueString(att, "");
                    if (!"".equals(s)) {
                        DateTimeFormatter format = DateTimeFormat.forPattern("dd.MM.yyyy");
                        DateTime dt = format.parseDateTime(s);
                        this.setYear((long) dt.getYear());
                    } else {
                        this.setYear(0L);
                    }
                    this.setmeetingdate(s);
                    break;

                case attMeetingParticipants:
                    this.setmeetingparticipants(getValueString(att, ""));
                    break;
                case attMeetingTime:
                    // broken code
                    //
                    //        DateTimeFormatter format = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                    //      this.setmeetingtime(format.parseDateTime(att.getLatestSample().getValueAsString()));
                    break;
                // file ...
                case attMinutesOfTheMeeting:
//                        byte[] arr = null;
//                        String filename = "";
//                        JEVisFile file = getValueString(att, "");
//                        arr = file.getBytes();
//                        filename = file.getFilename();
//                        this.setminutesofthemeeting(new File(filename));

                    break;
                default:
                    break;
            }
        }

    }

    public Meeting() {
        ID = 0;
        name = "";
        year = 0L;
        contentofmeetingandresults = "";
        meetingdate = "";
        meetingparticipants = "";
        meetingtime = "";
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getcontentofmeetingandresults() {
        return contentofmeetingandresults;
    }

    public String getmeetingdate() {
        return meetingdate;
    }

    public String getmeetingparticipants() {
        return meetingparticipants;
    }

    public String getmeetingtime() {
        return meetingtime;
    }

    public File getminutesofthemeeting() {
        return minutesofthemeeting;
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

    public void setcontentofmeetingandresults(String contentofmeetingandresults) {
        this.contentofmeetingandresults = contentofmeetingandresults;
    }

    public void setmeetingdate(String meetingdate) {
        this.meetingdate = meetingdate;
    }

    public void setmeetingparticipants(String meetingparticipants) {
        this.meetingparticipants = meetingparticipants;
    }

    public void setmeetingtime(String meetingtime) {
        this.meetingtime = meetingtime;
    }

    public void setminutesofthemeeting(File minutesofthemeeting) {
        this.minutesofthemeeting = minutesofthemeeting;
    }

    @Override
    public String toString() {
        return "Meeting[" + "ID = " + ID + ", contentofmeetingandresults = " + contentofmeetingandresults + ", meetingdate = " + meetingdate + ", meetingparticipants = " + meetingparticipants + ", meetingtime = " + meetingtime + ", minutesofthemeeting = " + minutesofthemeeting + ']';
    }
}
