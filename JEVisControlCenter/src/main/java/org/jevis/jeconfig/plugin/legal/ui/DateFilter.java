package org.jevis.jeconfig.plugin.legal.ui;

import org.jevis.jeconfig.plugin.legal.data.ObligationData;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jevis.jeconfig.plugin.legal.ui.DateFilter.DateField.VERSION;


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

    private List<DateTime> getDate(ObligationData data) {
        List<DateTime> dateTimes = new ArrayList<>();
        if (dateField == DateField.Date_Of_Examination) {
            setDateOfExaminationDate(data, dateTimes);
        } else if (dateField == DateField.ISSUE_DATE) {
            setIssueDate(data, dateTimes);
        } else if (dateField == VERSION) {
            setVersionDate(data, dateTimes);
        } else if (dateField == DateField.ALL) {
            setDateOfExaminationDate(data, dateTimes);
            setIssueDate(data, dateTimes);
            setVersionDate(data, dateTimes);
        }
        return dateTimes;
    }

    private static void setVersionDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getCurrentVersionDate() != null) {
            dateTimes.add(data.getCurrentVersionDate());
        }
    }

    private static void setIssueDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getIssueDate() != null) {
            dateTimes.add(data.getIssueDate());
        }
    }

    private static void setDateOfExaminationDate(ObligationData data, List<DateTime> dateTimes) {
        if (data.getDateOfExamination() != null) {
            dateTimes.add(data.getDateOfExamination());
        }
    }

    public boolean show(ObligationData data) {
        if (getDate(data).size() == 0) {
            return true;
        }
        try {
            Optional<DateTime> optionalDateTimeMax = getDate(data).stream().max(DateTime::compareTo);
            Optional<DateTime> optionalDateTimeMin = getDate(data).stream().min(DateTime::compareTo);

            if (optionalDateTimeMax.isPresent() && optionalDateTimeMin.isPresent()) {
                if (optionalDateTimeMax.get().isBefore(getFromDate())) return false;
                if (optionalDateTimeMin.get().isAfter(getUntilDate())) return false;
            } else {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static enum DateField {
        ALL, VERSION, ISSUE_DATE, Date_Of_Examination
    }
}
