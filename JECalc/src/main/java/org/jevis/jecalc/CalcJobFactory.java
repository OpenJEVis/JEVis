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
import org.jevis.jecalc.calculation.SampleMerger.InputType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author broder
 */
class CalcJobFactory {

    private final Stack<JEVisObject> calcObjectStack = new Stack<>();
    private final Logger logger = LogManager.getLogger(CalcJobFactory.class);

    CalcJobFactory(List<JEVisObject> calcObjects) {
        calcObjectStack.addAll(calcObjects);
    }

    boolean hasNextJob() {
        return !calcObjectStack.isEmpty();
    }

    CalcJob getCurrentCalcJob(SampleHandler sampleHandler, JEVisDataSource ds) {
        JEVisObject jevisObject = calcObjectStack.pop();

        logger.info("-------------------------------------------");
        long calcObjID = jevisObject.getID();
        logger.info("Create calc job for object with jevis id {}", calcObjID);
        sampleHandler.getLastSample(jevisObject, Calculation.EXPRESSION.getName());
        String expression = sampleHandler.getLastSampleAsString(jevisObject, Calculation.EXPRESSION.getName());
        List<JEVisAttribute> outputAttributes = getAllOutputAttributes(jevisObject);
        DateTime startTime = getStartTimeFromOutputs(outputAttributes);
        logger.debug("start time is", startTime.toString(DateTimeFormat.fullDateTime()));
        List<CalcInputObject> calcInputObjects = getInputDataObjects(jevisObject, startTime, ds);
        logger.debug("{} inputs found", calcInputObjects.size());
        CalcJob calcJob = new CalcJob(calcInputObjects, expression, outputAttributes, calcObjID);

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
            java.util.logging.Logger.getLogger(CalcJobFactory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return outputAttributes;
    }

    private DateTime getStartTimeFromOutputs(List<JEVisAttribute> outputAttributes) {
        DateTime startTime = null;
        for (JEVisAttribute valueAttribute : outputAttributes) {
            //if a attribute is without date -> start whole calculation
            DateTime ts = null;
            try {
                List<JEVisSample> sampleList = valueAttribute.getAllSamples();
                if (sampleList.size() > 0) {
                    JEVisSample smp = sampleList.get(0);
                    if (smp != null) {
                        if (startTime == null) ts = smp.getTimestamp();
                        else if (ts.isBefore(startTime)) ts = smp.getTimestamp();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (valueAttribute == null || ts == null) {
                startTime = new DateTime(0);
                break;
            }

        }
        if (startTime == null) {
            throw new IllegalStateException("Cant calculate a start date");
        }
        return startTime;
    }

    private List<CalcInputObject> getInputDataObjects(JEVisObject jevisObject, DateTime startTime, JEVisDataSource ds) {
        List<CalcInputObject> calcObjects = new ArrayList<>();
        try {

            JEVisClass inputClass = jevisObject.getDataSource().getJEVisClass(Calculation.INPUT.getName());
            List<JEVisObject> inputDataObjects = jevisObject.getChildren(inputClass, false);
            for (JEVisObject child : inputDataObjects) { //Todo differenciate based on input type
                JEVisAttribute targetAttr = child.getAttribute(Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute();
                if (valueAttribute == null) {
                    throw new IllegalStateException("Cant find valid values for input data with id " + child.getID());
                }

                String identifier = child.getAttribute(Calculation.IDENTIFIER.getName()).getLatestSample().getValueAsString();
                String inputTypeString = child.getAttribute(Calculation.INPUT_TYPE.getName()).getLatestSample().getValueAsString();
                InputType inputType = InputType.valueOf(inputTypeString);
                List<JEVisSample> samples = getSamplesFromInputType(valueAttribute, inputType, startTime);
                CalcInputObject calcObject = new CalcInputObject(samples, identifier, inputType);
                calcObjects.add(calcObject);
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(CalcJobFactory.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return calcObjects;
    }

    private List<JEVisSample> getSamplesFromInputType(JEVisAttribute valueAttribute, InputType inputType, DateTime startTime) {
        List<JEVisSample> returnSamples = new ArrayList<>();
        switch (inputType) {
            case PERIODIC:
                returnSamples = valueAttribute.getSamples(startTime, new DateTime());
                break;
            case STATIC:
                JEVisSample constant = valueAttribute.getLatestSample();
                if (constant != null) {
                    returnSamples.add(constant);
                } else {
                    throw new IllegalArgumentException("Constant with id " + valueAttribute.getObject().getID() + " has no value");
                }
                break;
            case NON_PERIODIC:
                returnSamples = valueAttribute.getAllSamples();
                break;
        }
        return returnSamples;
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
