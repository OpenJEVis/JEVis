package org.jevis.report3.data;

import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.format.DateTimeFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author broder
 */
public class DataHelper {

    public static boolean checkAllObjectsNotNull(Object... objects) {
        boolean notNull = true;
        for (Object object : objects) {
            if (object == null) {
                notNull = false;
                break;
            }
        }
        return notNull;
    }

    public static boolean checkValidSchedule(String scheduleString) {
        try {
            ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DataHelper.class.getName()).log(Level.ERROR, "Schedule string is invalid: " + scheduleString, ex);
            return false;
        }
        return true;
    }

    public static boolean checkValidDateFormat(String startRecordString) {
        try {
            DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DataHelper.class.getName()).log(Level.ERROR, "Start record has non valid format: " + startRecordString, ex);
            return false;
        }
        return true;
    }
}
