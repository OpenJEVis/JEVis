package org.jevis.jeconfig.application.Chart.Charts.MultiAxis;

import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class LimitLine {

    private Double value;
    private Color color;
    private Integer yAxisIndex;
    private ObservableList<Double> strokeDashArray;

    public LimitLine(Double value, Color color, Integer yAxisIndex, ObservableList<Double> strokeDashArray) {
        this.value = value;
        this.color = color;
        this.yAxisIndex = yAxisIndex;
        this.strokeDashArray = strokeDashArray;
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
