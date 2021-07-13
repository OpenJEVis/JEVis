/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.List;

/**
 * The interface for the DataSource. Each DataSource object represents a data
 * source in the JEVis System at runtime. Each Data Source implementation runs
 * in his own thread, so in principle its enough to implement the run and
 * initialize method, cause they are the only methods which are used from
 * outside. For structure purposes it is recommended to use also the other
 * methods and implement the work flow similar to the other drivers.
 *
 * @author Broder
 */
public interface DataSource {
    Logger logger = LogManager.getLogger(DataSource.class);


    void run();

    /**
     * Initialize the Data source. For the generic data sources all attributes
     * under the data source object in the JEVis System are loaded.
     *
     * @param dataSourceJEVis
     */
    void initialize(JEVisObject dataSourceJEVis);

    /**
     * Sends the sample request to the data source and gets the data from the
     * query.
     *
     * @param channel
     * @return
     */
    List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception;

    /**
     * Parse the data from the input from the data source query. There
     *
     * @param input
     */
    void parse(List<InputStream> input);

    /**
     * Imports the results.
     */
    void importResult();

    default boolean isReady(JEVisObject object) {
        DateTime lastRun = getLastRun(object);
        Long cycleTime = getCycleTime(object);
        DateTime nextRun = lastRun.plusMillis(cycleTime.intValue());
        return DateTime.now().withZone(getTimeZone(object)).equals(nextRun) || DateTime.now().isAfter(nextRun);
    }

    default DateTimeZone getTimeZone(JEVisObject object) {
        DateTimeZone zone = DateTimeZone.UTC;

        JEVisAttribute timeZoneAttribute = null;
        try {
            timeZoneAttribute = object.getAttribute("Timezone");
            if (timeZoneAttribute != null) {
                JEVisSample lastTimeZoneSample = timeZoneAttribute.getLatestSample();
                if (lastTimeZoneSample != null) {
                    zone = DateTimeZone.forID(lastTimeZoneSample.getValueAsString());
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        return zone;
    }

    default DateTime getLastRun(JEVisObject object) {
        DateTime dateTime = new DateTime(1990, 1, 1, 0, 0, 0).withZone(getTimeZone(object));

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    dateTime = new DateTime(lastSample.getValueAsString());
                }
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: ", e);
        }

        return dateTime;
    }

    default Long getCycleTime(JEVisObject object) {
        Long aLong = null;

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Cycle Time");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    aLong = lastSample.getValueAsLong();
                }
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source cycle time: ", e);
        }

        return aLong;
    }

    default void finishCurrentRun(JEVisObject object) {
        Long cycleTime = getCycleTime(object);
        DateTime lastRun = getLastRun(object);
        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                DateTime dateTime = lastRun.plusMillis(cycleTime.intValue());
                JEVisSample newSample = lastRunAttribute.buildSample(DateTime.now(), dateTime);
                newSample.commit();
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: ", e);
        }
    }
}
