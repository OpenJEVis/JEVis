/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datasource.DataSourceLoader;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Artur Iablokov
 */
public abstract class AbstractCliApp {
    private static final Logger logger = LogManager.getLogger(AbstractCliApp.class);

    private static final String JEVIS_BRANCH = "jevis.";
    private static final String AUTH_BRANCH = "authentication.";
    private static final String DS = "datasource";
    private static final String JEVUSER = "jevisuser";
    private static final String JEVPW = "jevispass";

    private final Map<String, JEVisOption> optMap;
    private boolean active = false;
    protected JEVisDataSource ds;
    protected JCommander comm;
    protected BasicSettings settings = new BasicSettings();
    protected String[] args;
    protected ForkJoinPool forkJoinPool;
    protected int cycleTime = 900000;

    protected ConcurrentHashMap<Long, String> runningJobs = new ConcurrentHashMap();
    protected ConcurrentHashMap<Long, String> plannedJobs = new ConcurrentHashMap();
    private int threadCount = 4;
    private String emergency_config;

    /**
     * @param args start params
     */
    public AbstractCliApp(String[] args) {
        this.optMap = new HashMap<>();
        this.args = args;
        comm = JCommander.newBuilder().addObject(settings).build();
    }

    /**
     * @param args    start params
     * @param appname application name
     */
    public AbstractCliApp(String[] args, String appname) {
        this(args);
        comm.setProgramName(appname);
    }

    /**
     * initializes the settings and, if successful, executes the logic
     */
    public void execute() {
        init();

        if (isActive()) {
            setServiceStatus(comm.getProgramName(), 1L);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> setServiceStatus(comm.getProgramName(), 0L)));
        } else {
            if (settings.emergency_Config != null) emergency_config = settings.emergency_Config;
        }

        switch (settings.servicemode) {
            case BasicSettings.SINGLE:
                runSingle(settings.jevisid);
                break;
            case BasicSettings.SERVICE:
                runService();
                break;
            case BasicSettings.COMPLETE:
                runComplete();
                break;
        }
    }

    private void init() {
        addCommands();
        try {
            comm.parse(args);
        } catch (ParameterException e) {
            logger.fatal(e);
            //show help if required params are missing
            e.usage();
            System.exit(1);
        }

        handleBasic();
        if (isActive()) {
            handleAdditionalCommands();
        }
    }

    /**
     * Handles all commands from @see BasicSettings
     */
    private void handleBasic() {
        if (settings.help) {
            comm.usage();
        } else {

            try {
                optMap.putAll(ConfHelper.ParseJEVisConfiguration(settings.config, JEVIS_BRANCH, optMap));
                optMap.putAll(ConfHelper.ParseJEVisConfiguration(settings.config, AUTH_BRANCH, optMap));
            } catch (ConfigurationException ex) {
                logger.warn("Configuration parsing failed. Сheck the configuration file", ex);
            }

            if (!settings.options.isEmpty()) {
                optMap.putAll(ConfHelper.ParseJEVisConfiguration(settings.options, optMap));
            }

            DataSourceLoader dsl = new DataSourceLoader();

            try {
                ds = dsl.getDataSource(optMap.get(DS));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                logger.fatal("JEVisDataSource not created. Сheck the configuration file data.", ex);
            }

            ds.setConfiguration(new ArrayList<>(optMap.values()));

            try {
                active = ds.connect(optMap.get(JEVUSER).getValue(), optMap.get(JEVPW).getValue());
            } catch (Exception ex) {
                logger.fatal("Could not connect! Check login and password.", ex);
            }

            if (isActive()) {
                try {
                    ds.preload();
                } catch (JEVisException e) {
                    logger.fatal("Could not preload items!", e);
                }
            }
        }
    }

    /**
     * Adds new commands to the program
     * <p>
     * EXAMPLE:
     * <p>
     * (inner class) - defines additional commands public class Command {
     *
     * @Parameter(names = {"-nc", "--newcommand"}, description = "new command
     * description here") public boolean nc;}
     * @Override protected void addCommands() { comm.addObject(cm);}
     */
    protected abstract void addCommands();

    /**
     * Handles additional commands
     * <p>
     * EXAMPLE:
     *
     * @Override protected void handleAdditionalCommands() { if (nc) {{
     * logger.info("new command is true") }}
     */
    protected abstract void handleAdditionalCommands();

    /**
     * run for single mode business logic in this method
     */
    protected abstract void runSingle(Long id);

    /**
     * runs the service mode of the current service
     */
    protected void runService() {
        logger.info("Start Service Mode");

        Thread service = new Thread(this::runServiceHelp);
        Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread(service)
        );

        try {
            service.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            logger.info("Press CTRL^C to exit..");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * used for override in extended apps for service mode
     */
    protected abstract void runServiceHelp();

    /**
     * run for complete mode business logic in this method
     */
    protected abstract void runComplete();

    /**
     * Initializes the thread pool used for processing jobs of the current service
     *
     * @param serviceClassName
     */
    protected void initializeThreadPool(String serviceClassName) {

        threadCount = 4;
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServices = ds.getObjects(serviceClass, false);
            threadCount = listServices.get(0).getAttribute("Max Number Threads").getLatestSample().getValueAsLong().intValue();
            logger.info("Set Thread count to: " + threadCount);
        } catch (Exception e) {
            logger.error("Couldn't get Service thread count from the JEVis System");
        }
        forkJoinPool = new ForkJoinPool(threadCount);
    }

    /**
     * checks the service status of the JEVis Service
     *
     * @param serviceClassName
     * @return Boolean
     */
    protected Boolean checkServiceStatus(String serviceClassName) {
        Boolean enabled = true;
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServiceObjects = ds.getObjects(serviceClass, false);
            enabled = listServiceObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
        } catch (Exception e) {
            logger.error("Couldn't get Service status from the JEVis System");
        }
        return enabled;
    }

    /**
     * retrieves the cycle time for the JEVis Service
     *
     * @param serviceClassName
     * @return Boolean
     */
    protected void getCycleTimeFromService(String serviceClassName) {
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServices = ds.getObjects(serviceClass, false);
            cycleTime = listServices.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
            logger.info("Service cycle time from service: " + cycleTime);
        } catch (Exception e) {
            logger.error("Couldn't get Service cycle time from the JEVis System");
        }
    }

    /**
     * Sets the status for the JEVis Service
     *
     * @param serviceClassName
     * @param status
     */
    protected void setServiceStatus(String serviceClassName, Long status) {
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServices = ds.getObjects(serviceClass, false);
            JEVisAttribute statusAttribute = listServices.get(0).getAttribute("Status");
            logger.info("Set status of" + serviceClassName + " " + status);
            JEVisSample newStatus = statusAttribute.buildSample(DateTime.now(), status);
            newStatus.commit();
        } catch (Exception e) {
            logger.error("Couldn't write status to the JEVis System");
        }
    }


    public class Command {

        @Parameter(names = {"--driver-folder", "-df"}, description = "Sets the root folder for the driver structure")
        public String driverFolder;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public boolean isActive() {
        return active;
    }

    public String getEmergency_config() {
        return emergency_config;
    }
}
