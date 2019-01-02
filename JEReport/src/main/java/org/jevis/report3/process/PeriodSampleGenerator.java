/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.dataprocessing.function.AggregatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.commons.dataprocessing.function.MathFunction;
import org.jevis.commons.dataprocessing.function.NullFunction;
import org.jevis.report3.data.attribute.AttributeConfiguration;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName;
import org.jevis.report3.data.attribute.ReportAttributeConfiguration;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author broder
 */
public class PeriodSampleGenerator implements SampleGenerator {

    boolean isValid = false;
    private Interval interval;
    private static final Logger logger = LogManager.getLogger(PeriodSampleGenerator.class);

    public PeriodSampleGenerator(DateTime from, DateTime until) {
        interval = new Interval(from, until);
    }

    public PeriodSampleGenerator(Interval interval) {
        this.interval = interval;
    }

    @Override
    public Map<String, Object> work(ReportLinkProperty linkData, ReportAttributeProperty attributeData, ReportProperty property) {
        //get the configuration (aggregation value and period value)
        AttributeConfiguration periodConfiguration = attributeData.getAttributeConfiguration(ReportConfigurationName.Period);
        JEVisObject dataObject = linkData.getDataObject();
        JEVisAttribute attribute = null;
        try {
            attribute = dataObject.getAttribute(attributeData.getAttributeName());
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        List<JEVisSample> samples = generateSamples(attribute, interval);
        logger.debug("Generated " + samples.size() + " samples.");

        List<JEVisSample> aggregatedSamples = getAggregatedSamples(periodConfiguration, linkData, attributeData, samples);
        logger.debug("Generated " + aggregatedSamples.size() + " aggregated samples.");

        Map<String, Object> sampleMap = ProcessHelper.getAttributeSamples(aggregatedSamples, attribute, property.getTimeZone());
        logger.debug("Created sample map with " + sampleMap.size() + " entries.");

        return sampleMap;
    }

    private List<JEVisSample> generateSamples(JEVisAttribute attribute, Interval interval) {
        //calc the sample list
        return attribute.getSamples(interval.getStart(), interval.getEnd());
    }

    private List<JEVisSample> getAggregatedSamples(AttributeConfiguration periodConfiguration, ReportLinkProperty linkData, ReportAttributeProperty attributeData, List<JEVisSample> samples) {
        //aggregate
        String modeName = null;
        try {
            modeName = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION).getLatestSample().getValueAsString();
            logger.info("Mode name: " + modeName);
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        if (modeName == null) {
            return new ArrayList<>();
        }

        BasicProcess aggregate = new BasicProcess();
        try {
            aggregate.setJEVisDataSource(linkData.getDataObject().getDataSource());
        } catch (JEVisException e) {
            logger.error(e);
        }

        ManipulationMode mode = ManipulationMode.get(modeName.toUpperCase());
        switch (mode) {
            case MIN:
                aggregate.setFunction(new MathFunction(mode.name().toLowerCase()));
                aggregate.setID(ManipulationMode.MIN.toString());
                break;
            case MAX:
                aggregate.setFunction(new MathFunction(mode.name().toLowerCase()));
                aggregate.setID(ManipulationMode.MAX.toString());
                break;
            case MEDIAN:
                aggregate.setFunction(new MathFunction(mode.name().toLowerCase()));
                aggregate.setID(ManipulationMode.MEDIAN.toString());
                break;
            case AVERAGE:
                aggregate.setFunction(new MathFunction(mode.name().toLowerCase()));
                aggregate.setID(ManipulationMode.AVERAGE.toString());
                break;
            case TOTAL:
                aggregate.setFunction(new AggregatorFunction());
                aggregate.setID(ManipulationMode.TOTAL.toString());
                break;
            default:
                aggregate.setFunction(new NullFunction());
                aggregate.setID("Null");
                break;
        }

        AggregationPeriod period = AggregationPeriod.get(modeName.toUpperCase());
        switch (period) {
            case DAILY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                break;
            case HOURLY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                break;
            case WEEKLY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                break;
            case MONTHLY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                break;
            case QUARTERLY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(3).toString()));
                break;
            case YEARLY:
                aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                break;
            case NONE:
                try {
                    Period p = linkData.getDataObject().getAttribute(attributeData.getAttributeName()).getInputSampleRate();
                    aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, p.toString()));
                } catch (JEVisException e) {
                    try {
                        Period p = linkData.getDataObject().getAttribute(attributeData.getAttributeName()).getDisplaySampleRate();
                        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, p.toString()));
                    } catch (JEVisException e1) {
                        logger.fatal(e);
                        return samples;
                    }
                }
                break;
            default:
                break;
        }

        BasicProcess input = new BasicProcess();
        try {
            input.setJEVisDataSource(linkData.getDataObject().getDataSource());
        } catch (JEVisException e) {
            logger.error(e);
        }
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(samples));
        input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, attributeData.getAttributeName()));
        input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, linkData.getDataObject().getID() + ""));
        aggregate.setSubProcesses(Collections.singletonList(input));

        return aggregate.getResult();
    }
}
