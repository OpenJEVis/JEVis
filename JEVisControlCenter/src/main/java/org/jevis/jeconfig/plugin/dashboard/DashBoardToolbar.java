package org.jevis.jeconfig.plugin.dashboard;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.DashboardPojo;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetNavigator;
import org.jevis.jeconfig.plugin.dashboard.timeframe.ToolBarIntervalSelector;
import org.jevis.jeconfig.tool.I18n;

import java.util.List;

public class DashBoardToolbar extends ToolBar {

    private static final Logger logger = LogManager.getLogger(DashBoardToolbar.class);
    private ComboBox<JEVisObject> listAnalysesComboBox;
    private double iconSize = 20;
    private ToggleButton backgroundButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", this.iconSize, this.iconSize));
    private final DashboardControl dashboardControl;

    final ImageView lockIcon = JEConfig.getImage("if_lock_blue_68757.png", this.iconSize, this.iconSize);
    final ImageView snapToGridIcon = JEConfig.getImage("Snap_to_Grid.png", this.iconSize, this.iconSize);

    final ImageView unlockIcon = JEConfig.getImage("if_lock-unlock_blue_68758.png", this.iconSize, this.iconSize);
    final ToggleButton unlockButton = new ToggleButton("", this.lockIcon);
    final ToggleButton snapGridButton = new ToggleButton("", snapToGridIcon);
    final ImageView pauseIcon = JEConfig.getImage("pause_32.png", this.iconSize, this.iconSize);
    final ImageView playIcon = JEConfig.getImage("play_32.png", this.iconSize, this.iconSize);
    private ToggleButton runUpdateButton = new ToggleButton("", this.playIcon);
//    private ProgressBar progressBar = new ProgressBar();


    public DashBoardToolbar(DashboardControl dashboardControl) {
        this.dashboardControl = dashboardControl;
        this.dashboardControl.registerToolBar(this);
    }


