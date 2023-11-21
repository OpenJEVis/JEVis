package org.jevis.mscons;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class InterchangeHeader {
    private String syntaxIdentifier;
    private String sender;
    private String recipient;

    private String date;

    private String time;

    private String controlReference;

    private String applicationReference;


    private DateTime createdDateTime;


    public String getSyntaxIdentifier() {
        return syntaxIdentifier;
    }

    public void setSyntaxIdentifier(String syntaxIdentifier) {
        this.syntaxIdentifier = syntaxIdentifier;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public void genearteDateTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyMMdd HHmm");
        createdDateTime = dateTimeFormatter.parseDateTime(date + " " + time);

    }


    public void setDate(String date) {
        this.date = date;
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
                "SyntaxIdentifier='" + syntaxIdentifier + '\'' +
                ", Sender='" + sender + '\'' +
                ", Recipient='" + recipient + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", controlReference='" + controlReference + '\'' +
                ", applicationReference='" + applicationReference + '\'' +
                ", dateTime=" + createdDateTime +
                '}';
    }
}
