package org.jevis.jeconfig.dialog;

import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.GraphDataModel;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;

public class NewAnalysisDialog {
    private JEVisDataSource ds;
    private GraphDataModel model;
    private GraphPluginView graphPluginView;
    private Boolean changed;

    public NewAnalysisDialog(JEVisDataSource ds, GraphDataModel model, GraphPluginView graphPluginView, Boolean changed) {
        this.ds = ds;
        this.model = model;
        this.graphPluginView = graphPluginView;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        GraphDataModel newModel = new GraphDataModel(ds, graphPluginView);

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

        if (selectionDialog.show() == Response.OK) {

            model.setCurrentAnalysis(null);
            model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
            model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());
            changed = true;
        }
    }
}
