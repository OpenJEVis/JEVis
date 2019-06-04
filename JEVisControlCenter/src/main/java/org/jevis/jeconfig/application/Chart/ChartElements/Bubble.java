package org.jevis.jeconfig.application.Chart.ChartElements;

public class Bubble {
    private Double x;
    private Double y;
    private Double size;

    public Bubble(Double x, Double y, Double size) {
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
}
