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
import org.jevis.api.*;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author bf
 */
public class JEVisImporterAdapter {
    private static final Logger logger = LogManager.getLogger(JEVisImporterAdapter.class);
    private static final String STATUS_LOG = "Status Log";

    public synchronized static void importResults(List<Result> results, Importer importer, JEVisObject channel) {
        DateTime date = importer.importResult(results);
        setLastReadout(channel, date);
        logger.info("Result List was imported to JEVis Last Readout was set To: {}", date);
    }

    public synchronized static void importResultsWithOffset(List<Result> results, Importer importer, JEVisObject channel, int offset) {
        DateTime date = importer.importResult(results);
        date = date.plusSeconds(offset);
        setLastReadout(channel, date);
        logger.info("Result List was imported to JEVis Last Readout was set To: {}", date);
    }

    public synchronized static void importResults(List<Result> results, List<JEVisSample> statusResults, Importer importer, JEVisObject channel) {
        if (results.isEmpty() && statusResults.isEmpty()) return;

        DateTime date = importer.importResult(results);
        DateTime statusDate = null;
        if (!statusResults.isEmpty()) {
            try {
                JEVisType statusLogType = channel.getJEVisClass().getType(STATUS_LOG);
                JEVisAttribute statusLog = channel.getAttribute(statusLogType);
                statusLog.addSamples(statusResults);
                statusDate = statusResults.get(statusResults.size() - 1).getTimestamp();

            } catch (JEVisException e) {
                logger.error("Could not add Status Log Samples", e);
            }
        }
        if (statusDate == null) {
            setLastReadout(channel, date);
        } else {
            if (date == null) {
                setLastReadout(channel, statusDate);
            } else if (statusDate.isAfter(date)) {
                setLastReadout(channel, statusDate);
            } else {
                setLastReadout(channel, date);
            }
        }
    }

    private static void setLastReadout(JEVisObject channel, DateTime lastDateTime) {
        try {
            String toString = lastDateTime.toString();

            channel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).deleteSamplesBetween(new DateTime(1990, 1, 1, 0, 0, 0, 0), new DateTime().minusMonths(1));
            JEVisSample buildSample = channel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(new DateTime(), toString);
            buildSample.commit();
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }
}
