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
package org.jevis.commons.datasource;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.CommonOptions;

/**
 * The DataSourceLoader helps with the dynamic loading of a JEVisDataSource
 *
 * @author Florian Simon
 */
public class DataSourceLoader {

    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataSourceLoader.class);

    public DataSourceLoader() {
    }

    /**
     * Load a JEVisDataSource based on a JEVisConfiguration object. The
     * DataSource is not initialized yet.
     *
     * @param config
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public JEVisDataSource getDataSource(JEVisOption config) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.trace("DSL config.size: {}", config.getOptions().size());
        if (config.getKey().equalsIgnoreCase(CommonOptions.DataSource.DataSource.getKey())) {

            if (config.hasOption(CommonOptions.DataSource.CONNECTION.getKey())) {
                return (JEVisDataSource) Class.forName("org.jevis.api.sql.JEVisDataSourceSQL").newInstance();
            }
            if (config.hasOption(CommonOptions.DataSource.CLASS.getKey())) {
                JEVisOption classOption = config.getOption(CommonOptions.DataSource.CLASS.getKey());
                String className = classOption.getValue();
                return (JEVisDataSource) Class.forName(className).newInstance();
            } else {
                return (JEVisDataSource) Class.forName("org.jevis.jeapi.ws.JEVisDataSourceWS").newInstance();
            }

        } else {
            throw new ClassNotFoundException("DataSource option group not found");
        }

    }

    /**
     * Get a DataSource by class name
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public JEVisDataSource getDataSource(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return (JEVisDataSource) Class.forName(className).newInstance();
    }

    /**
     * instantiate the class by name
     *
     * @param <T>
     * @param className
     * @param type
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public <T> T instantiate(final String className, final Class<T> type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return type.cast(Class.forName(className).newInstance());
    }

}
