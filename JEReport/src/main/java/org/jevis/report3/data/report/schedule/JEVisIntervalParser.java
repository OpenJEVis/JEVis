/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author broder
 */
public class JEVisIntervalParser {

    private final DateTimeFormatter pattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static JEVisSample lastNotUpdatedSample;
    private static Interval interval;
    private static String newValue;

    static String getNewValue() {
        return newValue;
    }

    void parseSamples(List<JEVisSample> allSamples) {
        for (JEVisSample sample : allSamples) {
            try {
                String intervalString = sample.getValueAsString();
                String[] split = intervalString.split(";");
                DateTime from = pattern.parseDateTime(split[0]);
                DateTime until = pattern.parseDateTime(split[1]);
                boolean updated = Boolean.getBoolean(split[2]);
                if (!updated) {
                    lastNotUpdatedSample = sample;
                    interval = new Interval(from, until);
                    newValue = split[0] + ";" + split[1] + ";true";
                    break;
                }
            } catch (JEVisException ex) {
                Logger.getLogger(JEVisIntervalParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static JEVisSample getLastNotUpdatedSample() {
        return lastNotUpdatedSample;
    }

    static Interval getInterval() {
        return interval;
    }
}
