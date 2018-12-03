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
    private ManipulationMode manipulationMode;
    private AggregationPeriod aggregationPeriod;

    public SampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, DateTime from, DateTime until, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.manipulationMode = manipulationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = new Interval(from, until);
    }

    public SampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, Interval interval, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.manipulationMode = manipulationMode;
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

        BasicProcess input = new BasicProcess();
        input.setJEVisDataSource(ds);
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(samples));
        input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, attribute.getName()));
        input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, object.getID().toString()));
        input.getOptions().add(new BasicProcessOption(ProcessOptions.OFFSET, ""));
        aggregate.setSubProcesses(Collections.singletonList(input));

        switch (manipulationMode) {
            case MIN:
                aggregate.setFunction(new MathFunction(ManipulationMode.MIN));
                aggregate.setID(ManipulationMode.MIN.toString());
                break;
            case MAX:
                aggregate.setFunction(new MathFunction(ManipulationMode.MAX));
                aggregate.setID(ManipulationMode.MAX.toString());
                break;
            case MEDIAN:
                aggregate.setFunction(new MathFunction(ManipulationMode.MEDIAN));
                aggregate.setID(ManipulationMode.MEDIAN.toString());
                break;
            case AVERAGE:
                aggregate.setFunction(new MathFunction(ManipulationMode.AVERAGE));
                aggregate.setID(ManipulationMode.AVERAGE.toString());
                break;
            case TOTAL:
                aggregate.setFunction(new AggregatorFunction());
                aggregate.setID(ManipulationMode.TOTAL.toString());
                break;
            case RUNNING_MEAN:
                aggregate.setFunction(new MathFunction(ManipulationMode.RUNNING_MEAN));
                aggregate.setID(ManipulationMode.RUNNING_MEAN.toString());
                break;
            case CENTRIC_RUNNING_MEAN:
                aggregate.setFunction(new MathFunction(ManipulationMode.CENTRIC_RUNNING_MEAN));
                aggregate.setID(ManipulationMode.CENTRIC_RUNNING_MEAN.toString());
                break;
            case SORTED_MIN:
                aggregate.setFunction(new MathFunction(ManipulationMode.SORTED_MIN));
                aggregate.setID(ManipulationMode.SORTED_MIN.toString());
                break;
            case SORTED_MAX:
                aggregate.setFunction(new MathFunction(ManipulationMode.SORTED_MAX));
                aggregate.setID(ManipulationMode.SORTED_MAX.toString());
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

        return aggregate.getResult();
    }
}
