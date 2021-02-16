package org.jevis.jeconfig.dialog;

import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;

public class NewAnalysisDialog {
    private final JEVisDataSource ds;
    private final AnalysisDataModel model;
    private final ChartPlugin chartPlugin;
    private Boolean changed;

    public NewAnalysisDialog(JEVisDataSource ds, AnalysisDataModel model, ChartPlugin chartPlugin, Boolean changed) {
        this.ds = ds;
        this.model = model;
        this.chartPlugin = chartPlugin;
        this.changed = changed;

        newAnalysis();
    }

    private void newAnalysis() {

        AnalysisDataModel newModel = new AnalysisDataModel(ds, chartPlugin);

        ChartSelectionDialog selectionDialog = new ChartSelectionDialog(ds, newModel);

        if (selectionDialog.show() == Response.OK) {

            model.setCurrentAnalysis(null);
            model.setCharts(selectionDialog.getChartPlugin().getData().getCharts());
            model.setSelectedData(selectionDialog.getChartPlugin().getData().getSelectedData());
            changed = true;
        }
    }
}
