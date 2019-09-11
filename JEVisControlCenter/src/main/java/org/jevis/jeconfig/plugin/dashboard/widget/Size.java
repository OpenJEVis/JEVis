package org.jevis.jeconfig.plugin.dashboard.widget;

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
        return this.height;
    }

    public double getWidth() {
        return this.width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Size size = (Size) o;

        if (Double.compare(size.height, this.height) != 0) return false;
        return Double.compare(size.width, this.width) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.height);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
