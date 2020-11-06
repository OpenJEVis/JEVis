/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.constants.GapFillingReferencePeriod.MONTH;
import static org.jevis.commons.dataprocessing.MathDataObject.AttributeName.*;

/**
 * @author gschutz
 */
public class MathDataObject {

    public static final String CLASS_NAME = "Math Data";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";
    private static final Logger logger = LogManager.getLogger(MathDataObject.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JEVisObject mathDataObject;
    private final JEVisObject parentDataObject;
    //additional attributes
    private final SampleHandler sampleHandler;
    private Boolean enabled;
    private JEVisAttribute inputAttribute;

    private JEVisAttribute valueAttribute;
    private JEVisAttribute enabledAttribute;
    private JEVisAttribute typeAttribute;
    private JEVisAttribute formulaAttribute;
    private JEVisAttribute referencePeriodAttribute;
    private JEVisAttribute referencePeriodCountAttribute;
    private JEVisAttribute periodOffsetAttribute;
    private JEVisAttribute fillPeriodAttribute;
    private JEVisAttribute beginningAttribute;
    private JEVisAttribute endingAttribute;
    private DateTime lastRawDate;
    private int processingSize = 10000;
    private List<JEVisSample> sampleCache;
    private Boolean fillPeriod;

    private Period inputDataPeriod;
    private AggregationPeriod referencePeriod;
    private Long periodOffset;
    private Long referencePeriodCount;
    private ManipulationMode manipulationMode;
    private DateTime beginning;
    private DateTime ending;
    private String formula;

    public MathDataObject(JEVisObject forecastObject, ObjectHandler objectHandler) {
        mathDataObject = forecastObject;
        parentDataObject = objectHandler.getFirstParent(forecastObject);
        sampleHandler = new SampleHandler();
    }

    public void getAttributes() throws JEVisException {
        if (enabledAttribute == null) {
            enabledAttribute = getMathDataObject().getAttribute(ENABLED.getAttributeName());
        }

        if (valueAttribute == null) {
            valueAttribute = getMathDataObject().getAttribute(VALUE.getAttributeName());
        }

        if (typeAttribute == null) {
            typeAttribute = getMathDataObject().getAttribute(MANIPULATION.getAttributeName());
        }

        if (formulaAttribute == null) {
            formulaAttribute = getMathDataObject().getAttribute(FORMULA.getAttributeName());
        }

        if (referencePeriodAttribute == null) {
            referencePeriodAttribute = getMathDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        }

        if (referencePeriodCountAttribute == null) {
            referencePeriodCountAttribute = getMathDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        }

        if (periodOffsetAttribute == null) {
            periodOffsetAttribute = getMathDataObject().getAttribute(PERIOD_OFFSET.getAttributeName());
        }

        if (fillPeriodAttribute == null) {
            fillPeriodAttribute = getMathDataObject().getAttribute(FILL_PERIOD.getAttributeName());
        }

        if (beginningAttribute == null) {
            beginningAttribute = getMathDataObject().getAttribute(BEGINNING.getAttributeName());
        }

        if (endingAttribute == null) {
            endingAttribute = getMathDataObject().getAttribute(ENDING.getAttributeName());
        }
    }

    public void reloadAttributes() throws JEVisException {
        getMathDataObject().getDataSource().reloadAttribute(enabledAttribute);
        getMathDataObject().getDataSource().reloadAttribute(valueAttribute);
        getMathDataObject().getDataSource().reloadAttribute(typeAttribute);
        getMathDataObject().getDataSource().reloadAttribute(formulaAttribute);
        getMathDataObject().getDataSource().reloadAttribute(referencePeriodAttribute);
        getMathDataObject().getDataSource().reloadAttribute(referencePeriodCountAttribute);
        getMathDataObject().getDataSource().reloadAttribute(periodOffsetAttribute);
        getMathDataObject().getDataSource().reloadAttribute(fillPeriodAttribute);
        getMathDataObject().getDataSource().reloadAttribute(beginningAttribute);
        getMathDataObject().getDataSource().reloadAttribute(endingAttribute);
    }

    public Boolean getEnabled() {
        if (enabled == null)
            enabled = sampleHandler.getLastSample(getMathDataObject(), ENABLED.getAttributeName(), false);
        return enabled;
    }

    public AggregationPeriod getReferencePeriod() {
        if (referencePeriod == null) {
            referencePeriod = sampleHandler.getLastSample(getMathDataObject(), REFERENCE_PERIOD.getAttributeName(), AggregationPeriod.NONE);
        }
        return referencePeriod;
    }

    public Long getReferencePeriodCount() {
        if (referencePeriodCount == null) {
            referencePeriodCount = sampleHandler.getLastSample(getMathDataObject(), REFERENCE_PERIOD_COUNT.getAttributeName(), 1L);
        }
        return referencePeriodCount;
    }

    public Long getPeriodOffset() {
        if (periodOffset == null) {
            periodOffset = sampleHandler.getLastSample(getMathDataObject(), PERIOD_OFFSET.getAttributeName(), 0L);
        }
        return periodOffset;
    }

    public ManipulationMode getManipulationMode() {
        if (manipulationMode == null) {
            manipulationMode = sampleHandler.getLastSample(getMathDataObject(), MANIPULATION.getAttributeName(), ManipulationMode.NONE);
        }
        return manipulationMode;
    }

    public String getFormula() {
        if (formula == null) {
            formula = sampleHandler.getLastSample(getMathDataObject(), FORMULA.getAttributeName(), "");
        }
        return formula;
    }

    public Boolean isFillPeriod() {
        if (fillPeriod == null)
            fillPeriod = sampleHandler.getLastSample(getMathDataObject(), FILL_PERIOD.getAttributeName(), false);
        return fillPeriod;
    }

    public DateTime getBeginning() {
        if (beginning == null)
            beginning = sampleHandler.getLastSample(getMathDataObject(), BEGINNING.getAttributeName(), getStartDate());
        return beginning;
    }

    public DateTime getEnding() {
        if (ending == null)
            ending = sampleHandler.getLastSample(getMathDataObject(), ENDING.getAttributeName(), getEndDate().minusMillis(1));
        return ending;
    }

    public JEVisObject getMathDataObject() {
        return mathDataObject;
    }

    public JEVisObject getParentDataObject() {
        return parentDataObject;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        Map<DateTime, JEVisSample> notesMap = new HashMap<>();
        try {
            final JEVisClass dataNoteClass = parentDataObject.getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : mathDataObject.getParents().get(0).getChildren(dataNoteClass, true)) {
                if (obj.getName().contains(mathDataObject.getName())) {
                    JEVisAttribute userNoteAttribute = obj.getAttribute("User Notes");
                    if (userNoteAttribute.hasSample()) {
                        for (JEVisSample smp : userNoteAttribute.getAllSamples()) {
                            notesMap.put(smp.getTimestamp(), smp);
                        }
                    }
                }
            }
        } catch (JEVisException e) {
        }
        return notesMap;
    }

