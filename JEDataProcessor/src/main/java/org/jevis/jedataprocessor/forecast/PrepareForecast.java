package org.jevis.jedataprocessor.forecast;

import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class PrepareForecast implements ProcessStep {
    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();

        Period inputPeriod = forecastDataObject.getInputDataPeriod();
        List<CleanInterval> intervals = new ArrayList<>();

        //TODO bigger than months
        if (inputPeriod.toStandardDuration().getMillis() > 0) {
            DateTime start = forecastDataObject.getStartDate();
            DateTime end = forecastDataObject.getEndDate();

            if (start != null && end != null) {
                while (start.isBefore(end)) {
                    DateTime endOfInterval = start.plus(inputPeriod).minusMillis(1);
                    Interval interval = new Interval(start, endOfInterval);
                    CleanInterval cleanInterval = new CleanInterval(interval, start);
                    intervals.add(cleanInterval);

                    start = start.plus(inputPeriod);
                }
                resourceManager.setIntervals(intervals);
            }
        }
    }
}
