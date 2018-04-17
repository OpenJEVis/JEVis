/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.function.AggregationFunktion;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.report3.ReportLauncher;
import org.jevis.report3.data.attribute.AttributeConfiguration;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName;
import org.jevis.report3.data.attribute.ReportAttributeConfiguration;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 *
 * @author broder
 */
public class PeriodSampleGenerator implements SampleGenerator {

    boolean isValid = false;
    private Interval interval;

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
            Logger.getLogger(PeriodSampleGenerator.class.getName()).log(Level.ERROR, null, ex);
        }

        List<JEVisSample> samples = generateSamples(attribute, interval);

        List<JEVisSample> aggregatedSamples = getAggregatedSamples(periodConfiguration, linkData, attributeData, samples);
        Map<String, Object> sampleMap = ProcessHelper.getAttributeSamples(aggregatedSamples, attribute, property.getTimeZone());

        return sampleMap;
    }

    List<JEVisSample> generateSamples(JEVisAttribute attribute, Interval interval) {
        //calc the sample list
        List<JEVisSample> samples = attribute.getSamples(interval.getStart(), interval.getEnd());
        return samples;
    }

    private List<JEVisSample> getAggregatedSamples(AttributeConfiguration periodConfiguration, ReportLinkProperty linkData, ReportAttributeProperty attributeData, List<JEVisSample> samples) {
        //aggregate
        String modeName = null;
        try {
            modeName = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION).getLatestSample().getValueAsString();
        } catch (JEVisException ex) {
            Logger.getLogger(PeriodSampleGenerator.class.getName()).log(Level.ERROR, null, ex);
        }

        if (modeName == null) {
            return new ArrayList<>();
        }

        BasicProcess aggrigate = new BasicProcess();
        aggrigate.setJEVisDataSource(ReportLauncher.getDataSource());
        aggrigate.setID("Dynamic");

        AggregationModus mode = AggregationModus.get(modeName.toUpperCase());
        switch (mode) {
            case AVERAGE:
                aggrigate.setFunction(new AggregationFunktion(mode.name().toLowerCase()));
                break;
            case TOTAL:
                aggrigate.setFunction(new AggregationFunktion(mode.name().toLowerCase()));
                break;
            default:
                aggrigate.setFunction(new AggregationFunktion(AggregationModus.TOTAL.name().toLowerCase()));
                break;
        }

        AggregationPeriod period = AggregationPeriod.get(modeName.toUpperCase());
        switch (period) {
            case DAILY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                break;
            case HOURLY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                break;
            case WEEKLY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                break;
            case MONTHLY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                break;
            case QUARTERLY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(3).toString()));
                break;
            case YEARLY:
                aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                break;
            default:
                return samples;
        }
        BasicProcess input = new BasicProcess();
        input.setJEVisDataSource(ReportLauncher.getDataSource());
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(samples));
        input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, attributeData.getAttributeName()));
        input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, linkData.getDataObject().getID() + ""));
        aggrigate.setSubProcesses(Arrays.asList(input));
        List<JEVisSample> aggregatedSamples = aggrigate.getResult();
        return aggregatedSamples;
    }

    private enum AggregationPeriod {

        NONE, HOURLY, DAILY, MONTHLY, WEEKLY, QUARTERLY, YEARLY;

        public static AggregationPeriod get(String modusName) {
            String period = modusName.split("_")[0];
            return valueOf(period);
        }
    }

    private enum AggregationModus {

        TOTAL, AVERAGE;

        public static AggregationModus get(String modusName) {
            String[] modusArray = modusName.split("_");
            String modus = TOTAL.name();
            if (modusArray.length == 2) {
                modus = modusArray[1];
            }
            return valueOf(modus);
        }
    }
}
