package org.jevis.jecc.plugin.dashboard;

import com.google.common.collect.Iterables;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.math3.util.Precision;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.GlobalToolBar;
import org.jevis.jecc.Icon;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.plugin.dashboard.config2.DashboardPojo;
import org.jevis.jecc.plugin.dashboard.config2.NewWidgetSelector;
import org.jevis.jecc.plugin.dashboard.timeframe.ToolBarIntervalSelector;
import org.jevis.jecc.plugin.dashboard.widget.ImageWidget;
import org.jevis.jecc.plugin.dashboard.widget.Widget;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DashBoardToolbar extends ToolBar {

    private static final Logger logger = LogManager.getLogger(DashBoardToolbar.class);

    private final double iconSize = 20;
    private final DashboardControl dashboardControl;
    private final Button backgroundButton = new Button("", ControlCenter.getSVGImage(Icon.IMAGE, this.iconSize, this.iconSize));
    private final ObjectRelations objectRelations;
    private final Region lockIcon = ControlCenter.getSVGImage(Icon.LOCK, this.iconSize, this.iconSize);
    private final ToggleButton unlockButton = new ToggleButton("", this.lockIcon);
    private final Region snapToGridIcon = ControlCenter.getSVGImage(Icon.SNAP_TO_GRID, this.iconSize, this.iconSize);
    private final ToggleButton snapGridButton = new ToggleButton("", snapToGridIcon);
    private final Region unlockIcon = ControlCenter.getSVGImage(Icon.UNLOCK, this.iconSize, this.iconSize);
    private final Region pauseIcon = ControlCenter.getSVGImage(Icon.PAUSE, this.iconSize, this.iconSize);
    private final Region playIcon = ControlCenter.getSVGImage(Icon.PLAY, this.iconSize, this.iconSize);
    private final Button runUpdateButton = new Button("", this.playIcon);
    private final Region loadIcon = ControlCenter.getSVGImage(Icon.FOLDER_OPEN, this.iconSize, this.iconSize);
    private final Button loadDialogButton = new Button("", this.loadIcon);
    private final ToggleButton showGridButton = new ToggleButton("", ControlCenter.getSVGImage(Icon.GRID, this.iconSize, this.iconSize));
    private final Button treeButton = new Button("", ControlCenter.getSVGImage(Icon.SETTINGS, this.iconSize, this.iconSize));
    private final Button settingsButton = new Button("", ControlCenter.getImage("Service Manager.png", this.iconSize, this.iconSize));
    private final Button save = new Button("", ControlCenter.getSVGImage(Icon.SAVE, this.iconSize, this.iconSize));
    private final Button exportPNG = new Button("", ControlCenter.getSVGImage(Icon.IMAGE, this.iconSize, this.iconSize));
    private final Button exportPDF = new Button("", ControlCenter.getSVGImage(Icon.PDF, this.iconSize, this.iconSize));
    //private Button newButton = new Button("", JEConfig.getImage("1390343812_folder-open.png", this.iconSize, this.iconSize));
    private final Button deleteDashboard = new Button("", ControlCenter.getSVGImage(Icon.DELETE, this.iconSize, this.iconSize));
    private final Button deleteWidget = new Button("", ControlCenter.getSVGImage(Icon.DELETE, this.iconSize, this.iconSize));
    private final Button zoomIn = new Button("", ControlCenter.getSVGImage(Icon.ZOOM_IN, this.iconSize, this.iconSize));
    private final Button zoomOut = new Button("", ControlCenter.getSVGImage(Icon.ZOOM_OUT, this.iconSize, this.iconSize));
    private final Button enlarge = new Button("", ControlCenter.getSVGImage(Icon.MAXIMIZE, this.iconSize, this.iconSize));
    private final Button newB = new Button("", ControlCenter.getSVGImage(Icon.PLUS, this.iconSize, this.iconSize));
    private final ToggleButton sidebarEditor = new ToggleButton("", ControlCenter.getSVGImage(Icon.TUNE, this.iconSize, this.iconSize));
    private final Button reloadButton = new Button("", ControlCenter.getSVGImage(Icon.REFRESH, this.iconSize, this.iconSize));
    private final Button navigator = new Button("", ControlCenter.getSVGImage(Icon.SETTINGS, this.iconSize, this.iconSize));
    private final ToggleButton customWorkDay = new ToggleButton("", ControlCenter.getSVGImage(Icon.CALENDAR, iconSize, iconSize));
    private final Button homeButton = new Button("", ControlCenter.getSVGImage(Icon.HOME, iconSize, iconSize));
    private final Button moveButton = new Button("", ControlCenter.getImage("move.png", this.iconSize, this.iconSize));
    private final Menu newWidgetMenuItem = new Menu("New");
    private final Button copyButton = new Button("", ControlCenter.getSVGImage(Icon.COPY, this.iconSize, this.iconSize));
    private final ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);
    private final ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
    private ToolBarIntervalSelector toolBarIntervalSelector;
    private ComboBox<Double> listZoomLevel;
    private Boolean multiSite = null;
    private Boolean multiDir = null;
    private Button newWidget;
    private NewWidgetSelector widgetSelector;
    private final Separator separatorEditMode = new Separator();

    private boolean disableEventListener = false;
    private ComboBox<JEVisObject> listAnalysesComboBox;


    public DashBoardToolbar(DashboardControl dashboardControl) {
        this.dashboardControl = dashboardControl;
        this.objectRelations = new ObjectRelations(dashboardControl.getDataSource());
        initLayout();
        this.dashboardControl.registerToolBar(this);
    }


    public static ComboBox<Double> buildZoomLevelListView() {
        ObservableList<Double> zoomLevel = FXCollections.observableArrayList();

        List<Double> zoomLevels = new ArrayList<>();
        zoomLevels.add(DashboardControl.fitToScreen);
        zoomLevel.add(DashboardControl.fitToWidth);
        zoomLevel.add(DashboardControl.fitToHeight);

        /** ComboBox need all posible values or the buttonCell will not work work in java 1.8 **/
        double zs = 0;
        for (double d = 0; d <= DashboardControl.MAX_ZOOM; d += DashboardControl.zoomSteps) {
            zs = Precision.round((zs + 0.05d), 2);
            zoomLevels.add(zs);
        }
        zoomLevel.addAll(zoomLevels);


        ComboBox<Double> doubleComboBox = new ComboBox<>(zoomLevel);
        DecimalFormat df = new DecimalFormat("##0");
        df.setMaximumFractionDigits(2);
        Callback<ListView<Double>, ListCell<Double>> cellFactory = new Callback<ListView<Double>, ListCell<Double>>() {
            @Override
            public ListCell<Double> call(ListView<Double> param) {
                return new ListCell<Double>() {
                    @Override
                    protected void updateItem(Double item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            return;
                        }

                        if (item != null) {
                            Platform.runLater(() -> {
                                if (item == DashboardControl.fitToScreen) {
                                    setText(I18n.getInstance().getString("plugin.dashboard.zoom.fitscreen"));
                                } else if (item == DashboardControl.fitToWidth) {
                                    setText(I18n.getInstance().getString("plugin.dashboard.zoom.fitwidth"));
                                } else if (item == DashboardControl.fitToHeight) {
                                    setText(I18n.getInstance().getString("plugin.dashboard.zoom.fitheight"));
                                } else {
                                    setText(df.format(Precision.round(item * 100, 2)) + "%");
                                }
                            });
                        }
                    }
                };
            }


        };

        //TODO JFX17
        doubleComboBox.setConverter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                if (object == null || object == DashboardControl.fitToScreen) {
                    return (I18n.getInstance().getString("plugin.dashboard.zoom.fitscreen"));
                } else if (object == DashboardControl.fitToWidth) {
                    return (I18n.getInstance().getString("plugin.dashboard.zoom.fitwidth"));
                } else if (object == DashboardControl.fitToHeight) {
                    return (I18n.getInstance().getString("plugin.dashboard.zoom.fitheight"));
                } else {
                    return (df.format(Precision.round(object * 100, 2)) + "%");
                }
            }

            @Override
            public Double fromString(String string) {
                return doubleComboBox.getItems().get(doubleComboBox.getSelectionModel().getSelectedIndex());
            }
        });
        doubleComboBox.setValue(1.0d);
        doubleComboBox.setPrefWidth(150d);


        return doubleComboBox;
    }

    public void showTooltips(boolean show) {
        //helpButton.setSelected(show);
        //toolTipDocu.showHelpTooltips(show);
    }


    public void hideToolTips() {
        //if (toolTipDocu.isShowingProperty().get()) {
        //helpButton.setSelected(!toolTipDocu.isShowingProperty().getValue());
        // toolTipDocu.toggleHelp();
        //}
    }

    public void initLayout() {
        logger.debug("InitLayout");
        ObservableList<JEVisObject> observableList = this.dashboardControl.getAllDashboards();
        this.listAnalysesComboBox = new ComboBox<>(observableList);
        setCellFactoryForComboBox();
        this.listAnalysesComboBox.setPrefWidth(350);
        this.listAnalysesComboBox.setMinWidth(350);


        GlobalToolBar.changeBackgroundOnHoverUsingBinding(treeButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(settingsButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportPNG);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportPDF);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(deleteWidget);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(deleteDashboard);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomIn);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(enlarge);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reloadButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(backgroundButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(navigator);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(loadDialogButton);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(homeButton);

        this.customWorkDay.setSelected(dashboardControl.customWorkdayProperty.getValue());

        widgetSelector = new NewWidgetSelector(dashboardControl);
        widgetSelector.getSelectedWidgetProperty().addListener((observable, oldValue, newWidget) -> {
//            Widget newWidget = widgetSelector.getSelectedWidget();
            //newWidget.getConfig().setUuid(dashboardControl.getNextFreeUUID());
            dashboardControl.addWidget(newWidget);
            newWidget.setEditable(true);
            dashboardControl.setSelectedWidget(newWidget);
        });


        copyButton.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.copy")));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(copyButton);
        copyButton.setOnAction(event -> {
            try {
                logger.debug("Copy widget:");
                if (!dashboardControl.getSelectedWidgets().isEmpty()) {
                    Widget oldWidget = Iterables.getLast(dashboardControl.getSelectedWidgets());
                    Widget newWidget = oldWidget.clone();
                    newWidget.getConfig().setUuid(dashboardControl.getNextFreeUUID());

                    /* shift the widget a bit to so it not over the original */
                    double newXPos = newWidget.getConfig().getxPosition() + newWidget.getConfig().getSize().getWidth() + 50;
                    if (newXPos > dashboardControl.getDashboardPane().getWidth()) {
                        newWidget.getConfig().setxPosition(newWidget.getConfig().getxPosition() + 50);
                    } else {
                        newWidget.getConfig().setxPosition(newXPos);
                    }
                    dashboardControl.addWidget(newWidget);
                    newWidget.updateConfig();
                    newWidget.setEditable(true);
                    dashboardControl.setSelectedWidget(newWidget);

                    /* Workaround for the ImageWidget, we need to reset the image object, or we overwrite the old one*/
                    if (newWidget instanceof ImageWidget) {
                        ((ImageWidget) newWidget).restImageConfig();
                    }
                }

            } catch (Exception ex) {
                logger.error("Error while copying widget", ex);
            }
        });

        listZoomLevel = buildZoomLevelListView();

        this.listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (disableEventListener) return;
                if (oldValue != newValue) {
                    this.dashboardControl.selectDashboard(newValue);

                    Platform.runLater(() -> {
                        //** workaround for the Combobox **/
                        this.runUpdateButton.requestFocus();
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.listZoomLevel.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (disableEventListener) return;
            dashboardControl.setZoomFactor(newValue);
        });

        reloadButton.setOnAction(event -> {
            this.dashboardControl.reload();
        });

        exportPNG.setOnAction(event -> {
            this.dashboardControl.toPNG();
        });

        exportPDF.setOnAction(event -> {
            this.dashboardControl.toPDF();
        });

        save.setOnAction(event -> {
            this.dashboardControl.save();
        });

        unlockButton.setOnAction(event -> {
            this.dashboardControl.setEditable(unlockButton.isSelected());
        });

        snapGridButton.setOnAction(event -> {
            this.dashboardControl.setSnapToGrid(!dashboardControl.snapToGridProperty.getValue());
        });

        showGridButton.setOnAction(event -> {
            this.dashboardControl.showGrid(!dashboardControl.showGridProperty.getValue());
        });

        this.runUpdateButton.setOnAction(event -> {
            this.dashboardControl.switchUpdating();
        });

        this.backgroundButton.setOnAction(event -> {
            this.dashboardControl.startWallpaperSelection();
        });

        navigator.setOnAction(event -> {
            this.dashboardControl.openWidgetNavigator();
        });

        customWorkDay.setOnAction(event -> {
            this.dashboardControl.setCustomWorkday(!dashboardControl.customWorkdayProperty.getValue());
        });

        zoomIn.setOnAction(event -> {
            this.dashboardControl.zoomIn();
        });

        zoomOut.setOnAction(event -> {
            this.dashboardControl.zoomOut();
        });

        toolBarIntervalSelector = new ToolBarIntervalSelector(this.dashboardControl);

        //not in use
        newB.setOnAction(event -> {
            this.dashboardControl.createNewDashboard();
        });

        infoButton.setOnAction(event -> {
            this.dashboardControl.toggleWidgetTooltips();
        });

        deleteWidget.setOnAction(event -> {
            dashboardControl.removeAllWidgets(dashboardControl.getSelectedWidgets());
        });

        deleteDashboard.setOnAction(event -> {
            dashboardControl.deleteDashboard();
        });


        loadDialogButton.setOnAction(event -> {
            dashboardControl.showLoadDialog();
        });

        //this.dashboardControl.showSideEditorProperty.bindBidirectional(sidebarEditor.selectedProperty());

        sidebarEditor.setOnAction(event -> {
            this.dashboardControl.showSideEditorProperty.setValue(sidebarEditor.isSelected());
        });

        homeButton.setTooltip(new Tooltip(I18n.getInstance().getString("dashboard.navigator.home")));
        homeButton.setOnAction(event -> {
            this.dashboardControl.loadFirstDashboard();
        });


        /**
         helpButton.setOnAction(event -> {
         this.dashboardControl.toggleTooltip();
         });
         **/


        Separator sep1 = new Separator();
        Separator sep3 = new Separator();
        Separator sep4 = new Separator();
        Separator sep5 = new Separator();
        Separator sep6 = new Separator();

        showGridButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.showgrid")));
        snapGridButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.usegrid")));
        unlockButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.unlock")));
        newB.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.new")));
        runUpdateButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.update")));
        reloadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.reload")));
        save.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.save")));
        listAnalysesComboBox.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.list")));
        zoomIn.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.zoomin")));
        zoomOut.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.zoomout")));
        listZoomLevel.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.zoomlevel")));
        deleteWidget.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.delete")));
        deleteDashboard.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.deleteboard")));
        navigator.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.settings")));
        exportPNG.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.export")));
        exportPDF.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.dashboard.toolbar.tip.exportPDF")));
        reloadButton.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload")));
        customWorkDay.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.customworkday")));
        sidebarEditor.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.showsidebar")));

        newWidget = widgetSelector.getNewB();

        Region spacerForRightSide = new Region();
        HBox.setHgrow(spacerForRightSide, Priority.ALWAYS);

        Platform.runLater(() -> {
            getItems().clear();
            getItems().setAll(
                    listAnalysesComboBox, homeButton
                    , sep3, toolBarIntervalSelector, customWorkDay
                    , sep1, zoomOut, zoomIn, listZoomLevel, reloadButton
                    , sep4, loadDialogButton, save, deleteDashboard
                    , sep5, exportPNG, exportPDF
                    , sep6, runUpdateButton, unlockButton, navigator, widgetSelector, newWidget, copyButton, deleteWidget
                    , separatorEditMode, showGridButton, snapGridButton, sidebarEditor
                    , JEVisHelp.getInstance().buildSpacerNode(), helpButton, infoButton
            );
        });

        //getItems().addAll(JEVisHelp.getInstance().buildSpacerNode(), );
        Platform.runLater(() -> JEVisHelp.getInstance().addHelpItems(DashBordPlugIn.class.getSimpleName(), "", JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER, getItems()));

        updateView(dashboardControl.getActiveDashboard());
    }

    public void setUpdateRunning(boolean updateRunning) {
        Platform.runLater(() -> {
            if (updateRunning) {
                this.runUpdateButton.setGraphic(this.pauseIcon);
            } else {
                this.runUpdateButton.setGraphic(this.playIcon);
            }
        });
    }

    public void setEditable(boolean editable) {
        Platform.runLater(() -> {
            if (editable) {
                this.unlockButton.setGraphic(this.unlockIcon);
            } else {
                this.unlockButton.setGraphic(this.lockIcon);
            }
        });

    }

    public void updateZoomLevelView(double zoomLvL) {
        disableEventListener = true;
        listZoomLevel.setValue(zoomLvL);
        disableEventListener = false;
    }

    public void updateDashboardList(final ObservableList<JEVisObject> dashboardList, final DashboardPojo dashboardSettings) {
        logger.debug("updateDashboardList: {}", dashboardSettings);
        disableEventListener = true;

        AlphanumComparator ac = new AlphanumComparator();
        if (!isMultiDir() && !isMultiSite())
            dashboardList.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));
        else {
            dashboardList.sort((o1, o2) -> {

                String prefix1 = "";
                String prefix2 = "";

                if (isMultiSite()) {
                    prefix1 += objectRelations.getObjectPath(o1);
                }
                if (isMultiDir()) {
                    prefix1 += objectRelations.getRelativePath(o1);
                }
                prefix1 += o1.getName();

                if (isMultiSite()) {
                    prefix2 += objectRelations.getObjectPath(o2);
                }
                if (isMultiDir()) {
                    prefix2 += objectRelations.getRelativePath(o2);
                }
                prefix2 += o2.getName();

                return ac.compare(prefix1, prefix2);
            });
        }

        listAnalysesComboBox.setItems(dashboardList);

        if (dashboardSettings.getDashboardObject() != null) {
            this.listAnalysesComboBox.getSelectionModel().select(dashboardSettings.getDashboardObject());
        }
        disableEventListener = false;
    }

    public void updateView(final DashboardPojo dashboardSettings) {
        logger.debug("updateDashboard: {}", dashboardSettings);

        unlockButton.setSelected(this.dashboardControl.editableProperty.getValue());
        showGridButton.setSelected(this.dashboardControl.showGridProperty.getValue());
        snapGridButton.setSelected(this.dashboardControl.snapToGridProperty.getValue());
        customWorkDay.setSelected(this.dashboardControl.customWorkdayProperty.getValue());
        listZoomLevel.setValue(dashboardControl.getZoomFactory());
        toolBarIntervalSelector.updateView();
        infoButton.setSelected(this.dashboardControl.showWidgetHelpProperty.getValue());
        sidebarEditor.setSelected(this.dashboardControl.showSideEditorProperty.getValue());
        //toolTipDocu.showHelpTooltips(this.dashboardControl.showHelpProperty.getValue());

        //Disable
        widgetSelector.setDisable(!dashboardControl.editableProperty.get());
        copyButton.setDisable(!dashboardControl.editableProperty.get());
        deleteWidget.setDisable(!dashboardControl.editableProperty.get());
        navigator.setDisable(!dashboardControl.editableProperty.get());
        snapGridButton.setDisable(!dashboardControl.editableProperty.get());
        showGridButton.setDisable(!dashboardControl.editableProperty.get());
        navigator.setDisable(!dashboardControl.editableProperty.get());
        sidebarEditor.setDisable(!dashboardControl.editableProperty.get());
        newWidget.setDisable(!dashboardControl.editableProperty.get());
        deleteDashboard.setDisable(!dashboardControl.editableProperty.get());


        //Hide
        widgetSelector.setVisible(dashboardControl.editableProperty.get());
        copyButton.setVisible(dashboardControl.editableProperty.get());
        deleteWidget.setVisible(dashboardControl.editableProperty.get());
        navigator.setVisible(dashboardControl.editableProperty.get());
        snapGridButton.setVisible(dashboardControl.editableProperty.get());
        showGridButton.setVisible(dashboardControl.editableProperty.get());
        navigator.setVisible(dashboardControl.editableProperty.get());
        sidebarEditor.setVisible(dashboardControl.editableProperty.get());
        newWidget.setVisible(dashboardControl.editableProperty.get());
        separatorEditMode.setVisible(dashboardControl.editableProperty.get());


        updateDashboardList(dashboardControl.getAllDashboards(), dashboardSettings);
    }


    private void setCellFactoryForComboBox() {
        try {
            JEVisClass organisationClass = DashBoardToolbar.this.dashboardControl.getDataSource().getJEVisClass("Organization");

            Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
                @Override
                public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                    return new ListCell<JEVisObject>() {
                        @Override
                        protected void updateItem(JEVisObject obj, boolean empty) {
                            super.updateItem(obj, empty);
                            if (obj == null || empty) {
                                setGraphic(null);
                                setText(null);
                            } else {
                                this.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
                                if (!isMultiSite() && !isMultiDir()) {
                                    setText(obj.getName());
                                } else {
                                    String prefix = "";
                                    if (isMultiSite()) {
                                        prefix += objectRelations.getObjectPath(obj);
                                    }
                                    if (isMultiDir()) {
                                        prefix += objectRelations.getRelativePath(obj);
                                    }
                                    setText(prefix + obj.getName());
                                }
                            }

                        }
                    };
                }
            };

            //TODO JFX17

            listAnalysesComboBox.setConverter(new StringConverter<JEVisObject>() {
                @Override
                public String toString(JEVisObject object) {
                    if (object != null) {
                        if (!isMultiSite() && !isMultiDir()) {
                            return (object.getName());
                        } else {
                            String prefix = "";
                            if (isMultiSite()) {
                                prefix += objectRelations.getObjectPath(object);
                            }
                            if (isMultiDir()) {
                                prefix += objectRelations.getRelativePath(object);
                            }
                            return (prefix + object.getName());
                        }
                    } else return "";
                }

                @Override
                public JEVisObject fromString(String string) {
                    return listAnalysesComboBox.getItems().stream().filter(jeVisObject -> jeVisObject.getName().equals(string)).findFirst().orElse(null);
                }
            });
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public boolean isMultiSite() {
        if (multiSite == null) {
            boolean is = false;
            try {
                JEVisClass directoryClass = dashboardControl.getDataSource().getJEVisClass("Analyses Directory");
                List<JEVisObject> objects = dashboardControl.getDataSource().getObjects(directoryClass, true);

                List<JEVisObject> buildingParents = new ArrayList<>();
                for (JEVisObject jeVisObject : objects) {
                    JEVisObject buildingParent = objectRelations.getBuildingParent(jeVisObject);
                    if (!buildingParents.contains(buildingParent)) {
                        buildingParents.add(buildingParent);

                        if (buildingParents.size() > 1) {
                            is = true;
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            multiSite = is;
        }

        return multiSite;
    }

    public boolean isMultiDir() {
        if (multiDir == null) {
            boolean is = false;
            try {
                JEVisClass directoryClass = dashboardControl.getDataSource().getJEVisClass("Analyses Directory");
                List<JEVisObject> objects = dashboardControl.getDataSource().getObjects(directoryClass, true);
                if (objects.size() > 1) {
                    is = true;
                }
            } catch (Exception ignored) {
            }
            multiDir = is;
        }

        return multiDir;
    }

    public ComboBox<JEVisObject> getListAnalysesComboBox() {
        return this.listAnalysesComboBox;
    }

    private void showAllTooltips(List<Object> controls) {
        for (Object obj : controls) {
            try {
                if (obj instanceof Control control) {
                    Tooltip tooltip = new Tooltip();
                    control.setTooltip(tooltip);
                    tooltip.setId("hmmmmmmmmmmmmmmm");
                    if (tooltip != null && !tooltip.getText().isEmpty()) {
                        if (tooltip.isShowing()) Platform.runLater(() -> tooltip.hide());
                        else {
                            Bounds sceneBounds = control.localToScene(control.getBoundsInLocal());

                            double x = sceneBounds.getMinX() + 2;
                            double y = sceneBounds.getMinY();// + 25;//4;

                            Platform.runLater(() -> {
                                try {
                                    tooltip.setGraphic(new Region());
                                    tooltip.show(control, x + 27, y + 60);
                                    Label parent = (Label) tooltip.getGraphic().getParent();
                                    parent.getTransforms().add(new Rotate(90));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            });
                        }
                    }
                }

            } catch (Exception ex) {
                logger.error(ex, ex);
            }

        }
    }


}
