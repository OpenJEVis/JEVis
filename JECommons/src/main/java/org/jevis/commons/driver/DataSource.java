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

import java.io.InputStream;
import java.util.List;
import org.jevis.api.JEVisObject;

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
public interface DataSource extends Runnable {

    @Override
    public void run();

    /**
     * Initialize the Data source. For the generic data sources all attributes
     * under the data source object in the JEVis System are loaded.
     *
     * @param dataSourceJEVis
     */
    public void initialize(JEVisObject dataSourceJEVis);

    /**
     * Sends the sample request to the data source and gets the data from the
     * query.
     *
     * @param channel
     * @return
     */
    public List<InputStream> sendSampleRequest(JEVisObject channel);

    /**
     * Parse the data from the input from the data source query. There
     *
     * @param input
     */
    public void parse(List<InputStream> input);

    /**
     * Imports the results.
     *
     */
    public void importResult();

}
