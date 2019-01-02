/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of Launcher.
 * <p>
 * Launcher is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * Launcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Launcher. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Launcher is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jealarm;

import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.cli.AbstractCliApp;

import java.util.Objects;

/**
 * The Launcher is an minimalistic alarm notification tool for JEVis 3.0
 * <p>
 * TODO: Add the possibility to create template file for the emails which allow
 * much more control over the formatting of the email notification.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Launcher extends AbstractCliApp {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Launcher.class);

    private final static Option help = new Option("h", "help", true, "Print this message.");
    private final static Option configFile = new Option("c", "config", true, "Set the configuration file");
    private final static Option mode = new Option("sm", "servicemode", true, "Set the service mode");
    private final static Options options = new Options();
    private static final String APP_INFO = "JEAlarm";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "JEAlarm";
    private final Command commands = new Command();
    private Config config;

    public Launcher(String[] args, String appname) {
        super(args);

    }

    public static void main(String[] args) {

        logger.info("-------Start JEAlarm-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    @Override
    protected void addCommands() {

    }

    @Override
    protected void handleAdditionalCommands() {
        options.addOption(configFile);
        options.addOption(help);
        options.addOption(mode);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        HelpFormatter formatter = new HelpFormatter();

        if (Objects.requireNonNull(cmd).hasOption(help.getLongOpt())) {
            formatter.printHelp("Launcher 1.0", options);
        }

        if (cmd.hasOption(configFile.getLongOpt())) {
            config = null;
            try {
                config = new Config(cmd.getOptionValue(configFile.getLongOpt()));
            } catch (ConfigurationException e) {
                logger.error(e);
            }
        } else {
            logger.info("Missing configuration file..");
            formatter.printHelp("Launcher 1.0  2019-01-02", options);
        }
    }

    @Override
    protected void runSingle(Long id) {
        AlarmHandler ah = new AlarmHandler(config, ds);
        for (Alarm alarm : Objects.requireNonNull(config).getAlarms()) {
            try {
                ah.checkAlarm(alarm);
            } catch (JEVisException e) {
                logger.error(e);
            }
        }
    }

    @Override
    protected void runServiceHelp() {
        try {
            ds.clearCache();
            ds.preload();
            getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
        } catch (JEVisException e) {
            logger.error(e);
        }

        if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
            logger.info("Service is enabled.");
            try {
                AlarmHandler ah = new AlarmHandler(config, ds);
                for (Alarm alarm : Objects.requireNonNull(config).getAlarms()) {
                    ah.checkAlarm(alarm);
                }
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }
        } else {
            logger.info("Service is disabled.");
        }
        try {
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    @Override
    protected void runComplete() {

    }
}
