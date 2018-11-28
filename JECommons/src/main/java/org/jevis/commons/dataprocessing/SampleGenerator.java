package org.jevis.commons.dataprocessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.function.AggregatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.commons.dataprocessing.function.MathFunction;
import org.jevis.commons.dataprocessing.function.NullFunction;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Collections;
import java.util.List;

public class SampleGenerator {
    private static final Logger logger = LogManager.getLogger(SampleGenerator.class);
    private Interval interval;
    private JEVisDataSource ds;
    private JEVisAttribute attribute;
    private JEVisObject object;
    private AggregationMode aggregationMode;
    private AggregationPeriod aggregationPeriod;

    public SampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, DateTime from, DateTime until, AggregationMode aggregationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.aggregationMode = aggregationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = new Interval(from, until);
    }

    public SampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, Interval interval, AggregationMode aggregationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.aggregationMode = aggregationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = interval;
    }

    public List<JEVisSample> generateSamples() {
        //calc the sample list
//        List<JEVisSample> samples = attribute.getSamples(new DateTime(2010, 1, 1, 1, 1, 1),
//                new DateTime(3000, 1, 1, 1, 1, 1, 1));
        List<JEVisSample> samples = attribute.getSamples(interval.getStart(), interval.getEnd());
        return samples;
    }

    public List<JEVisSample> getAggregatedSamples(List<JEVisSample> samples) {
        //aggregate


        BasicProcess aggregate = new BasicProcess();
        aggregate.setJEVisDataSource(ds);

        switch (aggregationMode) {
            case MIN:
                aggregate.setFunction(new MathFunction(aggregationMode.name().toLowerCase()));
                aggregate.setID(AggregationMode.MIN.toString());
                break;
            case MAX:
                aggregate.setFunction(new MathFunction(aggregationMode.name().toLowerCase()));
                aggregate.setID(AggregationMode.MAX.toString());
                break;
            case MEDIAN:
                aggregate.setFunction(new MathFunction(aggregationMode.name().toLowerCase()));
                aggregate.setID(AggregationMode.MEDIAN.toString());
                break;
            case AVERAGE:
                aggregate.setFunction(new MathFunction(aggregationMode.name().toLowerCase()));
                aggregate.setID(AggregationMode.AVERAGE.toString());
                break;
            case TOTAL:
                aggregate.setFunction(new AggregatorFunction());
                aggregate.setID(AggregationMode.TOTAL.toString());
                break;
            case RUNNINGMEAN:
                aggregate.setFunction(new MathFunction(aggregationMode.name().toLowerCase()));
                aggregate.setID(AggregationMode.RUNNINGMEAN.toString());
                break;
            case NONE:
                aggregate.setFunction(new NullFunction());
                aggregate.setID("Null");
                break;
            default:
                aggregate.setFunction(new NullFunction());
                aggregate.setID("Null");
                break;
        }

        switch (aggregationPeriod) {
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
                break;
            default:
                break;
        }

        BasicProcess input = new BasicProcess();
        input.setJEVisDataSource(ds);
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(samples));
        input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, attribute.getName()));
        input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, object.getID().toString()));
        aggregate.setSubProcesses(Collections.singletonList(input));

        return aggregate.getResult();
    }
}
