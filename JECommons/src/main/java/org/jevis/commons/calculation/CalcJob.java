/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class CalcJob {

    private static final Logger logger = LogManager.getLogger(CalcJob.class);
    private List<CalcInputObject> calcObjects;
    private String expression;
    private List<JEVisAttribute> outputs;
    private long calcObjID;
    private boolean processedAllInputSamples = false;
    private Double staticValue = 0.0;
    private Double allZeroValue;
    private String DIV0Handling = "";

    public CalcJob() {
    }

    CalcJob(List<CalcInputObject> calcObjects, String expression, List<JEVisAttribute> outputObjects, long calcObjID) {
        this.calcObjects = calcObjects;
        this.expression = expression;
        this.outputs = outputObjects;
        this.calcObjID = calcObjID;
    }

    public void execute() {
        SampleMerger sampleMerger = new SampleMerger();
        for (CalcInputObject calcObject : calcObjects) {
            sampleMerger.addSamples(calcObject.getSamples(), calcObject.getIdentifier(), calcObject.getInputType());
            logger.debug("added {} samples with identifier {} to merger", calcObject.getSamples().size(), calcObject.getIdentifier());
        }
        Map<DateTime, List<Sample>> mergedSamples = sampleMerger.merge();
        logger.debug("{} mergable calculations found", mergedSamples.size());
        ResultCalculator resultCalc = new ResultCalculator(mergedSamples, new CalcTemplate(expression));
        List<JEVisSample> calculateResult = resultCalc.calculateResult(DIV0Handling, staticValue, allZeroValue);
        logger.info("{} results calculated", calculateResult.size());
        saveToOutput(calculateResult);

    }

    public List<JEVisSample> getResults() {
        SampleMerger sampleMerger = new SampleMerger();
        for (CalcInputObject calcObject : calcObjects) {
            sampleMerger.addSamples(calcObject.getSamples(), calcObject.getIdentifier(), calcObject.getInputType());
            logger.debug("added {} samples with identifier {} to merger", calcObject.getSamples().size(), calcObject.getIdentifier());
        }
        Map<DateTime, List<Sample>> mergedSamples = sampleMerger.merge();
        logger.debug("{} mergable calculations found", mergedSamples.size());
        ResultCalculator resultCalc = new ResultCalculator(mergedSamples, new CalcTemplate(expression));
        List<JEVisSample> calculateResult = resultCalc.calculateResult(DIV0Handling, staticValue, allZeroValue);
        logger.info("{} results calculated", calculateResult.size());
        return calculateResult;
    }

    private void saveToOutput(List<JEVisSample> calculateResult) {
        try {
            //generate output
            for (JEVisAttribute output : outputs) {
                boolean hasSamples = output.hasSample();
                Map<DateTime, JEVisSample> listOldSamples = new HashMap<>();
                for (JEVisSample jeVisSample : output.getAllSamples()) {
                    listOldSamples.put(jeVisSample.getTimestamp(), jeVisSample);
                }

                for (JEVisSample sample : calculateResult) {
                    DateTime date = sample.getTimestamp();
                    if (date != null) {

                        if (hasSamples) {
                            JEVisSample smp = listOldSamples.get(date);
                            if (smp != null) {
                                output.deleteSamplesBetween(date.minusMillis(1), date.plusMillis(1));
                            }
                        }
                    }
                }

                output.addSamples(calculateResult);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    long getCalcObjectID() {
        return calcObjID;
    }

    public void setHasProcessedAllInputSamples(boolean b) {
        processedAllInputSamples = b;
    }

    public boolean hasProcessedAllInputSamples() {
        return processedAllInputSamples;
    }

    public void setCalcInputObjects(List<CalcInputObject> calcInputObjects) {
        this.calcObjects = calcInputObjects;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setOutputAttributes(List<JEVisAttribute> outputAttributes) {
        this.outputs = outputAttributes;
    }

    public void setCalcObjID(long calcObjID) {
        this.calcObjID = calcObjID;
    }

    public Double getStaticValue() {
        return staticValue;
    }

    public Double getAllZeroValue() {
        return allZeroValue;
    }

    public void setAllZeroValue(Double allZeroValue) {
        this.allZeroValue = allZeroValue;
    }

    public void setStaticValue(Double staticValue) {
        this.staticValue = staticValue;
    }

    public String getDIV0Handling() {
        return DIV0Handling;
    }

    public void setDIV0Handling(String div0Handling) {
        this.DIV0Handling = div0Handling;
    }
}
