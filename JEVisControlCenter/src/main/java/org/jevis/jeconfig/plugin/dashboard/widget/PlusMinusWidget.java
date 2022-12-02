package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderWidths;
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
import org.jevis.jeconfig.plugin.dashboard.config.WidgetConfig;
import org.jevis.jeconfig.plugin.dashboard.config2.*;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelDataHandler;
import org.jevis.jeconfig.plugin.dashboard.datahandler.DataModelWidget;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlusMinusWidget extends Widget implements DataModelWidget {

    private static final Logger logger = LogManager.getLogger(PlusMinusWidget.class);
    public static String WIDGET_ID = "PlusMinus";
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public static String SHAPE_DESIGN_NODE_NAME = "minMax";
    private final NumberFormat nf = NumberFormat.getInstance();
    private final DoubleProperty displayedSample = new SimpleDoubleProperty(Double.NaN);
    private Interval lastInterval = null;
    private final boolean forceLastValue = true;
    private JEVisSample lastSample = null;

    private IncrementPojo incrementPojo;
    private Boolean customWorkday = true;

    private Tile minusPlus;


    public PlusMinusWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
        minusPlus = TileBuilder.create().skinType(Tile.SkinType.PLUS_MINUS).animated(false).backgroundColor(Color.TRANSPARENT).build();
        minusPlus.setOnTileEvent(tileEvent -> {
            try {
                if (tileEvent.getEventType().equals(TileEvent.EventType.FINISHED)) {
                    System.out.println(tileEvent.getEventType().toString());
                    BigDecimal bd = new BigDecimal(minusPlus.getValue());
                    bd = bd.setScale(config.getDecimals(), RoundingMode.HALF_UP);
                    setData(bd.doubleValue());
                }
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(typeID());
        widgetPojo.setSize(new Size(control.getActiveDashboard().yGridInterval * 2, control.getActiveDashboard().xGridInterval * 4));
        widgetPojo.setBorderSize(new BorderWidths(0));

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

        if (incrementPojo != null) {
            widgetConfigDialog.addTab(incrementPojo.getConfigTab());
        }

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
        Platform.runLater(() -> {
            minusPlus.setDecimals(config.getDecimals());
            minusPlus.setIncrement(incrementPojo.getIncrement());
        });


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
        this.sampleHandler = new DataModelDataHandler(getDataSource(), this.control, this.config.getConfigNode(WidgetConfig.DATA_HANDLER_NODE), this.getId());
        this.sampleHandler.setMultiSelect(false);
        initData();

        try {
            logger.debug(this.config.getConfigNode(SHAPE_DESIGN_NODE_NAME));
            this.incrementPojo = new IncrementPojo(this.control, this.config.getConfigNode(SHAPE_DESIGN_NODE_NAME));
            logger.debug(incrementPojo);
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        if (incrementPojo == null) {
            logger.error("Limit is null make new: " + config.getUuid());
            this.incrementPojo = new IncrementPojo(this.control);
        }


        Platform.runLater(() -> {
            setGraphic(minusPlus);
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


        if (incrementPojo != null) {
            dashBoardNode
                    .set(SHAPE_DESIGN_NODE_NAME, incrementPojo.toJSON());
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

    private void initData() {
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
                    String unit = sampleHandler.getDataModel().get(0).getAttribute().getDisplayUnit().getLabel();
                    minusPlus.setUnit(unit);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (lastSample != null) {
                minusPlus.setValue(lastSample.getValueAsDouble());


            } else {
                Platform.runLater(() -> {
                    minusPlus.setValue(0);
                    showAlertOverview(true, I18n.getInstance().getString("plugin.dashboard.alert.nodata"));
                });

            }

        } catch (Exception ex) {
            logger.error("Error while updating ValueWidget: [ID:{}]:{}", widgetUUID, ex);
            Platform.runLater(() -> {
                showAlertOverview(true, ex.getMessage());
                minusPlus.setValue(0);
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

    private void setData(double value) throws JEVisException {
        JEVisAttribute jeVisAttribute = sampleHandler.getDataModel().get(0).getObject().getAttribute(JC.Data.a_Value);
        logger.info("set data {} to objekt {}", value, jeVisAttribute.getObject().getID());
        JEVisSample jeVisSample = jeVisAttribute.buildSample(new DateTime(), value);
        jeVisSample.commit();

    }


}
