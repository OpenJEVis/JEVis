package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.AnalysisRequest;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

public class AnalysisLinkButton extends MFXButton {

    private static final Logger logger = LogManager.getLogger(AnalysisLinkButton.class);
    private AnalysisRequest analysisRequest;

    public AnalysisLinkButton(JEVisAttribute attribute) {
        super("", ControlCenter.getImage("1415314386_Graph.png", 20, 20));

        setStyle("-fx-background-color: transparent;");

        if (attribute != null) {
            try {

                DateTime timestampFromLastSample = attribute.getTimestampFromLastSample();

                DateTime startDateFromSampleRate = org.jevis.commons.utils.CommonMethods.getStartDateFromSampleRate(attribute);

                analysisRequest = new AnalysisRequest(attribute.getObject(),
                        AggregationPeriod.NONE,
                        ManipulationMode.NONE,
                        startDateFromSampleRate, timestampFromLastSample);
                analysisRequest.setAttribute(attribute);

                setOnAction(event -> ControlCenter.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest));

            } catch (Exception e) {
                logger.error("Could not build analysis link button: ", e);
                setDisable(true);
            }
        } else {
            setDisable(true);
        }
    }

    public AnalysisRequest getAnalysisRequest() {
        return analysisRequest;
    }
}
