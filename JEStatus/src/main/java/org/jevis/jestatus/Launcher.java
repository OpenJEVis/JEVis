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
package org.jevis.jestatus;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;

import java.util.List;

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
    private static final String APP_INFO = "JEStatus";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "JEStatus";
    private final Command commands = new Command();
    private Config config;
    private Long furthestReported;
    private Long latestReported;

    public Launcher(String[] args, String appname) {
        super(args, appname);

    }

    public static void main(String[] args) {

        logger.info("-------Start JEStatus-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }

    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(Long id) {
        AlarmHandler ah = new AlarmHandler(ds, furthestReported, latestReported);

        try {
            ah.checkAlarm();
        } catch (JEVisException e) {
            logger.error(e);
        }
    }

    @Override
    protected void runServiceHelp() {

        if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
            try {
                ds.clearCache();
                ds.preload();
                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);
                getTimeConstraints();
            } catch (JEVisException e) {
                logger.error(e);
            }

            if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {
                logger.info("Service is enabled.");
                try {
                    AlarmHandler ah = new AlarmHandler(ds, furthestReported, latestReported);
                    ah.checkAlarm();

                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            } else {
                logger.info("Service is disabled.");
            }
        } else {
            logger.info("Still running queue. Going to sleep again.");
        }

        try {
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    private void getTimeConstraints() {
        try {
            JEVisClass serviceClass = ds.getJEVisClass(APP_SERVICE_CLASS_NAME);
            List<JEVisObject> listServiceObjects = ds.getObjects(serviceClass, false);
            furthestReported = listServiceObjects.get(0).getAttribute("Furthest reported").getLatestSample().getValueAsLong();
            latestReported = listServiceObjects.get(0).getAttribute("Latest reported").getLatestSample().getValueAsLong();
        } catch (Exception e) {
            logger.error("Couldn't get Service status from the JEVis System");
        }
    }

    @Override
    protected void runComplete() {

    }
}
