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
    private static final String DS_CACHE = "org.jevis.application.cache.JEVisDataSourceCache";

    private final Map<String, JEVisOption> optMap;
    private boolean active = false;
    protected JEVisDataSource ds;
    protected JCommander comm;
    protected BasicSettings settings = new BasicSettings();
    protected String[] args;
    protected ForkJoinPool forkJoinPool;
    protected int cycleTime = 900000;

    protected ConcurrentHashMap<String, String> runningJobs = new ConcurrentHashMap();

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
        if (active) {
            if (settings.servicemode.equals(BasicSettings.SINGLE)) {
                runSingle(settings.jevisid);
            } else if (settings.servicemode.equals(BasicSettings.SERVICE)) {
                runService(settings.cycle_time);
            } else if (settings.servicemode.equals(BasicSettings.COMPLETE)) {
                runComplete();
            }
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
        if (active) {
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

            /**
             * Caching is not supported by JEAPI-WS. JEVisDataSourceCache is deprecated.
             */
//            if (settings.cache) {
//                Class[] argsClass = new Class[]{JEVisDataSource.class};
//                try {
//                    Class cacheClass = Class.forName(DS_CACHE);
//                    Constructor ctor = cacheClass.getConstructor(argsClass);
//                    ds = JEVisDataSource.class.cast(ctor.newInstance(ds));
//                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
//                    Logger.getLogger(AbstractCliApp.class.getName()).log(Level.SEVERE, "Failed to create the cache.", ex);
//                } catch (ClassNotFoundException ex) {
//                    Logger.getLogger(AbstractCliApp.class.getName()).log(Level.SEVERE, "JEVisDataSourceCache class not found. Show org.jevis.application.cache for more info.", ex);
//                }
//            }

            ds.setConfiguration(new ArrayList<>(optMap.values()));

            try {
                active = ds.connect(optMap.get(JEVUSER).getValue(), optMap.get(JEVPW).getValue());
            } catch (JEVisException ex) {
                logger.fatal("Could not connect! Check login and password.", ex);
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
     * run for service mode business logic in this method
     */
    protected void runService(Integer cycle_time) {
        logger.info("Start Service Mode");

        Thread service = new Thread(() -> runServiceHelp());
        Runtime.getRuntime().addShutdownHook(
                new ShutdownHookThread(service)
        );

        if (cycle_time != null) cycleTime = cycle_time;

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

    protected abstract void runServiceHelp();

    /**
     * run for complete mode business logic in this method
     */
    protected abstract void runComplete();


    protected void initializeThreadPool(String JEVisClassName) {
        Integer threadCount = 4;
        try {
            JEVisClass dataCollectorClass = ds.getJEVisClass(JEVisClassName);
            List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
            threadCount = listDataCollectorObjects.get(0).getAttribute("Max Number Threads").getLatestSample().getValueAsLong().intValue();
            logger.info("Set Thread count to: " + threadCount);
        } catch (Exception e) {

        }
        forkJoinPool = new ForkJoinPool(threadCount);
    }

    protected Boolean checkServiceStatus(String JEVisClassName) {
        Boolean enabled = true;
        try {
            JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
            List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
            enabled = listDataCollectorObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
        } catch (JEVisException e) {

        }
        return enabled;
    }

    protected void getCycleTimeFromService(String JEVisClassName) throws JEVisException {
        JEVisClass dataCollectorClass = ds.getJEVisClass(JEVisClassName);
        List<JEVisObject> listDataCollectorObjects = ds.getObjects(dataCollectorClass, false);
        cycleTime = listDataCollectorObjects.get(0).getAttribute("Cycle Time").getLatestSample().getValueAsLong().intValue();
        logger.info("Service cycle time from service: " + cycleTime);
    }

    public class Command {

        @Parameter(names = {"--driver-folder", "-df"}, description = "Sets the root folder for the driver structure")
        public String driverFolder;
    }
}
