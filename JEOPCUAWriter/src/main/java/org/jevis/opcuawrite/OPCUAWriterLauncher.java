/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.opcuawrite;

import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


/**
 * @author broder
 */
public class OPCUAWriterLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(OPCUAWriterLauncher.class);
    private static Injector injector;
    private static final String APP_INFO = "JEOPCUAWriter";
    private static final String TARGET_ID = "Target ID";
    private static final String VALUE = "Value";
    private static final String LOYTEC_SERVER = "Loytec XML-DL Server";

    private final Command commands = new Command();
    private boolean firstRun = true;
    private final List<JEVisObject> outputChannels = new ArrayList<>();
    private final OPCUAWriter opcuaWriter = new OPCUAWriter();

    private static LogTaskManager taskManager;

    public OPCUAWriterLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEOpcUaWriter-------");
        OPCUAWriterLauncher app = new OPCUAWriterLauncher(args, APP_INFO);
        app.execute();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEOPCUAWriter";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {

        for (Long id : ids) {
            JEVisObject loytecServer = null;

            try {
                logger.info("Try adding Single Mode for ID {}", id);
                loytecServer = ds.getObject(id);
            } catch (Exception ex) {
                logger.error("Could not find Object with id: {}", id);
            }

            if (loytecServer != null) {
                execute(loytecServer);
            }


        }


    }


    @Override
    protected void runServiceHelp() {

        if (checkConnection()) {

            checkForTimeout();

            if (plannedJobs.size() == 0 && runningJobs.size() == 0) {
                if (!firstRun) {
                    try {
                        ds.clearCache();
                        ds.preload();
                    } catch (JEVisException e) {
                    }
                } else firstRun = false;

                getCycleTimeFromService(APP_SERVICE_CLASS_NAME);

                if (checkServiceStatus(APP_SERVICE_CLASS_NAME)) {

                    List<JEVisObject> enabeledLoytecServsObjects = getEnabledDataServer();

                    executeOPCUA(enabeledLoytecServsObjects);

                    logger.info("Queued all loytec objects, entering sleep mode for {} ms", cycleTime);

                } else {
                    logger.info("Service was disabled.");
                }
            } else {
                logger.info("Still running queue. Going to sleep again.");
            }
        }

        sleep();
    }

    @Override
    protected void runComplete() {

    }


    private void getOutputChannels(JEVisObject object) {

        try {
            if (object.getJEVisClassName().equals("Loytec XML-DL Output Channel")) {
                outputChannels.add(object);
            }
            for (JEVisObject child : object.getChildren()) {
                getOutputChannels(child);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }


    private void setLastReadout(JEVisObject outputChannel, DateTime dateTime) throws JEVisException {
        outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(DateTime.now(), dateTime.toString()).commit();
    }

    private void execute(JEVisObject loytecServer) {


        try {
            getOutputChannels(loytecServer);
            opcuaWriter.connectToOPCUAServer(loytecServer);
            for (JEVisObject outputChannel : outputChannels) {

                JEVisObject dataObject = ds.getObject(Long.valueOf(outputChannel.getAttribute(TARGET_ID).getLatestSample().getValue().toString().split(":")[0]));

                if (opcuaWriter.sendOPCUANotification(outputChannel, dataObject)) {
                    setLastReadout(outputChannel, dataObject.getAttribute(VALUE).getTimestampFromLastSample());
                }
            }

        } catch (UaException | ExecutionException | InterruptedException e) {
            logger.error(e);
            for (JEVisObject outputChannel : outputChannels) {
                OPCUAStatus opcUAStatus = new OPCUAStatus(OPCUAStatus.OPC_SERVER_NOT_REACHABLE);
                opcUAStatus.writeStatus(outputChannel, DateTime.now());
            }
        } catch (Exception e) {
            logger.error(e);
        }


        opcuaWriter.disconnect();


    }

    private List<JEVisObject> getEnabledDataServer() {
        JEVisClass loytecServerClass = null;
        List<JEVisObject> loytecServerObjects;
        List<JEVisObject> enabledloytecServer = new ArrayList<>();
        try {
            loytecServerClass = ds.getJEVisClass(LOYTEC_SERVER);
            loytecServerObjects = ds.getObjects(loytecServerClass, true);


            for (JEVisObject loytecServer : loytecServerObjects) {
                if (loytecServer.getAttribute("Enabled").getLatestSample().getValueAsBoolean()) {
                    enabledloytecServer.add(loytecServer);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return enabledloytecServer;
    }


    private void executeOPCUA(List<JEVisObject> loytecServerObjects) {

        logger.info("Number of Reports: {}", loytecServerObjects.size());
        setServiceStatus(APP_SERVICE_CLASS_NAME, 2L);

        loytecServerObjects.forEach(loytecServer -> {
            if (!runningJobs.containsKey(loytecServer.getID())) {
                Runnable runnable = () -> {
                    try {
                        Thread.currentThread().setName(loytecServer.getName() + ":" + loytecServer.getID().toString());
                        runningJobs.put(loytecServer.getID(), new DateTime());

                        LogTaskManager.getInstance().buildNewTask(loytecServer.getID(), loytecServer.getName());
                        LogTaskManager.getInstance().getTask(loytecServer.getID()).setStatus(Task.Status.STARTED);

                        logger.info("---------------------------------------------------------------------");
                        logger.info("current loytec server object: {} with id: {}", loytecServer.getName(), loytecServer.getID());
                        if (!loytecServer.getAttribute("Enabled").getLatestSample().getValueAsBoolean()) {
                            logger.info("Loytec Server is not enabled");
                        } else {

                            execute(loytecServer);
                        }
                    } catch (Exception e) {
                        LogTaskManager.getInstance().getTask(loytecServer.getID()).setStatus(Task.Status.FAILED);
                        removeJob(loytecServer);

                        logger.info("Planned Jobs: {} running Jobs: {}", plannedJobs.size(), runningJobs.size());

                        checkLastJob();
                    } finally {
                        LogTaskManager.getInstance().getTask(loytecServer.getID()).setStatus(Task.Status.FINISHED);
                        removeJob(loytecServer);

                        logger.info("Planned Jobs: {} running Jobs: {}", plannedJobs.size(), runningJobs.size());

                        checkLastJob();
                    }
                };

                FutureTask<?> ft = new FutureTask<Void>(runnable, null);

                runnables.put(loytecServer.getID(), ft);
                executor.submit(ft);
            } else {
                logger.info("Still processing Job {}:{}", loytecServer.getName(), loytecServer.getID());
            }
        });


    }
}