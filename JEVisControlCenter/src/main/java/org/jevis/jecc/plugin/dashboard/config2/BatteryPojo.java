package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.application.control.ColorPickerAdv;
import org.jevis.jecc.plugin.dashboard.DashboardControl;

public class BatteryPojo {


    private static final Logger logger = LogManager.getLogger(BatteryPojo.class);
    final DashboardControl dashboardControl;
    int gaugeWidgetID = -1;
    ObservableList<String> skins = FXCollections.observableArrayList();
    private double maximum = 0;
    private double minimum = 0;
    private Color colorValueIndicator = Color.GREEN;
    private boolean showValue = true;

    private CheckBox jfxCheckBoxShowValue;


    private TextField textFieldMaxValue;

    private TextField textFieldMinValue;

    private ColorPickerAdv colorPickerAdvValueIndicator;


    public BatteryPojo(DashboardControl control) {
        this(control, null);
    }

    public BatteryPojo(DashboardControl control, JsonNode jsonNode) {
        this.dashboardControl = control;

        if (jsonNode != null) {
            if (jsonNode.has("maximum")) {
                maximum = jsonNode.get("maximum").asDouble();
            }
            if (jsonNode.has("minimum")) {
                minimum = jsonNode.get("minimum").asDouble();

            }

            if (jsonNode.has("showValue")) {

                showValue = jsonNode.get("showValue").asBoolean();
            }
            if (jsonNode.has("color")) {

                colorValueIndicator = Color.valueOf(jsonNode.get("color").asText());
            }


        }


    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
    }

    public boolean isShowValue() {
        return showValue;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public Color getColorValueIndicator() {
        return colorValueIndicator;
    }

    public void setColorValueIndicator(Color colorValueIndicator) {
        this.colorValueIndicator = colorValueIndicator;
    }

    @Override
    public String toString() {
        return "LinearGaugePojo{" +
                "gaugeWidgetID=" + gaugeWidgetID +
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", color=" + colorValueIndicator +
                ", dashboardControl=" + dashboardControl +
                ", showValue=" + showValue +
                ", skins=" + skins +
                '}';
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.battery")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));


        jfxCheckBoxShowValue = new CheckBox();
        jfxCheckBoxShowValue.setSelected(showValue);

        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showValue")), jfxCheckBoxShowValue);


        textFieldMinValue = new TextField();
        textFieldMinValue.setText(String.valueOf(minimum));

        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.min")), textFieldMinValue);

        textFieldMaxValue = new TextField();
        textFieldMaxValue.setText(String.valueOf(maximum));

        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.max")), textFieldMaxValue);


        colorPickerAdvValueIndicator = new ColorPickerAdv();
        colorPickerAdvValueIndicator.setValue(colorValueIndicator);

        gridPane.addRow(3, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.valueIndicatorColor")), colorPickerAdvValueIndicator);


        tab.setContent(gridPane);
        return tab;
    }

    public int getLimitSource() {
        return gaugeWidgetID;
    }

    public int getGaugeWidgetID() {
        return gaugeWidgetID;
    }

    public void setGaugeWidgetID(int gaugeWidgetID) {
        this.gaugeWidgetID = gaugeWidgetID;
    }

    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();


        dataNode.put("minimum", minimum);
        dataNode.put("maximum", maximum);
        dataNode.put("showValue", showValue);
        dataNode.put("color", colorValueIndicator.toString());
        logger.debug(dataNode);
        return dataNode;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        BatteryPojo gaugeDesign;

        public GaugeDesignTab(String text, BatteryPojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {

            try {
                showValue = jfxCheckBoxShowValue.isSelected();

                minimum = Double.valueOf(textFieldMinValue.getText());
                maximum = Double.valueOf(textFieldMaxValue.getText());

                colorValueIndicator = colorPickerAdvValueIndicator.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}