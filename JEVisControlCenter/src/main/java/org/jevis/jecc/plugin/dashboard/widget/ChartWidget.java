package org.jevis.jecc.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.Chart.ChartElements.CustomNumericAxis;
import org.jevis.jecc.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jecc.application.Chart.ChartType;
import org.jevis.jecc.application.Chart.Charts.HeatMapChart;
import org.jevis.jecc.application.Chart.Charts.TableChartV;
import org.jevis.jecc.application.Chart.Charts.XYChart;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.application.Chart.data.ChartModel;
import org.jevis.jecc.application.tools.ColorHelper;
import org.jevis.jecc.plugin.charts.DataSettings;
import org.jevis.jecc.plugin.charts.ToolBarSettings;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.config.WidgetConfig;
import org.jevis.jecc.plugin.dashboard.config2.JsonNames;
import org.jevis.jecc.plugin.dashboard.config2.Size;
import org.jevis.jecc.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jecc.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChartWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ChartWidget.class);
    public static String WIDGET_ID = "Chart";
    // private final WidgetLegend legend = new WidgetLegend();
    private final BorderPane borderPane = new BorderPane();
    private final BorderPane bottomBorderPane = new BorderPane();
    private XYChart xyChart;
    private HeatMapChart heatMapChart;
    private Interval lastInterval = null;
    private Boolean customWorkDay = true;

    public ChartWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
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
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 12, control.getActiveDashboard().xGridInterval * 20));

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

        this.sampleHandler.setInterval(interval);
        this.sampleHandler.update();

        try {
            Platform.runLater(() -> {
                this.borderPane.setCenter(null);
                this.xyChart = null;
                this.heatMapChart = null;
                //         this.legend.getItems().clear();
            });

            this.sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    //            Platform.runLater(() -> this.legend.getItems().add(
                    //                  this.legend.buildHorizontalLegendItem(chartDataModel.getName(), chartDataModel.getColor(),
                    //                        this.config.getFontColor(), this.config.getFontSize(), chartDataModel.getObject(),
                    //                      chartDataModel.getSamples().isEmpty(), I18n.getInstance().getString("plugin.dashboard.alert.nodata"), false)));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            /**
             * LineChart does not support updateData, so we need to create a new one every time;
             */

            ToolBarSettings toolBarSettings = new ToolBarSettings();
            toolBarSettings.setShowIcons(false);
            toolBarSettings.setCustomWorkday(customWorkDay);

            DataSettings dataSettings = new DataSettings();
            dataSettings.setCurrentAnalysis(control.getActiveDashboard().getDashboardObject());

            ChartModel chartModel = this.sampleHandler.getChartModel();
            chartModel.setMinFractionDigits(getConfig().getDecimals());
            chartModel.setMaxFractionDigits(getConfig().getDecimals());

            boolean isOnlyTable = true;
            for (ChartDataRow chartDataRow : this.sampleHandler.getDataModel()) {
                if (!chartDataRow.getChartType().equals(ChartType.TABLE_V)) {
                    isOnlyTable = false;
                    break;
                }
            }

            if (isOnlyTable) {
                Platform.runLater(() -> {
                    TableChartV tableChart = new TableChartV(getDataSource(), chartModel);
                    tableChart.showRowSums(true);

                    Label titleLabel = new Label(chartModel.getChartName());
                    titleLabel.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
                    titleLabel.setAlignment(Pos.CENTER);
                    HBox hBox = new HBox(8, titleLabel, tableChart.getFilterEnabledBox());
                    hBox.setAlignment(Pos.CENTER);


                    TableHeaderTable tableHeaderTable = new TableHeaderTable(tableChart.getXyChartSerieList());
                    tableChart.setTableHeader(tableHeaderTable);
                    tableHeaderTable.maxWidthProperty().bind(this.borderPane.widthProperty());

                    this.xyChart = tableChart;
//                    chartModel.setShowSum_NOEVENT(true);
                    this.xyChart.createChart(this.sampleHandler.getDataModel(), toolBarSettings, dataSettings, true);

                    VBox vBox = new VBox(hBox, tableHeaderTable);
                    VBox.setVgrow(hBox, Priority.NEVER);
                    VBox.setVgrow(tableHeaderTable, Priority.ALWAYS);
                    this.borderPane.setCenter(vBox);
                    //           this.legend.getItems().clear();
                });
            } else {
                boolean isHeatMap = this.sampleHandler.getDataModel().stream().anyMatch(chartDataRow -> chartDataRow.getChartType() == ChartType.HEAT_MAP);

                if (isHeatMap) {
                    chartModel.setChartType(ChartType.HEAT_MAP);
                    chartModel.setColorMapping(ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);

                    Platform.runLater(() -> {
                        // TODO JFX17 legend
                        //  this.legend.getItems().clear();
                        this.heatMapChart = new HeatMapChart(getDataSource(), chartModel, sampleHandler.getDataModel(), getConfig().getBackgroundColor(), getConfig().getFontColor());
                        this.heatMapChart.getRegion().setPrefSize(getConfig().getSize().getWidth() - 20, getConfig().getSize().getHeight());
                        this.borderPane.setCenter(this.heatMapChart.getRegion());
                    });
                } else {
                    Platform.runLater(() -> {
                        this.xyChart = new XYChart(getDataSource(), chartModel);
                        this.xyChart.createChart(this.sampleHandler.getDataModel(), toolBarSettings, dataSettings, true);

                        this.borderPane.setCenter(this.xyChart.getChart());
                    });
                }
            }

            Size configSize = getConfig().getSize();
            if (xyChart != null) {
                Platform.runLater(() -> xyChart.getChart().setPrefSize(configSize.getWidth() - 20, configSize.getHeight()));
            } else if (heatMapChart != null) {
//                Platform.runLater(() -> formatHeatMapChart(this.heatMapChart));
            }
            updateConfig();

            /** workaround because we make a new chart every time**/
        } catch (Exception ex) {
            logger.error(ex);
        }

        showProgressIndicator(false);

    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

        String cssBGColor = ColorHelper.toRGBCode(Color.TRANSPARENT);
        Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
        Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
        String fontColor = ColorHelper.toRGBCode(this.config.getFontColor());

        Platform.runLater(() -> {
            try {

                this.setBackground(bgColorTrans);
                //          if (this.legend != null) {
                //            this.legend.setBackground(bgColorTrans);
                //          this.legend.setStyle("-fx-background-color: transparent; -fx-text-color: " + fontColor + ";");
                //          }
                this.borderPane.setBackground(bgColor);

                try {
                    if (xyChart != null) {
                        //lineChart.getChart().getPlotBackground().setBackground(bgColor);
                        //lineChart.setBackGround(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

                        xyChart.getChart().getPlotBackground().setStyle("-fx-background-color: " + cssBGColor + ";");
                        xyChart.getChart().setStyle("-fx-background-color: " + cssBGColor + ";");

                        this.xyChart.getChart().getAxes().forEach(axis -> {
                            if (axis instanceof CustomNumericAxis) {
                                CustomNumericAxis defaultNumericAxis = (CustomNumericAxis) axis;
                                defaultNumericAxis.getAxisLabel().setVisible(false);
                                defaultNumericAxis.setStyle("-fx-tick-label-fill: " + fontColor + ";");
                            }
                        });

                        xyChart.getChart().requestLayout();
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
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
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public void init() {
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), WIDGET_ID);
        this.sampleHandler.setMultiSelect(true);

        //      this.legend.setAlignment(Pos.CENTER);


//        bottomBorderPane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("bottomBorderPane: " + newValue);
//        });
        this.borderPane.setBottom(bottomBorderPane);
        //  this.borderPane.setBottom(this.legend);
        setGraphic(this.borderPane);


        /** Dummy chart **/
        //this.lineChart = new LineChart(new AnalysisDataModel(getDataSource(),new GraphPluginView(getDataSource(),"dummy")) , this.sampleHandler.getDataModel(), 0, "");
        //this.borderPane.setCenter(lineChart.getChart());
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
        return ControlCenter.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }

    @Override
    public DataModelDataHandler getDataHandler() {
        return this.sampleHandler;
    }

    @Override
    public void setDataHandler(DataModelDataHandler dataHandler) {
        this.sampleHandler = dataHandler;
    }

    @Override
    public void setCustomWorkday(Boolean customWorkday) {
        this.customWorkDay = customWorkday;
    }

}