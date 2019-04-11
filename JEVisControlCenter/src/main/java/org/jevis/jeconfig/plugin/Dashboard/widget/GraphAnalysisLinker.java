package org.jevis.jeconfig.plugin.Dashboard.widget;

import com.jfoenix.controls.JFXButton;
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.Dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.joda.time.Interval;

import java.util.List;

public class GraphAnalysisLinker {

    public static String ANALYSIS_LINKER_NODE = "analysisLinker";
    private final JEVisDataSource dataSource;
    private GraphAnalysisLinkerNode node = new GraphAnalysisLinkerNode();

    public GraphAnalysisLinker(JEVisDataSource dataSource, GraphAnalysisLinkerNode jsonNode) {
        if (jsonNode != null) {
            this.node = jsonNode;
        }

        this.dataSource = dataSource;
    }

    public JFXButton buildLinkerButton() {
        JFXButton button = new JFXButton("", JEConfig.getImage("1415314386_Graph.png", 20, 20));
        return button;
    }

    public void applyConfig(JFXButton button, List<ChartDataModel> dataModels, Interval interval) {
        button.setOnAction(event -> {
            try {
                AggregationPeriod aggregationPeriod = AggregationPeriod.HOURLY;
                ManipulationMode manipulationMode = ManipulationMode.TOTAL;
                for (ChartDataModel dataModel : dataModels) {
                    aggregationPeriod = dataModel.getAggregationPeriod();
                    manipulationMode = dataModel.getManipulationMode();
                    break;
                }
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                AnalysisRequest analysisRequest = new AnalysisRequest(
                        dataSource.getObject(node.getGraphAnalysisObject())
                        , aggregationPeriod, manipulationMode, analysisTimeFrame
                        , interval.getStart(), interval.getEnd());
                System.out.println("Open Analysis: " + node.getGraphAnalysisObject());
                JEConfig.openObjectInPlugin(GraphPluginView.PLUGIN_NAME, analysisRequest);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

}
