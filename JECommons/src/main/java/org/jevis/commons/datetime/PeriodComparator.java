package org.jevis.commons.datetime;

import org.joda.time.Period;

import java.util.Comparator;

public class PeriodComparator implements Comparator<Period> {

    public PeriodComparator() {
    }


    @Override
    public int compare(Period p1, Period p2) {
        boolean p1HasMonths = p1.getMonths() > 1;
        boolean p1HasYear = p1.getYears() > 1;
        boolean p1HasDays = p1.getDays() > 1;
        boolean p1HasHours = p1.getHours() > 1;
        boolean p1HasMinutes = p1.getMinutes() > 1;
        boolean p1HasSeconds = p1.getSeconds() > 1;

        boolean p2HasMonths = p2.getMonths() > 1;
        boolean p2HasYear = p2.getYears() > 1;
        boolean p2HasDays = p2.getDays() > 1;
        boolean p2HasHours = p2.getHours() > 1;
        boolean p2HasMinutes = p2.getMinutes() > 1;
        boolean p2HasSeconds = p2.getSeconds() > 1;

        if (p1HasYear && !p2HasYear) {
            return 1;
        } else if (!p1HasYear && p1HasMonths
                && !p2HasYear && !p2HasMonths) {
            return 1;
        } else if (!p1HasYear && !p1HasMonths && p1HasDays
                && !p2HasYear && !p2HasMonths && !p2HasDays) {
            return 1;
        } else if (!p1HasYear && !p1HasMonths && !p1HasDays && p1HasHours
                && !p2HasYear && !p2HasMonths && !p2HasDays && !p2HasHours) {
            return 1;
        } else if (!p1HasYear && !p1HasMonths && !p1HasDays && !p1HasHours && p1HasMinutes
                && !p2HasYear && !p2HasMonths && !p2HasDays && !p2HasHours && !p2HasMinutes) {
            return 1;
        } else if (!p1HasYear && !p1HasMonths && !p1HasDays && !p1HasHours && !p1HasMinutes && p1HasSeconds
                && !p2HasYear && !p2HasMonths && !p2HasDays && !p2HasHours && !p2HasMinutes && !p2HasSeconds) {
            return 1;
        } else if (p1HasYear && p2HasYear) {
            if (p1.getYears() > p2.getYears()) {
                return 1;
            } else if (p1.getYears() == p2.getYears()) {
                return 0;
            }
        } else if (p1HasMonths && p2HasMonths) {
            if (p1.getMonths() > p2.getMonths()) {
                return 1;
            } else if (p1.getMonths() == p2.getMonths()) {
                return 0;
            }
        } else if (p1HasDays && p2HasDays) {
            if (p1.getDays() > p2.getDays()) {
                return 1;
            } else if (p1.getDays() == p2.getDays()) {
                return 0;
            }
        } else if (p1HasHours && p2HasHours) {
            if (p1.getHours() > p2.getHours()) {
                return 1;
            } else if (p1.getHours() == p2.getHours()) {
                return 0;
            }
        } else if (p1HasMinutes && p2HasMinutes) {
            if (p1.getMinutes() > p2.getMinutes()) {
                return 1;
            } else if (p1.getMinutes() == p2.getMinutes()) {
                return 0;
            }
        } else if (p1HasSeconds && p2HasSeconds) {
            if (p1.getSeconds() > p2.getSeconds()) {
                return 1;
            } else if (p1.getSeconds() == p2.getSeconds()) {
                return 0;
            }
        }

        return -1;
    }
}
