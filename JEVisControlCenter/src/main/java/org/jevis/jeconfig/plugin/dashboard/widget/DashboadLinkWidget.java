package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashBordPlugIn;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.DashboardLinkerNode;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.jevis.jeconfig.tool.Layouts;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;

public class DashboadLinkWidget extends Widget {

    public static String DASHBOARD_LINKER_NODE_NAME = "dashboardLinker";
    public static String WIDGET_ID = "Dashboard-Link";
    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(DashboadLinkWidget.class);
    private AnchorPane anchorPane = new AnchorPane();
    private JsonNode linkerNode;
//    private GraphAnalysisLinkerNode dataModelNode;

    private DashboardLinkerNode dataModelNode = new DashboardLinkerNode();
    private DataModelDataHandler sampleHandler;
    public static ObjectMapper objectMapper = new ObjectMapper();

    JFXButton linkButton = new JFXButton("", JEConfig.getImage("if_dashboard_46791.png", 20, 20));
    private TimeFrameFactory selectedTimeFrameFactory;
    private Interval lastInterval = null;
    private JEVisObject linkedDashboardObj;

    public DashboadLinkWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public DashboadLinkWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public void debug() {

    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.dashboardlinkerwidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setBorderSize(new BorderWidths(0));

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        try {
            lastInterval = interval;
            Platform.runLater(() -> {
                showProgressIndicator(false);
            });

            selectedTimeFrameFactory = control.getActiveDashboard().getTimeFrame();
//            linkedDashboardObj = getDataSource().getObject(3799l);

            linkButton.setOnAction(event -> {
                control.selectDashboard(linkedDashboardObj);
                control.setActiveTimeFrame(selectedTimeFrameFactory);
                control.setInterval(interval);
            });


        } catch (
                Exception ex) {
            logger.error(ex);
        }


    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                //            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                this.setBackground(bgColorTrans);
                this.anchorPane.setBackground(bgColor);
                this.setBorder(null);

                if (dataModelNode.getDashboardObject() != null) {
                    try {
                        linkedDashboardObj = getDataSource().getObject(dataModelNode.getDashboardObject());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

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


        /** TODO: implement ConfigTab **/
        Tab tab = new Tab("Link");
        widgetConfigDialog.addTab(tab);

        try {
            JEVisClass analysisClass = getDataSource().getJEVisClass(DashBordPlugIn.CLASS_ANALYSIS);
            List<JEVisObject> allAnalysis = getDataSource().getObjects(analysisClass, true);
            AlphanumComparator alphanumComparator = new AlphanumComparator();
            Collections.sort(allAnalysis, new Comparator<JEVisObject>() {
                @Override
                public int compare(JEVisObject o1, JEVisObject o2) {
                    return alphanumComparator.compare(o1.getName(), o2.getName());
                }
            });

            ListView<JEVisObject> analysisListView = new ListView<>(FXCollections.observableArrayList(allAnalysis));
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

            if (linkedDashboardObj != null) {
                try {
                    analysisListView.getSelectionModel().select(linkedDashboardObj);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            analysisListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                linkedDashboardObj = newValue;
                dataModelNode.setDashboardObject(linkedDashboardObj.getID());
            });

            tab.setContent(analysisListView);

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Runnable task = () -> {
                    widgetConfigDialog.commitSettings();
                    updateConfig(getConfig());
                    updateData(lastInterval);
                };
                control.getExecutor().submit(task);


            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void init() {
        logger.debug("Linker.Widget.init");

        anchorPane.getChildren().add(linkButton);
        Layouts.setAnchor(linkButton, 0);
        try {

            linkerNode = this.config.getConfigNode(DASHBOARD_LINKER_NODE_NAME);
            if (linkerNode != null) {
                dataModelNode = this.mapper.treeToValue(this.config.getConfigNode(DASHBOARD_LINKER_NODE_NAME), DashboardLinkerNode.class);

            } else {
                dataModelNode = new DashboardLinkerNode();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
        Layouts.setAnchor(anchorPane, 5);
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
                .set(DASHBOARD_LINKER_NODE_NAME, objectMapper.valueToTree(dataModelNode));

        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/LinkWidget2.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

}
