package org.jevis.jecc.dialog;

import javafx.application.Platform;
import org.jevis.api.JEVisDataSource;
import org.jevis.jecc.Constants;
import org.jevis.jecc.application.Chart.ChartPluginElements.NewSelectionDialog;
import org.jevis.jecc.application.Chart.data.DataModel;
import org.jevis.jecc.application.tools.JEVisHelp;
import org.jevis.jecc.plugin.charts.ChartPlugin;

public class NewAnalysisDialog {
    private final JEVisDataSource ds;
    private final DataModel model;
    private final ChartPlugin chartPlugin;
    private final Boolean changed;

    public NewAnalysisDialog(JEVisDataSource ds, DataModel model, ChartPlugin chartPlugin, Boolean changed) {
        this.ds = ds;
        this.model = model;
        this.chartPlugin = chartPlugin;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        model.reset();

        NewSelectionDialog newSelectionDialog = new NewSelectionDialog(ds, model);
        newSelectionDialog.setOnCloseRequest(event -> {
            if (newSelectionDialog.getResponse() == Response.OK) {

                chartPlugin.handleRequest(Constants.Plugin.Command.SAVE);
                Platform.runLater(() -> chartPlugin.getToolBarView().setDisableToolBarIcons(false));
            }
            JEVisHelp.getInstance().deactivatePluginModule();
        });

        newSelectionDialog.show();
    }
}
