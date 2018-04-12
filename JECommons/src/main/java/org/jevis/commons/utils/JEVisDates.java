/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author fs
 */
public class JEVisDates {

//    public static final String JEVis_DEFAULT = ISODateTimeFormat.dateTime().toString();//;"yyyy-MM-dd HH:mm:ss Z";
    private static final Logger logger = LogManager.getLogger(JEVisDates.class);
    public static DateTimeFormatter DEFAULT_DATE_FORMAT = ISODateTimeFormat.dateTime();
    
    /**
     * Returns an default JEVisDate formatted date string.
     *
     * @param date
     * @return
     */
    public static String printDefaultDate(DateTime date) {
        logger.error("print: {} {}",date);
        if (date == null) {
            throw new IllegalArgumentException();
        }
        return DEFAULT_DATE_FORMAT.print(date);

    }

    public static void saveDefaultDate(JEVisAttribute attibute, DateTime timestamp, DateTime date) throws IllegalArgumentException, JEVisException {
        logger.error("Save Date: {} {} {}",attibute,timestamp,date);
        if (date == null || attibute == null) {
            throw new IllegalArgumentException();
        }

        JEVisSample sample = attibute.buildSample(timestamp, printDefaultDate(date));
        sample.commit();
    }

    public static DateTime parseDefaultDate(JEVisAttribute date) throws IllegalArgumentException, JEVisException {
        logger.error("Parse Date: {}",date);
        if (date == null || !date.hasSample()) {
            throw new IllegalArgumentException();
        }

        return parseDefaultDate(date.getLatestSample().getValueAsString());
    }

    /**
     * Parse an date out of an defautl JEVis Date. If the timezone is missing we
     * use UTC as fallback. Formate: 'yyyy-MM-dd HH:mm:ss z'
     *
     * @param datestring
     * @return
     */
    public static DateTime parseDefaultDate(String datestring) throws IllegalArgumentException {
        logger.error("Parse Date: {}",datestring);
        if (datestring == null || datestring.isEmpty()) {
            throw new IllegalArgumentException();
        }

        try {
            DateTime dateTime = DateTime.parse(datestring, DEFAULT_DATE_FORMAT);
            return dateTime;
        } catch (Exception ex) {
            //fallback; try without timezone using UTC            

            DateTime dateTime = DateTime.parse(datestring, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
            return dateTime;

        }

    }

}
