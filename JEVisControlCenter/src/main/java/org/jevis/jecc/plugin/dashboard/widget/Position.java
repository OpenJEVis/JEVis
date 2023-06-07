package org.jevis.jecc.plugin.dashboard.widget;

public class Position {


    private double xPos = 100;
    private double yPos = 100;

    public Position(double xPos, double yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public double getxPos() {
        return this.xPos;
    }

    public double getyPos() {
        return this.yPos;
    }
}
