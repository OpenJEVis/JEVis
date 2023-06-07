package org.jevis.jecc.application.Chart.ChartElements;

import org.jevis.api.JEVisSample;

import java.util.List;

public class Bubble {
    private final List<JEVisSample> xSamples;
    private final List<JEVisSample> ySamples;
    private Double x;
    private Double y;
    private Double size;

    public Bubble(List<JEVisSample> xSamples, List<JEVisSample> ySamples, Double x, Double y, Double size) {
        this.xSamples = xSamples;
        this.ySamples = ySamples;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public List<JEVisSample> getXSamples() {
        return xSamples;
    }

    public List<JEVisSample> getYSamples() {
        return ySamples;
    }
}
