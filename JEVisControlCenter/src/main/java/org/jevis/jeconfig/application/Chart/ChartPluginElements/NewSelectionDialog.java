package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tabs.ChartTab;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.tabs.CommonSettingTab;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.Chart.data.DataModel;
import org.jevis.jeconfig.dialog.Response;

import java.util.ArrayList;
import java.util.List;

public class NewSelectionDialog extends JFXDialog {
    private static final Logger logger = LogManager.getLogger(NewSelectionDialog.class);
    private final JEVisDataSource ds;
    private final DataModel newDataModel;
    private Response response = Response.CANCEL;

    public NewSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, DataModel dataModel) {
        super();

        this.setDialogContainer(dialogContainer);
        this.ds = ds;
        this.newDataModel = dataModel;

        VBox mainBox = new VBox();
        mainBox.setPadding(new Insets(12));
        mainBox.setSpacing(8);

        TabPane tabPane = new TabPane();

        if (newDataModel.getChartModels().isEmpty()) {
            ChartModel chartModel = new ChartModel();
            chartModel.setChartId(0);
            chartModel.setChartName(I18n.getInstance().getString("graph.title"));
            newDataModel.getChartModels().add(chartModel);
        }

        CommonSettingTab commonSettingTab = new CommonSettingTab(newDataModel);

        tabPane.getTabs().add(commonSettingTab);

        for (ChartModel chartModel : newDataModel.getChartModels()) {
            ChartTab chartTab = new ChartTab(dialogContainer, ds, chartModel);
            chartTab.setAggregationPeriodColumnVisible(false);
            chartTab.setManipulationModeColumnVisible(false);

            chartTab.setOnClosed(event -> newDataModel.getChartModels().remove(chartModel));

            tabPane.getTabs().add(chartTab);
        }

        Tab addTab = new Tab();
        addTab.setGraphic(JEConfig.getSVGImage(Icon.PLUS, 8, 10));
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                ChartModel chartModel = new ChartModel();
                chartModel.setChartName(getNextChartName());
                newDataModel.getChartModels().add(chartModel);
                ChartTab newChartTab = new ChartTab(getDialogContainer(), ds, chartModel);
                newChartTab.setAggregationPeriodColumnVisible(false);
                newChartTab.setManipulationModeColumnVisible(false);

                tabPane.getTabs().add(tabPane.getTabs().size() - 1, newChartTab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
            }
        });

        tabPane.getTabs().add(addTab);

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {
                response = Response.OK;
                this.close();
            } catch (Exception e) {

            }
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> this.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        mainBox.getChildren().addAll(tabPane, buttonBar);
        mainBox.setMinHeight(960);
        mainBox.setMinWidth(1024);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        this.setContent(mainBox);
    }


    private String getNextChartName() {
        List<String> oldNames = new ArrayList<>();
        for (ChartModel chartModel : newDataModel.getChartModels()) {
            oldNames.add(chartModel.getChartName());
        }

        String newName = I18n.getInstance().getString("graph.title");
        int i = 1;
        boolean found = false;
        while (oldNames.contains(newName)) {
            for (String s : oldNames) {
                if (s.equals(newName)) {
                    found = true;
                    i++;
                }
            }

        }

        if (found) {
            newName += " " + i;
        }

        return newName;
    }

    public Response getResponse() {
        return response;
    }
}
