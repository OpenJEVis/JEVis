package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

public class AnalysisLinkButton extends JFXButton {

    private static final Logger logger = LogManager.getLogger(AnalysisLinkButton.class);
    private AnalysisRequest analysisRequest;

    public AnalysisLinkButton(Node icon, JEVisAttribute attribute) {

        super("",icon);
        init(attribute);
    }
    Node icon;

    public AnalysisLinkButton(JEVisAttribute attribute) {
        super("", JEConfig.getImage("1415314386_Graph.png", 20, 20));

        init(attribute);
    }

    private void init(JEVisAttribute attribute) {
        setStyle("-fx-background-color: transparent;");

        if (attribute != null) {
            try {

                DateTime timestampFromLastSample = attribute.getTimestampOfLastSample();

                DateTime startDateFromSampleRate = org.jevis.commons.utils.CommonMethods.getStartDateFromSampleRate(attribute);

                analysisRequest = new AnalysisRequest(attribute.getObject(),
                        AggregationPeriod.NONE,
                        ManipulationMode.NONE,
                        startDateFromSampleRate, timestampFromLastSample);
                analysisRequest.setAttribute(attribute);

                setOnAction(event -> JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest));

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
