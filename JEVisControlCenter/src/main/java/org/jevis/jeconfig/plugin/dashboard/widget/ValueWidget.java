package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.jevistree.methods.CommonMethods;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValueWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private final NumberFormat nf = new DecimalFormat("#,##0.##");//NumberFormat.getInstance();
    private final NumberFormat nfPercent = new DecimalFormat("0");
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final StringProperty displayedUnit = new SimpleStringProperty("");
    private Limit limit;
    public static String PERCENT_NODE_NAME = "percent";
    private Interval lastInterval = null;
    private ChangeListener<Number> limitListener = null;
    private ChangeListener<Number> percentListener = null;
    private ValueWidget limitWidget = null;
    private ValueWidget percentWidget = null;
    private String percentText = "";


    public static String LIMIT_NODE_NAME = "limit";
    private Percent percent;
    private Boolean customWorkday = true;

    public ValueWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        setId(WIDGET_ID);
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 4));


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
            this.label.setText(I18n.getInstance().getString("plugin.dashboard.loading"));
        });

        String widgetUUID = "-1";


        AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
        try {
            widgetUUID = getConfig().getUuid() + "";
            this.sampleHandler.setAutoAggregation(true);
            this.sampleHandler.setInterval(interval);
//            setIntervalForLastValue(interval);
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

                    displayedSample.setValue(total.get());
                    checkLimit();
                    checkPercent();
                } else {
                    displayedSample.setValue(Double.NaN);
                }
            } else {
                displayedSample.setValue(Double.NaN);
                logger.warn("ValueWidget is missing SampleHandler.datamodel: [ID:{}]", widgetUUID);
            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: [ID:{}]:{}", widgetUUID, ex);
            Platform.runLater(() -> {
                this.label.setText("error");
                showAlertOverview(true, ex.getMessage());
            });
        }

        Platform.runLater(() -> {
            showProgressIndicator(false);
        });

        //updateLayout();
        updateText();
        logger.debug("Value.updateData.done: {}", this.getConfig().getTitle());
    }

    private void updateText() {
        logger.debug("updateText: {}", this.getConfig().getTitle());
        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());

        StringProperty valueText = new SimpleStringProperty();
        if (displayedSample.getValue().isNaN()) {
            valueText.setValue("");
        } else {
            if (getConfig().getShowValue()) {
                valueText.setValue(this.nf.format(displayedSample.getValue()) + " " + displayedUnit.getValue());
            } else {
                valueText.setValue("");
            }
        }

        String displayedText = "";

        if (getConfig().getShowValue() && !percentText.isEmpty()) {
            displayedText = valueText.getValue() + " (" + percentText + ")";
        }
        if (getConfig().getShowValue() && percentText.isEmpty()) {
            displayedText = valueText.getValue();
        }
        if (!getConfig().getShowValue() && !percentText.isEmpty()) {
            displayedText = percentText;
        }
        if (!getConfig().getShowValue() && percentText.isEmpty()) {
            displayedText = "- %";
        }
        valueText.setValue(displayedText);

        //if (!percentText.isEmpty()) {
        /*
        if (getConfig().getShowValue()) {
            valueText.setValue(valueText.getValue() + " (" + percentText + ")");
        } else {
            if (!percentText.isEmpty()) {
                valueText.setValue(percentText);
            } else {
                valueText.setValue("- %");
            }

        }
        */
        // }

        Platform.runLater(() -> {
            this.label.setText(valueText.getValue());
        });

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
    }

    @Override
    public void openConfig() {
//        System.out.println("The Thread name is0 " + Thread.currentThread().getName());
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);
        sampleHandler.setAutoAggregation(true);

        logger.debug("Value.openConfig() [{}] limit ={}", config.getUuid(), limit);
        if (limit != null) {
            widgetConfigDialog.addTab(limit.getConfigTab());
        }

        if (percent != null) {
            widgetConfigDialog.addTab(percent.getConfigTab());
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


    private void updateValueListener() {
        try {
            if (limitListener != null) {
                limitWidget.getDisplayedSampleProperty().removeListener(limitListener);
            }

            if (limit != null && limit.getLimitWidgetID() > 0) {
                for (Widget sourceWidget : ValueWidget.this.control.getWidgets()) {
                    if (sourceWidget.getConfig().getUuid() == (limit.getLimitWidgetID())) {

                        limitWidget = ((ValueWidget) sourceWidget);
                        limitListener = new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                limit.setLowerLimitDynamic(newValue.doubleValue());
                                limit.setUpperLimitDynamic(newValue.doubleValue());
                                checkLimit();
                                updateText();
                            }
                        };
                        limitWidget.getDisplayedSampleProperty().addListener(limitListener);

                        //first time of the Value sourceValue does nop update
                        limit.setLowerLimitDynamic(limitWidget.getDisplayedSampleProperty().getValue().doubleValue());
                        limit.setUpperLimitDynamic(limitWidget.getDisplayedSampleProperty().getValue().doubleValue());
                        checkLimit();

                        break;
                    }

                }
            } else {
                checkLimit();
                updateText();
            }

        } catch (Exception exception) {
            logger.error("Error while updating limit: {}", exception, exception);
        }

        try {

            if (percentListener != null) {
                percentWidget.getDisplayedSampleProperty().removeListener(percentListener);
            }

            if (percent != null && percent.getPercentWidgetID() > 0) {
                for (Widget sourceWidget : ValueWidget.this.control.getWidgets()) {
                    if (sourceWidget.getConfig().getUuid() == (percent.getPercentWidgetID())) {
                        percentWidget = ((ValueWidget) sourceWidget);
                        percentListener = new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                checkPercent(newValue.doubleValue());
                                updateText();
                            }
                        };
                        percentWidget.getDisplayedSampleProperty().addListener(percentListener);

                        //first time of the Value sourceValue does nop update
                        checkPercent(percentWidget.getDisplayedSampleProperty().getValue());

                        break;
                    }
                }
            } else {
                percentText = "";
                updateText();
            }
        } catch (Exception exception) {
            logger.error("Error while updating percent: {}", exception, exception);
        }
    }

    private void checkPercent() {
        if (percentWidget != null) {
            checkPercent(percentWidget.getDisplayedSampleProperty().getValue());
        }
    }

    private void checkPercent(double reference) {
        if (percent.getPercentWidgetID() <= 0) {
            return;
        }

        Double value = ValueWidget.this.displayedSample.get();
        Double result = value / reference * 100;
        if (!result.isNaN()) {
            if (result >= 0.01) {
                ValueWidget.this.nfPercent.setMinimumFractionDigits(percent.getMinFracDigits());
                ValueWidget.this.nfPercent.setMaximumFractionDigits(percent.getMaxFracDigits());
                percentText = ValueWidget.this.nfPercent.format(result) + "%";
            } else {
                percentText = " < 0.01 %";
            }
        } else {
            percentText = "";
        }
        //updateText();
    }

    private void checkLimit() {
        Platform.runLater(() -> {
            try {
                logger.debug("checkLimit: {}", config.getUuid());
                Color fontColor = this.config.getFontColor();
                this.label.setFont(new Font(this.config.getFontSize()));

                if (limit != null) {
                    this.label.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(limit.getExceedsLimitColor(fontColor, displayedSample.get())) + " !important;");
                } else {
                    this.label.setStyle("-fx-text-fill: " + ColorHelper.toRGBCode(fontColor) + " !important;");
                }


            } catch (Exception ex) {
                logger.error(ex);
            }
        });
    }


    @Override
    public void updateConfig() {
        updateValueListener();
        updateText();
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());
                this.label.setContentDisplay(ContentDisplay.CENTER);
                this.label.setAlignment(this.config.getTitlePosition());
                Font font = Font.font(this.label.getFont().getFamily(), this.getConfig().getFontWeight(), this.getConfig().getFontPosture(), this.config.getFontSize());
                this.label.setFont(font);
                this.label.setUnderline(this.getConfig().getFontUnderlined());
            });
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
    public void init() {
        logger.debug("init Value Widget: " + getConfig().getUuid());

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), WIDGET_ID);
        this.sampleHandler.setMultiSelect(false);

        logger.debug("Value.init() [{}] {}", config.getUuid(), this.config.getConfigNode(LIMIT_NODE_NAME));
        try {
            this.limit = new Limit(this.control, this.config.getConfigNode(LIMIT_NODE_NAME));
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (limit == null) {
            logger.error("Limit is null make new: " + config.getUuid());
            this.limit = new Limit(this.control);
        }

        try {
            this.percent = new Percent(this.control, this.config.getConfigNode(PERCENT_NODE_NAME));
            nfPercent.setMinimumFractionDigits(percent.getMinFracDigits());
            nfPercent.setMaximumFractionDigits(percent.getMaxFracDigits());
            nfPercent.setRoundingMode(RoundingMode.HALF_UP);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (percent == null) {
            logger.error("Percent is null make new: " + config.getUuid());
            this.percent = new Percent(this.control);
        }

        this.label.setPadding(new Insets(0, 8, 0, 8));
        setGraphic(this.label);

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
        dashBoardNode.set(JsonNames.Widget.DATA_HANDLER_NODE, this.sampleHandler.toJsonNode());


        if (limit != null) {
            dashBoardNode.set(LIMIT_NODE_NAME, limit.toJSON());
        }

        if (percent != null) {
            dashBoardNode.set(PERCENT_NODE_NAME, percent.toJSON());
        }


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
