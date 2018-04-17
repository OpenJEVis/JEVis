/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm;

import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class AlarmPeriod {

    private DateTime alarmRaist;
    private List<Double> sollValues = new ArrayList<>();
    private double tolerance = 0;
    private List<Double> istValues = new ArrayList<>();
    private DateTime lastAlarm;

    public AlarmPeriod() {
    }

    public void addAlarmPoint(DateTime date, double istValue, double sollValue, int state) {
        sollValues.add(sollValue);
        istValues.add(istValue);
        if (alarmRaist == null) {
            alarmRaist = date;
        }
        lastAlarm = date;
        System.out.println("---Add to period: " + alarmRaist + "   new end: " + lastAlarm);
    }

    public void setTolerance(Double tol) {
        this.tolerance = tol;
    }

    public double getTolerance() {
        return tolerance;
    }

    public double getSumSoll() {
        double sum = 0;
        if (sollValues.size() > 0) {
            for (Double v : sollValues) {
                sum += v;
            }

            sum = sum / sollValues.size();
        }

        return sum;
    }

    public double getSumIst() {
        double sum = 0;
        if (istValues.size() > 0) {
            for (Double v : istValues) {
                sum += v;
            }

            sum = sum / istValues.size();
        }

        return sum;
    }

    public DateTime getPeriodEnd() {
        return lastAlarm;
    }

    public DateTime getPeriodStart() {
        return alarmRaist;
    }

}
