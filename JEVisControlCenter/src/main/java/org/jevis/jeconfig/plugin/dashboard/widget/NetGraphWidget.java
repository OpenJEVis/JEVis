package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.chart.RadarChart;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.calculation.CalcInputObject;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CalcMethods;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class NetGraphWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(NetGraphWidget.class);
    public static String WIDGET_ID = "NetGraph";
    public static String PERCENT_NODE_NAME = "percent";
    public static String GAUGE_DESIGN_NODE_NAME = "NetGraph";
    private Tile netGraph;
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");
    private NetGraphPojo netGraphPojo;
    private Interval lastInterval = null;
    private final ChangeListener<Number> limitListener = null;
    private final ChangeListener<Number> percentListener = null;
    private final NetGraphWidget limitWidget = null;
    private final NetGraphWidget percentWidget = null;
    private final String percentText = "";
    private Percent percent;


    Map<Long, ChartData> chartData = new HashMap<>();

    private Boolean customWorkday = true;

    public NetGraphWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);


        netGraph = TileBuilder.create().skinType(Tile.SkinType.RADAR_CHART)
                .prefSize(150, 150)
                .radarChartMode(RadarChart.Mode.POLYGON)
                .startFromZero(false)
                .minValue(10)
                .autoScale(false)
                .animated(false)
                .gradientStops(new Stop(0.00000, Color.TRANSPARENT),
                        new Stop(0.00001, Color.web("#3552a0")),
                        new Stop(0.09090, Color.web("#456acf")),
                        new Stop(0.27272, Color.web("#45a1cf")),
                        new Stop(0.36363, Color.web("#30c8c9")),
                        new Stop(0.45454, Color.web("#30c9af")),
                        new Stop(0.50909, Color.web("#56d483")),
                        new Stop(0.72727, Color.web("#9adb49")),
                        new Stop(0.81818, Color.web("#efd750")),
                        new Stop(0.90909, Color.web("#ef9850")),
                        new Stop(1.00000, Color.web("#ef6050")))
                .build();
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 6, control.getActiveDashboard().xGridInterval * 2));
        widgetPojo.setDecimals(1);


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Value.updateData: {} {}", this.getConfig().getTitle(), interval);
        lastInterval = interval;
        Platform.runLater(() -> {
            showAlertOverview(false, "");
        });

        if (sampleHandler == null) {
            return;
        } else {
            showProgressIndicator(true);
        }
        Platform.runLater(() -> {
            String widgetUUID = "-1";
            AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
            //try {
            widgetUUID = getConfig().getUuid() + "";
            this.sampleHandler.setAutoAggregation(true);
            this.sampleHandler.setInterval(interval);
            this.sampleHandler.update();
            if (!this.sampleHandler.getDataModel().isEmpty()) {
                for (ChartDataRow dataModel : this.sampleHandler.getDataModel()) {
                    dataModel.setCustomWorkDay(customWorkday);
                    List<JEVisSample> results;
                    String unit = dataModel.getUnitLabel();
                    displayedUnit.setValue(unit);
                    results = dataModel.getSamples();
                    if (!results.isEmpty()) {
                        total.set(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, dataModel));
                        double value = getValue(netGraphPojo.isInPercent(), total.get(), netGraphPojo.getNetGraphDataRow(dataModel.getId()).getMax(), netGraphPojo.getNetGraphDataRow(dataModel.getId()).getMin(), 1);
                        if (chartData.containsKey(dataModel.getObject().getID())) {
                            chartData.get(dataModel.getObject().getID()).setValue(value);
                        } else {
                            ChartData chartData1 = new ChartData(dataModel.getName(), value, this.config.getFontColor(), this.config.getFontColor(), this.config.getFontColor(), Instant.now(), false, 0);
                            chartData.put(dataModel.getObject().getID(), chartData1);
                        }

                    } else {
                        try {
                            if (chartData.containsKey(dataModel.getObject().getID())) {
                                chartData.get(dataModel.getObject().getID()).setValue(0);
                            } else {
                                ChartData chartData1 = new ChartData(dataModel.getName(), getValue(netGraphPojo.isInPercent(), 0, netGraphPojo.getNetGraphDataRow((dataModel.getObject().getID())).getMax(), netGraphPojo.getNetGraphDataRow((dataModel.getObject().getID())).getMin(), this.config.getDecimals()), this.config.getFontColor(), this.config.getFontColor(), this.config.getFontColor(), Instant.now(), false, 0);
                                chartData.put(dataModel.getObject().getID(), chartData1);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        showAlertOverview(true, I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
                    }

                }
                ;
                netGraph.addChartData(chartData.entrySet().stream().map(longChartDataEntry -> longChartDataEntry.getValue()).collect(Collectors.toList()));
                if (netGraphPojo.isInPercent()) {
                    netGraph.setUnit("%");
                } else {
                    netGraph.setUnit(displayedUnit.get());
                }
                logger.debug(chartData);


            }
        });
    }

    private void updateText() {


    }

    private double getValue(boolean isPercent, double value, double max, double min, int decimalPlaces) {
        if (!isPercent) {
            return value;
        } else {
            return Helper.convertToPercent(value, max, min, decimalPlaces);
        }
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
        this.customWorkday = customWorkday;
    }

    @Override
    public void debug() {
        this.sampleHandler.debug();
    }

    @Override
    public void updateLayout() {
    }

    @Override
    public void openConfig() {
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        ObservableList<org.jevis.jeconfig.application.Chart.data.ChartData> dataTable = widgetConfigDialog.addGeneralTabsDataModelNetGraph(this.sampleHandler);
        sampleHandler.setAutoAggregation(true);

        logger.debug("Value.openConfig() [{}] limit ={}", config.getUuid(), netGraphPojo);
        if (netGraphPojo != null) {
            widgetConfigDialog.addTab(netGraphPojo.getConfigTab(dataTable));
        }

        widgetConfigDialog.requestFirstTabFocus();

        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.debug("OK Pressed {}", this);
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
                updateConfig();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }


    @Override
    public void updateConfig() {
        logger.debug("UpdateConfig");
        netGraph.setPrefSize(config.getSize().getWidth(), config.getSize().getHeight());
        Platform.runLater(() -> {
            try {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                updateSkin();
                updateText();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    private void updateSkin() {

        netGraph.setBackgroundColor(this.config.getBackgroundColor());
        netGraph.setTitle("");
        netGraph.setForegroundBaseColor(this.config.getFontColor());
        if (!netGraphPojo.isInPercent()) {
            netGraph.setMaxValue(netGraphPojo.getMax());
            netGraph.setMinValue(10);
        } else {
            netGraph.setMaxValue(100);
            netGraph.setMinValue(10);
        }
        switch (netGraphPojo.getSkin()) {
            case SECTOR:
                netGraph.setRadarChartMode(RadarChart.Mode.SECTOR);
                break;
            case POLYGON:
                netGraph.setRadarChartMode(RadarChart.Mode.POLYGON);
                break;
        }

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
    public void init() {
        logger.debug("init Value Widget: " + getConfig().getUuid());

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
        this.sampleHandler.setMultiSelect(true);

        logger.debug("Value.init() [{}] {}", config.getUuid(), this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
        try {
            this.netGraphPojo = new NetGraphPojo(this.control, this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
            if (sampleHandler.getDataModel().size() > 0) {
                if (sampleHandler.getDataModel().get(0) != null) {
                    displayedUnit.setValue(sampleHandler.getDataModel().get(0).getUnitLabel());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (netGraphPojo == null) {
            logger.error("Gauge Setting is null make new: " + config.getUuid());
            this.netGraphPojo = new NetGraphPojo(this.control);
        }

        Platform.runLater(() -> {
            setGraphic(this.netGraph);
        });

        setOnMouseClicked(event -> {
            if (!control.editableProperty.get() && event.getButton().equals(MouseButton.PRIMARY)
                    && event.getClickCount() == 1 && !event.isShiftDown()) {
                int row = 0;

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                GridPane gp = new GridPane();
                gp.setHgap(4);
                gp.setVgap(8);
                for (ChartDataRow chartDataRow : sampleHandler.getDataModel()) {
                    if (chartDataRow.getEnPI()) {
                        try {
                            alert.setHeaderText(CalcMethods.getTranslatedFormula(chartDataRow.getCalculationObject()));

                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), chartDataRow.getObject().getDataSource(), chartDataRow.getCalculationObject(),
                                    this.getDataHandler().getDuration().getStart(), this.getDataHandler().getDuration().getEnd(), true);

                            for (CalcInputObject calcInputObject : calcJob.getCalcInputObjects()) {

                                Label objectName = new Label();
                                if (calcInputObject.getValueAttribute().getObject().getJEVisClassName().equals("Clean Data")) {
                                    JEVisObject parent = CommonMethods.getFirstParentalDataObject(calcInputObject.getValueAttribute().getObject());
                                    if (parent != null) {
                                        objectName.setText(parent.getName());
                                    }
                                } else if (calcInputObject.getValueAttribute().getObject().getJEVisClassName().equals("Data")) {
                                    objectName.setText(calcInputObject.getValueAttribute().getObject().getName());
                                }

                                JFXTextField field = new JFXTextField();
                                field.setMinWidth(240);
                                Double value = Double.NaN;
                                DateTime date = new DateTime();
                                String formatString = PeriodHelper.STANDARD_PATTERN;
                                try {
                                    value = calcInputObject.getSamples().get(0).getValueAsDouble();
                                    date = calcInputObject.getSamples().get(0).getTimestamp();
                                    Period periodForDate = CleanDataObject.getPeriodForDate(calcInputObject.getValueAttribute().getObject(), date);
                                    formatString = PeriodHelper.getFormatString(periodForDate, false);
                                } catch (Exception e) {
                                    logger.error(e);
                                }

                                field.setText(value + " " + UnitManager.getInstance().format(calcInputObject.getValueAttribute().getDisplayUnit())
                                        + " @ " + date.toString(formatString));

                                gp.addRow(row, objectName, field);
                                row++;
                            }
                        } catch (Exception e) {
                            logger.error("Error while loading calculation", e);
                        }
                    }

                }
                if (!gp.getChildren().isEmpty()) {
                    alert.getDialogPane().setContent(gp);
                    TopMenu.applyActiveTheme(alert.getDialogPane().getScene());
                    alert.showAndWait();
                }

            } else if (!control.editableProperty.get() && event.getButton().equals(MouseButton.PRIMARY)
                    && event.getClickCount() == 1 && event.isShiftDown()) {
                debug();
            }

        });

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


        if (netGraphPojo != null) {
            dashBoardNode
                    .set(GAUGE_DESIGN_NODE_NAME, netGraphPojo.toJSON());
        }


        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/LinearGaugeWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    public DoubleProperty getDisplayedSampleProperty() {
        return displayedSample;
    }

    public enum SKIN {
        SECTOR,
        POLYGON,
    }


}
