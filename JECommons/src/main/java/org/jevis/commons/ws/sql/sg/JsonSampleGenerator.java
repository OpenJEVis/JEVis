package org.jevis.commons.ws.sql.sg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.function.AggregationProcessor;
import org.jevis.commons.dataprocessing.function.AggregationTools;
import org.jevis.commons.dataprocessing.function.InputProcessor;
import org.jevis.commons.dataprocessing.function.MathProcessor;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import java.util.List;

public class JsonSampleGenerator {
    private static final Logger logger = LogManager.getLogger(JsonSampleGenerator.class);
    private final Interval interval;
    private final SQLDataSource ds;
    private final JsonAttribute attribute;
    private final JsonObject object;
    private final ManipulationMode manipulationMode;
    private final AggregationPeriod aggregationPeriod;
    private final WorkDays workDays;
    private final Boolean customWorkday;
    private final DateTimeZone timeZone;


    public JsonSampleGenerator(SQLDataSource ds, JsonObject object, JsonAttribute attribute, DateTime from, DateTime until, Boolean customWorkday, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod, DateTimeZone dateTimeZone) {
        this.ds = ds;
        this.object = object;
        this.attribute = attribute;
        this.manipulationMode = manipulationMode;
        this.aggregationPeriod = aggregationPeriod;
        this.interval = new Interval(from, until);
        this.customWorkday = customWorkday;
        this.timeZone = dateTimeZone;

        this.workDays = new WorkDays(ds, object);
        this.workDays.setEnabled(customWorkday);
    }

    public List<JsonSample> getAggregatedSamples() {

        InputProcessor input = new InputProcessor(ds);
        AggregationTools aggregationTools = new AggregationTools(timeZone, workDays, aggregationPeriod);

        Interval inputInterval = aggregationTools.getInterval(workDays, interval.getStart().withZone(timeZone), interval.getEnd().withZone(timeZone), aggregationPeriod);
        List<JsonSample> inputSamples = input.getJsonResult(object, attribute, inputInterval.getStart(), inputInterval.getEnd());

        if (inputSamples.isEmpty()) return inputSamples;

        switch (aggregationPeriod) {
            case DAILY:
            case WEEKLY:
            case MONTHLY:
            case QUARTERLY:
            case YEARLY:
                workDays.setEnabled(true);
                break;
            default:
                workDays.setEnabled(false);
                break;
        }

        if (manipulationMode != ManipulationMode.NONE) {
            MathProcessor math = new MathProcessor(ds, aggregationTools, workDays, manipulationMode, aggregationPeriod);
            return math.getJsonResult(inputSamples, attribute, inputInterval.getStart(), inputInterval.getEnd());

        } else if (aggregationPeriod != AggregationPeriod.NONE) {
            AggregationProcessor aggregation = new AggregationProcessor(ds, aggregationTools, workDays, aggregationPeriod);

            return aggregation.getJsonResult(inputSamples, attribute, inputInterval.getStart(), inputInterval.getEnd());
        } else {
            return inputSamples;
        }
    }

    public Interval getInterval() {
        return interval;
    }

    public SQLDataSource getDs() {
        return ds;
    }

    public JsonAttribute getAttribute() {
        return attribute;
    }

    public JsonObject getObject() {
        return object;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public Boolean getCustomWorkday() {
        return customWorkday;
    }

    public WorkDays getWorkDays() {
        return workDays;
    }
}
