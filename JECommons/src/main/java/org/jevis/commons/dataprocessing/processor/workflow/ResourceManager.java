/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.workflow;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.GapFillingReferencePeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author gschutz
 */
public class ResourceManager {

    private static final Logger logger = LogManager.getLogger(ResourceManager.class);
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    public List<CleanInterval> intervals = new ArrayList<>();
    private CleanDataObject cleanDataObject;
    private ForecastDataObject forecastDataObject;
    private MathDataObject mathDataObject;
    private List<JEVisSample> rawSamplesDown;
    private Map<DateTime, JEVisSample> notesMap;
    private Map<DateTime, JEVisSample> userDataMap;
    private List<JEVisSample> sampleCache = new ArrayList<>();
    private List<CleanInterval> rawIntervals = new ArrayList<>();
    private Boolean isClean = true;

    public ResourceManager() {
        numberFormat.setMinimumFractionDigits(4);
        numberFormat.setMaximumFractionDigits(4);
    }

    public List<CleanInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<CleanInterval> intervals) {
        this.intervals = intervals;
    }

    public CleanDataObject getCleanDataObject() {
        return cleanDataObject;
    }

    public void setCleanDataObject(CleanDataObject cleanDataObject) {
        this.cleanDataObject = cleanDataObject;
    }

    public ForecastDataObject getForecastDataObject() {
        return forecastDataObject;
    }

    public void setForecastDataObject(ForecastDataObject forecastDataObject) {
        this.forecastDataObject = forecastDataObject;
    }

    public MathDataObject getMathDataObject() {
        return mathDataObject;
    }

    public void setMathDataObject(MathDataObject mathDataObject) {
        this.mathDataObject = mathDataObject;
    }

    public List<JEVisSample> getRawSamplesDown() {
        return rawSamplesDown;
    }

    public void setRawSamplesDown(List<JEVisSample> rawSamplesDown) {
        this.rawSamplesDown = rawSamplesDown;
    }

    public Map<DateTime, JEVisSample> getNotesMap() {
        return notesMap;
    }

    public void setNotesMap(Map<DateTime, JEVisSample> notesMap) {
        this.notesMap = notesMap;
    }

    public Map<DateTime, JEVisSample> getUserDataMap() {
        return userDataMap;
    }

    public void setUserDataMap(Map<DateTime, JEVisSample> userDataMap) {
        this.userDataMap = userDataMap;
    }

    public List<JEVisSample> getSampleCache() {
        if (sampleCache == null || sampleCache.isEmpty()) {
            try {
                DateTime date = getCleanDataObject().getFirstDate();
                DateTime minDateForCache = date;
                DateTime lastDateForCache = date;

                for (JsonGapFillingConfig jsonGapFillingConfig : getCleanDataObject().getGapFillingConfig()) {
                    try {
                        String referenceperiod = jsonGapFillingConfig.getReferenceperiod();
                        String referenceperiodcount = jsonGapFillingConfig.getReferenceperiodcount();

                        if (referenceperiod != null && referenceperiodcount != null) {
                            Integer c = Integer.parseInt(referenceperiodcount);
                            GapFillingReferencePeriod referencePeriod = GapFillingReferencePeriod.parse(referenceperiod);

                            switch (referencePeriod) {
                                case DAY:
                                    date = date.minusDays(c);
                                    break;
                                case WEEK:
                                    date = date.minusWeeks(c);
                                    break;
                                case MONTH:
                                    date = date.minusMonths(c);
                                    break;
                                case YEAR:
                                    date = date.minusYears(c);
                                    break;
                                case ALL:
                                    date = getCleanDataObject().getValueAttribute().getTimestampFromFirstSample();
                                    break;
                                case NONE:
                                    break;
                            }
                        }

                        if (date.isBefore(minDateForCache)) {
                            minDateForCache = date;
                        }
                    } catch (Exception e) {
                        logger.error("Error in gap filling config from object {}:{}", getCleanDataObject().getCleanObject().getName(), getCleanDataObject().getCleanObject().getID(), e);
                    }
                }

                sampleCache = getCleanDataObject().getCleanObject().getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME).getSamples(minDateForCache, lastDateForCache);

                if (sampleCache.isEmpty()) {
                    sampleCache = null;
                }
            } catch (Exception e) {
                logger.error("No caching possible: ", e);
            }
        }
        return sampleCache;
    }

    public void setSampleCache(List<JEVisSample> sampleCache) {
        this.sampleCache = sampleCache;
    }

    public Long getID() {
        if (cleanDataObject == null || cleanDataObject.getCleanObject() == null) {
            return -1L;
        }
        return cleanDataObject.getCleanObject().getID();
    }

    public void setRawIntervals(List<CleanInterval> rawIntervals) {
        this.rawIntervals = rawIntervals;
    }

    public Boolean isClean() {
        return isClean;
    }

    public void setClean(Boolean clean) {
        isClean = clean;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }
}
