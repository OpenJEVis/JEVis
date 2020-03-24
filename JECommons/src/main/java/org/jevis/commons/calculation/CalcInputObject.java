/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

/**
 * @author broder
 */
public class CalcInputObject {

    private final String identifier;
    private final JEVisAttribute valueAttribute;
    private static final Logger logger = LogManager.getLogger(CalcInputObject.class);
    private List<JEVisSample> samples;
    private final CalcInputType inputType;

    public CalcInputObject(String identifier, CalcInputType inputType, JEVisAttribute valueAttribute) {
        this.identifier = identifier;
        this.inputType = inputType;
        this.valueAttribute = valueAttribute;
    }

    public CalcInputType getInputType() {
        return inputType;
    }

    public JEVisAttribute getValueAttribute() {
        return valueAttribute;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<JEVisSample> getSamples() {
        return samples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }


    public void buildSamplesFromInputType(JEVisAttribute valueAttribute, CalcInputType inputType, DateTime startTime, DateTime endTime) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        try {
            valueAttribute.getDataSource().reloadAttribute(valueAttribute);
        } catch (JEVisException e) {
            logger.error("Could not reload attribute. ", e);
        }

        JEVisObject correspondingUserDataObject = null;
        boolean foundUserDataObject = false;
        final JEVisClass userDataClass;
        try {
            userDataClass = valueAttribute.getDataSource().getJEVisClass("User Data");
            for (JEVisObject parent : valueAttribute.getObject().getParents()) {
                for (JEVisObject child : parent.getChildren()) {
                    if (child.getJEVisClass().equals(userDataClass)) {
                        correspondingUserDataObject = child;
                        foundUserDataObject = true;
                        break;
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                //todo try to make it better for incomplete periods (aggregation)
                returnSamples = valueAttribute.getSamples(startTime, endTime);
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

        if (foundUserDataObject) {
            try {
                SortedMap<DateTime, JEVisSample> map = new TreeMap<>();
                for (JEVisSample jeVisSample : returnSamples) {
                    map.put(jeVisSample.getTimestamp(), jeVisSample);
                }

                JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");
                List<JEVisSample> userValues = userDataValueAttribute.getSamples(startTime, endTime);

                for (JEVisSample userValue : userValues) {
                    String note = map.get(userValue.getTimestamp()).getNote();
                    VirtualSample virtualSample = new VirtualSample(userValue.getTimestamp(), userValue.getValueAsDouble());
                    virtualSample.setNote(note + "," + USER_VALUE);
                    virtualSample.setAttribute(map.get(userValue.getTimestamp()).getAttribute());

                    map.remove(userValue.getTimestamp());
                    map.put(virtualSample.getTimestamp(), virtualSample);
                }

                returnSamples = new ArrayList<>(map.values());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        this.samples = returnSamples;

    }

    public void buildSamplesFromInputType(JEVisAttribute valueAttribute, CalcInputType inputType, DateTime startTime, DateTime endTime, AggregationPeriod aggregationPeriod) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        try {
            valueAttribute.getDataSource().reloadAttribute(valueAttribute);
        } catch (JEVisException e) {
            logger.error("Could not reload attribute. ", e);
        }
        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                try {

                    SampleGenerator sampleGenerator = new SampleGenerator(valueAttribute.getDataSource(), valueAttribute.getObject(), valueAttribute, startTime, endTime, ManipulationMode.NONE, aggregationPeriod);

                    returnSamples = sampleGenerator.getAggregatedSamples();
                } catch (JEVisException e) {
                    logger.error("Could not generate samples: " + e);
                }
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
        this.samples = returnSamples;

    }

    public void buildSamplesFromInputType(JEVisAttribute valueAttribute, CalcInputType inputType, DateTime startTime, DateTime endTime, Boolean absolute) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        try {
            valueAttribute.getDataSource().reloadAttribute(valueAttribute);
        } catch (JEVisException e) {
            logger.error("Could not reload attribute. ", e);
        }
        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                try {

                    SampleGenerator sampleGenerator = new SampleGenerator(valueAttribute.getDataSource(), valueAttribute.getObject(), valueAttribute, startTime, endTime, ManipulationMode.NONE, AggregationPeriod.NONE);
                    QuantityUnits qu = new QuantityUnits();
                    List<JEVisSample> tempList = sampleGenerator.getAggregatedSamples();

                    if (!tempList.isEmpty()) {
                        boolean isQuantity = qu.isQuantityUnit(valueAttribute.getDisplayUnit());

                        double sum = 0.0;
                        for (JEVisSample sample : tempList) {
                            sum += sample.getValueAsDouble();
                        }
                        if (!isQuantity) {
                            sum = sum / tempList.size();
                        }
                        VirtualSample virtualSample = new VirtualSample(startTime, sum);
                        virtualSample.setNote("");
                        returnSamples.add(virtualSample);
                    }
                } catch (JEVisException e) {
                    logger.error("Could not generate samples: " + e);
                }
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
        this.samples = returnSamples;

    }
}
