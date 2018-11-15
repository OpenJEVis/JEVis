/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.JEVisServerConnectionCLI;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jedataprocessor.CommandLineParser;
import org.jevis.jedataprocessor.alignment.PeriodAlignmentStep;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.CleanDataAttributeJEVis;
import org.jevis.jedataprocessor.data.CleanDataAttributeOffline;
import org.jevis.jedataprocessor.differential.DifferentialStep;
import org.jevis.jedataprocessor.functional.avg.AverageStep;
import org.jevis.jedataprocessor.functional.sum.SummationStep;
import org.jevis.jedataprocessor.gap.FillGapStep;
import org.jevis.jedataprocessor.limits.LimitsStep;
import org.jevis.jedataprocessor.save.ImportStep;
import org.jevis.jedataprocessor.scaling.ScalingStep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * @author broder
 */
public class ProcessManagerFactory {

    private static final Logger logger = LogManager.getLogger(ProcessManagerFactory.class);
    public static JEVisDataSource jevisDataSource;
    private static ForkJoinPool forkJoinPool;

    public static List<ProcessManager> getProcessManagerList() throws Exception {
        CommandLineParser cmd = CommandLineParser.getInstance();

//        establishConnection();
//        try {
//            jevisDataSource.getObject(7731l).getAttribute("Value").deleteAllSample();
//            jevisDataSource.getObject(7730l).getAttribute("Value").deleteAllSample();
//        } catch (JEVisException ex) {
//            logger.error(null, ex);
//        }
        List<ProcessManager> processManagers = new ArrayList<>();
        //case jevis single
        if (cmd.isAllJEvisMode() || cmd.isServiceMode()) {
            establishConnection();
            processManagers = initProcessManagersFromJEVisAll();
        } else if (cmd.isSingleJEvisMode()) {
            //case jevis all
            establishConnection();
            processManagers = initProcessManagersFromJEVisSingle();
        } else if (cmd.isOfflineMode()) {
            //case offline
            processManagers = initProcessManagersFromOffline();
        }

        initializeThreadPool();

        if (!checkServiceStatus()) processManagers.clear();

        return processManagers;
    }

    private static Boolean checkServiceStatus() {
        Boolean enabled = true;
        try {
            JEVisClass dataProcessorClass = jevisDataSource.getJEVisClass("JEDataProcessor");
            List<JEVisObject> listDataProcessorObjects = jevisDataSource.getObjects(dataProcessorClass, false);
            enabled = listDataProcessorObjects.get(0).getAttribute("Enable").getLatestSample().getValueAsBoolean();
            logger.info("Service is enabled is " + enabled);
        } catch (Exception e) {

        }
        return enabled;
    }

    private static void initializeThreadPool() {
        Integer threadCount = 4;
        try {
            JEVisClass dataProcessorClass = jevisDataSource.getJEVisClass("JEDataProcessor");
            List<JEVisObject> listDataProcessorObjects = jevisDataSource.getObjects(dataProcessorClass, false);
            threadCount = listDataProcessorObjects.get(0).getAttribute("Max Number Threads").getLatestSample().getValueAsLong().intValue();
            logger.info("Set Thread count to: " + threadCount);
        } catch (Exception e) {

        }
        forkJoinPool = new ForkJoinPool(threadCount);
    }

    private static boolean establishConnection() {
        String configFile = CommandLineParser.getInstance().getConfigPath();
        JEVisServerConnectionCLI con = new JEVisServerConnectionCLI(configFile);
        try {
//            jevisDataSource = new JEVisDataSourceSQL("openjevis.org", "13306", "jevis", "jevis", "jevistest");
//            return jevisDataSource.connect("Sys Admin", "OpenJEVis2016");
//            jevisDataSource = new JEVisDataSourceWS(con.getDb(), con.getPort(), con.getSchema(), con.getUser(), con.getPw());
//            return jevisDataSource.connect(con.getJevisUser(), con.getJevisPW());
            jevisDataSource = new JEVisDataSourceWS("http://" + con.getDb() + ":" + con.getPort());
            jevisDataSource.connect(con.getJevisUser(), con.getJevisPW());
            //Start ObjectCache

            jevisDataSource.getObjects();
        } catch (JEVisException ex) {
            logger.error("No Connection to database", ex);
        }
        return false;
    }

    private static List<ProcessManager> initProcessManagersFromJEVisAll() throws Exception {
        List<JEVisObject> reportObjects = getAllCleaningObjects();
        List<ProcessManager> processManagers = new ArrayList<>();
        for (JEVisObject cleanObject : reportObjects) {

            try {
                if (isEnabled(cleanObject)) {
                    logger.trace("Add Data Object to job list: [{}] {}", cleanObject.getID(), cleanObject.getName());
                    processManagers.addAll(getAllProcessSteps(cleanObject));
                }
            } catch (Exception ex) {
                throw new Exception(String.format("Failure while init jevisobject with id {}", cleanObject.getID()), ex);
            }
        }
        return processManagers;
    }

    private static List<ProcessManager> initProcessManagersFromJEVisSingle() throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        try {
            Long singleObject = CommandLineParser.getInstance().getSingleObject();
            JEVisObject processObject = jevisDataSource.getObject(singleObject);
            processManagers.addAll(getAllProcessSteps(processObject));
        } catch (JEVisException ex) {
            throw new Exception("Process classes missing", ex);
        }

