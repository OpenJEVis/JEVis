package org.jevis.jecc.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
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
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.data.ChartDataRow;
import org.jevis.jecc.plugin.dashboard.DashboardControl;
import org.jevis.jecc.plugin.dashboard.config2.*;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jecc.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinearGaugeWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(LinearGaugeWidget.class);
    public static String WIDGET_ID = "LinearGauge";
    public static String PERCENT_NODE_NAME = "percent";
    public static String GAUGE_DESIGN_NODE_NAME = "linearGaugeDesign";
    private final eu.hansolo.medusa.Gauge gauge;
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");
    private final ChangeListener<Number> limitListener = null;
    private final ChangeListener<Number> percentListener = null;
    private final LinearGaugeWidget limitWidget = null;
    private final LinearGaugeWidget percentWidget = null;
    private final String percentText = "";
    private LinearGaugePojo gaugeSettings;
    private Interval lastInterval = null;
    private Percent percent;
    private Boolean customWorkday = true;

    public LinearGaugeWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
        gauge = GaugeBuilder.create().animated(false).skinType(Gauge.SkinType.LINEAR).startFromZero(false).build();
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

        showAlertOverview(false, "");

        if (sampleHandler == null) {
            return;
        } else {
            showProgressIndicator(true);
        }


        String widgetUUID = "-1";
        AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
        widgetUUID = getConfig().getUuid() + "";
        this.sampleHandler.setAutoAggregation(true);
        this.sampleHandler.update(interval);

        if (!this.sampleHandler.getChartDataRows().isEmpty()) {
            ChartDataRow dataModel = this.sampleHandler.getChartDataRows().get(0);
            dataModel.setCustomWorkDay(customWorkday);
            List<JEVisSample> results;

            String unit = dataModel.getUnitLabel();
            displayedUnit.setValue(unit);

            results = dataModel.getSamples();
            if (!results.isEmpty()) {
                try {
                    total.set(results.get(0).getValueAsDouble());
                } catch (Exception e) {
                    logger.error(e);
                }
                if (gaugeSettings.isInPercent()) {
                    Platform.runLater(() -> gauge.setValue(Helper.convertToPercent(total.get(), gaugeSettings.getMaximum(), gaugeSettings.getMinimum(), this.config.getDecimals())));

                } else {
                    Platform.runLater(() -> gauge.setValue(total.get()));

                }
            } else {
                Platform.runLater(() -> gauge.setValue(0));
                showAlertOverview(true, I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
            }
        }
    }

    private void setIntervallForLastValue(Interval interval) {
        if (this.getDataHandler().getTimeFrameFactory() != null) {
            if (!this.getControl().getAllTimeFrames().getAll().contains(this.getDataHandler().getTimeFrameFactory()) && sampleHandler != null) {
                sampleHandler.durationProperty().setValue(this.sampleHandler.getDashboardControl().getInterval());
                sampleHandler.update(interval);
                if (!this.sampleHandler.getChartDataRows().get(0).getSamples().isEmpty()) {
                    Interval interval1 = null;
                    try {
                        interval1 = new Interval(this.sampleHandler.getChartDataRows().get(0).getSamples().get(this.sampleHandler.getChartDataRows().get(0).getSamples().size() - 1).getTimestamp().minusMinutes(1), this.sampleHandler.getChartDataRows().get(0).getSamples().get(this.sampleHandler.getChartDataRows().get(0).getSamples().size() - 1).getTimestamp());
                        sampleHandler.durationProperty().setValue(interval1);
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }


            } else if (sampleHandler != null) {
                this.sampleHandler.update(interval);
            }
        } else {
            this.sampleHandler.update(interval);
        }

    }

    private void updateText() {
        if (this.config != null && gauge != null) {
            gauge.setTitle(this.config.getTitle());
            gauge.setTitleColor(this.config.getFontColor());
            gauge.setUnitColor(this.config.getFontColor());
            gauge.setValueColor(this.config.getFontColor());
            gauge.setDecimals(this.config.getDecimals());
            gauge.setTickLabelColor(this.config.getFontColor());
            gauge.setTickMarkColor(gaugeSettings.getColorBorder());


            if (!gaugeSettings.isShowTitle()) {
                gauge.setTitle("");
            }
            if (!gaugeSettings.isShowUnit()) {
                gauge.setUnit("");
            }
            if (!gaugeSettings.isShowValue()) {
                gauge.setValueColor(Color.valueOf("#ffffff00"));
            }
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
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);
        sampleHandler.setAutoAggregation(true);

        logger.debug("Value.openConfig() [{}] limit ={}", config.getUuid(), gaugeSettings);
        if (gaugeSettings != null) {
            widgetConfigDialog.addTab(gaugeSettings.getConfigTab());
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
        gauge.setPrefSize(config.getSize().getWidth(), config.getSize().getHeight());
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
        if (gauge != null) {
            if (gaugeSettings != null) {
                logger.debug("update Skin");


                gauge.setBarColor(gaugeSettings.getColorValueIndicator());
                gauge.setMajorTickMarksVisible(gaugeSettings.isShowMajorTick());
                gauge.setMediumTickMarksVisible(gaugeSettings.isShowMediumTick());
                gauge.setMinorTickMarksVisible(gaugeSettings.isShowMinorTick());


                if (gaugeSettings.isInPercent()) {
                    gauge.setMinValue(0);
                    gauge.setMaxValue(100);
                    gauge.setUnit("%");
                } else {
                    gauge.setMinValue(gaugeSettings.getMinimum());
                    gauge.setMaxValue(gaugeSettings.getMaximum());
                    gauge.setUnit(displayedUnit.getValue());
                }
                logger.debug((gauge.getMaxValue() - gauge.getMinValue()) / 10);
                gauge.setMajorTickSpace(gaugeSettings.getMajorTickStep());
            }
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

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config, this.getId());
        this.sampleHandler.setMultiSelect(false);

        logger.debug("Value.init() [{}] {}", config.getUuid(), this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
        try {
            this.gaugeSettings = new LinearGaugePojo(this.control, this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
            if (!sampleHandler.getChartDataRows().isEmpty()) {
                if (sampleHandler.getChartDataRows().get(0) != null) {
                    displayedUnit.setValue(sampleHandler.getChartDataRows().get(0).getUnitLabel());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (gaugeSettings == null) {
            logger.error("Gauge Setting is null make new: " + config.getUuid());
            this.gaugeSettings = new LinearGaugePojo(this.control);
        }

        Platform.runLater(() -> {
            setGraphic(this.gauge);
        });

        setOnMouseClicked(event -> {
            if (!control.editableProperty.get() && event.getButton().equals(MouseButton.PRIMARY)
                    && event.getClickCount() == 1 && !event.isShiftDown()) {
                int row = 0;

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                GridPane gp = new GridPane();
                gp.setHgap(4);
                gp.setVgap(8);
                for (ChartDataRow chartDataRow : sampleHandler.getChartDataRows()) {
                    if (chartDataRow.isCalculation()) {
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

                                TextField field = new TextField();
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


        if (gaugeSettings != null) {
            dashBoardNode
                    .set(GAUGE_DESIGN_NODE_NAME, gaugeSettings.toJSON());
        }


        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return ControlCenter.getImage("widget/LinearGaugeWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    public DoubleProperty getDisplayedSampleProperty() {
        return displayedSample;
    }


}
