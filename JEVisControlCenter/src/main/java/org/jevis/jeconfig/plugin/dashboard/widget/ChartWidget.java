package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
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
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.ChartElements.TableHeaderTable;
import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
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
    private DataModelDataHandler sampleHandler;
    private final WidgetLegend legend = new WidgetLegend();
    private final BorderPane borderPane = new BorderPane();
    private Interval lastInterval = null;
    private final BorderPane bottomBorderPane = new BorderPane();

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
            Platform.runLater(() -> this.legend.getItems().clear());
            this.sampleHandler.getDataModel().forEach(chartDataModel -> {
                try {
                    String dataName = chartDataModel.getObject().getName();
                    Platform.runLater(() -> this.legend.getItems().add(
                            this.legend.buildHorizontalLegendItem(dataName + " " + chartDataModel.getUnit(), ColorHelper.toColor(chartDataModel.getColor()),
                                    this.config.getFontColor(), this.config.getFontSize(), chartDataModel.getObject(),
                                    chartDataModel.getSamples().isEmpty(), I18n.getInstance().getString("plugin.dashboard.alert.nodata"), false)));
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            /**
             * LineChart does not support updateData so we need to create an new one every time;
             */
            AnalysisDataModel model = new AnalysisDataModel(getDataSource(), null);
            model.setHideShowIconsNO_EVENT(false);
            ChartSetting chartSetting = new ChartSetting(0, "");
            chartSetting.setChartType(null);
            model.getCharts().setListSettings(Collections.singletonList(chartSetting));

            Platform.runLater(() -> {
                this.borderPane.setCenter(null);

                boolean isOnlyTable = true;
                for (ChartDataRow chartDataRow : this.sampleHandler.getDataModel()) {
                    if (!chartDataRow.getChartType().equals(ChartType.TABLE_V)) {
                        isOnlyTable = false;
                        break;
                    }
                }

                if (isOnlyTable) {
                    TableChartV tableChart = new TableChartV();

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

                } else {
                    this.xyChart = new XYChart();
                    this.xyChart.createChart(model, this.sampleHandler.getDataModel(), chartSetting, true);

                    this.borderPane.setCenter(this.xyChart.getChart());
                }

                Size configSize = getConfig().getSize();
                xyChart.getChart().setPrefSize(configSize.getWidth() - 20, configSize.getHeight());
                updateConfig();
            });
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
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                Background bgColorTrans = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
                this.setBackground(bgColorTrans);
                this.legend.setBackground(bgColorTrans);
                this.borderPane.setBackground(bgColor);

                try {
                    if (xyChart != null) {
                        //lineChart.getChart().getPlotBackground().setBackground(bgColor);
                        //lineChart.setBackGround(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                        String cssBGColor = ColorHelper.toRGBCode(Color.TRANSPARENT);
                        xyChart.getChart().getPlotBackground().setStyle("-fx-background-color: " + cssBGColor + ";");

                        xyChart.getChart().setStyle("-fx-background-color: " + cssBGColor + ";");
                        this.xyChart.getChart().getAxes().forEach(axis -> {
                            if (axis instanceof DefaultNumericAxis) {
                                DefaultNumericAxis defaultNumericAxis = (DefaultNumericAxis) axis;
                                defaultNumericAxis.getAxisLabel().setVisible(false);
                                defaultNumericAxis.setStyle("-fx-text-color: " + ColorHelper.toRGBCode(this.config.getFontColor()) + ";");
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
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
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


}