    public String getName() {
        return mathDataObject.getName() + ":" + mathDataObject.getID();
    }

    public JEVisAttribute getValueAttribute() throws JEVisException {
        if (valueAttribute == null)
            valueAttribute = getMathDataObject().getAttribute(VALUE.getAttributeName());
        return valueAttribute;
    }

    public JEVisAttribute getInputAttribute() throws JEVisException {
        if (inputAttribute == null)
            inputAttribute = getParentDataObject().getAttribute(VALUE.getAttributeName());
        return inputAttribute;
    }

    public void setInputAttribute(JEVisAttribute inputAttribute) {
        this.valueAttribute = inputAttribute;
    }

    public JEVisAttribute getTypeAttribute() throws JEVisException {
        if (typeAttribute == null)
            typeAttribute = getMathDataObject().getAttribute(MANIPULATION.getAttributeName());
        return typeAttribute;
    }

    public JEVisAttribute getReferencePeriodAttribute() throws JEVisException {
        if (referencePeriodAttribute == null)
            referencePeriodAttribute = getMathDataObject().getAttribute(REFERENCE_PERIOD.getAttributeName());
        return referencePeriodAttribute;
    }

    public JEVisAttribute getReferencePeriodCountAttribute() throws JEVisException {
        if (referencePeriodCountAttribute == null)
            referencePeriodCountAttribute = getMathDataObject().getAttribute(REFERENCE_PERIOD_COUNT.getAttributeName());
        return referencePeriodCountAttribute;
    }

