/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.jedataprocessor.aggregation.AggregationAlignmentStep;
import org.jevis.jedataprocessor.alignment.PeriodAlignmentStep;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.differential.DifferentialStep;
import org.jevis.jedataprocessor.forecast.ForecastStep;
import org.jevis.jedataprocessor.forecast.PrepareForecast;
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
    private boolean isClean = true;
    private boolean missingSamples = true;
    private boolean rerun = false;
    private DateTime lastFirstDate;
    private boolean isWorking = true;

    public ProcessManager(JEVisObject cleanObject, ObjectHandler objectHandler, int processingSize) {
        this.resourceManager = new ResourceManager();

        this.name = cleanObject.getName();
        this.id = cleanObject.getID();

        JEVisClass cleanDataClass;
        JEVisClass forecastDataClass;
        try {
            cleanDataClass = cleanObject.getDataSource().getJEVisClass(CleanDataObject.CLASS_NAME);
            forecastDataClass = cleanObject.getDataSource().getJEVisClass(ForecastDataObject.CLASS_NAME);

            if (cleanObject.getJEVisClass().equals(cleanDataClass)) {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                addDefaultSteps();
            } else if (cleanObject.getJEVisClass().equals(forecastDataClass)) {
                this.resourceManager.setForecastDataObject(new ForecastDataObject(cleanObject, objectHandler));
                this.resourceManager.getForecastDataObject().setProcessingSize(processingSize);
                addForecastSteps();
                isClean = false;
            } else {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                addDefaultSteps();
            }
        } catch (JEVisException e) {
            e.printStackTrace();
            this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
            this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
            addDefaultSteps();
        }
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

        ProcessStep aggregationAlignmentStep = new AggregationAlignmentStep();
        processSteps.add(aggregationAlignmentStep);

        ProcessStep gapStep = new FillGapStep();
        processSteps.add(gapStep);

        ProcessStep limitsStep = new LimitsStep();
        processSteps.add(limitsStep);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    private void addForecastSteps() {

        ProcessStep preparation = new PrepareForecast();
        processSteps.add(preparation);

        ProcessStep forecast = new ForecastStep();
        processSteps.add(forecast);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

    public void start() throws Exception {
        if (isClean) {

            logger.info("[{}:{}] Starting Process", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

            if (resourceManager.getCleanDataObject().checkConfig()) {

//        while (missingSamples) {
                reRun();
//        }
            }

            logger.info("[{}:{}] Finished", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

            resourceManager.setIntervals(null);
            resourceManager.setNotesMap(null);
            resourceManager.setRawSamplesDown(null);
            resourceManager.setSampleCache(null);
            resourceManager.setRawIntervals(null);
            resourceManager.getCleanDataObject().clearLists();
        } else {
            if (resourceManager.getForecastDataObject().isReady(resourceManager.getForecastDataObject().getForecastDataObject())) {
                reRun();
                resourceManager.getForecastDataObject().finishCurrentRun(resourceManager.getForecastDataObject().getForecastDataObject());
            }
        }
    }

    private void reRun() throws Exception {

        for (ProcessStep ps : processSteps) {
//            if (rerun && ps.getClass().equals(PrepareStep.class)) {
//                JEVisDataSource ds = resourceManager.getCleanDataObject().getCleanObject().getDataSource();
//                ds.clearCache();
//                ds.preload();
////                resourceManager.setCleanDataObject(new CleanDataObject(ds.getCleanObject(cleanObjectId), new ObjectHandler(ds)));
//                CleanDataObject cdo = resourceManager.getCleanDataObject();
//                cdo.setFirstDate(null);
//            }

//            Benchmark benchmark = new Benchmark();
            ps.run(resourceManager);
//            benchmark.printBenchmarkDetail("Finished step " + ps.getClass().getSimpleName() + " of object " + getName() + ":" + getId());

//            if (ps.getClass().equals(PrepareStep.class)) {
//                DateTime currentFirstDate = resourceManager.getCleanDataObject().getFirstDate();
//                if (!currentFirstDate.equals(lastFirstDate)) {
//                    lastFirstDate = resourceManager.getCleanDataObject().getFirstDate();
//
//                    if (resourceManager.getIntervals().size() > 10000) {
//                        resourceManager.setIntervals(resourceManager.getIntervals().subList(0, 10000));
//                        missingSamples = true;
//                        rerun = true;
//                    } else {
//                        missingSamples = false;
//                    }
//                } else {
//                    rerun = false;
//                    missingSamples = false;
//                }
//            }
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
