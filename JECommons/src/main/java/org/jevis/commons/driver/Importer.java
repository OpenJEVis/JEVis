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
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

/**
 * The interface for the Importer. Each Importer object represents an importer
 * object in the JEVis System. This class is optional. If no import object is
 * given, the system uses the JEVisImporter, who imports the results into the
 * JEVis System by default.
 *
 * @author Broder
 */
public interface Importer {

    /**
     * Imports the results into a given target.
     *
     * @param result
     * @return
     */
    public DateTime importResult(List<Result> result);

    /**
     * Initialize the importer.
     *
     * @param dataSource
     */
    public void initialize(JEVisObject dataSource);

    public Object getLatestDatapoint();

    /**
     * Gets the date of the latest imported data point. This information is
     * needed to set the lastreadout.
     *
     * @return
     */
//    public DateTime getLatestDatapoint();

}
