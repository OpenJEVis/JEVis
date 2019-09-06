package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import jfxtras.scene.control.ListView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.Interval;

import java.util.List;
import java.util.Optional;

public class LinkerWidget extends Widget {

    public static String WIDGET_ID = "Link";
    private final Label label = new Label();
    private GraphAnalysisLinker graphAnalysisLinker;
    private JFXButton openAnalysisButton;
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(LinkerWidget.class);
    private DataModelDataHandler sampleHandler;
    private AnchorPane anchorPane;
    private JsonNode linkerNode;
    private GraphAnalysisLinkerNode dataModelNode;
    private Interval lastInterval = null;
    public static ObjectMapper objectMapper = new ObjectMapper();

    public LinkerWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public LinkerWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new TableWidget");
        widgetPojo.setType(typeID());


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {

        try {
            lastInterval = interval;
            this.graphAnalysisLinker = new GraphAnalysisLinker(getDataSource(), dataModelNode);

            this.openAnalysisButton = this.graphAnalysisLinker.buildLinkerButton();
            this.graphAnalysisLinker.applyConfig(
                    this.openAnalysisButton,
                    DataModelDataHandler.getAggregationPeriod(interval),
                    DataModelDataHandler.getManipulationMode(interval), interval);


        } catch (
                Exception ex) {
            logger.error(ex);
        }


        Platform.runLater(() ->

        {
            this.anchorPane.getChildren().clear();
            this.anchorPane.getChildren().add(this.openAnalysisButton);
            Layouts.setAnchor(this.openAnalysisButton, 0);
        });


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(null);

        if (dataModelNode != null) {
            Tab tab = new Tab("Link");
            widgetConfigDialog.addTab(tab);

            try {
                JEVisClass analysisClass = getDataSource().getJEVisClass(GraphDataModel.ANALYSIS_CLASS_NAME);
                List<JEVisObject> allAnalysis = getDataSource().getObjects(analysisClass, true);


                jfxtras.scene.control.ListView<JEVisObject> analysisListView = new ListView<>(FXCollections.observableArrayList(allAnalysis));
                analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {

                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            String parent = "";
                            String parentParent = "";
                            try {
                                if (!obj.getParents().isEmpty()) {
                                    parent = obj.getParents().get(0).getName();

                                    if (!obj.getParents().get(0).getParents().isEmpty()) {
                                        parentParent = obj.getParents().get(0).getParents().get(0).getName();
                                    }
                                }
                            } catch (Exception ex) {

                            }

                            String finalName = "";
                            if (!parent.isEmpty()) {
                                finalName += parent + " / ";
                            }
                            if (!parentParent.isEmpty()) {
                                finalName += parentParent + " / ";
                            }
                            finalName += obj.getName();
                            setText(finalName);
                        }
                    }
                });

                if (dataModelNode.getGraphAnalysisObject() != null && dataModelNode.getGraphAnalysisObject() > 0) {
                    try {
                        JEVisObject selectedAnalyis = getDataSource().getObject(dataModelNode.getGraphAnalysisObject());
                        if (selectedAnalyis != null) {
                            analysisListView.setSelectedItem(selectedAnalyis);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                analysisListView.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    System.out.println("set target analysis: " + newValue);
                    dataModelNode.setGraphAnalysisObject(newValue.getID());
                });

                tab.setContent(analysisListView);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.get() == ButtonType.OK) {
            updateData(lastInterval);
        }
    }

    @Override
    public void init() {
        this.anchorPane = new AnchorPane();

        try {
//            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
//            this.sampleHandler.setMultiSelect(false);

            linkerNode = this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE);
            if (linkerNode != null) {
                dataModelNode = this.mapper.treeToValue(this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
            } else {
                dataModelNode = new GraphAnalysisLinkerNode();
            }
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
    public ObjectNode toNode() {

        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(GraphAnalysisLinker.ANALYSIS_LINKER_NODE, objectMapper.valueToTree(dataModelNode));
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/LinkWidget2.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

}
