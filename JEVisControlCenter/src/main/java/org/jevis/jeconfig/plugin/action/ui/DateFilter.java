package org.jevis.jeconfig.plugin.action.ui;

import org.jevis.jeconfig.plugin.action.data.ActionData;
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

    private DateTime getDate(ActionData data) {

        if (dateField == DateField.ABGESCHLOSSEN) {
            return data.doneDate.get();
        } else if (dateField == DateField.ERSTELLT) {
            return data.createDate.get();
        } else if (dateField == DateField.UMSETZUNG) {
            return data.plannedDate.get();
        }
        return null;
    }

    public boolean show(ActionData data) {
        try {
            if (dateField == DateField.ALL) {
                //System.out.println("Filter is all");
                /* Fallback, if all dates are empty show it */
                if (data.doneDate.get() == null && data.createDate.get() == null && data.plannedDate.get() == null)
                    return true;
                // System.out.println("data.doneDate.get(): " + data.doneDate.get());
                if (data.doneDate.get() != null && data.doneDate.get().isBefore(getUntilDate()) && data.doneDate.get().isAfter(getFromDate())) {
                    //System.out.println("done date true");
                    return true;
                }
                // System.out.println("data.createDate.get(): " + data.createDate.get());
                if (data.doneDate.get() != null && data.createDate.get().isBefore(getUntilDate()) && data.createDate.get().isAfter(getFromDate())) {
                    // System.out.println("create date true");
                    return true;
                }
                // System.out.println("data.plannedDate.get(): " + data.plannedDate.get());
                if (data.plannedDate.get() != null && data.plannedDate.get().isBefore(getUntilDate()) && data.plannedDate.get().isAfter(getFromDate())) {
                    //  System.out.println("palnned date true");
                    return true;
                }
                return false;
            }

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
        ALL, UMSETZUNG, ABGESCHLOSSEN, ERSTELLT
    }
}
