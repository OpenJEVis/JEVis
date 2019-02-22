package org.jevis.jeconfig.plugin.Dashboard.widget;

public class Position {


    private double xPos = 100;
    private double yPos = 100;

    public Position(double xPos, double yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public double getxPos() {
        return xPos;
    }

    public double getyPos() {
        return yPos;
    }
}
