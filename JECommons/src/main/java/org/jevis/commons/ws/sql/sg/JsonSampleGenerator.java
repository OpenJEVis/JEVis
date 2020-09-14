package org.jevis.commons.ws.sql.sg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.dataprocessing.function.AggregatorFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.commons.dataprocessing.function.MathFunction;
import org.jevis.commons.dataprocessing.function.NullFunction;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Collections;
import java.util.List;

public class JsonSampleGenerator {
    private static final Logger logger = LogManager.getLogger(JsonSampleGenerator.class);
    private final Interval interval;
    private final SQLDataSource ds;
    private final JsonAttribute attribute;
    private final JsonObject object;
    private final ManipulationMode manipulationMode;
    private final AggregationPeriod aggregationPeriod;
    private Boolean customWorkday = true;

    public JsonSampleGenerator(SQLDataSource ds, JsonObject object, JsonAttribute attribute, DateTime from, DateTime until, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.manipulationMode = manipulationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = new Interval(from, until);
    }

    public JsonSampleGenerator(SQLDataSource ds, JsonObject object, JsonAttribute attribute, DateTime from, DateTime until, Boolean customWorkday, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.manipulationMode = manipulationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = new Interval(from, until);
        this.customWorkday = customWorkday;
    }

    public List<JsonSample> getAggregatedSamples() {

        BasicProcess basicProcess = new BasicProcess();
        basicProcess.setSQLDataSource(ds);
        try {
            basicProcess.setJsonObject(ds.getObject(attribute.getObjectID()));
            basicProcess.setJsonAttribute(attribute);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        BasicProcess input = new BasicProcess();
        input.setSQLDataSource(ds);
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction());
        input.getOptions().add(new BasicProcessOption(InputFunction.ATTRIBUTE_ID, attribute.getType()));
        input.getOptions().add(new BasicProcessOption(InputFunction.OBJECT_ID, String.valueOf(object.getId())));
        input.getOptions().add(new BasicProcessOption(ProcessOptions.OFFSET, ""));
        input.getOptions().add(new BasicProcessOption(ProcessOptions.CUSTOM, customWorkday.toString()));
        input.getOptions().add(new BasicProcessOption(ProcessOptions.TS_START, interval.getStart().toString()));
        input.getOptions().add(new BasicProcessOption(ProcessOptions.TS_END, interval.getEnd().toString()));
        basicProcess.setSubProcesses(Collections.singletonList(input));
        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.CUSTOM, customWorkday.toString()));

        switch (manipulationMode) {
            case MIN:
                basicProcess.setFunction(new MathFunction(ManipulationMode.MIN, aggregationPeriod));
                basicProcess.setID(ManipulationMode.MIN.toString());
                break;
            case MAX:
                basicProcess.setFunction(new MathFunction(ManipulationMode.MAX, aggregationPeriod));
                basicProcess.setID(ManipulationMode.MAX.toString());
                break;
            case MEDIAN:
                basicProcess.setFunction(new MathFunction(ManipulationMode.MEDIAN, aggregationPeriod));
                basicProcess.setID(ManipulationMode.MEDIAN.toString());
                break;
            case AVERAGE:
                basicProcess.setFunction(new MathFunction(ManipulationMode.AVERAGE, aggregationPeriod));
                basicProcess.setID(ManipulationMode.AVERAGE.toString());
                break;
            case RUNNING_MEAN:
                basicProcess.setFunction(new MathFunction(ManipulationMode.RUNNING_MEAN, aggregationPeriod));
                basicProcess.setID(ManipulationMode.RUNNING_MEAN.toString());
                break;
            case CENTRIC_RUNNING_MEAN:
                basicProcess.setFunction(new MathFunction(ManipulationMode.CENTRIC_RUNNING_MEAN, aggregationPeriod));
                basicProcess.setID(ManipulationMode.CENTRIC_RUNNING_MEAN.toString());
                break;
            case SORTED_MIN:
                basicProcess.setFunction(new MathFunction(ManipulationMode.SORTED_MIN, aggregationPeriod));
                basicProcess.setID(ManipulationMode.SORTED_MIN.toString());
                break;
            case SORTED_MAX:
                basicProcess.setFunction(new MathFunction(ManipulationMode.SORTED_MAX, aggregationPeriod));
                basicProcess.setID(ManipulationMode.SORTED_MAX.toString());
                break;
            case CUMULATE:
                basicProcess.setFunction(new MathFunction(ManipulationMode.CUMULATE, aggregationPeriod));
                basicProcess.setID(ManipulationMode.CUMULATE.toString());
                break;
            default:
                basicProcess.setFunction(new NullFunction(manipulationMode, aggregationPeriod));
                basicProcess.setID("Null");
                break;
        }


        BasicProcess aggregationProcess = new BasicProcess();
        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.CUSTOM, customWorkday.toString()));
        if (aggregationPeriod == AggregationPeriod.NONE) {
            return basicProcess.getJsonResult();
        } else {
            if (manipulationMode == ManipulationMode.NONE) {
                aggregationProcess.setSQLDataSource(ds);
                try {
                    aggregationProcess.setJsonObject(ds.getObject(attribute.getObjectID()));
                    aggregationProcess.setJsonAttribute(attribute);
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                switch (aggregationPeriod) {
                    case QUARTER_HOURLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.minutes(15).toString()));
                        break;
                    case DAILY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                        break;
                    case HOURLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                        break;
                    case WEEKLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                        break;
                    case MONTHLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                        break;
                    case QUARTERLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(3).toString()));
                        break;
                    case YEARLY:
                        aggregationProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                        break;
                    default:
                        return basicProcess.getJsonResult();
                }
                aggregationProcess.setFunction(new AggregatorFunction());
                aggregationProcess.setID("Aggregation");

                aggregationProcess.setSubProcesses(Collections.singletonList(basicProcess));
                return aggregationProcess.getJsonResult();
            } else {
                switch (aggregationPeriod) {
                    case QUARTER_HOURLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.minutes(15).toString()));
                        break;
                    case DAILY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.days(1).toString()));
                        break;
                    case HOURLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.hours(1).toString()));
                        break;
                    case WEEKLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.weeks(1).toString()));
                        break;
                    case MONTHLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(1).toString()));
                        break;
                    case QUARTERLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.months(3).toString()));
                        break;
                    case YEARLY:
                        basicProcess.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, Period.years(1).toString()));
                        break;
                    default:
                        return basicProcess.getJsonResult();
                }
                return basicProcess.getJsonResult();
            }
        }
    }
}
