package org.jevis.jeconfig.dialog;

import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;

public class NewAnalysisDialog {
    private JEVisDataSource ds;
    private AnalysisDataModel model;
    private GraphPluginView graphPluginView;
    private Boolean changed;

    public NewAnalysisDialog(JEVisDataSource ds, AnalysisDataModel model, GraphPluginView graphPluginView, Boolean changed) {
        this.ds = ds;
        this.model = model;
        this.graphPluginView = graphPluginView;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        AnalysisDataModel newModel = new AnalysisDataModel(ds, graphPluginView);

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

        if (selectionDialog.show() == Response.OK) {

            model.setCurrentAnalysis(null);
            model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
            model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());
            changed = true;
        }
    }
}
