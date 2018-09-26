/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.jecalc.calculation.CalcTemplate;
import org.jevis.jecalc.calculation.ResultCalculator;
import org.jevis.jecalc.calculation.Sample;
import org.jevis.jecalc.calculation.SampleMerger;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
class CalcJob {

    private static final Logger logger = LogManager.getLogger(CalcJob.class);
    private final List<CalcInputObject> calcObjects;
    private final String expression;
    private final List<JEVisAttribute> outputs;
    private final long calcObjID;

    CalcJob(List<CalcInputObject> calcObjects, String expression, List<JEVisAttribute> outputObjects, long calcObjID) {
        this.calcObjects = calcObjects;
        this.expression = expression;
        this.outputs = outputObjects;
        this.calcObjID = calcObjID;
    }

    void execute() {
        SampleMerger sampleMerger = new SampleMerger();
        for (CalcInputObject calcObject : calcObjects) {
            sampleMerger.addSamples(calcObject.getSamples(), calcObject.getIdentifier(), calcObject.getInputType());
            logger.debug("added {} samples with identifier {} to merger", calcObject.getSamples(), calcObject.getIdentifier());
        }
        Map<DateTime, List<Sample>> mergedSamples = sampleMerger.merge();
        logger.debug("{} mergable calculations found", mergedSamples.size());
        ResultCalculator resultCalc = new ResultCalculator(mergedSamples, new CalcTemplate(expression));
        List<JEVisSample> calculateResult = resultCalc.calculateResult();
        logger.info("{} results calculated", calculateResult.size());
        saveToOutput(calculateResult);

    }

    private void saveToOutput(List<JEVisSample> calculateResult) {
        try {
            //generate output
            for (JEVisAttribute output : outputs) {
                output.addSamples(calculateResult);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    long getCalcObjectID() {
        return calcObjID;
    }

}
