/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevCalendar;
import envidatec.jevis.capi.data.JevSample;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author broder
 */
public class AbstractTimestamp {

    RegTreeNode regNode;
    List<Double> allTimestamps;
    List<JevSample<Object>> timeStampList;
//    List<JevCalendar> calList;
    Double max, min;

    public AbstractTimestamp(List<JevSample<Object>> listOfTimestamps) {
        timeStampList = listOfTimestamps;
        allTimestamps = new ArrayList<Double>();
//        calList = new ArrayList<JevCalendar>();
        double minVal = Double.MAX_VALUE;
        double maxVal = Double.MIN_VALUE;
        if (listOfTimestamps.size() > 0) {
            if (listOfTimestamps.get(0).getVal() instanceof String) {
                for (JevSample s : listOfTimestamps) {
//            System.out.println("CALENDAR " + new JevCalendar(new Date(s.getCal().getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(s.getCal().getTimeInMillis()))).toString());
                    Double cal = transformTimestampsToExcelTime(new JevCalendar(new Date(s.getCal().getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(s.getCal().getTimeInMillis()))));
                    allTimestamps.add(cal);
                }

            } else {
                for (JevSample s : listOfTimestamps) {
//            System.out.println("CALENDAR " + new JevCalendar(new Date(s.getCal().getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(s.getCal().getTimeInMillis()))).toString());
                    Double cal = transformTimestampsToExcelTime(new JevCalendar(new Date(s.getCal().getTimeInMillis() + Calendar.getInstance().getTimeZone().getOffset(s.getCal().getTimeInMillis()))));
                    allTimestamps.add(cal);
                    if (Double.parseDouble(
                            s.getVal().toString()) > maxVal) {
                        maxVal = Double.parseDouble(s.getVal().toString());
                        max = cal;
                    }
                    if (Double.parseDouble(
                            s.getVal().toString()) < minVal) {
                        minVal = Double.parseDouble(
                                s.getVal().toString());
                        min = cal;
                    }
                }
            }
        }
    }

    public Double getFirst() {
        if(allTimestamps.isEmpty()){
            return null;
        }
        return allTimestamps.get(0);
    }

    public Double getLast() {
         if(allTimestamps.isEmpty()){
            return null;
        }
        return allTimestamps.get(allTimestamps.size() - 1);
    }

    public List<Double> getall() {
        return allTimestamps;
    }
//    public List<JevCalendar> getall() {
//        return calList;
//    }

    public static double transformTimestampsToExcelTime(JevCalendar cal) {
        JevCalendar excelTime = new JevCalendar(1899, 12, 30, 0, 0);
        double days = excelTime.getDaysUntil(cal);
        double hourtmp = cal.get(Calendar.HOUR_OF_DAY) * 60;
        double mintmp = cal.get(Calendar.MINUTE);

        double d = (hourtmp + mintmp) / 1440;

        return days + d;
    }

    public Double getWithMaxVal() {
        return max;
    }

    public Double getWithMinVal() {
        return min;
    }
}
