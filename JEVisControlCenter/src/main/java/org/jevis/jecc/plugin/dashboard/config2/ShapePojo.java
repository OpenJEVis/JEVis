package org.jevis.jecc.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
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
import org.jevis.jecc.plugin.dashboard.widget.ShapeWidget.SHAPE;


public class ShapePojo {

    private static final Logger logger = LogManager.getLogger(ArrowConfig.class);
    private final DashboardControl dashboardControl;
    private final String JSON_SHAPE = "shape";
    int gaugeWidgetID = -1;
    private SHAPE shape = SHAPE.RECTANGLE;
    private ComboBox<SHAPE> comboBox;

    private Color minColor = Color.GREEN;

    private Color maxColor = Color.RED;

    private double minValue = 0;

    private double maxValue = 100;

    private double increment = 0;


    private TextField textFieldMinValue;

    private TextField textFieldMaxValue;

    private TextField textFieldStepDistance;

    private ColorPickerAdv colorPickerAdvMax;

    private ColorPickerAdv colorPickerAdvMin;

    public ShapePojo(DashboardControl dashboardControl, JsonNode jsonNode) {
        this.dashboardControl = dashboardControl;

        logger.debug(jsonNode);

        if (jsonNode != null) {


            if (jsonNode.has(JSON_SHAPE)) {
                String shapeStrg = jsonNode.get(JSON_SHAPE).asText(SHAPE.RECTANGLE.toString());
                try {
                    shape = SHAPE.valueOf(shapeStrg);
                } catch (Exception e) {
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
            if (jsonNode.has("increment")) {
                increment = jsonNode.get("increment").asDouble(0);
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
                ", comboBox=" + comboBox +
                ", minColor=" + minColor +
                ", maxColor=" + maxColor +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }

    public Tab getConfigTab() {

        ShapeTab tab = new ShapeTab(I18n.getInstance().getString("plugin.dashboard.shape")
                , this);

        GridPane gridPane = new GridPane();

        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.setPadding(new Insets(5, 8, 5, 8));

        comboBox = new ComboBox<>();

        for (SHAPE s : SHAPE.values()) {
            comboBox.getItems().add(s);
        }
        comboBox.setValue(shape);

        textFieldMaxValue = new TextField();
        textFieldMaxValue.setText(String.valueOf(maxValue));

        colorPickerAdvMin = new ColorPickerAdv();
        colorPickerAdvMin.setValue(minColor);

        colorPickerAdvMax = new ColorPickerAdv();
        colorPickerAdvMax.setValue(maxColor);

        textFieldMinValue = new TextField();
        textFieldMinValue.setText(String.valueOf(minValue));

        textFieldStepDistance = new TextField();
        textFieldStepDistance.setText(String.valueOf(increment));


        gridPane.addRow(0, new Label(I18n.getInstance().getString("plugin.dashboard.shape")), comboBox);

        gridPane.addRow(1, new Label(I18n.getInstance().getString("plugin.dashboard.min")), textFieldMinValue, colorPickerAdvMin);
        gridPane.addRow(2, new Label(I18n.getInstance().getString("plugin.dashboard.max")), textFieldMaxValue, colorPickerAdvMax);
        gridPane.addRow(3, new Label(I18n.getInstance().getString("plugin.dashboard.gaugewidget.stepdistance")), textFieldStepDistance);


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

        dataNode.put("increment", increment);

        return dataNode;
    }

    public double getStepDistance() {
        return increment;
    }

    public void setStepDistance(double stepDistance) {
        this.increment = stepDistance;
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
                shape = comboBox.getValue();

                minValue = Double.parseDouble(textFieldMinValue.getText());
                maxValue = Double.parseDouble(textFieldMaxValue.getText());

                maxColor = (colorPickerAdvMax.getValue());
                minColor = (colorPickerAdvMin.getValue());

                increment = Double.parseDouble(textFieldStepDistance.getText());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}



















