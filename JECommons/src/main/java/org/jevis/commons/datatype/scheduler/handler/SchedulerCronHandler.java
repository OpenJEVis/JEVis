//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.handler;

import org.jevis.commons.datatype.scheduler.SchedulerRule;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.datatype.scheduler.cron.CronTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Iterator;

public class SchedulerCronHandler extends SchedulerServiceHandler {
    public SchedulerCronHandler() {
    }

    public boolean isAfter(DateTime last, CronScheduler scheduler, int schedulerType) {
        DateTimeZone dtz = scheduler.getDatetTimeZone();
        Iterator var5 = scheduler.getAllRules().iterator();

        DateTime minNextRun;
        DateTime now;
        do {
            if (!var5.hasNext()) {
                return false;
            }

            SchedulerRule r = (SchedulerRule) var5.next();
            CronTime t = r.getStartTime();
            minNextRun = null;
            boolean run = true;
            if (schedulerType == 0) {
                minNextRun = last.withTime(0, 0, 0, 0);
            } else if (schedulerType == 1) {
                minNextRun = last;
            }

            if (!t.getHour().isAlias() && !t.getMin().isAlias()) {
                minNextRun = last;

                label48:
                while (true) {
                    while (true) {
                        if (!run) {
                            break label48;
                        }

                        if (this.checkDate(minNextRun, r) && minNextRun.isAfter(last)) {
                            run = false;
                        } else {
                            minNextRun.plusDays(1);
                        }
                    }
                }
            } else {
                label60:
                while (true) {
                    while (true) {
                        if (!run) {
                            break label60;
                        }

                        if (this.checkDate(minNextRun, r) && minNextRun.isAfter(last)) {
                            run = false;
                        } else {
                            if (t.getHour().isAlias()) {
                                minNextRun.plusHours(t.getHour().getDenominator());
                            }

                            if (t.getMin().isAlias()) {
                                minNextRun.plusMinutes(t.getMin().getDenominator());
                            }
                        }
                    }
                }
            }

            now = DateTime.now(dtz);
        } while (!now.isAfter(minNextRun));

        return true;
    }
}
