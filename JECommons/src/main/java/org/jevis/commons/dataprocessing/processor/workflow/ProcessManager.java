/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.delta.DeltaStep;
import org.jevis.commons.dataprocessing.processor.limits.LimitsStep;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareForecast;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareMath;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareStep;
import org.jevis.commons.dataprocessing.processor.steps.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gschutz
 */
public class ProcessManager {

    private static final Logger logger = LogManager.getLogger(ProcessManager.class);
    private final ResourceManager resourceManager;
    private final ObjectHandler objectHandler;
    private final int processingSize;
    private String name;
    private Long id;
    private List<ProcessStep> processSteps = new ArrayList<>();
    private boolean isClean = true;
    private boolean isForecast = false;
    private boolean isMathData = false;
    private boolean isFinished = false;

    public ProcessManager(JEVisObject cleanObject, ObjectHandler objectHandler, int processingSize) {
        this.resourceManager = new ResourceManager();

        this.name = cleanObject.getName();
        this.id = cleanObject.getID();
        this.objectHandler = objectHandler;
        this.processingSize = processingSize;

        JEVisClass cleanDataClass;
        JEVisClass forecastDataClass;
        JEVisClass mathDataClass;
        try {
            cleanDataClass = cleanObject.getDataSource().getJEVisClass(CleanDataObject.CLASS_NAME);
            forecastDataClass = cleanObject.getDataSource().getJEVisClass(ForecastDataObject.CLASS_NAME);
            mathDataClass = cleanObject.getDataSource().getJEVisClass(MathDataObject.CLASS_NAME);

            if (cleanObject.getJEVisClass().equals(cleanDataClass)) {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                processSteps.clear();
                addDefaultSteps();
            } else if (cleanObject.getJEVisClass().equals(forecastDataClass)) {
                this.resourceManager.setForecastDataObject(new ForecastDataObject(cleanObject, objectHandler));
                this.resourceManager.getForecastDataObject().setProcessingSize(processingSize);
                this.name = resourceManager.getForecastDataObject().getForecastDataObject().getName();
                this.id = resourceManager.getForecastDataObject().getForecastDataObject().getID();
                processSteps.clear();
                addForecastSteps();
                isClean = false;
                isMathData = false;
                isForecast = true;
                resourceManager.setClean(false);
                resourceManager.setForecast(true);
            } else if (cleanObject.getJEVisClass().equals(mathDataClass)) {
                this.resourceManager.setMathDataObject(new MathDataObject(cleanObject, objectHandler));
                this.resourceManager.getMathDataObject().setProcessingSize(processingSize);
                this.name = this.resourceManager.getMathDataObject().getMathDataObject().getName();
                this.id = this.resourceManager.getMathDataObject().getMathDataObject().getID();
                processSteps.clear();
                addMathSteps();
                isClean = false;
                isMathData = true;
                isForecast = false;
                resourceManager.setClean(false);
                resourceManager.setForecast(false);
            } else {
                this.resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
                this.resourceManager.getCleanDataObject().setProcessingSize(processingSize);
                processSteps.clear();
                addDefaultSteps();
            }
        } catch (Exception e) {
            logger.error("Could not determine object", e);
        }
    }

