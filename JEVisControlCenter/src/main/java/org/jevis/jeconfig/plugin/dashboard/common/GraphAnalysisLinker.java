package org.jevis.jeconfig.plugin.dashboard.common;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.AnalysisTimeFrame;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.jevis.jeconfig.plugin.dashboard.config.GraphAnalysisLinkerNode;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.Interval;

public class GraphAnalysisLinker {

    public static String ANALYSIS_LINKER_NODE = "analysisLinker";
    private final JEVisDataSource dataSource;
    private GraphAnalysisLinkerNode node = new GraphAnalysisLinkerNode();
    private AggregationPeriod aggregationPeriod;
    private ManipulationMode manipulationMode;
    private Interval interval;
    boolean targetIsreachable = false;
    JFXButton button = new JFXButton("", JEConfig.getImage("1415314386_Graph.png", 20, 20));

    public GraphAnalysisLinker(JEVisDataSource dataSource) {

        this.dataSource = dataSource;
        button.setOnAction(event -> {
            openLink();
        });


    }

    public JFXButton getLinkerButton() {
        return button;
    }

    private void openLink() {
        try {
            if (targetIsreachable) {
                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
                AnalysisRequest analysisRequest = new AnalysisRequest(
                        this.dataSource.getObject(this.node.getGraphAnalysisObject())
                        , aggregationPeriod, manipulationMode, analysisTimeFrame
                        , interval.getStart(), interval.getEnd());

                JEConfig.openObjectInPlugin(GraphPluginView.PLUGIN_NAME, analysisRequest);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText(I18n.getInstance().getString("plugin.dashboard.linker.error.message"));

                alert.showAndWait();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void applyNode(GraphAnalysisLinkerNode node) {
        try {
            this.node = node;
            if (this.node.getGraphAnalysisObject() > 0) {
                JEVisObject jeVisObject = this.dataSource.getObject(this.node.getGraphAnalysisObject());
                if (jeVisObject != null) {
                    Tooltip tooltip = new Tooltip(I18n.getInstance().getString("plugin.dashboard.linker.open") + " " + jeVisObject.getName());
                    button.setTooltip(tooltip);
                    targetIsreachable = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void applyConfig(AggregationPeriod aggregationPeriod, ManipulationMode manipulationMode, Interval interval) {
//        System.out.println("ApplyConfig: " + aggregationPeriod + "  man: " + manipulationMode + "  int: " + interval);
        this.aggregationPeriod = aggregationPeriod;
        this.manipulationMode = manipulationMode;
        this.interval = interval;

    }

//    public void applyConfig(JFXButton button, List<ChartDataModel> dataModels, Interval interval) {
//        button.setOnAction(event -> {
//            try {
//                AggregationPeriod aggregationPeriod = AggregationPeriod.HOURLY;
//                ManipulationMode manipulationMode = ManipulationMode.NONE;
//                for (ChartDataModel dataModel : dataModels) {
//                    aggregationPeriod = dataModel.getAggregationPeriod();
//                    manipulationMode = dataModel.getManipulationMode();
//                    break;
//                }
//                AnalysisTimeFrame analysisTimeFrame = new AnalysisTimeFrame(TimeFrame.CUSTOM);
//                AnalysisRequest analysisRequest = new AnalysisRequest(
//                        this.dataSource.getObject(this.node.getGraphAnalysisObject())
//                        , aggregationPeriod, manipulationMode, analysisTimeFrame
//                        , interval.getStart(), interval.getEnd());
//                JEConfig.openObjectInPlugin(GraphPluginView.PLUGIN_NAME, analysisRequest);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//        try {
//            JEVisObject jeVisObject = this.dataSource.getObject(this.node.getGraphAnalysisObject());
//            if (jeVisObject != null) {
//                Tooltip tooltip = new Tooltip(I18n.getInstance().getString("plugin.dashboard.linker.open") + " " + jeVisObject.getName());
//                button.setTooltip(tooltip);
//            }
//
//        } catch (JEVisException e) {
//            e.printStackTrace();
//        }
//
//    }

}
