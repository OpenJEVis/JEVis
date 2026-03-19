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
import org.joda.time.DateTimeZone;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared-state carrier for a single data-processing pipeline run.
 *
 * <p>One {@code ResourceManager} is created by {@link ProcessManager} at the
 * start of a run and passed to every {@link ProcessStep} in sequence.  Each
 * step reads the state it needs and writes back its results so that subsequent
 * steps can consume them.</p>
 *
 * <p>Between batches (when processing large datasets in chunks) the
 * {@link ProcessManager} calls the various setters with {@code null} to clear
 * transient state before the next batch begins.</p>
 */
public class ResourceManager {

    private static final Logger logger = LogManager.getLogger(ResourceManager.class);
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private List<CleanInterval> intervals = new ArrayList<>();
    private CleanDataObject cleanDataObject;
    private ForecastDataObject forecastDataObject;
    private MathDataObject mathDataObject;
    private List<JEVisSample> rawSamplesDown;
    private Map<DateTime, JEVisSample> notesMap;
    private Map<DateTime, JEVisSample> userDataMap;
    private List<JEVisSample> sampleCache = new ArrayList<>();
    private List<CleanInterval> rawIntervals = new ArrayList<>();
    /**
     * Determines which pipeline variant is active for this run.
     */
    private ProcessingType processingType = ProcessingType.CLEAN;
    private DateTimeZone timeZone;

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
                                    date = getCleanDataObject().getValueAttribute().getTimestampOfFirstSample();
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

    /** @return {@code true} if the active pipeline is {@link ProcessingType#CLEAN}. */
    public Boolean isClean() {
        return processingType == ProcessingType.CLEAN;
    }

    /** @return {@code true} if the active pipeline is {@link ProcessingType#FORECAST}. */
    public Boolean isForecast() {
        return processingType == ProcessingType.FORECAST;
    }

    /**
     * @return {@code true} if the active pipeline is {@link ProcessingType#MATH}.
     */
    public Boolean isMath() {
        return processingType == ProcessingType.MATH;
    }

    /**
     * Sets the active pipeline type for this run.
     * Replaces the former {@code setClean()}/{@code setForecast()} pair.
     *
     * @param processingType the pipeline type determined by {@link ProcessManager}
     */
    public void setProcessingType(ProcessingType processingType) {
        this.processingType = processingType;
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(DateTimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
