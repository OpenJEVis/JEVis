package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;

public class LinearGaugePojo {




    private static final Logger logger = LogManager.getLogger(LinearGaugePojo.class);

    int gaugeWidgetID = -1;

    private double maximum = 0;

    private double minimum = 0;

    private boolean inPercent = false;

    private Color color = Color.DARKBLUE;

    private Color colorBorder = Color.BLACK;


    final DashboardControl dashboardControl;

    private boolean showTitle = true;

    private boolean showUnit = true;

    private boolean showValue = true;

    private boolean showMajorTick = true;

    private boolean showMediumTick = true;

    private boolean showMinorTick = true;


    ObservableList<String> skins = FXCollections.observableArrayList();

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }



    public LinearGaugePojo(DashboardControl control) {
        this(control, null);
    }

    public int getLimitWidgetID() {
        return gaugeWidgetID;
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
            color = Color.valueOf(jsonNode.get("color").asText());
            showMajorTick = jsonNode.get("showMajorTick").asBoolean();
            showMediumTick = jsonNode.get("showMediumTick").asBoolean();
            showMinorTick = jsonNode.get("showMinorTick").asBoolean();
            colorBorder = Color.valueOf(jsonNode.get("colorBorder").asText());





        }


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

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "LinearGaugePojo{" +
                "gaugeWidgetID=" + gaugeWidgetID +
                ", maximum=" + maximum +
                ", minimum=" + minimum +
                ", inPercent=" + inPercent +
                ", color=" + color +
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


    private class GaugeDesignTab extends Tab implements ConfigTab {
        LinearGaugePojo gaugeDesign;

        public GaugeDesignTab(String text, LinearGaugePojo gaugeDesign) {
            super(text);
            this.gaugeDesign = gaugeDesign;
        }

        @Override
        public void commitChanges() {
        }
    }

    public Tab getConfigTab() {

        GaugeDesignTab tab = new GaugeDesignTab(I18n.getInstance().getString("plugin.dashboard.gaugewidget.tab")
                , this);
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        vBox.getChildren().add(createMaxMin());




        tab.setContent(vBox);
        return tab;
    }


    private VBox createMaxMin() {
        VBox vBox = new VBox();
        vBox.setSpacing(8);
        JFXCheckBox jfxCheckBoxShowTitle = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.gaugewidget.showTitle"));
        jfxCheckBoxShowTitle.setSelected(showTitle);
        JFXCheckBox jfxCheckBoxShowValue = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.gaugewidget.showValue"));
        jfxCheckBoxShowValue.setSelected(showValue);
        JFXCheckBox jfxCheckBoxShowUnit = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.gaugewidget.showUnit"));
        jfxCheckBoxShowUnit.setSelected(showUnit);

        jfxCheckBoxShowTitle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showTitle = newValue;
        });

        jfxCheckBoxShowUnit.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showUnit = newValue;
        });

        jfxCheckBoxShowValue.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showValue = newValue;
        });


        JFXCheckBox checkBoxInPercent = new JFXCheckBox(I18n.getInstance().getString("plugin.dashboard.gaugewidget.inPercent"));
        checkBoxInPercent.setSelected(inPercent);

        checkBoxInPercent.selectedProperty().addListener((observable, oldValue, newValue) -> {
            inPercent = newValue;
        });


        JFXTextField minTextField = new JFXTextField(String.valueOf(minimum));
        minTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            minimum = Double.parseDouble(newValue);
        });
        JFXTextField maxTextField = new JFXTextField(String.valueOf(maximum));
        maxTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            maximum = Double.parseDouble(newValue);
        });

        JFXCheckBox jfxCheckBoxShowMajorTick = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.lineargaugewidget.showMajorTick"));
        jfxCheckBoxShowMajorTick.setSelected(showMajorTick);
        jfxCheckBoxShowMajorTick.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showMajorTick = newValue;
        });
        JFXCheckBox jfxCheckBoxShowMediumTick = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.lineargaugewidget.showMediumTick"));
        jfxCheckBoxShowMediumTick.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showMediumTick = newValue;
        });
        jfxCheckBoxShowMediumTick.setSelected(showMediumTick);
        JFXCheckBox jfxCheckBoxShowMinorTick = new JFXCheckBox(I18n.getInstance().getString("plugin.graph.dashboard.lineargaugewidget.showMinorTick"));
        jfxCheckBoxShowMinorTick.setSelected(showMinorTick);
        jfxCheckBoxShowMinorTick.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showMinorTick = newValue;
        });



        ColorPickerAdv colorPickerAdv = new ColorPickerAdv();
        colorPickerAdv.setValue(color);
        colorPickerAdv.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            color = newValue;
        });

        ColorPickerAdv colorPickerAdvBorderColor = new ColorPickerAdv();
        colorPickerAdvBorderColor.setValue(colorBorder);
        colorPickerAdvBorderColor.selectColorProperty().addListener((observable, oldValue, newValue) -> {
            colorBorder = newValue;
        });

        HBox hBoxShowTitle = new HBox(jfxCheckBoxShowTitle);
        hBoxShowTitle.setPadding(new Insets(5,8,5,8));
        hBoxShowTitle.setSpacing(8);

        HBox hBoxShowUnit = new HBox(jfxCheckBoxShowUnit);
        hBoxShowUnit.setPadding(new Insets(5,8,5,8));
        hBoxShowUnit.setSpacing(8);

        HBox hBoxShowValue = new HBox(jfxCheckBoxShowValue);
        hBoxShowValue.setPadding(new Insets(5,8,5,8));
        hBoxShowValue.setSpacing(8);


        HBox hBoxInPercent = new HBox(checkBoxInPercent);
        hBoxInPercent.setPadding(new Insets(5,8,5,8));
        hBoxInPercent.setSpacing(8);


        HBox hBoxMaximum = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.max")), maxTextField);
        hBoxMaximum.setPadding(new Insets(5,8,5,8));
        hBoxMaximum.setSpacing(8);


        HBox hBoxMinimum = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.min")), minTextField);
        hBoxMinimum.setPadding(new Insets(5,8,5,8));
        hBoxMinimum.setSpacing(8);

        HBox hBoxColor = new HBox(new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.color")), colorPickerAdv);
        hBoxColor.setPadding(new Insets(5,8,5,8));
        hBoxColor.setSpacing(8);

        HBox hBoxShowMajorTick = new HBox(jfxCheckBoxShowMajorTick);
        hBoxShowMajorTick.setPadding(new Insets(5,8,5,8));
        hBoxShowMajorTick.setSpacing(8);

        HBox hBoxShowMediumTick = new HBox(jfxCheckBoxShowMediumTick);
        hBoxShowMediumTick.setPadding(new Insets(5,8,5,8));
        hBoxShowMediumTick.setSpacing(8);

        HBox hBoxShowMinorTick = new HBox(jfxCheckBoxShowMinorTick);
        hBoxShowMinorTick.setPadding(new Insets(5,8,5,8));
        hBoxShowMinorTick.setSpacing(8);

        HBox hBoxColorOutside = new HBox(new Label(I18n.getInstance().getString("plugin.graph.dashboard.lineargaugewidget.borderColor")),colorPickerAdvBorderColor);
        hBoxColorOutside.setPadding(new Insets(5,8,5,8));
        hBoxColorOutside.setSpacing(8);


        vBox.getChildren().addAll(hBoxShowTitle,hBoxShowUnit,hBoxShowValue,hBoxInPercent,hBoxMinimum,hBoxMaximum,hBoxColor,hBoxShowMajorTick,hBoxShowMediumTick,hBoxShowMinorTick,hBoxColorOutside,new Separator(Orientation.HORIZONTAL));
        return vBox;
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
        dataNode.put("color", color.toString());
        dataNode.put("showMajorTick", showMajorTick);
        dataNode.put("showMediumTick", showMediumTick);
        dataNode.put("showMinorTick", showMinorTick);
        dataNode.put("colorBorder", colorBorder.toString());
        System.out.println(dataNode);
        return dataNode;
    }

}