/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.processor.limits.LimitsStepN;
import org.jevis.commons.dataprocessing.processor.preparation.PrepareStepN;
import org.jevis.commons.dataprocessing.processor.steps.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gschutz
 */
public class ProcessManagerN {

    private static final Logger logger = LogManager.getLogger(ProcessManagerN.class);
    private final ResourceManagerN resourceManager;
    private final String name;
    private final Long id;
    private List<ProcessStepN> processSteps = new ArrayList<>();
    private boolean isClean = true;

    public ProcessManagerN(JEVisObject cleanObject, ObjectHandler objectHandler, int processingSize) {
        this.resourceManager = new ResourceManagerN();

        this.name = cleanObject.getName();
        this.id = cleanObject.getID();

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
                addDefaultSteps();
            } else if (cleanObject.getJEVisClass().equals(forecastDataClass)) {
                this.resourceManager.setForecastDataObject(new ForecastDataObject(cleanObject, objectHandler));
                this.resourceManager.getForecastDataObject().setProcessingSize(processingSize);
                addForecastSteps();
                isClean = false;
                resourceManager.setClean(false);
            } else if (cleanObject.getJEVisClass().equals(mathDataClass)) {
                this.resourceManager.setMathDataObject(new MathDataObject(cleanObject, objectHandler));
                this.resourceManager.getMathDataObject().setProcessingSize(processingSize);
                addMathSteps();
                isClean = false;
                resourceManager.setClean(false);
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

        ProcessStepN preparation = new PrepareStepN();
        processSteps.add(preparation);

        ProcessStepN alignmentStep = new PeriodAlignmentStepN();
        processSteps.add(alignmentStep);

        ProcessStepN gapStep = new FillGapStepN();
        processSteps.add(gapStep);

        ProcessStepN diffStep = new DifferentialStepN();
        processSteps.add(diffStep);

        ProcessStepN multiStep = new ScalingStepN();
        processSteps.add(multiStep);

        ProcessStepN aggregationAlignmentStep = new AggregationAlignmentStepN();
        processSteps.add(aggregationAlignmentStep);


        ProcessStepN limitsStep = new LimitsStepN();
        processSteps.add(limitsStep);

        ProcessStepN importStep = new ImportStepN();
        processSteps.add(importStep);
    }

    private void addForecastSteps() {

//        ProcessStepN preparation = new PrepareForecastN();
//        processSteps.add(preparation);
//
//        ProcessStepN forecast = new ForecastStepN();
//        processSteps.add(forecast);
//
//        ProcessStepN importStep = new ImportStepN();
//        processSteps.add(importStep);
    }

    private void addMathSteps() {

//        ProcessStepN preparation = new PrepareMathN();
//        processSteps.add(preparation);
//
//        ProcessStepN math = new MathStepN();
//        processSteps.add(math);
//
//        ProcessStepN importStep = new ImportStepN();
//        processSteps.add(importStep);
    }

    public void setProcessSteps(List<ProcessStepN> processSteps) {
        this.processSteps = processSteps;
    }

    public void start() throws Exception {
        if (isClean) {

            logger.info("[{}:{}] Starting Process", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

            if (resourceManager.getCleanDataObject().checkConfig()) {
                reRun();
            }

            logger.info("[{}:{}] Finished", resourceManager.getCleanDataObject().getCleanObject().getName(), resourceManager.getID());

            resourceManager.setIntervals(null);
            resourceManager.setNotesMap(null);
            resourceManager.setRawSamplesDown(null);
            resourceManager.setSampleCache(null);
            resourceManager.setRawIntervals(null);
            resourceManager.getCleanDataObject().clearLists();
        } else if (resourceManager.getForecastDataObject() != null) {
            if (resourceManager.getForecastDataObject().isReady(resourceManager.getForecastDataObject().getForecastDataObject())) {
                reRun();
                resourceManager.getForecastDataObject().finishCurrentRun(resourceManager.getForecastDataObject().getForecastDataObject());
            }
        } else if (resourceManager.getMathDataObject() != null) {
            if (resourceManager.getMathDataObject().isReady()) {
                reRun();
                resourceManager.getMathDataObject().finishCurrentRun(resourceManager.getMathDataObject().getMathDataObject());
            }
        }
    }

    private void reRun() throws Exception {

        if (isClean) {
            resourceManager.getCleanDataObject().reloadAttributes();
        } else if (resourceManager.getForecastDataObject() != null) {
            resourceManager.getForecastDataObject().reloadAttributes();
        } else if (resourceManager.getMathDataObject() != null) {
            resourceManager.getMathDataObject().reloadAttributes();
        }
        for (ProcessStepN ps : processSteps) {
            ps.run(resourceManager);
        }
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
