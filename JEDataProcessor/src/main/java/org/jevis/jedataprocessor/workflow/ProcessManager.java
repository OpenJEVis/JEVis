/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.task.LogTaskManager;
import org.jevis.commons.task.Task;
import org.jevis.jedataprocessor.data.CleanDataAttribute;
import org.jevis.jedataprocessor.data.ResourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class ProcessManager {

    private static final Logger logger = LogManager.getLogger(ProcessManager.class);
    private final ResourceManager resourceManager;
    private List<ProcessStep> processSteps = new ArrayList<>();

    public ProcessManager(CleanDataAttribute calcAttribute) {
        resourceManager = new ResourceManager();
        resourceManager.setCalcAttribute(calcAttribute);
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

    public void start() {
        logger.info("[{}] Starting Process", resourceManager.getID());
        Long id = -1l;
        try {
            id = resourceManager.getID();
            LogTaskManager.getInstance().buildNewTask(resourceManager.getID(), LogTaskManager.parentName(resourceManager.getCalcAttribute().getObject()));
            LogTaskManager.getInstance().getTask(resourceManager.getID()).setStatus(Task.Status.STARTED);

            resourceManager.getCalcAttribute().checkConfig();


            for (ProcessStep ps : processSteps) {
                ps.run(resourceManager);
            }

            logger.info("[{}] Finished", resourceManager.getID(), resourceManager.getCalcAttribute().getObject().getName());
            LogTaskManager.getInstance().getTask(resourceManager.getID()).setStatus(Task.Status.FINISHED);
        } catch (Exception e) {
            if (logger.isDebugEnabled() || logger.isTraceEnabled()) {
                logger.error("[{}] Error in process: \n {} \n ", id, org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
            } else {
                logger.error("[{}] Error in process: \n {} message: {}", id, getShortErrorMessage(e), e.getMessage());
            }
            LogTaskManager.getInstance().getTask(id).setExeption(e);
            LogTaskManager.getInstance().getTask(id).setStatus(Task.Status.FAILED);
        }

    }

    private String getShortErrorMessage(Exception ex) {
//        System.out.println("getShortErrorMessage: " + ex);
        try {
            for (StackTraceElement te : ex.getStackTrace()) {
                if (te.getClassName().startsWith("org.jevis")) {
                    String shortClassName = "";
                    if (te.getClassName().lastIndexOf(".") != -1) {
                        shortClassName = te.getClassName().substring(te.getClassName().lastIndexOf(".") + 1);
                    } else {
                        shortClassName = te.getClassName();
                    }
                    return shortClassName + ":" + te.getLineNumber() + ":" + te.getMethodName();
                }
            }
            return ex.toString();
        } catch (Exception exp2) {
            return ex.toString();
        }

    }

    private void addFunctionalSteps(List<ProcessStep> processSteps, JEVisObject cleanObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
