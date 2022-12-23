package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes.AnalysesComboBox;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.GraphAnalysisLinker;
import org.jevis.jeconfig.plugin.dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinkerWidget extends Widget {

    public static String WIDGET_ID = "Analyse-Link";
    private GraphAnalysisLinker graphAnalysisLinker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(LinkerWidget.class);
    private final AnchorPane anchorPane = new AnchorPane();
    private JsonNode linkerNode;
    private GraphAnalysisLinkerNode dataModelNode;
    private Interval lastInterval = null;
    public static ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectRelations objectRelations;
    private boolean hasInit = false;

    public LinkerWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        objectRelations = new ObjectRelations(control.getDataSource());
    }

    @Override
    public void debug() {

    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.linkerwidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setBorderSize(new BorderWidths(0));
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 1));

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        try {
            Platform.runLater(() -> {
                showProgressIndicator(false);
            });
            lastInterval = interval;

            this.graphAnalysisLinker.applyConfig(AggregationPeriod.NONE, ManipulationMode.NONE, interval);

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {
        if (!hasInit) {
            return;
        }
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                //            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                this.setBackground(bgColorTrans);
                this.anchorPane.setBackground(bgColor);
                this.setBorder(null);
                this.graphAnalysisLinker.applyNode(dataModelNode);
                this.anchorPane.getChildren().setAll(graphAnalysisLinker.getLinkerButton());
                Layouts.setAnchor(graphAnalysisLinker.getLinkerButton(), 0);
                this.layout();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        });

    }

    @Override
    public boolean isStatic() {
        return false;
    }



    @Override
    public List<DateTime> getMaxTimeStamps() {
        return new ArrayList<>();
    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(null);


        if (dataModelNode != null) {
            /** TODO: implement ConfigTab **/
            Tab tab = new Tab("Link");
            widgetConfigDialog.addTab(tab);

            try {
                JEVisClass analysisClass = getDataSource().getJEVisClass(AnalysesComboBox.ANALYSIS_CLASS_NAME);
                JEVisClass analysisDirClass = getDataSource().getJEVisClass(AnalysesComboBox.ANALYSES_DIRECTORY_CLASS_NAME);
                List<JEVisObject> allAnalyses = getDataSource().getObjects(analysisClass, true);
                List<JEVisObject> allAnalysesDir = getDataSource().getObjects(analysisClass, true);
                boolean multipleDir = allAnalysesDir.size() > 2;
                AlphanumComparator ac = new AlphanumComparator();
                if (!multipleDir) allAnalyses.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
                else {
                    allAnalyses.sort((o1, o2) -> {

                        String prefix1 = "";
                        String prefix2 = "";

                        if (ChartTools.isMultiSite(getDataSource())) {
                            prefix1 += objectRelations.getObjectPath(o1);
                        }
                        if (ChartTools.isMultiDir(getDataSource(), o1)) {
                            prefix1 += objectRelations.getRelativePath(o1);
                        }
                        prefix1 += o1.getName();

                        if (ChartTools.isMultiSite(getDataSource())) {
                            prefix2 += objectRelations.getObjectPath(o2);
                        }
                        if (ChartTools.isMultiDir(getDataSource(), o2)) {
                            prefix2 += objectRelations.getRelativePath(o2);
                        }
                        prefix2 += o2.getName();

                        return ac.compare(prefix1, prefix2);
                    });
                }

                JFXListView<JEVisObject> analysisListView = new JFXListView<>();
                analysisListView.setItems(FXCollections.observableArrayList(allAnalyses));
                analysisListView.setCellFactory(param -> new ListCell<JEVisObject>() {

                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            if (!ChartTools.isMultiSite(getDataSource()) && !ChartTools.isMultiDir(getDataSource(), obj))
                                setText(obj.getName());
                            else {
                                String prefix = "";
                                if (ChartTools.isMultiSite(getDataSource()))
                                    prefix += objectRelations.getObjectPath(obj);
                                if (ChartTools.isMultiDir(getDataSource(), obj)) {
                                    prefix += objectRelations.getRelativePath(obj);
                                }

                                setText(prefix + obj.getName());
                            }
                        }
                    }
                });

                if (dataModelNode.getGraphAnalysisObject() != null && dataModelNode.getGraphAnalysisObject() > 0) {
                    try {
                        JEVisObject selectedAnalyis = getDataSource().getObject(dataModelNode.getGraphAnalysisObject());
                        if (selectedAnalyis != null) {
                            analysisListView.getSelectionModel().select(selectedAnalyis);
                            analysisListView.scrollTo(selectedAnalyis);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    dataModelNode.setGraphAnalysisObject(newValue.getID());
                });

                tab.setContent(analysisListView);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void init() {
        logger.debug("Linker.Widget.init");

        try {
//            this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
//            this.sampleHandler.setMultiSelect(false);

            linkerNode = this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE);
            if (linkerNode != null) {
                dataModelNode = this.mapper.treeToValue(this.config.getConfigNode(GraphAnalysisLinker.ANALYSIS_LINKER_NODE), GraphAnalysisLinkerNode.class);
            } else {
                dataModelNode = new GraphAnalysisLinkerNode();
            }

            graphAnalysisLinker = new GraphAnalysisLinker(getDataSource());
        } catch (Exception ex) {
            logger.error(ex);
        }
        Layouts.setAnchor(anchorPane, 5);
        setGraphic(this.anchorPane);
        hasInit = true;
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
