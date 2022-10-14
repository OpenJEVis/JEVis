package org.jevis.jeconfig.application.control;

import com.jfoenix.controls.JFXButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisUser;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

public class AnalysisLinkButton extends JFXButton {

    private static final Logger logger = LogManager.getLogger(AnalysisLinkButton.class);

    public AnalysisLinkButton(JEVisAttribute attribute) {
        super("", JEConfig.getImage("1415314386_Graph.png", 20, 20));

        setStyle("-fx-background-color: transparent;");

        if (attribute != null) {
            try {
                JEVisObject buildingObject = org.jevis.commons.utils.CommonMethods.getFirstParentalObjectOfClass(attribute.getObject(), "Building");
                JEVisObject analysisDir = null;
                if (buildingObject != null) {
                    for (JEVisObject child : buildingObject.getChildren()) {
                        if (child.getJEVisClassName().equals("Analyses Directory")) {
                            analysisDir = child;
                            break;
                        }
                    }
                }

                JEVisUser currentUser = attribute.getDataSource().getCurrentUser();
                if (analysisDir != null && (currentUser.canCreate(analysisDir.getID()) && currentUser.canDelete(analysisDir.getID()))) {
                    DateTime timestampFromLastSample = attribute.getTimestampFromLastSample();

                    DateTime startDateFromSampleRate = org.jevis.commons.utils.CommonMethods.getStartDateFromSampleRate(attribute);

                    AnalysisRequest analysisRequest = new AnalysisRequest(attribute.getObject(),
                            AggregationPeriod.NONE,
                            ManipulationMode.NONE,
                            startDateFromSampleRate, timestampFromLastSample);
                    analysisRequest.setAttribute(attribute);

                    setOnAction(event -> JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest));
                } else setDisable(true);
            } catch (Exception e) {
                logger.error("Could not build analysis link button: ", e);
                setDisable(true);
            }
        } else {
            setDisable(true);
        }
    }
}
