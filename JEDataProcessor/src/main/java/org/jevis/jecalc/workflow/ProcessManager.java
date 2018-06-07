/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.workflow;

import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.scaling.ScalingStep;
import org.jevis.jecalc.differential.DifferentialStep;
import org.jevis.jecalc.alignment.PeriodAlignmentStep;
import org.jevis.jecalc.gap.FillGapStep;
import org.jevis.jecalc.save.ImportStep;
import org.jevis.jecalc.data.CleanDataAttribute;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author broder
 */
public class ProcessManager {

    private static final Logger logger = LoggerFactory.getLogger(ProcessManager.class);
    private List<ProcessStep> processSteps = new ArrayList<>();
    private final ResourceManager resourceManager;

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
            currentStep.run(resourceManager);
        });
    }

    private void addFunctionalSteps(List<ProcessStep> processSteps, JEVisObject cleanObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
