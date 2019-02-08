package org.jevis.jeconfig.plugin.Dashboard.widget;

public class Layout {

    private double xPos = 100;
    private double yPos = 100;
    private double height = 50;
    private double width = 100;

    public Layout(double xPos, double yPos, double width, double height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.height = height;
        this.width = width;
    }

    public double getxPos() {
        return xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }
}
