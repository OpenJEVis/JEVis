package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class LinearGaugePojo {


    private static final Logger logger = LogManager.getLogger(LinearGaugePojo.class);
    final DashboardControl dashboardControl;
    int gaugeWidgetID = -1;
    ObservableList<String> skins = FXCollections.observableArrayList();
    private double maximum = 0;
    private double minimum = 0;
    private boolean inPercent = false;
    private Color colorValueIndicator = Color.DARKBLUE;
    private Color colorBorder = Color.BLACK;
    private boolean showTitle = true;
    private boolean showUnit = true;
    private boolean showValue = true;
    private boolean showMajorTick = true;
    private boolean showMediumTick = true;
    private boolean showMinorTick = true;
    private JFXCheckBox jfxCheckBoxShowTitle;

    private JFXCheckBox jfxCheckBoxShowValue;

    private JFXCheckBox jfxCheckBoxShowUnit;

    private JFXCheckBox jfxCheckBoxInPercent;

    private JFXCheckBox jfxCheckBoxShowMajorTick;

    private JFXCheckBox jfxCheckBoxShowMediumTick;

    private JFXCheckBox jfxCheckBoxShowMinorTick;

    private JFXTextField jfxTextFieldMaxValue;

    private JFXTextField jfxTextFieldMinValue;

    private ColorPickerAdv colorPickerAdvBoarderColor;

    private ColorPickerAdv colorPickerAdvValueIndicator;


    public LinearGaugePojo(DashboardControl control) {
        this(control, null);
    }

    public LinearGaugePojo(DashboardControl control, JsonNode jsonNode) {

        //gauge.setSkinType(Gauge.SkinType.SIMPLE);
        skins.addAll(eu.hansolo.medusa.Gauge.SkinType.DASHBOARD.toString(), eu.hansolo.medusa.Gauge.SkinType.SIMPLE.toString());
        this.dashboardControl = control;

        if (jsonNode != null) {

            maximum = jsonNode.get("maximum").asDouble();
            minimum = jsonNode.get("minimum").asDouble();
            inPercent = jsonNode.get("inPercent").asBoolean();
            showTitle = jsonNode.get("showTitle").asBoolean();
            showUnit = jsonNode.get("showUnit").asBoolean();
            showValue = jsonNode.get("showValue").asBoolean();
            colorValueIndicator = Color.valueOf(jsonNode.get("color").asText());
            showMajorTick = jsonNode.get("showMajorTick").asBoolean();
            showMediumTick = jsonNode.get("showMediumTick").asBoolean();
            showMinorTick = jsonNode.get("showMinorTick").asBoolean();
            colorBorder = Color.valueOf(jsonNode.get("colorBorder").asText());


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

    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public boolean isShowUnit() {
        return showUnit;
    }

    public void setShowUnit(boolean showUnit) {
        this.showUnit = showUnit;
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
                ", inPercent=" + inPercent +
                ", color=" + colorValueIndicator +
                ", dashboardControl=" + dashboardControl +
                ", showTitle=" + showTitle +
                ", showUnit=" + showUnit +
                ", showValue=" + showValue +
                ", skins=" + skins +
                '}';
    }

    public boolean isShowMajorTick() {
        return showMajorTick;
    }

    public void setShowMajorTick(boolean showMajorTick) {
        this.showMajorTick = showMajorTick;
    }

    public boolean isShowMediumTick() {
        return showMediumTick;
    }

    public void setShowMediumTick(boolean showMediumTick) {
        this.showMediumTick = showMediumTick;
    }

    public boolean isShowMinorTick() {
        return showMinorTick;
    }

    public void setShowMinorTick(boolean showMinorTick) {
        this.showMinorTick = showMinorTick;
    }

    public Color getColorBorder() {
        return colorBorder;
    }

    public void setColorBorder(Color colorBorder) {
        this.colorBorder = colorBorder;
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.gaugewidget.tab")
                , this);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(8, 5, 8, 5));

        jfxCheckBoxShowTitle = new JFXCheckBox();
        jfxCheckBoxShowTitle.setSelected(showTitle);

        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showTitle")), jfxCheckBoxShowTitle);

        jfxCheckBoxShowUnit = new JFXCheckBox();
        jfxCheckBoxShowUnit.setSelected(showUnit);

        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showUnit")), jfxCheckBoxShowUnit);

        jfxCheckBoxShowValue = new JFXCheckBox();
        jfxCheckBoxShowValue.setSelected(showValue);

        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.showValue")), jfxCheckBoxShowValue);

        jfxCheckBoxInPercent = new JFXCheckBox();
        jfxCheckBoxInPercent.setSelected(inPercent);

        gridPane.addRow(3, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent")), jfxCheckBoxInPercent);

        jfxTextFieldMinValue = new JFXTextField();
        jfxTextFieldMinValue.setText(String.valueOf(minimum));

        gridPane.addRow(4, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.min")), jfxTextFieldMinValue);

        jfxTextFieldMaxValue = new JFXTextField();
        jfxTextFieldMaxValue.setText(String.valueOf(maximum));

        gridPane.addRow(5, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.max")), jfxTextFieldMaxValue);


        colorPickerAdvValueIndicator = new ColorPickerAdv();
        colorPickerAdvValueIndicator.setValue(colorValueIndicator);

        gridPane.addRow(6, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.valueIndicatorColor")), colorPickerAdvValueIndicator);

        jfxCheckBoxShowMajorTick = new JFXCheckBox();
        jfxCheckBoxShowMajorTick.setSelected(showMajorTick);


        gridPane.addRow(7, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMajorTick")), jfxCheckBoxShowMajorTick);


        jfxCheckBoxShowMediumTick = new JFXCheckBox();
        jfxCheckBoxShowMediumTick.setSelected(showMediumTick);

        gridPane.addRow(8, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMediumTick")), jfxCheckBoxShowMediumTick);

        jfxCheckBoxShowMinorTick = new JFXCheckBox();
        jfxCheckBoxShowMinorTick.setSelected(showMinorTick);

        gridPane.addRow(9, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.showMinorTick")), jfxCheckBoxShowMinorTick);


        colorPickerAdvBoarderColor = new ColorPickerAdv();
        colorPickerAdvBoarderColor.setValue(colorBorder);

        gridPane.addRow(10, new Label(I18n.getInstance().getString("plugin.dashboard.lineargaugewidget.borderColor")), colorPickerAdvBoarderColor);

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
        dataNode.put("inPercent", inPercent);
        dataNode.put("showTitle", showTitle);
        dataNode.put("showValue", showValue);
        dataNode.put("showUnit", showUnit);
        dataNode.put("color", colorValueIndicator.toString());
        dataNode.put("showMajorTick", showMajorTick);
        dataNode.put("showMediumTick", showMediumTick);
        dataNode.put("showMinorTick", showMinorTick);
        dataNode.put("colorBorder", colorBorder.toString());
        logger.debug(dataNode);
        return dataNode;
    }

    private class GaugeDesignTab extends Tab implements ConfigTab {
        LinearGaugePojo gaugeDesign;

        public GaugeDesignTab(String text, LinearGaugePojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {

            try {
                showTitle = jfxCheckBoxShowTitle.isSelected();
                showUnit = jfxCheckBoxShowUnit.isSelected();
                showValue = jfxCheckBoxShowValue.isDisable();

                minimum = Double.valueOf(jfxTextFieldMinValue.getText());
                maximum = Double.valueOf(jfxTextFieldMaxValue.getText());

                showMajorTick = jfxCheckBoxShowMajorTick.isSelected();
                showMediumTick = jfxCheckBoxShowMediumTick.isSelected();
                showMinorTick = jfxCheckBoxShowMinorTick.isSelected();

                colorBorder = colorPickerAdvBoarderColor.getValue();
                colorValueIndicator = colorPickerAdvValueIndicator.getValue();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

}