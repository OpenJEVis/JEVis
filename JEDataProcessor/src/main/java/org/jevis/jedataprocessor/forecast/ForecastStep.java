package org.jevis.jedataprocessor.forecast;

import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.util.GapsAndLimits;
import org.jevis.jedataprocessor.workflow.ProcessStep;

import java.util.ArrayList;
import java.util.List;

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

            switch (type.toLowerCase()) {
                case JEDataProcessorConstants.GapFillingType.MINIMUM:
                    gal.fillMinimum();
                    break;
                case JEDataProcessorConstants.GapFillingType.MAXIMUM:
                    gal.fillMaximum();
                    break;
                case JEDataProcessorConstants.GapFillingType.MEDIAN:
                    gal.fillMedian();
                    break;
                case JEDataProcessorConstants.GapFillingType.AVERAGE:
                    gal.fillAverage();
                    break;
            }
            gal.clearLists();
        }

    }
}
