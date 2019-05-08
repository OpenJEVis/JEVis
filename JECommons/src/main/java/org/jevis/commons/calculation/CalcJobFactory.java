/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.datetime.PeriodArithmetic;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author broder
 */
public class CalcJobFactory {

    private static final Logger logger = LogManager.getLogger(CalcJobFactory.class);
    private CalcJob calcJob;
    private List<JEVisObject> calcInputObjects;
    private DateTime lastEndTime;

    public CalcJobFactory() {
        this.calcJob = new CalcJob();
    }

    public CalcJob getCalcJobForTimeFrame(SampleHandler sampleHandler, JEVisDataSource ds, JEVisObject jevisObject,
                                          DateTime startTime, DateTime endTime, AggregationPeriod aggregationPeriod) {
        this.calcInputObjects = null;
        calcJob.setCalcInputObjects(null);
        calcJob.setOutputAttributes(null);

        logger.info("-------------------------------------------");
        long calcObjID = jevisObject.getID();
        logger.info("Create calc job for object with JEVis id {}", calcObjID);

        String expression = sampleHandler.getLastSample(jevisObject, Calculation.EXPRESSION.getName(), "");
        List<JEVisAttribute> outputAttributes = getAllOutputAttributes(jevisObject);
        if (outputAttributes.isEmpty()) {
            throw new RuntimeException("No output target");
        }

        logger.debug("start time is: " + startTime);

        List<CalcInputObject> calcInputObjects = getInputDataObjects(jevisObject, ds, startTime, endTime, aggregationPeriod);
        logger.debug("{} inputs found", calcInputObjects.size());
        String div0Handling = null;
        Double staticValue = null;
        Double allZeroValue = null;
        try {
            div0Handling = sampleHandler.getLastSample(jevisObject, Calculation.DIV0_HANDLING.getName(), "");
            staticValue = sampleHandler.getLastSample(jevisObject, Calculation.STATIC_VALUE.getName(), 0.0);
            JEVisAttribute allZeroValueAtt = jevisObject.getAttribute(Calculation.ALL_ZERO_VALUE.getName());
            if (allZeroValueAtt.hasSample())
                allZeroValue = allZeroValueAtt.getLatestSample().getValueAsDouble();
        } catch (Exception e) {
            logger.error(e);
        }

        calcJob.setCalcInputObjects(calcInputObjects);
        calcJob.setExpression(expression);
        calcJob.setOutputAttributes(outputAttributes);
        calcJob.setCalcObjID(calcObjID);
        calcJob.setStaticValue(staticValue);
        calcJob.setAllZeroValue(allZeroValue);
        calcJob.setDIV0Handling(div0Handling);

        return calcJob;
    }

    public CalcJob getCalcJobForTimeFrame(SampleHandler sampleHandler, JEVisDataSource ds, JEVisObject jevisObject,
                                          DateTime startTime, DateTime endTime, Boolean absolute) {
        this.calcInputObjects = null;
        calcJob.setCalcInputObjects(null);
        calcJob.setOutputAttributes(null);

        logger.info("-------------------------------------------");
        long calcObjID = jevisObject.getID();
        logger.info("Create calc job for object with jevis id {}", calcObjID);

        String expression = sampleHandler.getLastSample(jevisObject, Calculation.EXPRESSION.getName(), "");
        List<JEVisAttribute> outputAttributes = getAllOutputAttributes(jevisObject);

        logger.debug("start time is: " + startTime);

        List<CalcInputObject> calcInputObjects = getInputDataObjects(jevisObject, ds, startTime, endTime, absolute);
        logger.debug("{} inputs found", calcInputObjects.size());
        String div0Handling = null;
        Double staticValue = null;
        Double allZeroValue = null;
        try {
            div0Handling = sampleHandler.getLastSample(jevisObject, Calculation.DIV0_HANDLING.getName(), "");
            staticValue = sampleHandler.getLastSample(jevisObject, Calculation.STATIC_VALUE.getName(), 0.0);
            JEVisAttribute allZeroValueAtt = jevisObject.getAttribute(Calculation.ALL_ZERO_VALUE.getName());
            if (allZeroValueAtt.hasSample())
                allZeroValue = allZeroValueAtt.getLatestSample().getValueAsDouble();
        } catch (Exception e) {

        }

        calcJob.setCalcInputObjects(calcInputObjects);
        calcJob.setExpression(expression);
        calcJob.setOutputAttributes(outputAttributes);
        calcJob.setCalcObjID(calcObjID);
        calcJob.setStaticValue(staticValue);
        calcJob.setAllZeroValue(allZeroValue);
        calcJob.setDIV0Handling(div0Handling);

        return calcJob;
    }

