package org.jevis.commons.datetime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

import java.time.LocalTime;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

public class WorkDays {

    private static final Logger logger = LogManager.getLogger(WorkDays.class);
    private final JEVisObject currentObject;
    private JEVisObject nextSiteParent;
    private JEVisClass siteClass;
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);

    public WorkDays(JEVisObject currentObject) {
        this.currentObject = currentObject;
        if (currentObject != null) {
            try {
                siteClass = currentObject.getDataSource().getJEVisClass("Building");
            } catch (JEVisException e) {
                logger.fatal("Could not get JEVisClass for Building");
            }
        }

        getWorkDays();
    }

    private void getWorkDays() {
        if (currentObject != null) {
            try {
                JEVisObject site = getNextSiteRecursive(currentObject);
                LocalTime start = null;
                LocalTime end = null;
                try {
                    JEVisAttribute attStart = site.getAttribute("Workday Beginning");
                    JEVisAttribute attEnd = site.getAttribute("Workday End");
                    if (attStart.hasSample()) {
                        String startStr = attStart.getLatestSample().getValueAsString();
                        DateTime dtStart = DateTime.parse(startStr);
                        start = LocalTime.of(dtStart.getHourOfDay(), dtStart.getMinuteOfHour(), 0, 0);
                    }
                    if (attEnd.hasSample()) {
                        String endStr = attEnd.getLatestSample().getValueAsString();
                        DateTime dtEnd = DateTime.parse(endStr);
                        end = LocalTime.of(dtEnd.getHourOfDay(), dtEnd.getMinuteOfHour(), 59, 999999999);
                    }
                } catch (Exception e) {
                }

                if (start != null && end != null) {
                    workdayStart = start;
                    workdayEnd = end;
                }
            } catch (Exception e) {

            }
        }
    }

    private JEVisObject getNextSiteRecursive(JEVisObject object) throws JEVisException {

        for (JEVisObject parent : object.getParents()) {
            if (parent.getJEVisClass().equals(siteClass)) {
                nextSiteParent = parent;
                break;
            } else {
                getNextSiteRecursive(parent);
            }
        }

        return nextSiteParent;
    }

    public LocalTime getWorkdayStart() {
        return workdayStart;
    }

    public LocalTime getWorkdayEnd() {
        return workdayEnd;
    }

    public void setWorkdayEnd(LocalTime workdayEnd) {
        this.workdayEnd = workdayEnd;
    }
}
