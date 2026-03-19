package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.task.LogTaskManager;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Final pipeline step: persists computed samples to the JEVis data store.
 *
 * <p>The step collects all non-null, non-NaN, non-Infinite result values from
 * the {@link CleanInterval} list held in the {@link ResourceManager} and
 * writes them in chunks of up to 30 000 samples per API call
 * ({@link #insertSamples}) to keep individual HTTP payloads manageable.</p>
 *
 * <p><strong>Deduplication strategy:</strong> for CLEAN and FORECAST objects
 * the attribute may already contain samples in the time range being processed
 * (e.g. from a previous run).  Instead of deleting each conflicting sample
 * individually (N API calls), this step issues a single
 * {@link JEVisAttribute#deleteSamplesBetween} call covering the entire result
 * range before the insert loop, reducing N round-trips to one.</p>
 *
 * <p>For FORECAST objects whose "keep values" flag is set the existing samples
 * are deliberately preserved and only new ones are appended.</p>
 */
public class ImportStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(ImportStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        DateTime benchStart = new DateTime();
        importIntoJEVis(resourceManager);
        logger.debug("{} finished in {}", this.getClass().getSimpleName(), new Period(benchStart, new DateTime()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale())));
    }

    private void importIntoJEVis(ResourceManager resourceManager) throws Exception {
        JEVisObject cleanObject = null;
        Integer periodOffset = 0;
        if (resourceManager.getCleanDataObject() != null) {
            CleanDataObject cleanAttr = resourceManager.getCleanDataObject();
            cleanObject = cleanAttr.getCleanObject();
            periodOffset = cleanAttr.getPeriodOffset();

            for (CleanInterval currentInt : resourceManager.getIntervals()) {
                VirtualSample sample = currentInt.getResult();
                DateTime offsetTs = sample.getTimestamp();

                if (!currentInt.getOutputPeriod().equals(Period.ZERO) && periodOffset > 0) {

                    offsetTs = PeriodHelper.getNextPeriod(offsetTs, currentInt.getOutputPeriod(), periodOffset, true, resourceManager.getTimeZone());

                    sample.setTimeStamp(offsetTs);
                }
            }
        } else if (resourceManager.getForecastDataObject() != null) {
            ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
            cleanObject = forecastDataObject.getForecastDataObject();
            JEVisAttribute valueAttribute = forecastDataObject.getValueAttribute();
            JEVisAttribute keepValuesAttribute = forecastDataObject.getKeepValuesAttribute();
            boolean keepValues = false;

            if (keepValuesAttribute != null && keepValuesAttribute.hasSample()) {
                JEVisSample latestSample = keepValuesAttribute.getLatestSample();
                if (latestSample != null) {
                    keepValues = latestSample.getValueAsBoolean();
                }
            }

            if (!resourceManager.getIntervals().isEmpty() && !keepValues) {
                valueAttribute.deleteAllSample();
            }
        } else if (resourceManager.getMathDataObject() != null) {
            MathDataObject mathDataObject = resourceManager.getMathDataObject();
            cleanObject = mathDataObject.getMathDataObject();
        }

        JEVisAttribute attribute = cleanObject.getAttribute(CleanDataObject.VALUE_ATTRIBUTE_NAME);

        if (attribute == null) {
            return;
        }

        boolean hasSamples = attribute.hasSample();
        Map<DateTime, JEVisSample> listOldSamples = new HashMap<>();
        DateTime firstDateTimeOfResults = null;
        DateTime lastDateTimeOfResults = null;

        boolean monthPeriods = false;
        if (resourceManager.isClean()) {
            for (CleanInterval interval : resourceManager.getIntervals()) {
                if (interval.getOutputPeriod().equals(Period.months(1))) {
                    monthPeriods = true;
                    break;
                }
            }
        }

        if ((resourceManager.isClean() && !resourceManager.getIntervals().isEmpty() && !monthPeriods) || resourceManager.isForecast() && !resourceManager.getIntervals().isEmpty()) {
            firstDateTimeOfResults = resourceManager.getIntervals().get(0).getInterval().getStart();
            lastDateTimeOfResults = resourceManager.getIntervals().get(resourceManager.getIntervals().size() - 1).getInterval().getEnd();

            for (JEVisSample jeVisSample : attribute.getSamples(firstDateTimeOfResults, lastDateTimeOfResults)) {
                listOldSamples.put(jeVisSample.getTimestamp(), jeVisSample);
            }
        }

        // Batch-delete existing samples in the result range with a single API call
        // instead of issuing one deleteSamplesBetween per sample inside the loop.
        if (hasSamples && (resourceManager.isClean() || resourceManager.isForecast()) && !listOldSamples.isEmpty()) {
            attribute.deleteSamplesBetween(firstDateTimeOfResults, lastDateTimeOfResults);
        }

        List<JEVisSample> cleanSamples = new ArrayList<>();
        for (CleanInterval curInterval : resourceManager.getIntervals()) {
            JEVisSample sample = curInterval.getResult();

            Double value = sample.getValueAsDouble();
            if (value == null || value.isNaN() || value.isInfinite()) {
                continue;
            }
            DateTime date = sample.getTimestamp();
            if (date != null) {
                DateTime timestamp = sample.getTimestamp();
                JEVisSample sampleSql = attribute.buildSample(timestamp, value, sample.getNote());
                cleanSamples.add(sampleSql);
            }
        }
        if (!cleanSamples.isEmpty()) {
            logger.info("[{}] Start import of new Samples: {}", resourceManager.getID(), cleanSamples.size());
            insertSamples(attribute, cleanSamples);
            logger.info("[{}] Import finished for samples: {}", resourceManager.getID(), cleanSamples.size());
        } else {
            logger.info("[{}] No new Samples.", resourceManager.getID());
        }
        LogTaskManager.getInstance().getTask(resourceManager.getID()).addStep("S. Import", cleanSamples.size() + "");
    }

    /**
     * Inserts {@code samples} into {@code attribute} in chunks of at most
     * 30 000 entries per call to keep individual HTTP payloads manageable.
     *
     * @param attribute the target JEVis attribute
     * @param samples   the fully built samples to persist; must not be empty
     * @throws JEVisException if the underlying data-source call fails
     */
    private void insertSamples(JEVisAttribute attribute, List<JEVisSample> samples) throws JEVisException {
        int perChunk = 30000;
        for (int i = 0; i < samples.size(); i += perChunk) {
            if ((i + perChunk) < samples.size()) {
                List<JEVisSample> chunk = samples.subList(i, i + perChunk);
                if (!chunk.isEmpty()) {
                    attribute.addSamples(chunk);
                }
            } else {
                List<JEVisSample> chunk = samples.subList(i, samples.size());
                if (!chunk.isEmpty()) {
                    attribute.addSamples(chunk);
                }
                break;
            }
        }
    }

}
