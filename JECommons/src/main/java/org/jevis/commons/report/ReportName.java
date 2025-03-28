package org.jevis.commons.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.WorkDays;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class ReportName {
    private static final Logger logger = LogManager.getLogger(ReportName.class);

    public static String getPrefix(JEVisObject reportObject, DateTime startDate) {
        String prefix = "";
        try {
            WorkDays wd = new WorkDays(reportObject);
            startDate = startDate.withZone(wd.getDateTimeZone());

            JEVisAttribute scheduleAttribute = reportObject.getAttribute("Schedule");

            if (scheduleAttribute != null && scheduleAttribute.hasSample()) {
                JEVisSample latestScheduleSample = scheduleAttribute.getLatestSample();

                if (latestScheduleSample != null) {
                    Period schedule = Period.valueOf(latestScheduleSample.getValueAsString().toUpperCase());

                    switch (schedule) {
                        case MINUTELY:
                        case QUARTER_HOURLY:
                        case HOURLY:
                            prefix = startDate.toString(DateTimeFormat.forPattern("yyyyMMdd_HHmm").withZone(wd.getDateTimeZone()));
                            break;
                        case DAILY:
                        case WEEKLY:
                            prefix = startDate.toString(DateTimeFormat.forPattern("yyyyMMdd").withZone(wd.getDateTimeZone()));
                            break;
                        case MONTHLY:
                        case QUARTERLY:
                            prefix = startDate.toString(DateTimeFormat.forPattern("yyyyMM").withZone(wd.getDateTimeZone()));
                            break;
                        case YEARLY:
                            prefix = startDate.toString(DateTimeFormat.forPattern("yyyy").withZone(wd.getDateTimeZone()));
                            break;
                        case CUSTOM:
                        case CUSTOM2:
                            break;
                    }

                    if (!prefix.isEmpty()) {
                        prefix += "_";
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Could not get period.");
        }

        return prefix;
    }
}
