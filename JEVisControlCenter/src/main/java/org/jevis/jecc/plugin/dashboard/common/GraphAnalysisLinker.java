package org.jevis.jecc.plugin.dashboard.common;


import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.plugin.AnalysisRequest;
import org.jevis.jecc.plugin.charts.ChartPlugin;
import org.jevis.jecc.plugin.dashboard.config.GraphAnalysisLinkerNode;
import org.joda.time.Interval;

public class GraphAnalysisLinker {

    public static String ANALYSIS_LINKER_NODE = "analysisLinker";
    private final JEVisDataSource dataSource;
    boolean targetIsreachable = false;
    Button button = new Button("", ControlCenter.getImage("1415314386_Graph.png", 20, 20));
    private GraphAnalysisLinkerNode node = new GraphAnalysisLinkerNode();
    private AggregationPeriod aggregationPeriod;
    private ManipulationMode manipulationMode;
    private Interval interval;

    public GraphAnalysisLinker(JEVisDataSource dataSource) {
        button.setStyle("-fx-background-color: transparent;");

        this.dataSource = dataSource;
        button.setOnAction(event -> {
            openLink();
        });


    }

    public Button getLinkerButton() {
        return button;
    }

    private void openLink() {
        try {
            if (targetIsreachable) {
                AnalysisRequest analysisRequest = new AnalysisRequest(
                        this.dataSource.getObject(this.node.getGraphAnalysisObject())
                        , aggregationPeriod, manipulationMode
                        , interval.getStart(), interval.getEnd());

                ControlCenter.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, analysisRequest);
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

//    public void applyConfig(Button button, List<ChartDataModel> dataModels, Interval interval) {
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
//                Tooltip tooltip = new  Tooltip(I18n.getInstance().getString("plugin.dashboard.linker.open") + " " + jeVisObject.getName());
//                button.setTooltip(tooltip);
//            }
//
//        } catch (JEVisException e) {
//            e.printStackTrace();
//        }
//
//    }

}
