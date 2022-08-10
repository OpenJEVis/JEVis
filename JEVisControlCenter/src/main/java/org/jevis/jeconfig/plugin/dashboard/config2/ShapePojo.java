package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.ShapeWidget.*;


public class ShapePojo {

    private static final Logger logger = LogManager.getLogger(ArrowConfig.class);
    private final DashboardControl dashboardControl;

    private SHAPE shape = SHAPE.RECTANGLE;

    int gaugeWidgetID = -1;
    private final String JSON_SHAPE = "shape";

    private JFXComboBox<SHAPE> jfxComboBox;

    private Color minColor = Color.GREEN;

    private Color maxColor = Color.RED;

    private double minValue = 0;

    private double maxValue = 100;

    private HBox min;
    private HBox max;

    private JFXTextField jfxTextFieldMinValue;

    private JFXTextField jfxTextFieldMaxValue;

    private ColorPickerAdv colorPickerAdvMax;

    private ColorPickerAdv colorPickerAdvMin;

    public ShapePojo(DashboardControl dashboardControl, JsonNode jsonNode) {
        this.dashboardControl = dashboardControl;

        System.out.println(jsonNode);

        if (jsonNode != null) {
            String shapeStrg = jsonNode.get(JSON_SHAPE).asText();
            shape = SHAPE.valueOf(shapeStrg);
            maxColor = Color.valueOf(jsonNode.get("maxColor").asText());
            maxValue = jsonNode.get("maxValue").doubleValue();
            minColor = Color.valueOf(jsonNode.get("minColor").asText());
            minValue = jsonNode.get("minValue").doubleValue();
        }


    }

    public ShapePojo(DashboardControl dashboardControl) {
        this(dashboardControl, null);
    }


    public SHAPE getShape() {
        return shape;
    }

    public Color getMinColor() {
        return minColor;
    }

    public void setMinColor(Color minColor) {
        this.minColor = minColor;
    }

    public Color getMaxColor() {
        return maxColor;
    }

    public void setMaxColor(Color maxColor) {
        this.maxColor = maxColor;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {
        return "ShapePojo{" +
                "dashboardControl=" + dashboardControl +
                ", shape=" + shape +
                ", JSON_SHAPE='" + JSON_SHAPE + '\'' +
                ", jfxComboBox=" + jfxComboBox +
                ", minColor=" + minColor +
                ", maxColor=" + maxColor +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", min=" + min +
                ", max=" + max +
                '}';
    }

    private class ShapeTab extends Tab implements ConfigTab {
        ShapePojo shapePojo;

        public ShapeTab(String text, ShapePojo shapePojo) {
            super(text);
            this.shapePojo = shapePojo;
        }

        @Override
        public void commitChanges() {
            shape = jfxComboBox.getValue();

            minValue = Double.parseDouble(jfxTextFieldMinValue.getText());
            maxValue = Double.parseDouble(jfxTextFieldMaxValue.getText());

            maxColor = (colorPickerAdvMax.getValue());
            minColor = (colorPickerAdvMin.getValue());


        }
    }

    public Tab getConfigTab() {

        ShapeTab tab = new ShapeTab(I18n.getInstance().getString("plugin.dashboard.shape")
                , this);

        GridPane gridPane = new GridPane();

        gridPane.setHgap(8);
        gridPane.setVgap(8);

        jfxComboBox = new JFXComboBox<>();

        for (SHAPE s : SHAPE.values()) {
            jfxComboBox.getItems().add(s);
        }
        jfxComboBox.setValue(shape);

        jfxTextFieldMaxValue = new JFXTextField();
        jfxTextFieldMaxValue.setText(String.valueOf(maxValue));

        colorPickerAdvMin = new ColorPickerAdv();
        colorPickerAdvMin.setValue(minColor);

        colorPickerAdvMax = new ColorPickerAdv();
        colorPickerAdvMax.setValue(maxColor);

        jfxTextFieldMinValue = new JFXTextField();
        jfxTextFieldMinValue.setText(String.valueOf(minValue));


        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.shape")), jfxComboBox);

        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.min")), jfxTextFieldMinValue, colorPickerAdvMin);
        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.max")), jfxTextFieldMaxValue, colorPickerAdvMax);


        tab.setContent(gridPane);

        return tab;


    }


    public ObjectNode toJSON() {
        ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
        dataNode.put(JSON_SHAPE, shape.toString());
        dataNode.put("maxColor", maxColor.toString());
        dataNode.put("maxValue", maxValue);

        dataNode.put("minColor", minColor.toString());
        dataNode.put("minValue", minValue);

        return dataNode;
    }
}



















