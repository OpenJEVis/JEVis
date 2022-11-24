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
    private final DataModel dataModel;
    private Response response = Response.CANCEL;

    public NewSelectionDialog(StackPane dialogContainer, JEVisDataSource ds, DataModel dataModel) {
        super();

        this.setDialogContainer(dialogContainer);
        this.ds = ds;
        this.dataModel = dataModel;

        VBox mainBox = new VBox();
        mainBox.setPadding(new Insets(12));
        mainBox.setSpacing(8);

        TabPane tabPane = new TabPane();

        if (this.dataModel.getChartModels().isEmpty()) {
            ChartModel chartModel = new ChartModel();
            chartModel.setChartId(0);
            chartModel.setChartName(I18n.getInstance().getString("graph.title"));
            this.dataModel.getChartModels().add(chartModel);
        }

        CommonSettingTab commonSettingTab = new CommonSettingTab(this.dataModel);

        for (ChartModel chartModel : this.dataModel.getChartModels()) {
            ChartTab chartTab = new ChartTab(dialogContainer, ds, chartModel);
            chartTab.setClosable(true);

            chartTab.setOnClosed(event -> this.dataModel.getChartModels().remove(chartModel));

            tabPane.getTabs().add(chartTab);
        }

        Tab addTab = new Tab();
        addTab.setGraphic(JEConfig.getSVGImage(Icon.PLUS, 12, 12));
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                ChartModel chartModel = new ChartModel();
                chartModel.setChartName(getNextChartName());
                chartModel.setChartId(getNextChartId());
                this.dataModel.getChartModels().add(chartModel);
                ChartTab newChartTab = new ChartTab(getDialogContainer(), ds, chartModel);
                newChartTab.setOnClosed(event -> this.dataModel.getChartModels().remove(chartModel));

                tabPane.getTabs().add(tabPane.getTabs().size() - 2, newChartTab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 3);
            }
        });

        tabPane.getTabs().add(addTab);
        tabPane.getTabs().add(commonSettingTab);

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {
                response = Response.OK;
                this.close();
            } catch (Exception e) {

            }
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> {
            response = Response.CANCEL;
            this.close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        mainBox.getChildren().addAll(tabPane, buttonBar);
        mainBox.setMinHeight(dialogContainer.getHeight() - 120);
        mainBox.setMinWidth(1024);

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        this.setContent(mainBox);
    }

    private int getNextChartId() {
        int id = 0;
        for (ChartModel chartModel : dataModel.getChartModels()) {
            id = Math.max(id, chartModel.getChartId());
        }

        id++;

        return id;
    }


    private String getNextChartName() {
        List<String> oldNames = new ArrayList<>();
        for (ChartModel chartModel : dataModel.getChartModels()) {
            oldNames.add(chartModel.getChartName());
        }

        String newName = I18n.getInstance().getString("graph.title");
        int i = 1;

        while (oldNames.contains(newName)) {
            for (String s : oldNames) {
                if (s.equals(newName)) {
                    i++;

                    if (newName.contains(" ")) {
                        newName = newName.substring(0, newName.lastIndexOf(" "));
                    }
                    newName += " " + i;
                }
            }

        }

        return newName;
    }

    public Response getResponse() {
        return response;
    }
}
