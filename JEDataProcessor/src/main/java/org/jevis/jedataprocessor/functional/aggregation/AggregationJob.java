/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.functional.aggregation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jedataprocessor.functional.aggregation.Aggregator.AggregationModus;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormat;

import java.util.List;

/**
 * @author broder
 */
public class AggregationJob {

    private static final Logger logger = LogManager.getLogger(AggregationJob.class);

    private final AggregationModus aggregationModus;
    private final List<JEVisSample> cleanSamples;
    private final JEVisObject cleanData;
    private final Period period;

    AggregationJob(AggregationModus aggregationModus, Period period, List<JEVisSample> cleanSamples, JEVisObject cleanData) {
        this.period = period;
        this.aggregationModus = aggregationModus;
        this.cleanSamples = cleanSamples;
        this.cleanData = cleanData;
    }

    public static AggregationJob createAggregationJob(JEVisObject aggregationObject, JEVisObject cleanData, DateTime startDate, AggregationModus aggregationModus) throws Exception {

        if (startDate == null) {
            startDate = new DateTime(0);
        }
        logger.debug("Starttime: {}", startDate.toString(DateTimeFormat.fullDateTime()));
        Period inputSampleRate = aggregationObject.getAttribute("Value").getInputSampleRate();
        logger.debug("inputSampleRate: {}", inputSampleRate.toString(PeriodFormat.getDefault()));
        if (inputSampleRate.equals(Period.ZERO)) {
            throw new RuntimeException("Cant calculate with a zero period");
        }
        List<JEVisSample> cleanSamples = cleanData.getAttribute("Value").getSamples(startDate, new DateTime());
        logger.debug("{} clean samples found for calculation", cleanSamples.size());
        return new AggregationJob(aggregationModus, inputSampleRate, cleanSamples, cleanData);

    }

    public static AggregationJob createAggregationJob(JEVisObject aggregationObject, JEVisObject cleanData, AggregationModus aggregationModus) throws Exception {
        SampleHandler sampleHandler = new SampleHandler();
        DateTime lastSampleTimestamp = sampleHandler.getTimeStampFromLastSample(aggregationObject, "Value");
        return createAggregationJob(aggregationObject, cleanData, lastSampleTimestamp, aggregationModus);
    }

    public AggregationModus getAggregationModus() {
        return aggregationModus;
    }

    public List<JEVisSample> getCleanSamples() {
        return cleanSamples;
    }

    public JEVisObject getCleanData() {
        return cleanData;
    }

    public Period getPeriod() {
        return period;
    }

}
