/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jecalc.calculation.PeriodArithmetic;
import org.jevis.jecalc.calculation.SampleMerger.InputType;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

/**
 * @author broder
 */
class CalcJobFactory {


    private static final Logger logger = LogManager.getLogger(CalcJobFactory.class);
    private CalcJob calcJob;
    private List<JEVisObject> calcInputObjects;
    private DateTime lastEndTime;

    CalcJobFactory() {
        this.calcJob = new CalcJob();
    }

    CalcJob getCurrentCalcJob(SampleHandler sampleHandler, JEVisDataSource ds, JEVisObject jevisObject) {
        this.calcInputObjects = null;
        calcJob.setCalcInputObjects(null);
        calcJob.setOutputAttributes(null);

        logger.info("-------------------------------------------");
        long calcObjID = jevisObject.getID();
        logger.info("Create calc job for object with jevis id {}", calcObjID);

        String expression = sampleHandler.getLastSampleAsString(jevisObject, Calculation.EXPRESSION.getName());
        List<JEVisAttribute> outputAttributes = getAllOutputAttributes(jevisObject);

        DateTime startTime;
        if (lastEndTime == null) {
            startTime = getStartTimeFromOutputs(ds, outputAttributes, getCalcInputObjects(jevisObject));
        } else startTime = lastEndTime;
        logger.debug("start time is: " + startTime);

        List<CalcInputObject> calcInputObjects = getInputDataObjects(jevisObject, startTime, ds);
        logger.debug("{} inputs found", calcInputObjects.size());

        calcJob.setCalcInputObjects(calcInputObjects);
        calcJob.setExpression(expression);
        calcJob.setOutputAttributes(outputAttributes);
        calcJob.setCalcObjID(calcObjID);

        return calcJob;
    }

    private List<JEVisAttribute> getAllOutputAttributes(JEVisObject jevisObject) {
        List<JEVisAttribute> outputAttributes = new ArrayList<>();
        try {
            JEVisClass outputClass = jevisObject.getDataSource().getJEVisClass(Calculation.OUTPUT_DATA.getName());
            List<JEVisObject> outputs = jevisObject.getChildren(outputClass, false);
            for (JEVisObject output : outputs) {
                JEVisAttribute targetAttr = output.getAttribute(Calculation.OUTPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(output.getDataSource(), targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute();
                if (valueAttribute == null) {
                    logger.error("Cant find output for id {}, using fallback 'Value' Attribute ", output.getID());
                    outputAttributes.add(targetHelper.getObject().getAttribute("Value"));
                } else {
                    outputAttributes.add(valueAttribute);
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return outputAttributes;
    }

    private DateTime getStartTimeFromOutputs(JEVisDataSource ds, List<JEVisAttribute> outputAttributes, List<JEVisObject> inputDataObjects) {
        DateTime startTime = null;
        final DateTime ultimateStart = new DateTime(0);
        for (JEVisAttribute valueAttribute : outputAttributes) {
            //if a attribute is without date -> start whole calculation
            DateTime ts = null;
            try {
                List<JEVisSample> sampleList = valueAttribute.getAllSamples();
                if (sampleList.size() > 0) {
                    JEVisSample smp = sampleList.get(sampleList.size() - 1);

                    if (startTime == null) ts = smp.getTimestamp().plus(valueAttribute.getInputSampleRate());

                    if (!ts.equals(ultimateStart)) startTime = ts;
                } else {
                    if (startTime == null) {
                        startTime = ultimateStart;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        DateTime startTimeFromInputs = ultimateStart;

        for (JEVisObject obj : inputDataObjects) {
            JEVisAttribute targetAttr = null;
            try {
                targetAttr = obj.getAttribute(Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute();
                if (startTimeFromInputs.isBefore(valueAttribute.getTimestampFromFirstSample()))
                    startTimeFromInputs = valueAttribute.getTimestampFromFirstSample();
            } catch (JEVisException e) {
                logger.error(e);
            }

            /**
             * check needed for empty data rows
             */

            if (startTime.equals(ultimateStart)) {
                if (!startTimeFromInputs.equals(ultimateStart))
                    startTime = startTimeFromInputs;
            }

            /**
             * check needed for gaps in data
             */

            if (startTimeFromInputs.isAfter(startTime)) startTime = startTimeFromInputs;
        }


        if (startTime == null) {
            throw new IllegalStateException("Cant calculate a start date");
        }
        return startTime;
    }

    private List<JEVisObject> getCalcInputObjects(JEVisObject calcObject) {
        if (calcInputObjects == null) {
            try {
                JEVisClass inputClass = calcObject.getDataSource().getJEVisClass(Calculation.INPUT.getName());
                calcInputObjects = calcObject.getChildren(inputClass, false);
            } catch (JEVisException e) {
                calcInputObjects = new ArrayList<>();
            }
        }
        return calcInputObjects;
    }

    private List<CalcInputObject> getInputDataObjects(JEVisObject jevisObject, DateTime startTime, JEVisDataSource ds) {
        List<CalcInputObject> calcObjects = new ArrayList<>();
        Interval fromTo = null;
        Period period = null;
        DateTime endTime = null;
        try {
            for (JEVisObject child : getCalcInputObjects(jevisObject)) { //Todo differenciate based on input type
                JEVisAttribute targetAttr = child.getAttribute(Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute();

                if (fromTo == null) {
                    fromTo = new Interval(startTime, new DateTime());
                    period = valueAttribute.getInputSampleRate();

                    if (PeriodArithmetic.periodsInAnInterval(fromTo, period) < 10000) {
                        calcJob.setHasProcessedAllInputSamples(true);
                        endTime = new DateTime().minus(valueAttribute.getInputSampleRate());
                    } else {
                        calcJob.setHasProcessedAllInputSamples(false);
                        DateTime limitedMaxDate = startTime;
                        for (int i = 0; i < 10000; i++)
                            limitedMaxDate = limitedMaxDate.plus(period.toStandardDuration());
                        endTime = limitedMaxDate;
                        lastEndTime = endTime;
                    }
                }

                if (valueAttribute == null) {
                    throw new IllegalStateException("Cant find valid values for input data with id " + child.getID());
                }

                String identifier = child.getAttribute(Calculation.IDENTIFIER.getName()).getLatestSample().getValueAsString();
                String inputTypeString = child.getAttribute(Calculation.INPUT_TYPE.getName()).getLatestSample().getValueAsString();
                InputType inputType = InputType.valueOf(inputTypeString);

                CalcInputObject calcObject = new CalcInputObject(identifier, inputType, valueAttribute);
                calcObject.buildSamplesFromInputType(valueAttribute, inputType, startTime, endTime);
                logger.info("Got samples for id {}", calcObject.getIdentifier());
                calcObjects.add(calcObject);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return calcObjects;
    }


    public enum Calculation {

        CLASS("Calculation"),
        EXPRESSION("Expression"),
        ENABLED("Enabled"),
        INPUT("Input"),
        INPUT_DATA("Input Data"),
        OUTPUT_DATA("Output"),
        IDENTIFIER("Identifier"),
        INPUT_TYPE("Input Data Type");

        String name;

        Calculation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

}
