package org.jevis.jecc.application.Chart.ChartPluginElements;

import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.Icon;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.ChartPluginElements.tabs.ChartTab;
import org.jevis.jecc.application.Chart.ChartPluginElements.tabs.CommonSettingTab;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.Chart.data.DataModel;
import org.jevis.jecc.dialog.Response;

import java.util.ArrayList;
import java.util.List;

public class NewSelectionDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(NewSelectionDialog.class);
    private final JEVisDataSource ds;
    private final DataModel dataModel;
    private Response response = Response.CANCEL;

    public NewSelectionDialog(JEVisDataSource ds, DataModel dataModel) {
        super();

        setTitle(I18n.getInstance().getString("plugin.graph.newselectiondialog.title"));
        setHeaderText(I18n.getInstance().getString("plugin.graph.newselectiondialog.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        this.ds = ds;
        this.dataModel = dataModel;

        TabPane tabPane = new TabPane();

        if (this.dataModel.getChartModels().isEmpty()) {
            ChartModel chartModel = new ChartModel();
            chartModel.setChartId(0);
            chartModel.setChartName(I18n.getInstance().getString("graph.title"));
            this.dataModel.getChartModels().add(chartModel);
        }

        CommonSettingTab commonSettingTab = new CommonSettingTab(this.dataModel);

        for (ChartModel chartModel : this.dataModel.getChartModels()) {
            ChartTab chartTab = new ChartTab(ds, chartModel);
            chartTab.setClosable(true);

            chartTab.setOnClosed(event -> this.dataModel.getChartModels().remove(chartModel));

            tabPane.getTabs().add(chartTab);
        }

        Tab addTab = new Tab();
        addTab.setGraphic(ControlCenter.getSVGImage(Icon.PLUS, 12, 12));
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                ChartModel chartModel = new ChartModel();
                chartModel.setChartName(getNextChartName());
                chartModel.setChartId(getNextChartId());
                this.dataModel.getChartModels().add(chartModel);
                ChartTab newChartTab = new ChartTab(ds, chartModel);
                newChartTab.setOnClosed(event -> this.dataModel.getChartModels().remove(chartModel));

                tabPane.getTabs().add(tabPane.getTabs().size() - 2, newChartTab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 3);
            }
        });

        tabPane.getTabs().add(addTab);
        tabPane.getTabs().add(commonSettingTab);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(event -> {
            try {
                response = Response.OK;
                this.close();
            } catch (Exception e) {

            }
        });

        cancelButton.setOnAction(event -> {
            response = Response.CANCEL;
            this.close();
        });

        this.getDialogPane().setContent(tabPane);
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
