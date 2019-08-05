package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class LimitLine {

    private Double value;
    private Color color;
    private Integer yAxisIndex;
    private ObservableList<Double> strokeDashArray;
    private String name;

    public LimitLine(String name, Double value, Color color, Integer yAxisIndex, ObservableList<Double> strokeDashArray) {
        this.name = name;
        this.value = value;
        this.color = color;
        this.yAxisIndex = yAxisIndex;
        this.strokeDashArray = strokeDashArray;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Integer getyAxisIndex() {
        return yAxisIndex;
    }

    public void setyAxisIndex(Integer yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
    }

    public ObservableList<Double> getStrokeDashArray() {
        return strokeDashArray;
    }

    public void setStrokeDashArray(ObservableList<Double> strokeDashArray) {
        this.strokeDashArray = strokeDashArray;
    }
}
