package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.scene.paint.Color;

public class GaugeSectionPojo {
    private double start = 0;
    private double end = 0;
    private Color color = Color.DARKBLUE;

    public GaugeSectionPojo(double start, double end, Color color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public GaugeSectionPojo() {
    }

    public double getStart() {
        return start;
    }

    public void setStart(double start) {
        this.start = start;
    }

    public double getEnd() {
        return end;
    }

    public void setEnd(double end) {
        this.end = end;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "GaugeSection{" +
                "minimum=" + start +
                ", maximum=" + end +
                ", color=" + color +
                '}';
    }
}
