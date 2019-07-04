package org.jevis.jeconfig.plugin.Dashboard;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.GlobalToolBar;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.Dashboard.config2.DashboardPojo;
import org.jevis.jeconfig.plugin.Dashboard.config2.Navigator;
import org.jevis.jeconfig.plugin.Dashboard.timeframe.ToolBarIntervalSelector;
import org.jevis.jeconfig.tool.I18n;

public class DashBoardToolbar extends ToolBar {

    private static final Logger logger = LogManager.getLogger(DashBoardToolbar.class);
    private ComboBox<JEVisObject> listAnalysesComboBox;
    private double iconSize = 20;
    private ToggleButton backgroundButton = new ToggleButton("", JEConfig.getImage("if_32_171485.png", this.iconSize, this.iconSize));
    private final DashboardControl dashboardControl;

    final ImageView lockIcon = JEConfig.getImage("if_lock_blue_68757.png", this.iconSize, this.iconSize);
    final ImageView unlockIcon = JEConfig.getImage("if_lock-unlock_blue_68758.png", this.iconSize, this.iconSize);
    final ToggleButton unlockB = new ToggleButton("", this.lockIcon);
    final ImageView pauseIcon = JEConfig.getImage("pause_32.png", this.iconSize, this.iconSize);
    final ImageView playIcon = JEConfig.getImage("play_32.png", this.iconSize, this.iconSize);
    ToggleButton runUpdateButton = new ToggleButton("", this.playIcon);

    public DashBoardToolbar(DashboardControl dashboardControl) {
        this.dashboardControl = dashboardControl;
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
                this.unlockB.setGraphic(this.unlockIcon);
            } else {
                this.unlockB.setGraphic(this.lockIcon);
            }
        });

    }

    public void updateView(final DashboardPojo dashboardSettings) {
        logger.error("updateDashboard: {}", dashboardSettings);
        ObservableList<JEVisObject> observableList = this.dashboardControl.getAllDashboards();


        this.listAnalysesComboBox = new ComboBox<>(observableList);
        this.listAnalysesComboBox.setPrefWidth(300);
        this.listAnalysesComboBox.setMinWidth(300);
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
                    this.backgroundButton.requestFocus();
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        ToggleButton settingsButton = new ToggleButton("", JEConfig.getImage("Service Manager.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(settingsButton);

        ToggleButton save = new ToggleButton("", JEConfig.getImage("save.gif", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);

        ToggleButton exportPDF = new ToggleButton("", JEConfig.getImage("pdf_32_32.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(exportPDF);


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


        ToggleButton reload = new ToggleButton("", JEConfig.getImage("1403018303_Refresh.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.graph.toolbar.tooltip.reload"));
        reload.setTooltip(reloadTooltip);

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.backgroundButton);

        ToggleButton newWidgetButton = new ToggleButton("", JEConfig.getImage("Data.png", this.iconSize, this.iconSize));
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newWidgetButton);


        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.unlockB);


        reload.setOnAction(event -> {
            this.dashboardControl.reload();
        });

        exportPDF.setOnAction(event -> {
//            this.dashboardControl.toPDF();
        });

        save.setOnAction(event -> {
            this.dashboardControl.save();
        });

        this.unlockB.onActionProperty().addListener((observable, oldValue, newValue) -> {
            this.dashboardControl.setEditable(!this.unlockB.isSelected());
        });


        GlobalToolBar.changeBackgroundOnHoverUsingBinding(this.runUpdateButton);


        this.runUpdateButton.setOnAction(event -> {
            this.dashboardControl.switchUpdating();
        });


        newWidgetButton.setOnAction(event -> {
            this.dashboardControl.startWizard();
        });

        this.backgroundButton.setOnAction(event -> {

            this.dashboardControl.startWallpaperSelection();

        });

        settingsButton.setOnAction(event -> {
//            dashboardSettings.openConfig();
            Navigator navigator = new Navigator(this.dashboardControl);
            navigator.show();
        });


        zoomIn.setOnAction(event -> {
            this.dashboardControl.zoomIn();
            ;
        });

        zoomOut.setOnAction(event -> {
            this.dashboardControl.zoomOut();
        });

        ToolBarIntervalSelector toolBarIntervalSelector = new ToolBarIntervalSelector(
                this.dashboardControl.getDataSource(), this.dashboardControl, this.iconSize);


//        toolBarIntervalSelector.getIntervalProperty().addListener((observable, oldValue, newValue) -> {
//            this.dashboardControl.setInterval(newValue);
//        });

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        Separator sep3 = new Separator();
        Separator sep4 = new Separator();


        newButton.setDisable(true);
        delete.setDisable(true);
        save.setDisable(true);
        exportPDF.setVisible(false);

        getItems().clear();
        getItems().addAll(
                this.listAnalysesComboBox
                , sep3, toolBarIntervalSelector
                , sep1, zoomOut, zoomIn, reload
                , sep4, newButton, save, delete, newWidgetButton, settingsButton, this.backgroundButton, exportPDF
                , sep2, this.runUpdateButton, this.unlockB);
    }

    private void setCellFactoryForComboBox() {
        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (empty || obj == null || obj.getName() == null) {
                            setText("");
                        } else {
                            String prefix = "";
                            try {

                                JEVisObject secondParent = obj.getParents().get(0).getParents().get(0);
                                JEVisClass buildingClass = DashBoardToolbar.this.dashboardControl.getDataSource().getJEVisClass("Building");
                                JEVisClass organisationClass = DashBoardToolbar.this.dashboardControl.getDataSource().getJEVisClass("Organization");

                                if (secondParent.getJEVisClass().equals(buildingClass)) {

                                    try {
                                        JEVisObject organisationParent = secondParent.getParents().get(0).getParents().get(0);

                                        if (organisationParent.getJEVisClass().equals(organisationClass)) {

                                            prefix += organisationParent.getName() + " / " + secondParent.getName() + " / ";
                                        }
                                    } catch (JEVisException e) {
                                        logger.error("Could not get Organization parent of " + secondParent.getName() + ":" + secondParent.getID());

                                        prefix += secondParent.getName() + " / ";
                                    }
                                } else if (secondParent.getJEVisClass().equals(organisationClass)) {

                                    prefix += secondParent.getName() + " / ";

                                }

                            } catch (Exception e) {
                            }
                            setText(prefix + obj.getName());
                        }

                    }
                };
            }
        };

        this.listAnalysesComboBox.setCellFactory(cellFactory);
        this.listAnalysesComboBox.setButtonCell(cellFactory.call(null));
    }

    public ToggleButton getBackgroundButton() {
        return this.backgroundButton;
    }
}
