/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm;

import org.jevis.commons.alarm.UsageSchedule;
import org.jevis.commons.alarm.UsageScheduleType;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author Artur Iablokov
 */
public class ScheduleService {

    private static final int LOG_NORMAL = 1;
    private static final int LOG_SILENT = 2;
    private static final int LOG_STANDBY = 4;

    public static Integer getValueForLog(DateTime dt, List<UsageSchedule> up) {
        int res = 0;

        for (UsageSchedule us : up) {
            if (us.contains(dt)) {
                if (us.getType().equals(UsageScheduleType.SILENT)) {
                    res = res + LOG_SILENT;
                } else if (us.getType().equals(UsageScheduleType.STANDBY)) {
                    res = res + LOG_STANDBY;
                }
            }
        }

        if (res == 0) {
            res = LOG_NORMAL;
        }
        return res;
    }
}