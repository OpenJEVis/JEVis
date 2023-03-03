package org.jevis.jeconfig.plugin.nonconformities.ui;

import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;
import org.joda.time.DateTime;

public class DateFilter {

    DateTime from;
    DateTime until;
    DateField dateField;

    public DateFilter(DateField dateField, DateTime from, DateTime until) {
        this.dateField = dateField;
        this.from = from;
        this.until = until;
    }

    public DateTime getFromDate() {
        return from;
    }

    public DateTime getUntilDate() {
        return until;
    }

    public DateField dateField() {
        return dateField;
    }

    private DateTime getDate(NonconformityData data) {

        if (dateField == DateField.ABGESCHLOSSEN) {
            return data.getDoneDate();
        } else if (dateField == DateField.ERSTELLT) {
            return data.getCreateDate();
        } else if (dateField == DateField.UMSETZUNG) {
            return data.getDeadLine();
        }
        return null;
    }

    public boolean show(NonconformityData data) {
        try {
            if (dateField == DateField.ALLES) return true;
            if (getDate(data).isAfter(getUntilDate())) return false;
            if (getDate(data).isBefore(getFromDate())) return false;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "DateFilter{" +
                "from=" + from +
                ", until=" + until +
                ", dateField=" + dateField +
                '}';
    }

    public static enum DateField {
        ALLES, UMSETZUNG, ABGESCHLOSSEN, ERSTELLT
    }
}
