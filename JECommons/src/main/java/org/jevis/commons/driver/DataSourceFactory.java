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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import static org.jevis.commons.driver.DriverFactory.initialize;

/**
 *
 * @author Broder
 */
public class DataSourceFactory extends DriverFactory {

    private static Map<String, Class> _dataSourceClasses = new HashMap<>();
//    private static String _folder = "/home/broder/tmp/";

    public static void initializeDataSource(JEVisDataSource client) {
        _dataSourceClasses = initialize(client, DataCollectorTypes.DataSourceDriverDirectory.NAME, DataCollectorTypes.Driver.DataSourceDriver.NAME);
    }

    public static void setDataSourceClasses(Map<String, Class> _dataSourceClasses) {
        DataSourceFactory._dataSourceClasses = _dataSourceClasses;
    }

    public static DataSource getDataSource(JEVisObject dataSourceJEVis) {
        DataSource dataSource = null;
        try {
            String identifier = dataSourceJEVis.getJEVisClass().getName();
            Class datasourceClass = _dataSourceClasses.get(identifier);
            dataSource = (DataSource) datasourceClass.newInstance();
        } catch (JEVisException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(ParserFactory.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ParserFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return contains;
    }

}
