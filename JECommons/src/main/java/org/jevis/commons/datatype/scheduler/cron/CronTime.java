//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.jevis.commons.datatype.scheduler.cron;

public class CronTime {
    private CronHour hour;
    private CronMinute min;

    public CronTime() {
    }

    public CronHour getHour() {
        return this.hour;
    }

    public void setHour(String h) {
        this.hour = new CronHour();
        this.hour.setValue(h);
    }

    public CronMinute getMin() {
        return this.min;
    }

    public void setMin(String m) {
        this.min = new CronMinute();
        this.min.setValue(m);
    }
}
