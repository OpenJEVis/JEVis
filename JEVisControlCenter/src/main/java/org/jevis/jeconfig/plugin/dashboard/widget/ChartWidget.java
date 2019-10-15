package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.LineChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisLineChart;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetLegend;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChartWidget extends Widget {

    private static final Logger logger = LogManager.getLogger(PieChart.class);
    public static String WIDGET_ID = "Chart";

    private LineChart lineChart;
    private DataModelDataHandler sampleHandler;
    private WidgetLegend legend = new WidgetLegend();
    private JFXButton openAnalysisButton = new JFXButton();
    private ObjectMapper mapper = new ObjectMapper();
    private BorderPane borderPane = new BorderPane();
    private Interval lastInterval = null;

    private boolean autoAggregation = true;

    public ChartWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public ChartWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public void debug() {
        sampleHandler.debug();
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle("new Chart Widget");
        widgetPojo.setType(typeID());


        return widgetPojo;
    }

    @Override
    public void updateData(Interval interval) {
        logger.debug("Chart.Update: {}", interval);
        this.lastInterval = interval;
        if (sampleHandler == null) {
            showProgressIndicator(false);
            return;
        } else {
            showProgressIndicator(true);
        }

        showProgressIndicator(true);
        showAlertOverview(false, "");

        this.lineChart.setChartSettings(chart1 -> {
            MultiAxisLineChart multiAxisLineChart = (MultiAxisLineChart) chart1;
//                multiAxisLineChart.setAnimated(true);
            this.lineChart.getChart().setAnimated(false);

            multiAxisLineChart.setLegendSide(Side.BOTTOM);
            multiAxisLineChart.setLegendVisible(true);

        });

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();

        Platform.runLater(() -> {
            try {
                this.legend.getItems().clear();
                this.sampleHandler.getDataModel().forEach(chartDataModel -> {
                    try {
                        String dataName = chartDataModel.getObject().getName();
                        this.legend.getItems().add(
                                this.legend.buildLegendItem(dataName + " " + chartDataModel.getUnit(), chartDataModel.getColor(),
                                        this.config.getFontColor(), this.config.getFontSize(), chartDataModel.getObject()));
                        if (chartDataModel.getSamples().isEmpty()) {
                            showAlertOverview(true, "");
                        }

                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                });
                /**
                 * LineChart does not support updateData so we need to create an new one every time;
                 */
                this.lineChart = new LineChart(this.sampleHandler.getDataModel(),
                        false, false, false, false,
                        false, null, -1,
                        ManipulationMode.NONE, 0, "");

                this.lineChart.getChart().layout();
                this.borderPane.setCenter(this.lineChart.getChart());
                updateConfig();/** workaround because we make a new chart everytime**/
            } catch (Exception ex) {
                logger.error(ex);
            }

            showProgressIndicator(false);
        });
    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                setChartLabel((MultiAxisLineChart) this.lineChart.getChart(), this.config.getFontColor());
                this.setBackground(bgColorTrans);
                this.legend.setBackground(bgColorTrans);
//            this.legend.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                //            legend.setBackground(new Background(new BackgroundFill(config.backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)));
//            lineChart.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Inset
                this.borderPane.setBackground(bgColor);
                this.lineChart.applyColors();
                this.layout();
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public List<DateTime> getMaxTimeStamps() {
        if (sampleHandler != null) {
            return sampleHandler.getMaxTimeStamps();
        } else {
            return new ArrayList<>();
        }
    }


    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Runnable task = () -> {
                    widgetConfigDialog.commitSettings();
                    updateConfig(getConfig());
                    updateData(lastInterval);
                };
                control.getExecutor().submit(task);


            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    private void setChartLabel(MultiAxisLineChart chart, Color newValue) {
        chart.getY1Axis().setTickLabelFill(newValue);
        chart.getXAxis().setTickLabelFill(newValue);

        chart.getXAxis().setLabel("");
        chart.getY1Axis().setLabel("");
    }

    @Override
    public void init() {
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
        this.sampleHandler.setMultiSelect(true);

        this.lineChart = new LineChart(this.sampleHandler.getDataModel(), false, false, false, false, false, null, -1, ManipulationMode.NONE, 0, "");


        this.legend.setAlignment(Pos.CENTER);

        BorderPane bottomBorderPane = new BorderPane();
        bottomBorderPane.setCenter(this.legend);
        bottomBorderPane.setRight(this.openAnalysisButton);

        this.borderPane.setCenter(this.lineChart.getChart());
        this.borderPane.setBottom(bottomBorderPane);
        setGraphic(this.borderPane);


    }

    @Override
    public String typeID() {
        return WIDGET_ID;
    }

    @Override
    public ObjectNode toNode() {
        ObjectNode dashBoardNode = super.createDefaultNode();
        dashBoardNode
                .set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());
        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


}
