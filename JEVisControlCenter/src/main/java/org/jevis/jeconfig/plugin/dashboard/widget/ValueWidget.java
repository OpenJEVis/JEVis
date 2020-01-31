package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.util.concurrent.AtomicDouble;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Limit;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValueWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ValueWidget.class);
    public static String WIDGET_ID = "Value";
    private final Label label = new Label();
    private NumberFormat nf = NumberFormat.getInstance();
    private DataModelDataHandler sampleHandler;
    private DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private Limit limit;
    private Interval lastInterval = null;


    public static String LIMIT_NODE_NAME = "limit";

    public ValueWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    public ValueWidget(DashboardControl control) {
        super(control);
    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Value.Update: {}", interval);
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

        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());

        AtomicDouble total = new AtomicDouble(Double.MIN_VALUE);
        try {
            widgetUUID = getConfig().getUuid() + "";
            this.sampleHandler.setInterval(interval);
            this.sampleHandler.setAutoAggregation(true);
            this.sampleHandler.update();
            if (!this.sampleHandler.getDataModel().isEmpty()) {
                ChartDataModel dataModel = this.sampleHandler.getDataModel().get(0);
                List<JEVisSample> results;

                String unit = dataModel.getUnitLabel();

                results = dataModel.getSamples();
                if (!results.isEmpty()) {
                    total.set(DataModelDataHandler.getTotal(results));
//                    total.set(results.get(results.size() - 1).getValueAsDouble());
                    displayedSample.setValue(total.get());
                    Platform.runLater(() -> {
                        /** animation experiment **/
//                        Timeline timeline = new Timeline();
//                        DoubleProperty timeSeconds = new SimpleDoubleProperty(0);
//                        //timeSeconds.set(total.get());
//
//                        //this.label.textProperty().bind(timeSeconds.asString());
//                        timeSeconds.addListener((observable, oldValue, newValue) -> {
//                            //System.out.println("------New value: "+newValue);
//                            Platform.runLater(() -> {
//                                this.label.setText((this.nf.format(newValue)) + " " + unit);
//                            });
//
//                        });
//                        timeline = new Timeline();
//
//                        Duration startValue = Duration.seconds(total.doubleValue()*0.8);//Duration.seconds(15 + 1);
//                        timeline.setRate(total.doubleValue()/0.1);
//                        timeline.getKeyFrames().add(
//                                new KeyFrame(startValue, new KeyValue(timeSeconds, total.doubleValue())));
//                        timeline.playFromStart();

                        this.label.setText((this.nf.format(total.get())) + " " + unit);
                    });
                    checkLimit();
                } else {
                    Platform.runLater(() -> {
                        this.label.setText("-");
                    });

                    displayedSample.set(Double.NaN);//or NaN?
                }

            } else {
                Platform.runLater(() -> {
                    this.label.setText("");
                });
                displayedSample.set(Double.NaN);
                logger.warn("ValueWidget is missing SampleHandler.datamodel: [ID:{}]", widgetUUID);
            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: [ID:{}]:{}", widgetUUID, ex);
            Platform.runLater(() -> {
                this.label.setText("error");
                showAlertOverview(true, ex.getMessage());
            });
        }

//        showProgressIndicator(false);

        Platform.runLater(() -> {
            showProgressIndicator(false);
        });


    }


    @Override
    public DataModelDataHandler getDataHandler() {
        return this.sampleHandler;
    }

    private void checkLimit() {
        Platform.runLater(() -> {
            try {
                logger.debug("checkLimit: {}", config.getUuid());
//                this.label.setText(this.labelText.getValue());
                Color fontColor = this.config.getFontColor();

                if (limit != null) {
                    this.label.setTextFill(limit.getExceedsLimitColor(fontColor, displayedSample.get()));
                } else {
                    this.label.setTextFill(fontColor);
                }


            } catch (Exception ex) {
                logger.error(ex);
            }
        });
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
//        System.out.println("The Thread name is0 " + Thread.currentThread().getName());
        WidgetConfigDialog widgetConfigDialog = new WidgetConfigDialog(this);
        widgetConfigDialog.addGeneralTabsDataModel(this.sampleHandler);
        sampleHandler.setAutoAggregation(true);

        logger.error("Value.openConfig() [{}] limit ={}", config.getUuid(), limit);
        if (limit != null) {
            widgetConfigDialog.addTab(limit.getConfigTab());
        }
        widgetConfigDialog.requestFirstTabFocus();

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


    @Override
    public void updateConfig() {
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                Background bgColor = new Background(new BackgroundFill(this.config.getBackgroundColor(), CornerRadii.EMPTY, Insets.EMPTY));
                this.label.setBackground(bgColor);
                this.label.setTextFill(this.config.getFontColor());
                this.label.setContentDisplay(ContentDisplay.CENTER);
            });
        });


        try {
            if (limit != null && limit.getLimitWidgetID() > 0) {
                for (Widget sourceWidget : ValueWidget.this.control.getWidgets()) {
                    if (sourceWidget.getConfig().getUuid() == (limit.getLimitWidgetID())) {
                        ((ValueWidget) sourceWidget).getDisplayedSampleProperty().addListener((observable, oldValue, newValue) -> {
                            limit.setLowerLimitDynamic(newValue.doubleValue());
                            limit.setUpperLimitDynamic(newValue.doubleValue());
                            checkLimit();
                        });
                        break;
                    }

                }

            }
        } catch (Exception ex) {
            logger.error("Error while update config: {}|{}", ex.getStackTrace()[0].getLineNumber(), ex);
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

        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE));
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

        this.label.setPadding(new Insets(0, 8, 0, 8));
        setGraphic(this.label);
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


        if (limit != null) {
            dashBoardNode
                    .set(LIMIT_NODE_NAME, limit.toJSON());
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
