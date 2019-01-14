/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.jedataprocessor.alignment.PeriodAlignmentStep;
import org.jevis.jedataprocessor.data.CleanDataObject;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.differential.DifferentialStep;
import org.jevis.jedataprocessor.gap.FillGapStep;
import org.jevis.jedataprocessor.limits.LimitsStep;
import org.jevis.jedataprocessor.save.ImportStep;
import org.jevis.jedataprocessor.scaling.ScalingStep;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
public class ProcessManager {

    private static final Logger logger = LogManager.getLogger(ProcessManager.class);
    private final ResourceManager resourceManager;
    private List<ProcessStep> processSteps = new ArrayList<>();
    private String name;
    private Long id;

    public ProcessManager(JEVisObject cleanObject, ObjectHandler objectHandler) {
        resourceManager = new ResourceManager();
        resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
        name = cleanObject.getName();
        id = cleanObject.getID();

        addDefaultSteps();
    }

    private void addDefaultSteps() {

        ProcessStep preparation = new PrepareStep();
        processSteps.add(preparation);

        ProcessStep alignmentStep = new PeriodAlignmentStep();
        processSteps.add(alignmentStep);

        ProcessStep diffStep = new DifferentialStep();
        processSteps.add(diffStep);

        ProcessStep multiStep = new ScalingStep();
        processSteps.add(multiStep);

        ProcessStep gapStep = new FillGapStep();
        processSteps.add(gapStep);

        ProcessStep limitsStep = new LimitsStep();
        processSteps.add(limitsStep);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

    public void start() throws Exception {
        logger.info("[{}] Starting Process", resourceManager.getID());

        resourceManager.getCleanDataObject().checkConfig();


        for (ProcessStep ps : processSteps) {
            ps.run(resourceManager);
        }

        logger.info("[{}] Finished", resourceManager.getID(), resourceManager.getCleanDataObject().getObject().getName());
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
