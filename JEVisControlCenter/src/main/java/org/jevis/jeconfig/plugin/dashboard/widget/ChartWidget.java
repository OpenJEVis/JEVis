package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.hansolo.fx.charts.MatrixPane;
import eu.hansolo.fx.charts.data.MatrixChartItem;
import eu.hansolo.fx.charts.tools.ColorMapping;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.CustomNumericAxis;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.Chart.Charts.HeatMapChart;
import org.jevis.jeconfig.application.Chart.Charts.TableChartV;
import org.jevis.jeconfig.application.Chart.Charts.XYChart;
import org.jevis.jeconfig.application.Chart.data.AnalysisDataModel;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.common.WidgetLegend;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ChartWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ChartWidget.class);
    public static String WIDGET_ID = "Chart";

    private XYChart xyChart;
    private HeatMapChart heatMapChart;
    private DataModelDataHandler sampleHandler;
    private final WidgetLegend legend = new WidgetLegend();
    private final BorderPane borderPane = new BorderPane();
    private Interval lastInterval = null;
    private final BorderPane bottomBorderPane = new BorderPane();
    private Boolean customWorkDay = true;

    public ChartWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
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
                this.legend.getItems().clear();
            });

            this.sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    Platform.runLater(() -> this.legend.getItems().add(
                            this.legend.buildHorizontalLegendItem(chartDataModel.getTitle(), ColorHelper.toColor(chartDataModel.getColor()),
                                    this.config.getFontColor(), this.config.getFontSize(), chartDataModel.getObject(),
                                    chartDataModel.getSamples().isEmpty(), I18n.getInstance().getString("plugin.dashboard.alert.nodata"), false)));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            /**
             * LineChart does not support updateData, so we need to create a new one every time;
             */
            AnalysisDataModel model = new AnalysisDataModel(getDataSource(), null);
            model.setCurrentAnalysisNOEVENT(control.getActiveDashboard().getDashboardObject());
            model.setHideShowIconsNO_EVENT(false);
            model.setCustomWorkDayNO_EVENT(customWorkDay);
            ChartSetting chartSetting = new ChartSetting(0, "");
            chartSetting.setChartType(null);
            chartSetting.setMinFractionDigits(getConfig().getDecimals());
            chartSetting.setMaxFractionDigits(getConfig().getDecimals());
