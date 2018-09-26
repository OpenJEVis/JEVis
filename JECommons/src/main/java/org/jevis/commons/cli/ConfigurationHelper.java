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
package org.jevis.commons.cli;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.config.XMLConfigFileReader;

import java.io.File;

/**
 * This class helps with common command line interface tasks
 *
 * NOTE: Prototype, this class is not functional yet
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ConfigurationHelper {

    public static String DATASOURCE_PREFIX = "jevis.datasource";
    private static final Logger logger = LogManager.getLogger(ConfigurationHelper.class);

    /**
     * Will create a new ConfigurationHelper for the given argument[] arrays and
     * the Options
     *
     * TODO: implement
     *
     * @param args
     * @param options
     * @throws ParseException
     */
    public ConfigurationHelper(String[] args, Options options) throws ParseException {

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args, true);

        if (line.hasOption(CommonCLIOptions.CONFIG_FILE.getOpt())) {
            String fileName = line.getOptionValue(CommonCLIOptions.CONFIG_FILE.getOpt(), "");
            File cFile = new File(fileName);
            if (cFile.exists() && cFile.canRead()) {
                try {
                    XMLConfigFileReader reader = new XMLConfigFileReader(cFile);

                } catch (ConfigurationException ex) {
                    logger.fatal(ex);
                }
            } else {
                logger.info("Cant access config file: " + fileName);
            }
        }

    }

    /**
     * Get the datasource from the configuration
     *
     *
     * TODO: implement
     *
     * @return JEVisDataSource
     */
    public JEVisOption getDataSourceConfig() {
        JEVisOption config = new BasicOption();

        return config;
    }

    /**
     * Create an new configuration skeleton
     *
     * @param file
     * @throws ConfigurationException
     */
    public void createConfigFile(File file) throws ConfigurationException {

        //TODO:  XPath expressions for a better implementation see http://www.code-thrill.com/2012/05/configuration-that-rocks-with-apache.html
        XMLConfiguration config = new XMLConfiguration(file);

        config.setProperty("jevis.datasource.class", "org.jevis.api.sql.JEVisDataSourceSQL");
        config.setProperty("jevis.datasource.host", "openjevis.org");
        config.setProperty("jevis.datasource.schema", "jevis");
        config.setProperty("jevis.datasource.port", "13306");
        config.setProperty("jevis.datasource.username", "jevis");
        config.setProperty("jevis.datasource.password", "jevistest");

        config.save();

    }

}