    public Period getInputDataPeriod() {
        if (inputDataPeriod == null) {
            try {
                inputDataPeriod = getInputAttribute().getInputSampleRate();
            } catch (Exception e) {
                logger.error("Could not get input data period for object {}:{}", getParentDataObject().getName(), getParentDataObject().getID(), e);
            }
        }
        return inputDataPeriod;
    }

    public JEVisAttribute getEnabledAttribute() {
        return enabledAttribute;
    }

    public void setProcessingSize(int processingSize) {
        this.processingSize = processingSize;
    }

    public DateTime getStartDate() {
        return getLastRun(this.getMathDataObject());
    }

    public List<JEVisSample> getSampleCache() {
        if (this.sampleCache == null || this.sampleCache.isEmpty()) {
            GapFillingReferencePeriod referencePeriod = null;
            int referencePeriodCount = 6;
            try {
                if (getReferencePeriodAttribute().hasSample()) {
                    referencePeriod = GapFillingReferencePeriod.parse(getReferencePeriodAttribute().getLatestSample().getValueAsString());
                }
            } catch (JEVisException e) {
                logger.error("Could not get reference period from {}:{}, assuming default value of month", getMathDataObject().getName(), getMathDataObject().getID(), e);
                referencePeriod = MONTH;
            }

            try {
                if (getReferencePeriodCountAttribute().hasSample()) {
                    referencePeriodCount = getReferencePeriodCountAttribute().getLatestSample().getValueAsLong().intValue();
                }
            } catch (JEVisException e) {
                logger.error("Could not get reference period count from {}:{}, assuming default value of 6", getMathDataObject().getName(), getMathDataObject().getID(), e);
            }

            long duration = 0L;
            if (referencePeriod != null) {
                DateTime endDate = getStartDate();
                DateTime startDate = null;

                switch (referencePeriod) {
                    case DAY:
                        startDate = endDate.minusDays(referencePeriodCount);
                        break;
                    case MONTH:
                        startDate = endDate.minusMonths(referencePeriodCount);
                        break;
                    case WEEK:
                        startDate = endDate.minusWeeks(referencePeriodCount);
                        break;
                    case YEAR:
                        startDate = endDate.minusYears(referencePeriodCount);
                        break;
                    case ALL:
                        try {
                            sampleCache = getInputAttribute().getAllSamples();
                            return sampleCache;
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                try {
                    sampleCache = getInputAttribute().getSamples(startDate, endDate);
                    return sampleCache;
                } catch (JEVisException e) {
                    logger.error("Could not get samples from {}:{} in the interval {} to {}",
                            getMathDataObject().getName(), getMathDataObject().getID(), startDate, endDate, e);
                }
            }
            try {
                sampleCache = getInputAttribute().getAllSamples();
                return sampleCache;
            } catch (JEVisException ex) {
                ex.printStackTrace();
            }
            sampleCache = new ArrayList<>();
        }
        return sampleCache;
    }

    public DateTime getEndDate() {

        return getNextRun();
    }

    public boolean isReady() {
        DateTime nextRun = getEndDate();


        try {
            JEVisSample latestSample = getInputAttribute().getLatestSample();

            if (latestSample != null) {
                try {
                    return latestSample.getTimestamp().withZone(getTimeZone(getMathDataObject())).equals(nextRun) || DateTime.now().isAfter(nextRun);
                } catch (JEVisException e) {
                    logger.error("Could not check ready state", e);
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return false;
    }

    public DateTime getNextRunWithOffset() {
        AggregationPeriod aggregationPeriod = getReferencePeriod();
        Long referencePeriodCount = getReferencePeriodCount();
        DateTime start = getStartDate();
        Long offset = getPeriodOffset();
        for (int i = 0; i < referencePeriodCount; i++) {
            switch (aggregationPeriod) {
                case NONE:
                    break;
                case QUARTER_HOURLY:
                    if (offset > 0) {
                        start = start.plusMinutes(15);
                    } else if (offset < 0) {
                        start = start.minusMinutes(15);
                    }
                    break;
                case HOURLY:
                    if (offset > 0) {
                        start = start.plusHours(1);
                    } else if (offset < 0) {
                        start = start.minusHours(1);
                    }
                    break;
                case DAILY:
                    if (offset > 0) {
                        start = start.plusDays(1);
                    } else if (offset < 0) {
                        start = start.minusDays(1);
                    }
                    break;
                case WEEKLY:
                    if (offset > 0) {
                        start = start.plusWeeks(1);
                    } else if (offset < 0) {
                        start = start.minusWeeks(1);
                    }
                    break;
                case MONTHLY:
                    if (offset > 0) {
                        start = start.plusMonths(1);
                    } else if (offset < 0) {
                        start = start.minusMonths(1);
                    }
                    break;
                case QUARTERLY:
                    if (offset > 0) {
                        start = start.plusMonths(3);
                    } else if (offset < 0) {
                        start = start.minusMonths(3);
                    }
                    break;
                case YEARLY:
                    if (offset > 0) {
                        start = start.plusYears(1);
                    } else if (offset < 0) {
                        start = start.minusYears(1);
                    }
                    break;
            }
        }
        return start;
    }

    public DateTime getNextRun() {
        AggregationPeriod aggregationPeriod = getReferencePeriod();
        Long referencePeriodCount = getReferencePeriodCount();
        DateTime lastRun = getLastRun(getMathDataObject());
        for (int i = 0; i < referencePeriodCount; i++) {
            switch (aggregationPeriod) {
                case NONE:
                    break;
                case QUARTER_HOURLY:
                    lastRun = lastRun.plusMinutes(15);
                    break;
                case HOURLY:
                    lastRun = lastRun.plusHours(1);
                    break;
                case DAILY:
                    lastRun = lastRun.plusDays(1);
                    break;
                case WEEKLY:
                    lastRun = lastRun.plusWeeks(1);
                    break;
                case MONTHLY:
                    lastRun = lastRun.plusMonths(1);
                    break;
                case QUARTERLY:
                    lastRun = lastRun.plusMonths(3);
                    break;
                case YEARLY:
                    lastRun = lastRun.plusYears(1);
                    break;
            }
        }
        return lastRun;
    }

    private DateTimeZone getTimeZone(JEVisObject object) {
        DateTimeZone zone = DateTimeZone.UTC;

        JEVisAttribute timeZoneAttribute = null;
        try {
            timeZoneAttribute = object.getAttribute("Timezone");
            if (timeZoneAttribute != null) {
                JEVisSample lastTimeZoneSample = timeZoneAttribute.getLatestSample();
                if (lastTimeZoneSample != null) {
                    zone = DateTimeZone.forID(lastTimeZoneSample.getValueAsString());
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        return zone;
    }

    private DateTime getLastRun(JEVisObject object) {
        DateTime dateTime = new DateTime(2001, 1, 1, 0, 0, 0).withZone(getTimeZone(object));

        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                JEVisSample lastSample = lastRunAttribute.getLatestSample();
                if (lastSample != null) {
                    dateTime = new DateTime(lastSample.getValueAsString());
                }
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: ", e);
        }

        return dateTime;
    }


    public void finishCurrentRun(JEVisObject object) {

        DateTime nextRun = getNextRun();
        try {
            JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
            if (lastRunAttribute != null) {
                JEVisSample newSample = lastRunAttribute.buildSample(DateTime.now(), nextRun);
                newSample.commit();
            }

        } catch (JEVisException e) {
            logger.error("Could not get data source last run time: ", e);
        }
    }

    public enum AttributeName {
        VALUE("Value"),
        ENABLED("Enabled"),
        MANIPULATION("Manipulation"),
        FORMULA("Formula"),
        BEGINNING("Beginning"),
        ENDING("Ending"),
        REFERENCE_PERIOD("Reference Period"),
        REFERENCE_PERIOD_COUNT("Reference Period Count"),
        PERIOD_OFFSET("Period Offset"),
        FILL_PERIOD("Fill Period");

        private final String attributeName;

        AttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return attributeName;
        }
    }
}
