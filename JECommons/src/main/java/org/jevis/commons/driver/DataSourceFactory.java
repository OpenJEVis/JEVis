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
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Broder
 */
public class DataSourceFactory extends DriverFactory {
    private static final Logger logger = LogManager.getLogger(DataSourceFactory.class);

    private static Map<String, Class> _dataSourceClasses = new HashMap<>();
//    private static String _folder = "/home/broder/tmp/";

    public static void initializeDataSource(JEVisDataSource client) {
        _dataSourceClasses = initialize(client, DataCollectorTypes.DataSourceDriverDirectory.NAME, DataCollectorTypes.Driver.DataSourceDriver.NAME);
    }

    public static void setDataSourceClasses(Map<String, Class> _dataSourceClasses) {
        DataSourceFactory._dataSourceClasses = _dataSourceClasses;
    }

    public static DataSource getDataSource(JEVisObject dataSourceJEVis) {
        logger.debug("getDataSource: {} from {}", dataSourceJEVis, _dataSourceClasses);
        DataSource dataSource = null;
        try {
            String identifier = dataSourceJEVis.getJEVisClass().getName();
            logger.debug("class identifier: {}", identifier);
            Class dataSourceClass = _dataSourceClasses.get(identifier);
            /**
             * cast needs to be removed
             */
            dataSource = (DataSource) dataSourceClass.newInstance();
            logger.debug("done init dataSource");
        } catch (JEVisException | InstantiationException | NullPointerException | IllegalAccessException ex) {
            logger.fatal("Error creating DataSource", ex);
            ex.printStackTrace();
        }
        return dataSource;
    }

    public static boolean containDataSource(JEVisObject dataSourceJEVis) {
        boolean contains = false;
        try {
            String identifier = dataSourceJEVis.getJEVisClass().getName();
            Class datasourceClass = _dataSourceClasses.get(identifier);
            if (datasourceClass != null) {
                contains = true;
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return contains;
    }

}
