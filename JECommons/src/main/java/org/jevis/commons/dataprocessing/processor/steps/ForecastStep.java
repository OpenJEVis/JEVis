package org.jevis.commons.dataprocessing.processor.steps;

import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.processor.GapsAndLimits;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.json.JsonGapFillingConfig;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.constants.GapFillingType.parse;

public class ForecastStep implements ProcessStep {
    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        ForecastDataObject forecastDataObject = resourceManager.getForecastDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> sampleCache = forecastDataObject.getSampleCache();

        if (forecastDataObject.getTypeAttribute().hasSample()) {
            String type = forecastDataObject.getTypeAttribute().getLatestSample().getValueAsString();
            JsonGapFillingConfig c = forecastDataObject.getJsonGapFillingConfig();

            GapsAndLimits gal = new GapsAndLimits(intervals, GapsAndLimits.GapsAndLimitsType.FORECAST_TYPE,
                    c, new ArrayList<>(), new ArrayList<>(), sampleCache);

            switch (parse(type)) {
                case MINIMUM:
                    gal.fillMinimum();
                    break;
                case MAXIMUM:
                    gal.fillMaximum();
                    break;
                case MEDIAN:
                    gal.fillMedian();
                    break;
                case AVERAGE:
                    gal.fillAverage();
                    break;
            }
            gal.clearLists();
        }

    }
}
