package org.jevis.jeconfig.dialog;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.Constants;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.NewSelectionDialog;
import org.jevis.jeconfig.application.Chart.data.DataModel;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;

public class NewAnalysisDialog {
    private final StackPane dialogContainer;
    private final JEVisDataSource ds;
    private final DataModel model;
    private final ChartPlugin chartPlugin;
    private final Boolean changed;

    public NewAnalysisDialog(StackPane DialogContainer, JEVisDataSource ds, DataModel model, ChartPlugin chartPlugin, Boolean changed) {
        dialogContainer = DialogContainer;
        this.ds = ds;
        this.model = model;
        this.chartPlugin = chartPlugin;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        model.reset();

        NewSelectionDialog newSelectionDialog = new NewSelectionDialog(dialogContainer, ds, model);
        newSelectionDialog.setOnDialogClosed(event -> {
            if (newSelectionDialog.getResponse() == Response.OK) {

                chartPlugin.handleRequest(Constants.Plugin.Command.SAVE);
                Platform.runLater(() -> chartPlugin.getToolBarView().setDisableToolBarIcons(false));
            }
            JEVisHelp.getInstance().deactivatePluginModule();
        });

        newSelectionDialog.show();
    }
}