    public ComboBox<JEVisObject> getListAnalysesComboBox() {
        return this.listAnalysesComboBox;
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

    public void updateView(final DashboardPojo dashboardSettings) {
        logger.debug("updateDashboard: {}", dashboardSettings);
        ObservableList<JEVisObject> observableList = this.dashboardControl.getAllDashboards();


        this.listAnalysesComboBox = new ComboBox<>(observableList);
        this.listAnalysesComboBox.setPrefWidth(350);
        this.listAnalysesComboBox.setMinWidth(350);
        if (dashboardSettings.getDashboardObject() != null) {
            this.listAnalysesComboBox.getSelectionModel().select(dashboardSettings.getDashboardObject());
        }

        setCellFactoryForComboBox();

        ToggleButton treeButton = new ToggleButton("", JEConfig.getImage("Data.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(treeButton);

        this.listAnalysesComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                this.dashboardControl.selectDashboard(newValue);

                Platform.runLater(() -> {
                    //** workaround for the Combobox **/
                    this.runUpdateButton.requestFocus();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        ToggleButton settingsButton = new ToggleButton("", JEConfig.getImage("Service Manager.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(settingsButton);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        ToggleButton exportPNG = new ToggleButton("", JEConfig.getImage("export-image.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportPNG);


        ToggleButton newButton = new ToggleButton("", JEConfig.getImage("1390343812_folder-open.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newButton);

        ToggleButton delete = new ToggleButton("", JEConfig.getImage("if_trash_(delete)_16x16_10030.gif", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        ToggleButton zoomIn = new ToggleButton("", JEConfig.getImage("zoomIn_32.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomIn);

        ToggleButton zoomOut = new ToggleButton("", JEConfig.getImage("zoomOut_32.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(zoomOut);

        ToggleButton enlarge = new ToggleButton("", JEConfig.getImage("enlarge_32.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(enlarge);

        ToggleButton newB = new ToggleButton("", JEConfig.getImage("list-add.png", 18, 18));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);


        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.backgroundButton);

        ToggleButton newWidgetButton = new ToggleButton("", JEConfig.getImage("Data.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newWidgetButton);


//        GlobalToolBar.changeBackgroundOnHoverUsingBinding(snapGridButton);
//        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.unlockButton);


        reload.setOnAction(event -> {
            this.dashboardControl.reload();
        });

        exportPNG.setOnAction(event -> {
            this.dashboardControl.toPNG();
        });

        save.setOnAction(event -> {
            this.dashboardControl.save();
        });

        /**
         * Maybe bind in the future
         */
        unlockButton.selectedProperty().setValue(this.dashboardControl.editableProperty.getValue());
        snapGridButton.selectedProperty().setValue(this.dashboardControl.enableSnapToGridProperty.getValue());
        snapGridButton.setDisable(true);

        unlockButton.setOnAction(event -> {
            this.dashboardControl.setEditable(unlockButton.isSelected());
            this.dashboardControl.showGrid(unlockButton.isSelected());
            this.snapGridButton.setDisable(!unlockButton.isSelected());
            this.snapGridButton.selectedProperty().setValue(true);
        });

        snapGridButton.setOnAction(event -> {
            this.dashboardControl.setSnapToGrid(snapGridButton.isSelected());
        });


//        unlockButton.onActionProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("Umblock button: " + newValue);
//            this.dashboardControl.setEditable(new);
//        });


//        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.runUpdateButton);


        this.runUpdateButton.setOnAction(event -> {
            this.dashboardControl.switchUpdating();
        });


//        newWidgetButton.setOnAction(event -> {
//            this.dashboardControl.startWizard();
//        });

        this.backgroundButton.setOnAction(event -> {

            this.dashboardControl.startWallpaperSelection();

        });

        newWidgetButton.setOnAction(event -> {
//            dashboardSettings.openConfig();
            WidgetNavigator navigator = new WidgetNavigator(this.dashboardControl);
            navigator.show();
        });


        zoomIn.setOnAction(event -> {
            this.dashboardControl.zoomIn();
        });

        zoomOut.setOnAction(event -> {
            this.dashboardControl.zoomOut();
        });

        ToolBarIntervalSelector toolBarIntervalSelector = new ToolBarIntervalSelector(
                this.dashboardControl.getDataSource(), this.dashboardControl, this.iconSize);


//        toolBarIntervalSelector.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
//            this.dashboardControl.setInterval(newValue);
//        });


        newB.setOnAction(event -> {
            this.dashboardControl.selectDashboard(null);
        });


        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        Separator sep3 = new Separator();
        Separator sep4 = new Separator();


        newButton.setDisable(true);
        delete.setDisable(true);
//        save.setDisable(true);
//        exportPDF.setVisible(false);/** disabled because of an endless loop JAVAFX bug, should be fixed with JAVA 11**/

        Region spacerForRightSide = new Region();
        HBox.setHgrow(spacerForRightSide, Priority.ALWAYS);
        getItems().clear();
        getItems().addAll(
                this.listAnalysesComboBox, newB
                , sep3, toolBarIntervalSelector
                , sep1, zoomOut, zoomIn, reload
                , sep4, newButton, save, delete, newWidgetButton, exportPNG
                , sep2, this.runUpdateButton, this.unlockButton, snapGridButton);
//                , spacerForRightSide, progressBar);
    }

//    public ProgressBar getProgressBar() {
//        return progressBar;
//    }

    public void addTaskToProgressBar(List<Task> taskList) {

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
                            if (empty || obj == null || obj.getName() == null) {
                                setText(I18n.getInstance().getString("plugin.dashboard.toolbar.list.new"));
                            } else {
                                String prefix = "";
                                try {
                                    JEVisObject parentObj = null;
                                    JEVisObject secoundParentObj = null;
                                    JEVisObject thirdParentObj = null;

                                    try {
                                        parentObj = obj.getParents().get(0);
                                    } catch (NullPointerException np) {
                                    }
                                    try {
                                        secoundParentObj = parentObj.getParents().get(0);
                                    } catch (Exception np) {
                                    }
                                    try {
                                        thirdParentObj = secoundParentObj.getParents().get(0);
                                    } catch (Exception np) {
                                    }


                                    if (secoundParentObj != null && secoundParentObj.getJEVisClass().equals(organisationClass)) {
                                        prefix += secoundParentObj.getName() + " / ";
                                    }

                                    if (thirdParentObj != null && thirdParentObj.getJEVisClass().equals(organisationClass)) {
                                        prefix += thirdParentObj.getName() + " / ";
                                    }


                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setText(prefix + obj.getName());
                            }

                        }
                    };
                }
            };

            this.listAnalysesComboBox.setCellFactory(cellFactory);
            this.listAnalysesComboBox.setButtonCell(cellFactory.call(null));
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public ToggleButton getBackgroundButton() {
        return this.backgroundButton;
    }
}
