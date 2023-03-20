package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.dialog.EnterDataDialog;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValueEditWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ValueEditWidget.class);
    public static String WIDGET_ID = "Value Editor";
    private final JFXTextField labelValue = new JFXTextField();
    private final Label labelTimeStamp = new Label();
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    //private DataModelDataHandler sampleHandler;
    private final NumberFormat nf = NumberFormat.getInstance();
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private Limit limit;
    private Interval lastInterval = null;
    private EnterDataDialog enterDataDialog = null;
    private final ImageView imageView = JEConfig.getImage("add_table.png", 18, 18);
    private final JFXButton addButton = new JFXButton("", JEConfig.getImage("AddValue.png", 34, 34));
    private final boolean forceLastValue = true;
    private JEVisSample lastSample = null;
    private Boolean customWorkday = true;


    public ValueEditWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 1, control.getActiveDashboard().xGridInterval * 6));


        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {
        logger.debug("Value.Update: {}", interval);
        lastInterval = interval;
        //setCurrentInterval(interval);

        Platform.runLater(() -> {
            showAlertOverview(false, "");
        });

        if (sampleHandler == null || sampleHandler.getDataModel().isEmpty()) {
            return;
        } else {
            showProgressIndicator(true);
        }


        Platform.runLater(() -> {
            this.labelValue.setText(I18n.getInstance().getString("plugin.dashboard.loading"));
        });

        String widgetUUID = "-1";

        this.nf.setMinimumFractionDigits(this.config.getDecimals());
        this.nf.setMaximumFractionDigits(this.config.getDecimals());

        try {
            widgetUUID = getConfig().getUuid() + "";

            if (forceLastValue) {
                try {
                    lastSample = sampleHandler.getDataModel().get(0).getAttribute().getLatestSample();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                this.sampleHandler.setInterval(interval);
                this.sampleHandler.setAutoAggregation(true);
                this.sampleHandler.update();
                if (!this.sampleHandler.getDataModel().isEmpty()) {
                    ChartDataRow dataModel = this.sampleHandler.getDataModel().get(0);
                    dataModel.setCustomWorkDay(customWorkday);
                    List<JEVisSample> results = dataModel.getSamples();
                    lastSample = results.get(results.size() - 1);
                }

            }

            if (lastSample != null) {
                String unit = lastSample.getUnit().toString();
                displayedSample.setValue(lastSample.getValueAsDouble());
                enterDataDialog.setSample(lastSample);

                Platform.runLater(() -> {
                    try {
                        this.labelValue.setText((this.nf.format(displayedSample.get())) + " " + unit);
                        labelTimeStamp.setText(fmt.print(lastSample.getTimestamp()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                Platform.runLater(() -> {
                    this.labelValue.setText("-");
                    labelTimeStamp.setText("-");
                });

                displayedSample.set(Double.NaN);//or NaN?
            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: [ID:{}]:{}", widgetUUID, ex);
            Platform.runLater(() -> {
                this.labelValue.setText("error");
                showAlertOverview(true, ex.getMessage());
            });
        }

//        showProgressIndicator(false);

        Platform.runLater(() -> {
            showProgressIndicator(false);
        });

/**
 Platform.runLater(() -> {
 //testing
 labelTimeStamp.setText("2020-02-28 16:30");
 labelValue.setText("6531,98 kWh");
 });
 **/
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
                widgetConfigDialog.commitSettings();
                control.updateWidget(this);


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
                //Background bgColor = new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
                //this.labelValue.setBackground(bgColor);
                //this.labelValue.setTextFill(this.config.getFontColor());
                // this.labelValue.setContentDisplay(ContentDisplay.LEFT);

                //this.labelValue.setStyle("-fx-text-inner-color: "+this.config.getFontColor()+";");

                Color oldFont = ColorHelper.getHighlightColor(this.config.getBackgroundColor());
                this.labelTimeStamp.setBackground(bgColor);
                //this.labelTimeStamp.setTextFill(Color.GREY);
                this.labelTimeStamp.setTextFill(oldFont);
                this.labelTimeStamp.setContentDisplay(ContentDisplay.LEFT);
                //Font oldFont = labelTimeStamp.getFont();


                labelTimeStamp.setFont(new Font(labelValue.getFont().getSize() * 0.7));

                if (sampleHandler != null && sampleHandler.getDataModel() != null && !sampleHandler.getDataModel().isEmpty()) {
                    enterDataDialog.setTarget(false, sampleHandler.getDataModel().get(0).getAttribute());
                    enterDataDialog.setShowValuePrompt(true);
                }

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

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(8));
        gridPane.add(labelTimeStamp, 0, 0);
        gridPane.addRow(1, labelValue);

        GridPane.setHgrow(labelValue, Priority.ALWAYS);
        setGraphic(gridPane);

        enterDataDialog = new EnterDataDialog(getDataSource());
        enterDataDialog.setShowDetailedTarget(false);
        enterDataDialog.getNewSampleProperty().addListener((observable, oldValue, newValue) -> {
            this.updateData(lastInterval);
        });

        labelValue.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                enterDataDialog.show();
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
