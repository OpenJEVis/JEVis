package org.jevis.jecc.application.control;


import javafx.scene.Node;
import javafx.scene.control.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.plugin.AnalysisRequest;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

public class AnalysisLinkButton extends Button {

    private static final Logger logger = LogManager.getLogger(AnalysisLinkButton.class);
    Node icon;
    private AnalysisRequest analysisRequest;

    public AnalysisLinkButton() {
        super();
        initStyle();
    }

    public AnalysisLinkButton(JEVisAttribute attribute) {
        super("", ControlCenter.getImage("1415314386_Graph.png", 20, 20));
        initStyle();
        init(attribute);
    }

    private void initStyle() {
        setStyle("-fx-background-color: transparent;");
        setGraphic(ControlCenter.getSVGImage(Icon.GRAPH, 20, 20));
    }

    public void init(JEVisAttribute attribute) {
        if (attribute != null) {
            try {
                DateTime timestampFromLastSample = attribute.getTimestampOfLastSample();

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
