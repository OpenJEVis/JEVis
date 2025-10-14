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
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datasource.DataSourceLoader;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.commons.task.TaskPrinter;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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
    protected final ConcurrentHashMap<Long, DateTime> runningJobs = new ConcurrentHashMap<>();

    private final Map<String, JEVisOption> optMap;
    private boolean active = false;
    protected JEVisDataSource ds;
    protected JCommander comm;
    protected BasicSettings settings = new BasicSettings();
    protected String[] args;
    protected ExecutorService executor;
    protected int cycleTime = 900000;
    protected final ConcurrentHashMap<Long, DateTime> plannedJobs = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Long, FutureTask<?>> runnables = new ConcurrentHashMap<>();
    protected String APP_SERVICE_CLASS_NAME;
    protected static final String SERVICE_THREAD_COUNT = "Max Number Threads";
    private int threadCount = 4;
    private String emergency_config;
    private long maxThreadTime = 900000L;

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
                List<String> tempList = new ArrayList<>(Arrays.asList(settings.jevisid.split(",")));
                List<Long> idList = new ArrayList<>();

                for (String str : tempList) {
                    idList.add(Long.parseLong(str));
                }

                runSingle(idList);
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

    protected boolean checkConnection() {

        connect(0);

        return active;
    }

    private boolean connect(int counter) {

        if (counter < 12) {
            try {
                active = ds.connect(optMap.get(JEVUSER).getValue(), optMap.get(JEVPW).getValue());
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            if (!active) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
                connect(counter);
            }
        }

        return active;
    }

    protected void sleep() {
        try {
            logger.info("Entering sleep mode for " + cycleTime + " ms.");
            Thread.sleep(cycleTime);

            try {
                TaskPrinter.printJobStatus(LogTaskManager.getInstance());
            } catch (Exception e) {
                logger.error("Could not print task list", e);
            }

            runServiceHelp();
        } catch (InterruptedException e) {
            logger.error("Interrupted sleep: ", e);
        }
    }

    /**
     * Handles all commands from @see BasicSettings
     */
    protected void handleBasic() {
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
                connect(0);
            } catch (Exception ex) {
                logger.fatal("Could not connect! Check login and password.", ex);
            }

            if (isActive()) {
                try {
                    ds.preload();
                } catch (JEVisException e) {
                    logger.fatal("Could not preload items!", e);
                }

                try {
                    I18n.getInstance().selectBundle(Locale.getDefault());
                } catch (Exception e) {
                    logger.error("Could not get default locale!", e);
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
    protected abstract void runSingle(List<Long> ids);

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

    protected void checkLastJob() {
        if (plannedJobs.isEmpty() && runningJobs.isEmpty()) {
            logger.info("Last job. Clearing cache.");
            setServiceStatus(APP_SERVICE_CLASS_NAME, 1L);
//            ds.clearCache();
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
            JEVisAttribute threadCountAttribute = listServices.get(0).getAttribute(SERVICE_THREAD_COUNT);
            ds.reloadAttribute(threadCountAttribute);
            threadCount = threadCountAttribute.getLatestSample().getValueAsLong().intValue();
            logger.debug("Set Thread count to: {}", threadCount);
        } catch (Exception e) {
            logger.error("Couldn't get Service thread count from the JEVis System");
        }
        executor = Executors.newFixedThreadPool(threadCount);
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
            JEVisAttribute enabledAttribute = listServiceObjects.get(0).getAttribute("Enable");
            ds.reloadAttribute(enabledAttribute);
            enabled = enabledAttribute.getLatestSample().getValueAsBoolean();
        } catch (Exception e) {
            logger.error("Couldn't get Service status from the JEVis System");
        }
        return enabled;
    }

    /**
     * checks the service status of the JEVis Service
     *
     * @param serviceObject
     * @return Boolean
     */
    protected Boolean checkServiceStatus(JEVisObject serviceObject) {
        Boolean enabled = true;
        try {
            JEVisAttribute enabledAttribute = serviceObject.getAttribute("Enable");
            ds.reloadAttribute(enabledAttribute);
            enabled = enabledAttribute.getLatestSample().getValueAsBoolean();
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
            JEVisAttribute cycleTimeAttribute = listServices.get(0).getAttribute("Cycle Time");
            ds.reloadAttribute(cycleTimeAttribute);
            cycleTime = cycleTimeAttribute.getLatestSample().getValueAsLong().intValue();
            logger.info("Service cycle time from service: " + cycleTime);
        } catch (Exception e) {
            logger.error("Couldn't get Service cycle time from the JEVis System");
        }
    }

    /**
     * retrieves the cycle time for the JEVis Service
     *
     * @param serviceObject
     * @return Boolean
     */
    protected int getCycleTimeFromService(JEVisObject serviceObject) {
        int cycleTime = 900000;
        try {
            JEVisAttribute cycleTimeAttribute = serviceObject.getAttribute("Cycle Time");
            ds.reloadAttribute(cycleTimeAttribute);
            cycleTime = cycleTimeAttribute.getLatestSample().getValueAsLong().intValue();
            logger.info("Service cycle time from service: " + cycleTime);
        } catch (Exception e) {
            logger.error("Couldn't get Service cycle time from the JEVis System");
        }

        return cycleTime;
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

            if (status == 1L) {
                logger.info("Clearing old stati of {}", serviceClassName);
                statusAttribute.deleteSamplesBetween(new DateTime(0), new DateTime().minusDays(30));
            }

            logger.info("Set status of {} to {}", serviceClassName, status);
            JEVisSample newStatus = statusAttribute.buildSample(DateTime.now(), status);
            newStatus.commit();
        } catch (Exception e) {
            logger.error("Couldn't write status to the JEVis System");
        }
    }

    protected void removeJob(JEVisObject object) {
        runnables.forEach((objectID, futureTask) -> {
            if (objectID.equals(object.getID())) {
                futureTask.cancel(true);
            }
        });

        runningJobs.remove(object.getID());
        plannedJobs.remove(object.getID());
        runnables.remove(object.getID());
    }

    protected void checkForTimeout() {
        if (!runningJobs.isEmpty() && maxThreadTime != 0L) {
            SampleHandler sampleHandler = new SampleHandler();
            List<Long> toRemove = new ArrayList<>();
            JEVisClass dataSource = null;
            try {
                dataSource = ds.getJEVisClass("Data Server");
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            for (Map.Entry<Long, DateTime> entry : runningJobs.entrySet()) {
                try {
                    Interval interval = new Interval(entry.getValue(), new DateTime());

                    Long maxTime = maxThreadTime;
                    JEVisObject object = ds.getObject(entry.getKey());

                    if (dataSource != null && dataSource.getHeirs().contains(object.getJEVisClass())) {
                        maxTime = sampleHandler.getLastSample(object, "Max thread time", maxThreadTime);
                    }

                    if (interval.toDurationMillis() > maxTime) {
                        logger.warn("Task for {} is out of time, trying to cancel", entry.getKey());
                        try {
                            runnables.get(entry.getKey()).cancel(true);
                            logger.warn("Runnable for {} cancelled successfully", entry.getKey());
                        } catch (Exception e) {
                            logger.error("Could not cancel job {}", entry.getKey(), e);
                        }
                        toRemove.add(entry.getKey());
                        plannedJobs.remove(entry.getKey());
                        LogTaskManager.getInstance().getTask(entry.getKey()).setStatus(Task.Status.FAILED);
                    }
                } catch (Exception e) {
                    logger.error("Could not stop object with id {}", entry.getKey(), e);
                }
            }

            toRemove.forEach(runnables::remove);
        }
    }

    protected boolean isEnabled(JEVisObject jeVisObject) {
        JEVisAttribute enabledAtt = null;
        try {
            enabledAtt = jeVisObject.getAttribute("Enabled");
            if (enabledAtt != null && enabledAtt.hasSample()) {
                return enabledAtt.getLatestSample().getValueAsBoolean();
            }
        } catch (Exception e) {
            logger.error("Could not get enabled status of {} with id {}", jeVisObject.getName(), jeVisObject.getID(), e);
        }
        return false;
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

    public void setMaxThreadTime(long maxThreadTime) {
        this.maxThreadTime = maxThreadTime;
    }
}