//            chartSetting.setName(getConfig().getTitle());
            model.getCharts().setListSettings(Collections.singletonList(chartSetting));

            boolean isOnlyTable = true;
            for (ChartDataRow chartDataRow : this.sampleHandler.getDataModel()) {
                if (!chartDataRow.getChartType().equals(ChartType.TABLE_V)) {
                    isOnlyTable = false;
                    break;
                }
            }

            if (isOnlyTable) {
                Platform.runLater(() -> {
                    TableChartV tableChart = new TableChartV();
                    tableChart.showRowSums(true);

                    Label titleLabel = new Label(chartSetting.getName());
                    titleLabel.setStyle("-fx-font-size: 14px;-fx-font-weight: bold;");
                    titleLabel.setAlignment(Pos.CENTER);
                    HBox hBox = new HBox(titleLabel);
                    hBox.setAlignment(Pos.CENTER);


                    TableHeaderTable tableHeaderTable = new TableHeaderTable(tableChart.getXyChartSerieList());
                    tableChart.setTableHeader(tableHeaderTable);
                    tableHeaderTable.maxWidthProperty().bind(this.borderPane.widthProperty());

                    this.xyChart = tableChart;
//                    model.setShowSum_NOEVENT(true);
                    this.xyChart.createChart(model, this.sampleHandler.getDataModel(), chartSetting, true);

                    VBox vBox = new VBox(hBox, tableHeaderTable);
                    VBox.setVgrow(hBox, Priority.NEVER);
                    VBox.setVgrow(tableHeaderTable, Priority.ALWAYS);
                    this.borderPane.setCenter(vBox);
                    this.legend.getItems().clear();
                });
            } else {
                boolean isHeatMap = this.sampleHandler.getDataModel().stream().anyMatch(chartDataRow -> chartDataRow.getChartType() == ChartType.HEAT_MAP);

                if (isHeatMap) {
                    chartSetting.setChartType(ChartType.HEAT_MAP);
                    chartSetting.setColorMapping(ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);

                    Platform.runLater(() -> {
                        this.legend.getItems().clear();
                        this.heatMapChart = new HeatMapChart(this.sampleHandler.getDataModel(), chartSetting, getConfig().getBackgroundColor(), getConfig().getFontColor());
                        this.heatMapChart.getRegion().setPrefSize(getConfig().getSize().getWidth() - 20, getConfig().getSize().getHeight());
                        this.borderPane.setCenter(this.heatMapChart.getRegion());
                    });
                } else {
                    Platform.runLater(() -> {
                        this.xyChart = new XYChart();
                        this.xyChart.createChart(model, this.sampleHandler.getDataModel(), chartSetting, true);

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

    private void formatHeatMapChart(HeatMapChart heatMapChart) {
        try {
            ScrollPane sp = (ScrollPane) heatMapChart.getRegion();
            VBox spVer = (VBox) sp.getContent();
            MatrixPane<MatrixChartItem> matrixHeatMap = null;
            for (Node node : spVer.getChildren()) {
                if (node instanceof HBox) {
                    HBox spHor = (HBox) node;
                    matrixHeatMap = spHor.getChildren().stream().filter(node1 -> node1 instanceof MatrixPane).findFirst().map(node1 -> (MatrixPane<MatrixChartItem>) node1).orElse(matrixHeatMap);
                }
            }

            if (matrixHeatMap != null) {

                double pixelHeight = matrixHeatMap.getMatrix().getPixelHeight();
                double pixelWidth = matrixHeatMap.getMatrix().getPixelWidth();
                double spacerSizeFactor = matrixHeatMap.getMatrix().getSpacerSizeFactor();
                double width = matrixHeatMap.getMatrix().getWidth() - matrixHeatMap.getMatrix().getInsets().getLeft() - matrixHeatMap.getMatrix().getInsets().getRight();
                double height = matrixHeatMap.getMatrix().getHeight() - matrixHeatMap.getMatrix().getInsets().getTop() - matrixHeatMap.getMatrix().getInsets().getBottom();
                double pixelSize = Math.min((width / heatMapChart.getCOLS()), (height / heatMapChart.getROWS()));
                double spacer = pixelSize * spacerSizeFactor;

                double leftAxisWidth = 0;
                double rightAxisWidth = 0;
                int bottomAxisIndex = 0;
                Canvas bottomXAxis = null;

                for (Node node : spVer.getChildren()) {
                    if (node instanceof HBox) {
                        HBox spHor = (HBox) node;
                        boolean isLeftAxis = true;
                        for (Node node1 : spHor.getChildren()) {
                            if (node1 instanceof GridPane) {
                                GridPane axis = (GridPane) node1;

                                for (Node node2 : axis.getChildren()) {
                                    if (node2 instanceof Label) {
                                        boolean isOk = false;
                                        double newHeight = pixelHeight - 2;
                                        Font font = ((Label) node2).getFont();
                                        if (newHeight < 13) {
                                            final Label test = new Label(((Label) node2).getText());
                                            test.setFont(font);
                                            while (!isOk) {
                                                double height1 = test.getLayoutBounds().getHeight();
                                                if (height1 > pixelHeight - 2) {
                                                    newHeight = newHeight - 0.05;
                                                    test.setFont(new Font(font.getName(), newHeight));
                                                } else {
                                                    isOk = true;
                                                }
                                            }
                                        }

                                        if (newHeight < 12) {
                                            ((Label) node2).setFont(new Font(font.getName(), newHeight));
                                        }

                                        ((Label) node2).setPrefHeight(pixelHeight);

                                        final Label test = new Label(((Label) node2).getText());
                                        test.setFont(((Label) node2).getFont());
                                        double newWidth = test.getLayoutBounds().getWidth();

                                        if (isLeftAxis) {
                                            leftAxisWidth = Math.max(newWidth, axis.getLayoutBounds().getWidth());
                                            isLeftAxis = false;
                                        } else {
                                            rightAxisWidth = Math.max(newWidth, axis.getLayoutBounds().getWidth());
                                        }
                                    }
                                }
                            }
                        }

                    } else if (node instanceof GridPane || node instanceof Canvas) {

                        List<DateTime> xAxisList = heatMapChart.getxAxisList();
                        String X_FORMAT = heatMapChart.getX_FORMAT();

                        bottomXAxis = new Canvas(leftAxisWidth + width + rightAxisWidth, 30);
                        GraphicsContext gc = bottomXAxis.getGraphicsContext2D();
                        double x = leftAxisWidth + 4 + spacer + pixelWidth / 2;

                        for (DateTime dateTime : xAxisList) {
                            String ts = dateTime.toString(X_FORMAT);
                            Text text = new Text(ts);
                            Font helvetica = Font.font("Helvetica", 12);
                            text.setFont(helvetica);

                            final double textWidth = text.getLayoutBounds().getWidth();
                            final double textHeight = text.getLayoutBounds().getHeight();

                            gc.setFont(helvetica);

                            if (dateTime.getMinuteOfHour() == 0) {

                                gc.fillRect(x, 0, 2, 10);
                                gc.fillText(ts, x - textWidth / 2, 10 + textHeight + 2);

                            } else if (dateTime.getMinuteOfHour() % 15 == 0) {
                                gc.fillRect(x, 0, 1, 7);
                            }

                            x += pixelWidth;
                        }

                        bottomAxisIndex = spVer.getChildren().indexOf(node);
                    }
                }

                if (bottomXAxis != null) {
                    spVer.getChildren().set(bottomAxisIndex, bottomXAxis);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if (this.legend != null) {
                    this.legend.setBackground(bgColorTrans);
                    this.legend.setStyle("-fx-background-color: transparent; -fx-text-color: " + fontColor + ";");
                }
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

        this.legend.setAlignment(Pos.CENTER);


//        bottomBorderPane.heightProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("bottomBorderPane: " + newValue);
//        });
        this.borderPane.setBottom(bottomBorderPane);
        this.borderPane.setBottom(this.legend);
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
        return JEConfig.getImage("widget/ChartWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
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
