package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;
import org.joda.time.Interval;

public class LinkerWidget extends Widget {

    public static String WIDGET_ID = "Link";
    private final Label label = new Label();
    private GraphAnalysisLinker graphAnalysisLinker;
    private JFXButton openAnalysisButton = new JFXButton();
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(LinkerWidget.class);

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
//                    graphAnalysisLinker.applyConfig(openAnalysisButton, sampleHandler.getDataModel(), interval);
                } else {
//                    openAnalysisButton.setVisible(false);
                    logger.warn("no linker set");
                }

            } catch (Exception ex) {
                logger.error(ex);
            }
        }


    }


    @Override
    public void init() {
//        label.setText(config.title.getValue());
//        label.setPadding(new Insets(0, 8, 0, 8));
//        AnchorPane anchorPane = new AnchorPane();
//        anchorPane.getChildren().add(label);
//        Layouts.setAnchor(label, 0);
        setGraphic(openAnalysisButton);
    }


    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/TitleWidget.png", previewSize.getHeight(), previewSize.getWidth());
    }
}
