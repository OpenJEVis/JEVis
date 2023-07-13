package org.jevis.mscons;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class InterchangeHeader {
    private String SyntaxIdentifier;
    private String Sender;
    private String Recipient;

    private String date;

    private String time;

    private String controlReference;

    private String applicationReference;





    private DateTime dateTime;


    public String getSyntaxIdentifier() {
        return SyntaxIdentifier;
    }

    public void setSyntaxIdentifier(String syntaxIdentifier) {
        SyntaxIdentifier = syntaxIdentifier;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getRecipient() {
        return Recipient;
    }

    public void setRecipient(String recipient) {
        Recipient = recipient;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void genearteDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyMMdd HHmm");
       dateTime = dateTimeFormatter.parseDateTime(date + " " + time);

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getControlReference() {
        return controlReference;
    }

    public void setControlReference(String controlReference) {
        this.controlReference = controlReference;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
    }


    @Override
    public String toString() {
        return "InterchangeHeader{" +
                "SyntaxIdentifier='" + SyntaxIdentifier + '\'' +
                ", Sender='" + Sender + '\'' +
                ", Recipient='" + Recipient + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", controlReference='" + controlReference + '\'' +
                ", applicationReference='" + applicationReference + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
