/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.simplealarm.usageperiod.UsagePeriod;
import org.joda.time.DateTime;

/**
 *
 * @author ai
 */
public class PeriodService {

    private static final int LOG_NORMAL = 1;
    private static final int LOG_SILENT = 2;
    private static final int LOG_STANDBY = 4;

    public static Integer getValueForLog(DateTime dt, List<UsagePeriod> up) {
        int res = 1;
        for (UsagePeriod usp : up) {
            if (usp.isDayInPeriod(dt.getDayOfWeek()) & usp.isTimeInPeriod(dt.getHourOfDay())) {
                if (usp.getType() == 1) {
                    res = LOG_SILENT;
                } else {
                    res = LOG_STANDBY;
                }
            }
        }

        Logger.getLogger(UsagePeriod.class.getName()).log(Level.INFO, "Date Time of alarm: " + dt + ", day of week: "+ dt.getDayOfWeek()+ " setted value = " + res);

        return res;
    }
}
