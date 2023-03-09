package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tabs.ChartTab;
import org.jevis.jeconfig.application.Chart.data.ChartData;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.widget.GenericConfigNode;
import org.jevis.jeconfig.plugin.dashboard.widget.ValueWidget;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.jevis.jeconfig.tool.ScreenSize;

public class WidgetConfigDialog extends Alert {

    private static final Logger logger = LogManager.getLogger(WidgetConfigDialog.class);
    private final TabPane tabPane = new TabPane();
    private final StackPane dialogContainer;
    private DataModelDataHandler dataModelDataHandler;
    //private WidgetTreePlugin widgetTreePlugin;
    private final Widget widget;
    private ChartModel chartModel;

    /**
     * Create an new Widget Config Dialog.
     */
    public WidgetConfigDialog(Widget widget) {
        super(AlertType.INFORMATION);

        this.widget = widget;
        setTitle(I18n.getInstance().getString("dashboard.widget.editor.title"));
        setHeaderText(I18n.getInstance().getString("dashboard.widget.editor.header"));
        setResizable(true);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.tabPane);
        borderPane.setPrefWidth(ScreenSize.fitScreenWidth(1500));
        borderPane.setPrefHeight(650d);

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        dialogContainer = new StackPane(borderPane);
        getDialogPane().setContent(dialogContainer);

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);
    }


    public void requestFirstTabFocus() {
        Platform.runLater(() -> tabPane.getTabs().get(0).getContent().requestFocus());
    }

    private void addGeneralTab(DataModelDataHandler dataModelDataHandler) {
        GenericConfigNode genericConfigNode = new GenericConfigNode(widget.getDataSource(), widget, dataModelDataHandler);
        addTab(genericConfigNode);
    }

    public void addTab(Tab tab) {
        this.tabPane.getTabs().add(tab);
    }

    public void addGeneralTabsDataModel(DataModelDataHandler dataModelDataHandler) {
        addGeneralTab(dataModelDataHandler);

        if (dataModelDataHandler != null) {
            this.dataModelDataHandler = dataModelDataHandler;

            chartModel = dataModelDataHandler.getChartModel();
            ChartTab chartTab = new ChartTab(dialogContainer, dataModelDataHandler.getJeVisDataSource(), chartModel);
            chartTab.setText(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));
            chartTab.setClosable(false);
            chartTab.setMenuVisible(true);
            chartTab.setIntervalColumnVisible(false);

            if (widget instanceof ValueWidget) {
                chartTab.setCommonChartSettingsVisible(false);
                chartTab.setColorColumnVisible(false);
                chartTab.setAxisColumnVisible(false);
                chartTab.setChartTypeColumnVisible(false);
                chartTab.setCssColumnVisible(false);
                chartTab.setNameColumnVisible(false);
                chartTab.setAggregationPeriodColumnVisible(true);
                chartTab.setManipulationModeColumnVisible(true);
            }

            addTab(chartTab);
        }

    }
    public ObservableList<ChartData> addGeneralTabsDataModelNetGraph(DataModelDataHandler dataModelDataHandler) {
        addGeneralTab(dataModelDataHandler);

        if (dataModelDataHandler != null) {
            this.dataModelDataHandler = dataModelDataHandler;

            chartModel = dataModelDataHandler.getChartModel();
            ChartTab chartTab = new ChartTab(dialogContainer, dataModelDataHandler.getJeVisDataSource(), chartModel);

            chartTab.setText(I18n.getInstance().getString("plugin.dashboard.widget.config.tab.datamodel"));
            chartTab.setClosable(false);
            chartTab.setMenuVisible(false);
            chartTab.setIntervalColumnVisible(false);

            if (widget instanceof ValueWidget) {
                chartTab.setCommonChartSettingsVisible(false);
                chartTab.setColorColumnVisible(false);
                chartTab.setAxisColumnVisible(false);
                chartTab.setChartTypeColumnVisible(false);
                chartTab.setCssColumnVisible(false);
                chartTab.setNameColumnVisible(false);
            }

            addTab(chartTab);
            chartTab.getChartTable().getItems().addListener(new ListChangeListener<ChartData>() {
                @Override
                public void onChanged(Change<? extends ChartData> c) {
                    System.out.println("table changed");
                    System.out.println(c);
                }
            });
            return chartTab.getChartTable().getItems();
        }
        return null;

    }


    public void commitSettings() {
        this.tabPane.getTabs().forEach(tab -> {
            try {
                if (tab instanceof ConfigTab) {
                    ((ConfigTab) tab).commitChanges();
                } else if (tab instanceof ChartTab) {
                    dataModelDataHandler.setChartModel(chartModel);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        logger.debug("done wigetconfig commit for: {}", getTitle());
    }
}
