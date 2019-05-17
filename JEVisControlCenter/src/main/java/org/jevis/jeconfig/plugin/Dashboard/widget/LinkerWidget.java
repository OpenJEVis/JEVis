package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
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

    public LinkerWidget(JEVisDataSource jeVisDataSource) {
        super(jeVisDataSource, new WidgetConfig(WIDGET_ID));
    }

    public LinkerWidget(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        super(jeVisDataSource, config);
    }


    @Override
    public void update(Interval interval) {
        //if config changed
        if (config.hasChanged("")) {
//            Background bgColor = new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY));
//            label.setBackground(bgColor);
//            label.setTextFill(config.fontColor.getValue());
//            label.setText(config.title.getValue());
//            label.setFont(new Font(config.fontSize.getValue()));
//            label.setAlignment(config.titlePosition.getValue());

            try {
                if (config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE) != null) {
                    GraphAnalysisLinkerNode dataModelNode = mapper.treeToValue(config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
                    graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);

                    openAnalysisButton = graphAnalysisLinker.buildLinkerButton();
                    graphAnalysisLinker.applyConfig(openAnalysisButton, sampleHandler.getDataModel(), interval);
                } else {
//                    openAnalysisButton.setVisible(false);
                    logger.warn("no linker set");
                }

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        Platform.runLater(() -> {
            anchorPane.getChildren().clear();
            anchorPane.getChildren().add(openAnalysisButton);
            Layouts.setAnchor(openAnalysisButton, 0);
        });


    }


    @Override
    public void init() {
        anchorPane = new AnchorPane();

//        label.setText(config.title.getValue());
//        label.setPadding(new Insets(0, 8, 0, 8));
//        AnchorPane anchorPane = new AnchorPane();
//        anchorPane.getChildren().add(openAnalysisButton);
//        Layouts.setAnchor(openAnalysisButton, 0);
        try {
            sampleHandler = new DataModelDataHandler(getDataSource(), config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
            sampleHandler.setMultiSelect(false);
        } catch (Exception ex) {
            logger.error(ex);
        }
        setGraphic(anchorPane);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/TitleWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkerWidget that = (LinkerWidget) o;

        if (label != null ? !label.equals(that.label) : that.label != null) return false;
        if (graphAnalysisLinker != null ? !graphAnalysisLinker.equals(that.graphAnalysisLinker) : that.graphAnalysisLinker != null)
            return false;
        if (openAnalysisButton != null ? !openAnalysisButton.equals(that.openAnalysisButton) : that.openAnalysisButton != null)
            return false;
        if (mapper != null ? !mapper.equals(that.mapper) : that.mapper != null) return false;
        if (sampleHandler != null ? !sampleHandler.equals(that.sampleHandler) : that.sampleHandler != null)
            return false;
        return anchorPane != null ? anchorPane.equals(that.anchorPane) : that.anchorPane == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (graphAnalysisLinker != null ? graphAnalysisLinker.hashCode() : 0);
        result = 31 * result + (openAnalysisButton != null ? openAnalysisButton.hashCode() : 0);
        result = 31 * result + (mapper != null ? mapper.hashCode() : 0);
        result = 31 * result + (sampleHandler != null ? sampleHandler.hashCode() : 0);
        result = 31 * result + (anchorPane != null ? anchorPane.hashCode() : 0);
        return result;
    }
}
