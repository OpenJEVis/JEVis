/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;
import static org.jevis.commons.utils.CommonMethods.getNextSiteRecursive;

/**
 * Loads and holds the input samples for one variable of a JEVis Calculation object.
 * <p>
 * Three {@code buildSamplesFromInputType} overloads exist for different aggregation modes:
 * <ol>
 *   <li>No aggregation — raw samples in the given time range.</li>
 *   <li>{@link org.jevis.commons.dataprocessing.AggregationPeriod} — server-side aggregation.</li>
 *   <li>{@code Boolean absolute} — sums or averages within the window into a single sample.</li>
 * </ol>
 * If a sibling "User Data" object exists whose name contains the source object's name,
 * user-entered override values replace the raw samples in the loaded range.
 */
public class CalcInputObject {

    private static final Logger logger = LogManager.getLogger(CalcInputObject.class);
    private final String identifier;
    private final JEVisAttribute valueAttribute;
    private final CalcInputType inputType;
    private List<JEVisSample> samples;
    private DateTimeZone dateTimeZone = DateTimeZone.getDefault();

    public CalcInputObject(String identifier, CalcInputType inputType, JEVisAttribute valueAttribute) {
        this.identifier = identifier;
        this.inputType = inputType;
        this.valueAttribute = valueAttribute;

        try {
            JEVisClass siteClass = valueAttribute.getObject().getDataSource().getJEVisClass(JC.MonitoredObject.Building.name);
            JEVisObject site = getNextSiteRecursive(valueAttribute.getObject(), siteClass);
            JEVisAttribute zoneAtt = site.getAttribute(JC.MonitoredObject.Building.a_Timezone);
            if (zoneAtt.hasSample()) {
                String zoneStr = zoneAtt.getLatestSample().getValueAsString();
                dateTimeZone = DateTimeZone.forID(zoneStr);
            }
        } catch (Exception e) {
            logger.error("Could not get Site timezone for object {}:{}", valueAttribute.getObject().getName(), valueAttribute.getObject().getID(), e);
        }
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
        } catch (Exception e) {
            logger.error("Could not reload attribute. ", e);
        }

        JEVisObject correspondingUserDataObject = findUserDataObject(valueAttribute);
        boolean foundUserDataObject = correspondingUserDataObject != null;

        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                //todo try to make it better for incomplete periods (aggregation)
                returnSamples = valueAttribute.getSamples(startTime, endTime);

                if (foundUserDataObject) {
                    returnSamples = getUserData(startTime, endTime, correspondingUserDataObject, returnSamples);
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

                if (foundUserDataObject) {
                    returnSamples = getUserData(startTime, endTime, correspondingUserDataObject, returnSamples);
                }

                break;
        }

        this.samples = returnSamples;

    }

    public void buildSamplesFromInputType(JEVisAttribute valueAttribute, CalcInputType inputType, DateTime startTime, DateTime endTime, AggregationPeriod aggregationPeriod) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        try {
            valueAttribute.getDataSource().reloadAttribute(valueAttribute);
        } catch (Exception e) {
            logger.error("Could not reload attribute. ", e);
        }

        JEVisObject correspondingUserDataObject = findUserDataObject(valueAttribute);
        boolean foundUserDataObject = correspondingUserDataObject != null;

        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                returnSamples = valueAttribute.getSamples(startTime, endTime, true, aggregationPeriod.toString(), ManipulationMode.NONE.toString(), dateTimeZone.getID());

                if (foundUserDataObject) {
                    returnSamples = getUserData(startTime, endTime, correspondingUserDataObject, returnSamples);
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

                if (foundUserDataObject) {
                    returnSamples = getUserData(startTime, endTime, correspondingUserDataObject, returnSamples);
                }

                break;
        }

        this.samples = returnSamples;

    }

    public void buildSamplesFromInputType(JEVisAttribute valueAttribute, CalcInputType inputType, DateTime startTime, DateTime endTime, Boolean absolute) {

        List<JEVisSample> returnSamples = new ArrayList<>();
        try {
            valueAttribute.getDataSource().reloadAttribute(valueAttribute);
        } catch (Exception e) {
            logger.error("Could not reload attribute. ", e);
        }

        JEVisObject correspondingUserDataObject = findUserDataObject(valueAttribute);
        boolean foundUserDataObject = correspondingUserDataObject != null;

        switch (inputType) {
            case ASYNC:
            case PERIODIC:
                try {
                    QuantityUnits qu = new QuantityUnits();
                    List<JEVisSample> tempList = valueAttribute.getSamples(startTime, endTime, true, AggregationPeriod.NONE.toString(), ManipulationMode.NONE.toString(), dateTimeZone.getID());

                    if (!tempList.isEmpty()) {

                        if (foundUserDataObject) {
                            tempList = getUserData(startTime, endTime, correspondingUserDataObject, tempList);
                        }

                        boolean isQuantity = qu.isQuantityUnit(valueAttribute.getDisplayUnit());
                        isQuantity = qu.isQuantityIfCleanData(valueAttribute, isQuantity);

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
                } catch (Exception e) {
                    logger.error("Could not generate samples: ", e);
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

                if (foundUserDataObject) {
                    returnSamples = getUserData(startTime, endTime, correspondingUserDataObject, returnSamples);
                }

                break;
        }

        this.samples = returnSamples;

    }

    private List<JEVisSample> getUserData(DateTime startTime, DateTime endTime, JEVisObject correspondingUserDataObject, List<JEVisSample> tempList) {
        try {
            SortedMap<DateTime, JEVisSample> map = new TreeMap<>();
            for (JEVisSample jeVisSample : tempList) {
                map.put(jeVisSample.getTimestamp(), jeVisSample);
            }

            JEVisAttribute userDataValueAttribute = correspondingUserDataObject.getAttribute("Value");
            List<JEVisSample> userValues = userDataValueAttribute.getSamples(startTime, endTime);

            for (JEVisSample userValue : userValues) {
                DateTime ts = userValue.getTimestamp();
                try {
                    String note = "";
                    if (map.get(ts) != null) {
                        note += map.get(ts).getNote();
                    }
                    VirtualSample virtualSample = new VirtualSample(ts, userValue.getValueAsDouble());
                    virtualSample.setNote(note + "," + USER_VALUE);
                    virtualSample.setAttribute(map.get(ts).getAttribute());

                    map.remove(ts);
                    map.put(ts, virtualSample);
                } catch (Exception e) {
                    logger.error("Error applying user value for {}", ts, e);
                }
            }

            tempList = new ArrayList<>(map.values());
        } catch (Exception e) {
            logger.error("Could not apply user data", e);
        }
        return tempList;
    }

    /**
     * Searches siblings of the given attribute's owner object for a "User Data" object
     * whose name contains the owner's name, returning it if found, {@code null} otherwise.
     */
    private JEVisObject findUserDataObject(JEVisAttribute attr) {
        try {
            JEVisObject object = attr.getObject();
            JEVisClass userDataClass = attr.getDataSource().getJEVisClass("User Data");
            for (JEVisObject parent : object.getParents()) {
                for (JEVisObject child : parent.getChildren()) {
                    if (child.getJEVisClass().equals(userDataClass) && child.getName().contains(object.getName())) {
                        return child;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not find user data object for {}", attr.getObject().getID(), e);
        }
        return null;
    }
}
