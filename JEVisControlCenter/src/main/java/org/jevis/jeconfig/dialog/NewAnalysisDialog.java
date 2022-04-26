package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;

public class NewAnalysisDialog {
    private final StackPane dialogContainer;
    private final JEVisDataSource ds;
    private final AnalysisDataModel model;
    private final ChartPlugin chartPlugin;
    private final Boolean changed;

    public NewAnalysisDialog(StackPane DialogContainer, JEVisDataSource ds, AnalysisDataModel model, ChartPlugin chartPlugin, Boolean changed) {
        dialogContainer = DialogContainer;
        this.ds = ds;
        this.model = model;
        this.chartPlugin = chartPlugin;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        AnalysisDataModel newModel = new AnalysisDataModel(ds, chartPlugin);

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(dialogContainer, ds, newModel);

        selectionDialog.setOnDialogClosed(event -> {
            if (selectionDialog.getResponse() == Response.OK) {

                model.setCurrentAnalysis(null);
                model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
                model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());
                chartPlugin.handleRequest(Constants.Plugin.Command.SAVE);
                Platform.runLater(() -> chartPlugin.getToolBarView().setDisableToolBarIcons(false));
            }

            JEVisHelp.getInstance().deactivatePluginModule();
        });

        selectionDialog.show();
    }
}
