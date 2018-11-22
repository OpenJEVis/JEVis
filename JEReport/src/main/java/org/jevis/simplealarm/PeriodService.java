/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.simplealarm.usageperiod.UsagePeriod;
import org.joda.time.DateTime;

import java.util.List;

/**
 *
 * @author ai
 */
public class PeriodService {
    private static final Logger logger = LogManager.getLogger(PeriodService.class);

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

        logger.info("Date Time of alarm: " + dt + ", day of week: " + dt.getDayOfWeek() + " setted value = " + res);

        return res;
    }
}
