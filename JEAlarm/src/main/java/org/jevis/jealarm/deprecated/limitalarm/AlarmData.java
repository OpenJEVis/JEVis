/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jealarm.deprecated.limitalarm;

import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class AlarmData {

    private DateTime ts;
    private String message;
    private boolean isAlarm = true;

    public AlarmData(DateTime ts, boolean isAlarm, String message) {
        this.ts = ts;
        this.message = message;
        this.isAlarm = isAlarm;
    }

    public DateTime getTime() {
        return ts;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAlarm() {
        return isAlarm;
    }
}
