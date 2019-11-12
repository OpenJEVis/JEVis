package org.jevis.jedataprocessor.workflow;

import org.jevis.api.JEVisObject;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.jedataprocessor.prediction.PredictionStep;
import org.jevis.jedataprocessor.prediction.PreparePrediction;
import org.jevis.jedataprocessor.save.ImportStep;

public class PredictionManager extends ProcessManager {
    public PredictionManager(JEVisObject cleanObject, ObjectHandler objectHandler, int processingSize) {
        super(cleanObject, objectHandler, processingSize);
    }

    @Override
    public void addDefaultSteps() {
        ProcessStep preparePrediction = new PreparePrediction();
        processSteps.add(preparePrediction);

        ProcessStep prediction = new PredictionStep();
        processSteps.add(prediction);

        ProcessStep importStep = new ImportStep();
        processSteps.add(importStep);
    }
}
