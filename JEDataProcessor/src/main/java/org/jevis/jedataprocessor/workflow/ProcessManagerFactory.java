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
import org.jevis.commons.database.ObjectHandler;
import org.jevis.jedataprocessor.alignment.PeriodAlignmentStep;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.CleanDataAttributeJEVis;
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

/**
 * @author broder
 */
public class ProcessManagerFactory {

    private static final Logger logger = LogManager.getLogger(ProcessManagerFactory.class);
    private JEVisDataSource ds;

    public ProcessManagerFactory(JEVisDataSource ds) {
        this.ds = ds;
    }

    public List<ProcessManager> initProcessManagersFromJEVisAll() throws Exception {
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

    public List<ProcessManager> initProcessManagersFromJEVisSingle(Long objectID) throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        try {

            JEVisObject processObject = ds.getObject(objectID);
            processManagers.addAll(getAllProcessSteps(processObject));
        } catch (Exception ex) {
            logger.error("Process classes missing", ex);
        }

        return processManagers;
    }

    private List<JEVisObject> getAllCleaningObjects() throws Exception {
        JEVisClass reportClass;
        List<JEVisObject> reportObjects = new ArrayList<>();
        try {
            reportClass = ds.getJEVisClass(CleanDataAttributeJEVis.CLASS_NAME);
            reportObjects = ds.getObjects(reportClass, false);
            logger.info("Total amount of Clean Data Objects: " + reportObjects.size());
        } catch (JEVisException ex) {
            throw new Exception("Process classes missing", ex);
        }
        logger.info("{} cleaning objects found", reportObjects.size());
        return reportObjects;
    }

    private boolean isEnabled(JEVisObject cleanObject) {
        ObjectHandler objectHandler = new ObjectHandler(ds);
        CleanDataAttribute calcAttribute = new CleanDataAttributeJEVis(cleanObject, objectHandler);
        return calcAttribute.getEnabled();
    }

    private List<ProcessManager> getAllProcessSteps(JEVisObject cleanObject) throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        ProcessManagerFactory.ProcessType processType = getProcessType(cleanObject);
        if (processType.equals(ProcessManagerFactory.ProcessType.clean)) {
            processManagers.addAll(getProcessManagersByCleaningObject(cleanObject));
        } else if (processType.equals(ProcessManagerFactory.ProcessType.functional)) {
            processManagers.add(getProcessManagerByFunctionalObject(cleanObject));
        }
        return processManagers;
    }

    public ProcessManagerFactory.ProcessType getProcessType(JEVisObject processObject) throws Exception {
        String jeVisClassName = null;

        jeVisClassName = processObject.getJEVisClassName();

        if (jeVisClassName == null) {
            return null;
        }

        if (jeVisClassName.equals("Clean Data")) {
            return ProcessManagerFactory.ProcessType.clean;
        } else {
            String functionalClassName = null;
            functionalClassName = processObject.getJEVisClass().getInheritance().getName();

            if (functionalClassName != null && functionalClassName.equals("Functional Data")) {
                return ProcessManagerFactory.ProcessType.functional;
            }
        }
        return null;
    }

    private Collection<? extends ProcessManager> getProcessManagersByCleaningObject(JEVisObject cleanObject) throws Exception {
        List<ProcessManager> processManagers = new ArrayList<>();
        ObjectHandler objectHandler = new ObjectHandler(ds);
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

    private ProcessManager getProcessManagerByFunctionalObject(JEVisObject functionalObject) throws Exception {
        JEVisObject cleanObject = getCleanDataObject(functionalObject);
        ObjectHandler objectHandler = new ObjectHandler(ds);
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

    private JEVisObject getCleanDataObject(JEVisObject processObject) throws Exception {
        JEVisObject cleanObject = null;

        for (JEVisObject currentObject : processObject.getParents()) {
            if (currentObject.getJEVisClassName().equals("Clean Data")) {
                cleanObject = currentObject;
                break;
            }
        }

        return cleanObject;
    }

    private Collection<? extends ProcessStep> getDefaultSteps() {
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

    private ProcessStep getProcessStepByFunction(JEVisObject functionalObject) throws Exception {
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


    public enum ProcessType {

        clean, functional
    }

}
