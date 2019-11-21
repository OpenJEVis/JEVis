package org.jevis.jedataprocessor.prediction;

import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.JEDataProcessorConstants;
import org.jevis.commons.dataprocessing.PredictedDataObject;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.util.GapsAndLimits;
import org.jevis.jedataprocessor.workflow.ProcessStep;

import java.util.ArrayList;
import java.util.List;

public class PredictionStep implements ProcessStep {
    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        PredictedDataObject predictedDataObject = resourceManager.getPredictedDataObject();
        List<CleanInterval> intervals = resourceManager.getIntervals();
        List<JEVisSample> sampleCache = predictedDataObject.getInputAttribute().getAllSamples();

        if (predictedDataObject.getTypeAttribute().hasSample()) {
            String type = predictedDataObject.getTypeAttribute().getLatestSample().getValueAsString();
            JsonGapFillingConfig c = predictedDataObject.getJsonGapFillingConfig();

            GapsAndLimits gal = new GapsAndLimits(intervals, GapsAndLimits.GapsAndLimitsType.PREDICTION_TYPE,
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
