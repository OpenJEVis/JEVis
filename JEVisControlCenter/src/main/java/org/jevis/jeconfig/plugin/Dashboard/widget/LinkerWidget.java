package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.Dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.Dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.Interval;

public class LinkerWidget extends Widget {

    public static String WIDGET_ID = "Link";
    private final Label label = new Label();
    private GraphAnalysisLinker graphAnalysisLinker;
    private JFXButton openAnalysisButton;
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(LinkerWidget.class);
    private DataModelDataHandler sampleHandler;
    private AnchorPane anchorPane;

    public LinkerWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public void update(Interval interval) {

        try {
            if (this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE) != null) {
                GraphAnalysisLinkerNode dataModelNode = this.mapper.treeToValue(this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
                this.graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);

                this.openAnalysisButton = this.graphAnalysisLinker.buildLinkerButton();
                this.graphAnalysisLinker.applyConfig(this.openAnalysisButton, this.sampleHandler.getDataModel(), interval);
            } else {
//                    openAnalysisButton.setVisible(false);
                logger.warn("no linker set");
            }

        } catch (Exception ex) {
            logger.error(ex);
        }


        Platform.runLater(() -> {
            this.anchorPane.getChildren().clear();
            this.anchorPane.getChildren().add(this.openAnalysisButton);
            Layouts.setAnchor(this.openAnalysisButton, 0);
        });


    }


    @Override
    public void init() {
        this.anchorPane = new AnchorPane();

        try {
            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
            this.sampleHandler.setMultiSelect(false);
        } catch (Exception ex) {
            logger.error(ex);
        }
        setGraphic(this.anchorPane);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/TitleWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

}
