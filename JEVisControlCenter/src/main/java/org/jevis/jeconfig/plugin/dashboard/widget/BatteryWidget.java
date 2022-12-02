package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import com.jfoenix.controls.JFXTextField;
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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BatteryWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(BatteryWidget.class);
    public static String WIDGET_ID = "Battery";
    public static String PERCENT_NODE_NAME = "percent";
    public static String GAUGE_DESIGN_NODE_NAME = "BatteryDesign";
    private final Gauge battery;
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");
    private BatteryPojo batteryGaugePojo;
    private Interval lastInterval = null;
    private final ChangeListener<Number> limitListener = null;
    private final ChangeListener<Number> percentListener = null;
    private final BatteryWidget limitWidget = null;
    private final BatteryWidget percentWidget = null;
    private final String percentText = "";
    private Percent percent;
    private Boolean customWorkday = true;

    public BatteryWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
        battery = GaugeBuilder.create().animated(false).skinType(Gauge.SkinType.BATTERY).startFromZero(false).build();
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 2, control.getActiveDashboard().xGridInterval * 4));
        widgetPojo.setDecimals(1);
        widgetPojo.setBorderSize(new BorderWidths(0));


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
            //setIntervallForLastValue(interval);
            this.sampleHandler.update();
            if (!this.sampleHandler.getDataModel().isEmpty()) {
                ChartDataRow dataModel = this.sampleHandler.getDataModel().get(0);
                dataModel.setCustomWorkDay(customWorkday);
                List<JEVisSample> results;

                String unit = dataModel.getUnitLabel();
                displayedUnit.setValue(unit);

                results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getManipulatedData(this.sampleHandler.getDateNode(), results, dataModel));
                    battery.setValue(Helper.convertToPercent(total.get(), batteryGaugePojo.getMaximum(), batteryGaugePojo.getMinimum(), this.config.getDecimals()));
                } else {
                    battery.setValue(0);
                    showAlertOverview(true, I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
                }

            }
        });
    }

    private void setIntervallForLastValue(Interval interval) {
        if (this.getDataHandler().getTimeFrameFactory() != null) {
            if (!this.getControl().getAllTimeFrames().getAll().contains(this.getDataHandler().getTimeFrameFactory()) && sampleHandler != null) {
                sampleHandler.durationProperty().setValue(this.sampleHandler.getDashboardControl().getInterval());
                sampleHandler.update();
                if (this.sampleHandler.getDataModel().get(0).getSamples().size() > 0) {
                    Interval interval1 = null;
                    try {
                        interval1 = new Interval(this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp().minusMinutes(1), this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp());
                        sampleHandler.durationProperty().setValue(interval1);
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }


            } else {
                this.sampleHandler.setInterval(interval);
            }
        } else {
            this.sampleHandler.setInterval(interval);
        }

    }

    private void updateText() {
        if (this.config != null && battery != null) {
            battery.setValueColor(this.config.getFontColor());
            if (!batteryGaugePojo.isShowValue()) {
                battery.setValueColor(Color.valueOf("#ffffff00"));
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

        logger.debug("Value.openConfig() [{}] limit ={}", config.getUuid(), batteryGaugePojo);
        if (batteryGaugePojo != null) {
            widgetConfigDialog.addTab(batteryGaugePojo.getConfigTab());
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
        System.out.println(batteryGaugePojo);
        logger.debug("UpdateConfig");
        battery.setPrefSize(config.getSize().getWidth(), config.getSize().getHeight());
        battery.setDecimals(config.getDecimals());
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
        if (battery != null) {
            if (batteryGaugePojo != null) {
                logger.debug("update Skin");
                battery.setBarColor(batteryGaugePojo.getColorValueIndicator());
                battery.setMinValue(batteryGaugePojo.getMinimum());
                battery.setMaxValue(batteryGaugePojo.getMaximum());
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

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
        this.sampleHandler.setMultiSelect(false);

        logger.debug("Value.init() [{}] {}", config.getUuid(), this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
        try {
            this.batteryGaugePojo = new BatteryPojo(this.control, this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
            if (sampleHandler.getDataModel().size() > 0) {
                if (sampleHandler.getDataModel().get(0) != null) {
                    displayedUnit.setValue(sampleHandler.getDataModel().get(0).getUnitLabel());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (batteryGaugePojo == null) {
            logger.error("Gauge Setting is null make new: " + config.getUuid());
            this.batteryGaugePojo = new BatteryPojo(this.control);
        }

        Platform.runLater(() -> {
            setGraphic(this.battery);
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


        if (batteryGaugePojo != null) {
            dashBoardNode
                    .set(GAUGE_DESIGN_NODE_NAME, batteryGaugePojo.toJSON());
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


}