        return processManagers;
    }

    private static List<ProcessManager> initProcessManagersFromOffline() {
        List<ProcessManager> processManagers = new ArrayList<>();
        CommandLineParser cmd = CommandLineParser.getInstance();
        String pathToInputFile = cmd.getInputPath();
        String pathToCleanConfigFile = cmd.getCleanConfigPath();
        String pathToOutput = cmd.getOutputPath();
        CleanDataAttribute calcAttribute = new CleanDataAttributeOffline(pathToInputFile, pathToCleanConfigFile, pathToOutput);
        ProcessManager processManager = new ProcessManager(calcAttribute);
        processManagers.add(processManager);
        return processManagers;
    }

    private static List<JEVisObject> getAllCleaningObjects() throws Exception {
        JEVisClass reportClass;
        List<JEVisObject> reportObjects = new ArrayList<>();
        try {
            reportClass = jevisDataSource.getJEVisClass(CleanDataAttributeJEVis.CLASS_NAME);
            reportObjects = jevisDataSource.getObjects(reportClass, false);
            logger.info("Total amount of Clean Data Objects: " + reportObjects.size());
        } catch (JEVisException ex) {
            throw new Exception("Process classes missing", ex);
        }
        logger.info("{} cleaning objects found", reportObjects.size());
        return reportObjects;
    }

    private static List<ProcessManager> getAllProcessSteps(JEVisObject cleanObject) throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        ProcessType processType = getProcessType(cleanObject);
        if (processType.equals(ProcessType.clean)) {
            processManagers.addAll(getProcessManagersByCleaningObject(cleanObject));
        } else if (processType.equals(ProcessType.functional)) {
            processManagers.add(getProcessManagerByFunctionalObject(cleanObject));
        }
        return processManagers;
    }

    private static boolean isEnabled(JEVisObject cleanObject) {
        ObjectHandler objectHandler = new ObjectHandler(jevisDataSource);
        CleanDataAttribute calcAttribute = new CleanDataAttributeJEVis(cleanObject, objectHandler);
        return calcAttribute.getEnabled();
    }

    private static Collection<? extends ProcessStep> getDefaultSteps() {
        Collection<ProcessStep> processSteps = new ArrayList<>();

        ProcessStep preparation = new PrepareStep();
        processSteps.add(preparation);

        ProcessStep alignmentStep = new PeriodAlignmentStep();
        processSteps.add(alignmentStep);

        ProcessStep diffStep = new DifferentialStep();
        processSteps.add(diffStep);

        ProcessStep multStep = new ScalingStep();
        processSteps.add(multStep);

        ProcessStep gapStep = new FillGapStep();
        processSteps.add(gapStep);

        ProcessStep limitsStep = new LimitsStep();
        processSteps.add(limitsStep);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
        return processSteps;
    }

    private static ProcessStep getProcessStepByFunction(JEVisObject functionalObject) throws Exception {
        String jeVisClassName = null;
        jeVisClassName = functionalObject.getJEVisClassName();

        if (jeVisClassName == null) {
            return null;
        }

        switch (jeVisClassName) {
            case "Total":
                return new SummationStep(functionalObject);
            case "Average":
                return new AverageStep(functionalObject);
            default:
                return null;
        }
    }

    private static JEVisObject getCleanDataObject(JEVisObject processObject) throws Exception {
        JEVisObject cleanObject = null;

        for (JEVisObject currentObject : processObject.getParents()) {
            if (currentObject.getJEVisClassName().equals("Clean Data")) {
                cleanObject = currentObject;
                break;
            }
        }

        return cleanObject;
    }

    public static ProcessType getProcessType(JEVisObject processObject) throws Exception {
        String jeVisClassName = null;

        jeVisClassName = processObject.getJEVisClassName();

        if (jeVisClassName == null) {
            return null;
        }

        if (jeVisClassName.equals("Clean Data")) {
            return ProcessType.clean;
        } else {
            String functionalClassName = null;
            functionalClassName = processObject.getJEVisClass().getInheritance().getName();

            if (functionalClassName != null && functionalClassName.equals("Functional Data")) {
                return ProcessType.functional;
            }
        }
        return null;
    }

    private static Collection<? extends ProcessManager> getProcessManagersByCleaningObject(JEVisObject cleanObject) throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        ObjectHandler objectHandler = new ObjectHandler(jevisDataSource);
        CleanDataAttribute calcAttribute = new CleanDataAttributeJEVis(cleanObject, objectHandler);
        ProcessManager processManager = new ProcessManager(calcAttribute);
        List<ProcessStep> processSteps = new ArrayList<>();
        processSteps.addAll(getDefaultSteps());
        processManager.setProcessSteps(processSteps);
        processManagers.add(processManager);

        for (JEVisObject child : cleanObject.getChildren()) {
            processManagers.add(getProcessManagerByFunctionalObject(child));
        }

        return processManagers;
    }

    private static ProcessManager getProcessManagerByFunctionalObject(JEVisObject functionalObject) throws Exception {
        JEVisObject cleanObject = getCleanDataObject(functionalObject);
        ObjectHandler objectHandler = new ObjectHandler(jevisDataSource);
        CleanDataAttribute calcAttribute = new CleanDataAttributeJEVis(cleanObject, objectHandler);
        ProcessManager processManager = new ProcessManager(calcAttribute);
        List<ProcessStep> processSteps = new ArrayList<>();
        ProcessStep processStepByFunction = getProcessStepByFunction(functionalObject);
        if (processStepByFunction == null) {
            logger.warn("Cant find a process task for jevis object with id {}", functionalObject.getID());
        } else {
            processSteps.add(processStepByFunction);
        }
        processManager.setProcessSteps(processSteps);
        return processManager;
    }

    public enum ProcessType {

        clean, functional
    }

    public static ForkJoinPool getForkJoinPool() {
        return forkJoinPool;
    }
}
