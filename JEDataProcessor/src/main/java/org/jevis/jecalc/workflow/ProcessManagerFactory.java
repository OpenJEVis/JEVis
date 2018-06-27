/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.cli.JEVisServerConnectionCLI;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.jecalc.CommandLineParser;
import org.jevis.jecalc.alignment.PeriodAlignmentStep;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.CleanDataAttributeOffline;
import org.jevis.jecalc.differential.DifferentialStep;
import org.jevis.jecalc.functional.avg.AverageStep;
import org.jevis.jecalc.functional.sum.SummationStep;
import org.jevis.jecalc.gap.FillGapStep;
import org.jevis.jecalc.limits.LimitsStep;
import org.jevis.jecalc.save.ImportStep;
import org.jevis.jecalc.scaling.ScalingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * @author broder
 */
public class ProcessManagerFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManagerFactory.class);
    public static JEVisDataSource jevisDataSource;

    public static List<ProcessManager> getProcessManagerList() {
        CommandLineParser cmd = CommandLineParser.getInstance();

//        establishConnection();
//        try {
//            jevisDataSource.getObject(7731l).getAttribute("Value").deleteAllSample();
//            jevisDataSource.getObject(7730l).getAttribute("Value").deleteAllSample();
//        } catch (JEVisException ex) {
//            java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
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

        return processManagers;
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
        } catch (JEVisException ex) {
            logger.error("No Connection to database", ex);
        }
        return false;
    }

    private static List<ProcessManager> initProcessManagersFromJEVisAll() {
        List<JEVisObject> reportObjects = getAllCleaningObjects();
        List<ProcessManager> processManagers = new ArrayList<>();
        for (JEVisObject cleanObject : reportObjects) {
            try {
                if (isEnabled(cleanObject)) {
                    processManagers.addAll(getAllProcessSteps(cleanObject));
                }
            } catch (Exception ex) {
                logger.error("Failure while init jevisobject with id {}", cleanObject.getID(), ex);
            }
        }
        return processManagers;
    }

    private static List<ProcessManager> initProcessManagersFromJEVisSingle() {
        List<ProcessManager> processManagers = new ArrayList<>();
        try {
            Long singleObject = CommandLineParser.getInstance().getSingleObject();
            JEVisObject processObject = jevisDataSource.getObject(singleObject);
            processManagers.addAll(getAllProcessSteps(processObject));
        } catch (JEVisException ex) {
            logger.error("Process classes missing", ex);
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

    private static List<JEVisObject> getAllCleaningObjects() {
        JEVisClass reportClass;
        List<JEVisObject> reportObjects = new ArrayList<>();
        try {
            reportClass = jevisDataSource.getJEVisClass(CleanDataAttributeJEVis.CLASS_NAME);
            reportObjects = jevisDataSource.getObjects(reportClass, false);
        } catch (JEVisException ex) {
            logger.error("Process classes missing", ex);
        }
        logger.info("{} cleaning objects found", reportObjects.size());
        return reportObjects;
    }

    private static List<ProcessManager> getAllProcessSteps(JEVisObject cleanObject) {
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

    private static ProcessStep getProcessStepByFunction(JEVisObject functionalObject) {
        String jeVisClassName = null;
        try {
            jeVisClassName = functionalObject.getJEVisClassName();
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private static JEVisObject getCleanDataObject(JEVisObject processObject) {
        JEVisObject cleanObject = null;
        try {
            for (JEVisObject currentObject : processObject.getParents()) {
                if (currentObject.getJEVisClassName().equals("Clean Data")) {
                    cleanObject = currentObject;
                    break;
                }
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cleanObject;
    }

    public static ProcessType getProcessType(JEVisObject processObject) {
        String jeVisClassName = null;
        try {
            jeVisClassName = processObject.getJEVisClassName();
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (jeVisClassName == null) {
            return null;
        }

        if (jeVisClassName.equals("Clean Data")) {
            return ProcessType.clean;
        } else {
            String functionalClassName = null;
            try {
                functionalClassName = processObject.getJEVisClass().getInheritance().getName();
            } catch (JEVisException ex) {
                java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (functionalClassName != null && functionalClassName.equals("Functional Data")) {
                return ProcessType.functional;
            }
        }
        return null;
    }

    private static Collection<? extends ProcessManager> getProcessManagersByCleaningObject(JEVisObject cleanObject) {
        List<ProcessManager> processManagers = new ArrayList<>();
        ObjectHandler objectHandler = new ObjectHandler(jevisDataSource);
        CleanDataAttribute calcAttribute = new CleanDataAttributeJEVis(cleanObject, objectHandler);
        ProcessManager processManager = new ProcessManager(calcAttribute);
        List<ProcessStep> processSteps = new ArrayList<>();
        processSteps.addAll(getDefaultSteps());
        processManager.setProcessSteps(processSteps);
        processManagers.add(processManager);

        try {
            for (JEVisObject child : cleanObject.getChildren()) {
                processManagers.add(getProcessManagerByFunctionalObject(child));
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ProcessManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        return processManagers;
    }

    private static ProcessManager getProcessManagerByFunctionalObject(JEVisObject functionalObject) {
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
}
