/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jecalc.data.CleanDataAttribute;
import org.jevis.jecalc.data.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class ProcessManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
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
        logger.info("---------------------------------------------");
        logger.info("Current Clean Data Object: {}", resourceManager.getCalcAttribute().getName());
        processSteps.stream().forEach((currentStep) -> {
            try {
                currentStep.run(resourceManager);
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        });
    }

    private void addFunctionalSteps(List<ProcessStep> processSteps, JEVisObject cleanObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
