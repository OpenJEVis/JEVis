/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
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
import org.joda.time.DateTime;

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

    private boolean missingSamples = true;
    private boolean rerun = false;
    private DateTime lastFirstDate;
    private boolean isWorking = true;

    public void start() throws Exception {
        logger.info("[{}] Starting Process", resourceManager.getID());

        resourceManager.getCleanDataObject().checkConfig();

        while (missingSamples) {
            reRun();
        }

        logger.info("[{}] Finished", resourceManager.getID(), resourceManager.getCleanDataObject().getObject().getName());
    }

    private void reRun() throws Exception {

        for (ProcessStep ps : processSteps) {
            if (rerun && ps.getClass().equals(PrepareStep.class)) {
                JEVisDataSource ds = resourceManager.getCleanDataObject().getObject().getDataSource();
                ds.clearCache();
                ds.preload();
//                resourceManager.setCleanDataObject(new CleanDataObject(ds.getObject(cleanObjectId), new ObjectHandler(ds)));
                CleanDataObject cdo = resourceManager.getCleanDataObject();
                cdo.setFirstDate(null);
            }

            ps.run(resourceManager);

            if (ps.getClass().equals(PrepareStep.class)) {
                DateTime currentFirstDate = resourceManager.getCleanDataObject().getFirstDate();
                if (!currentFirstDate.equals(lastFirstDate)) {
                    lastFirstDate = resourceManager.getCleanDataObject().getFirstDate();

                    if (resourceManager.getIntervals().size() > 10000) {
                        resourceManager.setIntervals(resourceManager.getIntervals().subList(0, 10000));
                        missingSamples = true;
                        rerun = true;
                    } else {
                        missingSamples = false;
                    }
                } else {
                    rerun = false;
                    missingSamples = false;
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
