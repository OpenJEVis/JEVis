package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.JsonNames;
import org.jevis.jeconfig.plugin.dashboard.config2.Size;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetConfigDialog;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
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

public class ToogleSwitchWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(ToogleSwitchWidget.class);
    public static String WIDGET_ID = "Toggle Switch";
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    private final NumberFormat nf = NumberFormat.getInstance();
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private final Interval lastInterval = null;
    private final boolean forceLastValue = true;
    private JEVisSample lastSample = null;
    private Boolean customWorkday = true;

    private final Tile toogleSwitch;


    public ToogleSwitchWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        toogleSwitch = TileBuilder.create().skinType(Tile.SkinType.SWITCH).animated(false).backgroundColor(Color.TRANSPARENT).build();

        toogleSwitch.setOnSwitchPressed(switchEvent ->{
            try {
                if (toogleSwitch.isActive()) {
                    System.out.println("switch is on");
                    setData(1.0);
                }else {
                    System.out.println("switch is off");
                    setData(0.0);
                }
            }catch (Exception e){
                logger.error(e);
            }

        });


    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 2, control.getActiveDashboard().xGridInterval * 2));

        return widgetPojo;
    }


    @Override
    public void updateData(Interval interval) {

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
        widgetConfigDialog.requestFirstTabFocus();
        Optional<ButtonType> result = widgetConfigDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.debug("OK Pressed {}", this);
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

    }

    @Override
    public boolean isStatic() {
        return true;
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
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config, this.getId());
        this.sampleHandler.setMultiSelect(false);
        initData();
        Platform.runLater(() -> {
            setGraphic(toogleSwitch);
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
    private void initData(){
        Platform.runLater(() -> {
            showAlertOverview(false, "");
        });

        if (sampleHandler == null || sampleHandler.getDataModel().isEmpty()) {
            return;
        } else {
            showProgressIndicator(true);
        }



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
            }

            if (lastSample != null) {
               toogleSwitch.setActive(getValueAsBool(lastSample.getValueAsDouble()));


            } else {
                Platform.runLater(() -> {
                    toogleSwitch.setActive(false);
                    showAlertOverview(true, I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
                });

            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: [ID:{}]:{}", widgetUUID, ex);
            Platform.runLater(() -> {
                showAlertOverview(true, ex.getMessage());
                toogleSwitch.setActive(false);
            });
        }


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
    private boolean getValueAsBool(double value){
        return value == 1;
    }
    private void setData(double value) throws JEVisException {
       JEVisAttribute jeVisAttribute = sampleHandler.getDataModel().get(0).getObject().getAttribute(JC.Data.a_Value);
       logger.info("set data {} to objekt {}", value, jeVisAttribute.getObject().getID());
       JEVisSample jeVisSample = jeVisAttribute.buildSample(new DateTime(), value);
       jeVisSample.commit();

    }


}
