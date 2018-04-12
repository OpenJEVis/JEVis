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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;

/**
 *
 * @author bf
 */
public class DriverFactory {

    public static Map<String, Class> initialize(JEVisDataSource client, String driverDirClassName, String driverClassName) {
        Map<String, Class> classes = new HashMap<String, Class>();
        try {
            JEVisClass serviceClass = client.getJEVisClass(DataCollectorTypes.JEDataCollector.NAME);
            JEVisClass driverDirClass = client.getJEVisClass(driverDirClassName);
            JEVisClass jevisClass = client.getJEVisClass(driverClassName);
            List<JEVisObject> dataCollectorServices = client.getObjects(serviceClass, true);
            if (dataCollectorServices.size() == 1) {
                JEVisObject dataCollectorService = dataCollectorServices.get(0);
                List<JEVisObject> driverDirs = dataCollectorService.getChildren(driverDirClass, false);
                if (driverDirs.size() == 1) {
                    JEVisObject driverDir = driverDirs.get(0);
                    for (JEVisObject driver : driverDir.getChildren(jevisClass, true)) {
                        JEVisType enableType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.ENABLED);
                        Boolean enabled = DatabaseHelper.getObjectAsBoolean(driver, enableType);
                        if (!enabled) {
                            continue;
                        }

                        JEVisType fileType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.SOURCE_FILE);
                        JEVisFile file = DatabaseHelper.getObjectAsFile(driver, fileType);

                        JEVisType classType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.MAIN_CLASS);
                        String className = DatabaseHelper.getObjectAsString(driver, classType);

                        JEVisType jevisType = driver.getJEVisClass().getType(DataCollectorTypes.Driver.JEVIS_CLASS);
                        String jevisName = DatabaseHelper.getObjectAsString(driver, jevisType);
                        Class tmpClass = ByteClassLoader.loadDriver(file, className);
                        classes.put(jevisName, tmpClass);
                    }
                }
            }
        } catch (JEVisException | MalformedURLException | ClassNotFoundException ex) {
            Logger.getLogger(ParserFactory.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ParserFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return classes;
    }
}
