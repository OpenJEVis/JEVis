package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.control.ColorPickerAdv;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.ShapeWidget.SHAPE;


public class ShapePojo {

    private static final Logger logger = LogManager.getLogger(ArrowConfig.class);
    private final DashboardControl dashboardControl;
    private final String JSON_SHAPE = "shape";
    int gaugeWidgetID = -1;
    private SHAPE shape = SHAPE.RECTANGLE;
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

        logger.debug(jsonNode);

        if (jsonNode != null) {


            if (jsonNode.has(JSON_SHAPE)) {
                String shapeStrg = jsonNode.get(JSON_SHAPE).asText(SHAPE.RECTANGLE.toString());
                try{
                    shape = SHAPE.valueOf(shapeStrg);
                } catch (Exception e){
                    logger.error(e);
                }
            }
            if (jsonNode.has("maxColor")) {
                maxColor = Color.valueOf(jsonNode.get("maxColor").asText(Color.RED.toString()));
            }
            if (jsonNode.has("maxValue")) {
                maxValue = jsonNode.get("maxValue").doubleValue();
            }
            if (jsonNode.has("minColor")) {
                minColor = Color.valueOf(jsonNode.get("minColor").asText(Color.GREEN.toString()));
            }
            if (jsonNode.has("minValue")) {
                minValue = jsonNode.get("minValue").doubleValue();
            }
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

    public Tab getConfigTab() {

        ShapeTab tab = new ShapeTab(I18n.getInstance().getString("plugin.dashboard.shape")
                , this);

        GridPane gridPane = new GridPane();

        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(5, 8, 5, 8));

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

    private class ShapeTab extends Tab implements ConfigTab {
        ShapePojo shapePojo;

        public ShapeTab(String text, ShapePojo shapePojo) {
            super(text);
            this.shapePojo = shapePojo;
        }

        @Override
        public void commitChanges() {
            try {
                shape = jfxComboBox.getValue();

                minValue = Double.parseDouble(jfxTextFieldMinValue.getText());
                maxValue = Double.parseDouble(jfxTextFieldMaxValue.getText());

                maxColor = (colorPickerAdvMax.getValue());
                minColor = (colorPickerAdvMin.getValue());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}



