    private void addDefaultSteps() {

        ProcessStep preparation = new PrepareStep(this);
        processSteps.add(preparation);

        ProcessStep alignmentStep = new PeriodAlignmentStep();
        processSteps.add(alignmentStep);

        ProcessStep gapStep = new FillGapStep();
        processSteps.add(gapStep);

        ProcessStep diffStep = new DifferentialStep();
        processSteps.add(diffStep);

        ProcessStep multiStep = new ScalingStep();
        processSteps.add(multiStep);

        ProcessStep aggregationAlignmentStep = new AggregationAlignmentStep();
        processSteps.add(aggregationAlignmentStep);

        ProcessStep limitsStep = new LimitsStep();
        processSteps.add(limitsStep);

        ProcessStep deltaStep = new DeltaStep();
        processSteps.add(deltaStep);

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

    private void addMathSteps() {

        ProcessStep preparation = new PrepareMath();
        processSteps.add(preparation);

        ProcessStep math = new MathStep();
        processSteps.add(math);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }

    public void setProcessSteps(List<ProcessStep> processSteps) {
        this.processSteps = processSteps;
    }

    public void start() throws Exception {
        if (isClean) {
            do {
                logger.info("[{}:{}] Starting Process", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

                if (resourceManager.getCleanDataObject().checkConfig()) {
                    reRun();
                } else setFinished(true);

                logger.info("[{}:{}] Finished", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

                reinitializeCleanData();
            } while (!isFinished);
        } else if (isForecast) {
            logger.info("[{}:{}] Starting Process", resourceManager.getForecastDataObject().getForecastDataObject().getName(), resourceManager.getID());

            while (resourceManager.getForecastDataObject().isReady(resourceManager.getForecastDataObject().getForecastDataObject())) {
                reRun();
                resourceManager.getForecastDataObject().finishCurrentRun(resourceManager.getForecastDataObject().getForecastDataObject());

                reinitializeForecastData();
            }

            logger.info("[{}:{}] Finished", resourceManager.getForecastDataObject().getForecastDataObject().getName(), resourceManager.getID());
        } else if (isMathData) {
            logger.info("[{}:{}] Starting Process", resourceManager.getMathDataObject().getMathDataObject().getName(), resourceManager.getID());

            while (resourceManager.getMathDataObject().isReady()) {
                reRun();
                resourceManager.getMathDataObject().finishCurrentRun(resourceManager.getMathDataObject().getMathDataObject());

                reinitializeMathData();
            }

            logger.info("[{}:{}] Finished", resourceManager.getMathDataObject().getMathDataObject().getName(), resourceManager.getID());
        }
    }

    private void reinitializeCleanData() {
        processSteps.clear();

        JEVisObject cleanObject = resourceManager.getCleanDataObject().getCleanObject();

        resourceManager.setIntervals(null);
        resourceManager.setNotesMap(null);
        resourceManager.setUserDataMap(null);
        resourceManager.setRawSamplesDown(null);
        resourceManager.setSampleCache(null);
        resourceManager.setRawIntervals(null);
        resourceManager.setCleanDataObject(new CleanDataObject(cleanObject, objectHandler));
        resourceManager.getCleanDataObject().setProcessingSize(processingSize);

        addDefaultSteps();
    }

    private void reinitializeForecastData() {
        processSteps.clear();

        JEVisObject forecastObject = resourceManager.getForecastDataObject().getForecastDataObject();

        resourceManager.setIntervals(null);
        resourceManager.setNotesMap(null);
        resourceManager.setUserDataMap(null);
        resourceManager.setRawSamplesDown(null);
        resourceManager.setSampleCache(null);
        resourceManager.setRawIntervals(null);
        resourceManager.setForecastDataObject(new ForecastDataObject(forecastObject, objectHandler));
        resourceManager.getForecastDataObject().setProcessingSize(processingSize);

        addForecastSteps();
    }

    private void reinitializeMathData() {
        processSteps.clear();

        JEVisObject mathObject = resourceManager.getMathDataObject().getMathDataObject();

        resourceManager.setIntervals(null);
        resourceManager.setNotesMap(null);
        resourceManager.setUserDataMap(null);
        resourceManager.setRawSamplesDown(null);
        resourceManager.setSampleCache(null);
        resourceManager.setRawIntervals(null);
        resourceManager.setMathDataObject(new MathDataObject(mathObject, objectHandler));
        resourceManager.getMathDataObject().setProcessingSize(processingSize);

        addMathSteps();
    }

    private void reRun() throws Exception {


        if (isClean) {
            resourceManager.getCleanDataObject().reloadAttributes();
        } else if (isForecast) {
            resourceManager.getForecastDataObject().reloadAttributes();
        } else if (isMathData) {
            resourceManager.getMathDataObject().reloadAttributes();
        }

        for (ProcessStep ps : processSteps) {
            try {
                ps.run(resourceManager);
            } catch (Exception e) {
                setFinished(true);
                if (ps instanceof PrepareStep || ps instanceof PrepareForecast || ps instanceof PrepareMath) {
                    logger.info("Error in step {} of object {}:{}", ps, this.getName(), this.getId(), e);
                } else {
                    logger.error("Error in step {} of object {}:{}", ps, this.getName(), this.getId(), e);
                }
                throw e;
            }
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