    public CalcJob getCurrentCalcJob(SampleHandler sampleHandler, JEVisDataSource ds, JEVisObject jevisObject) {
        this.calcInputObjects = null;
        calcJob.setCalcInputObjects(null);
        calcJob.setOutputAttributes(null);

        logger.info("-------------------------------------------");
        long calcObjID = jevisObject.getID();
        logger.info("Create calc job for object with jevis id {}", calcObjID);

        String expression = sampleHandler.getLastSample(jevisObject, Calculation.EXPRESSION.getName(), "");
        List<JEVisAttribute> outputAttributes = getAllOutputAttributes(jevisObject);

        DateTime startTime;
        if (lastEndTime == null) {
            startTime = getStartTimeFromOutputs(ds, outputAttributes, getCalcInputObjects(jevisObject));
            if (outputAttributes.size() == 1) {
                startTime = startTime.minus(outputAttributes.get(0).getInputSampleRate());
            }

        } else startTime = lastEndTime;
        logger.debug("start time is: " + startTime);

        List<CalcInputObject> calcInputObjects = getInputDataObjects(jevisObject, startTime, ds);
        logger.debug("{} inputs found", calcInputObjects.size());
        String div0Handling = null;
        Double staticValue = null;
        Double allZeroValue = null;
        try {
            div0Handling = sampleHandler.getLastSample(jevisObject, Calculation.DIV0_HANDLING.getName(), "");
            staticValue = sampleHandler.getLastSample(jevisObject, Calculation.STATIC_VALUE.getName(), 0.0);
            JEVisAttribute allZeroValueAtt = jevisObject.getAttribute(Calculation.ALL_ZERO_VALUE.getName());
            if (allZeroValueAtt.hasSample())
                allZeroValue = allZeroValueAtt.getLatestSample().getValueAsDouble();
        } catch (Exception e) {

        }

        calcJob.setCalcInputObjects(calcInputObjects);
        calcJob.setExpression(expression);
        calcJob.setOutputAttributes(outputAttributes);
        calcJob.setCalcObjID(calcObjID);
        calcJob.setStaticValue(staticValue);
        calcJob.setAllZeroValue(allZeroValue);
        calcJob.setDIV0Handling(div0Handling);

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
                if (!targetHelper.getAttribute().isEmpty()) {
                    JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);
                    if (valueAttribute == null) {
                        logger.error("Cant find output for id {}, using fallback 'Value' Attribute ", output.getID());
                        outputAttributes.add(targetHelper.getObject().get(0).getAttribute("Value"));
                    } else {
                        outputAttributes.add(valueAttribute);
                    }
                } else {
                    logger.error("No output configure for id {}", output.getID());
                }

            }
        } catch (Exception ex) {
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
                if (valueAttribute.hasSample()) {
                    ds.reloadAttribute(valueAttribute);
                    JEVisSample smp = valueAttribute.getLatestSample();

                    if (startTime == null && smp != null) {
                        ts = smp.getTimestamp().plus(valueAttribute.getInputSampleRate());
                    }

                    if (ts != null && !ts.equals(ultimateStart)) {
                        startTime = ts;
                    }

                    /**
                     * should never happen but we will make sure...
                     */
                    if (startTime == null) {
                        startTime = ultimateStart;
                        break;
                    }
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
                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);
                ds.reloadAttribute(valueAttribute);
                if (startTimeFromInputs.isBefore(valueAttribute.getTimestampFromFirstSample()))
                    startTimeFromInputs = valueAttribute.getTimestampFromFirstSample();
            } catch (JEVisException e) {
                logger.error(e);
            }

            /**
             * check needed for empty data rows
             */

            if (Objects.requireNonNull(startTime).equals(ultimateStart)) {
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
                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);

                if (fromTo == null) {
                    fromTo = new Interval(startTime, new DateTime());
                    period = valueAttribute.getInputSampleRate();

                    if (PeriodArithmetic.periodsInAnInterval(fromTo, period) < 10000) {
                        calcJob.setHasProcessedAllInputSamples(true);
                        /**
                         * is this minus really necessary? do tests...
                         * disabled for  now, concrete testing for aggregated values is needed
                         */
//                        endTime = new DateTime().minus(valueAttribute.getInputSampleRate());
                        endTime = new DateTime();
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
                CalcInputType inputType = CalcInputType.valueOf(inputTypeString);

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

    private List<CalcInputObject> getInputDataObjects(JEVisObject jevisObject, JEVisDataSource ds, DateTime startTime, DateTime endTime, AggregationPeriod aggregationPeriod) {
        List<CalcInputObject> calcObjects = new ArrayList<>();

        try {
            for (JEVisObject child : getCalcInputObjects(jevisObject)) { //Todo differenciate based on input type
                JEVisAttribute targetAttr = child.getAttribute(Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);

                String identifier = child.getAttribute(Calculation.IDENTIFIER.getName()).getLatestSample().getValueAsString();
                String inputTypeString = child.getAttribute(Calculation.INPUT_TYPE.getName()).getLatestSample().getValueAsString();
                CalcInputType inputType = CalcInputType.valueOf(inputTypeString);

                CalcInputObject calcInputObject = new CalcInputObject(identifier, inputType, valueAttribute);
                calcInputObject.buildSamplesFromInputType(valueAttribute, inputType, startTime, endTime, aggregationPeriod);
                logger.info("Got samples for id {}", calcInputObject.getIdentifier());
                calcObjects.add(calcInputObject);
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return calcObjects;
    }


    private List<CalcInputObject> getInputDataObjects(JEVisObject jevisObject, JEVisDataSource ds, DateTime startTime, DateTime endTime, Boolean absolute) {
        List<CalcInputObject> calcObjects = new ArrayList<>();

        try {
            /**
             * find the combined end interval
             * hotfix to calculate inputs with different time range
             * Discussion: we we handle the error here or do we throw an error and the caller has to take care of this?
             */
            List<JEVisObject> inputObjectList = getCalcInputObjects(jevisObject);
//            List<DateTime> lastSampleList = new ArrayList<>();
//            DateTime combinedEndTime = startTime;
//            for (JEVisObject child : inputObjectList) {
//                JEVisAttribute targetAttr = child.getAttribute(Calculation.INPUT_DATA.getName());
//                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
//                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);
//                if (valueAttribute != null && valueAttribute.hasSample()) {
//                    lastSampleList.add(valueAttribute.getTimestampFromLastSample());
//                }
//            }
//            for (DateTime ts : lastSampleList) {
//                if (ts.isBefore(combinedEndTime) && ts.isAfter(startTime)) {
//                    combinedEndTime = ts;
//                }
//            }


            for (JEVisObject child : inputObjectList) { //Todo differenciate based on input type
                JEVisAttribute targetAttr = child.getAttribute(Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);

                String identifier = child.getAttribute(Calculation.IDENTIFIER.getName()).getLatestSample().getValueAsString();
                String inputTypeString = child.getAttribute(Calculation.INPUT_TYPE.getName()).getLatestSample().getValueAsString();
                CalcInputType inputType = CalcInputType.valueOf(inputTypeString);

                CalcInputObject calcInputObject = new CalcInputObject(identifier, inputType, valueAttribute);
                calcInputObject.buildSamplesFromInputType(valueAttribute, inputType, startTime, endTime, absolute);
                logger.info("Got samples for id {}", calcInputObject.getIdentifier());
                calcObjects.add(calcInputObject);
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
        INPUT_TYPE("Input Data Type"),
        DIV0_HANDLING("DIV0 Handling"),
        STATIC_VALUE("Static Value"),
        ALL_ZERO_VALUE("All Zero Value");

        String name;

        Calculation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

}
