package org.jevis.jeconfig.plugin.Dashboard.widget;

public class Size {
    public static Size DEFAULT = new Size(150, 250);
    public static Size BIGGER = new Size(200, 300);


    private double height = 50;
    private double width = 100;


    public Size(double height, double width) {
        this.height = height;
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

}
