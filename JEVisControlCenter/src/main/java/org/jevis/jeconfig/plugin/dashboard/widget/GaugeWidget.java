package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import com.jfoenix.controls.JFXTextField;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
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
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.methods.CommonMethods;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.jevis.jeconfig.plugin.dashboard.timeframe.LastPeriod;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.joda.time.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GaugeWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(GaugeWidget.class);
    public static String WIDGET_ID = "Gauge";
    private final eu.hansolo.medusa.Gauge gauge = GaugeBuilder.create().animated(false).build();
    private DataModelDataHandler sampleHandler;
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");

    private GaugePojo gaugeSettings;
    public static String PERCENT_NODE_NAME = "percent";
    private Interval lastInterval = null;
    private final ChangeListener<Number> limitListener = null;
    private final ChangeListener<Number> percentListener = null;
    private final GaugeWidget limitWidget = null;
    private final GaugeWidget percentWidget = null;
    private final String percentText = "";


    public static String GAUGE_DESIGN_NODE_NAME = "gaugeDesign";
    private Percent percent;
    private Boolean customWorkday = true;

    public GaugeWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
    }

    public GaugeWidget(DashboardControl control) {
        super(control);
    }

    public GaugeWidget() {
        super();
        setId(WIDGET_ID);
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 4, control.getActiveDashboard().xGridInterval * 4));
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
            widgetUUID = getConfig().getUuid() + "";

            this.sampleHandler.setAutoAggregation(true);


            setIntervallForLastValue(interval);
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
                    if (gaugeSettings.isInPercent()) {
                        gauge.setValue(convertToPercent(total.get(), gaugeSettings.getMaximum(), this.config.getDecimals()));

                    } else {
                        gauge.setValue(total.get());

                    }

                }else {
                    gauge.setValue(0);
                }

            }
        });
    }

    private void setIntervallForLastValue(Interval interval) {
        if (!this.getControl().getAllTimeFrames().getAll().contains(this.getDataHandler().getTimeFrameFactory())) {
            sampleHandler.durationPropertyProperty().setValue(this.sampleHandler.getDashboardControl().getInterval());
            sampleHandler.update();
            if (this.sampleHandler.getDataModel().get(0).getSamples().size() > 0) {

                System.out.println(" > 0");
                Interval interval1 = null;
                try {
                    interval1 = new Interval(this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp().minusMinutes(1), this.sampleHandler.getDataModel().get(0).getSamples().get(this.sampleHandler.getDataModel().get(0).getSamples().size() - 1).getTimestamp());
                    System.out.println(interval1);
                    sampleHandler.durationPropertyProperty().setValue(interval1);
                } catch (JEVisException e) {
                    throw new RuntimeException(e);
                }
            }


        } else {
        this.sampleHandler.setInterval(interval);
        }
    }

    private void updateText() {
        if (gauge != null  && config != null) {
        System.out.println("test");
            gauge.setTitle(this.config.getTitle());
            gauge.setTitleColor(this.config.getFontColor());
            gauge.setUnitColor(this.config.getFontColor());
            gauge.setValueColor(this.config.getFontColor());
            gauge.setDecimals(this.config.getDecimals());

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
        //updateText();
        //updateSkin();
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
            try {
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }


    private double convertToPercent(double value, double maximum, int decimalPlaces) {
        BigDecimal bd;
        if (maximum > 0) {
            bd = new BigDecimal(value / maximum * 100).setScale(2, RoundingMode.HALF_DOWN);
            return bd.doubleValue();
        } else return 0;


    }


    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
            updateText();
            updateSkin();
        });

    }

    private void updateSkin() {
        if (gauge != null) {
            gauge.setSkinType(eu.hansolo.medusa.Gauge.SkinType.SIMPLE_SECTION);
            if (gaugeSettings != null) {

                if (gaugeSettings.isInPercent()) {
                    gauge.setMinValue(0);
                    gauge.setMaxValue(100);
                    gauge.setUnit("%");
                } else {
                    gauge.setMinValue(gaugeSettings.getMinimum());
                    gauge.setMaxValue(gaugeSettings.getMaximum());
                    gauge.setUnit(displayedUnit.getValue());
                }
                List<Section> sections = gaugeSettings.getSections().stream().map(gaugeSection -> new Section(gaugeSection.getStart(), gaugeSection.getEnd(), gaugeSection.getColor())).collect(Collectors.toList());
                gauge.setSections(sections);
            } else {
                init();
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
            this.gaugeSettings = new GaugePojo(this.control, this.config.getConfigNode(GAUGE_DESIGN_NODE_NAME));
            if (sampleHandler.getDataModel().size() > 0) {
                if (sampleHandler.getDataModel().get(0) != null) {
                    displayedUnit.setValue(sampleHandler.getDataModel().get(0).getUnitLabel());
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (gaugeSettings == null) {
            logger.error("Gauge Setting is null make new: " + config.getUuid());
            this.gaugeSettings = new GaugePojo(this.control);
        }



        this.gauge.setPadding(new Insets(0, 8, 0, 8));
        setGraphic(this.gauge);

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
                                    this.getDataHandler().getDurationProperty().getStart(), this.getDataHandler().getDurationProperty().getEnd(), true);

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


        if (gaugeSettings != null) {
            dashBoardNode
                    .set(GAUGE_DESIGN_NODE_NAME, gaugeSettings.toJSON());
        }

//        if (percent != null) {
//            dashBoardNode
//                    .set(PERCENT_NODE_NAME, percent.toJSON());
//        }


        return dashBoardNode;
    }

    @Override
    public ImageView getImagePreview() {
        return JEConfig.getImage("widget/ValueWidget.png", this.previewSize.getHeight(), this.previewSize.getWidth());
    }


    public DoubleProperty getDisplayedSampleProperty() {
        return displayedSample;
    }


}
