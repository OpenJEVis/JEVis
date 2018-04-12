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
package org.jevis.commons.driver;

import java.util.List;
import org.apache.log4j.Level;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author bf
 */
public class JEVisImporterAdapter {

    public synchronized static void importResults(List<Result> results, Importer importer, JEVisObject channel) {
        DateTime date = importer.importResult(results);
        setLastReadout(channel, date);
    }

    private static void setLastReadout(JEVisObject channel, DateTime lastDateTime) {
        try {
            String toString = lastDateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));

            JEVisSample buildSample = channel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(new DateTime(), toString);
            buildSample.commit();
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(DataSourceHelper.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
