package org.jevis.jecc.plugin.dashboard.config2;

import org.jevis.api.JEVisObject;

public class NetGraphDataRow {

    JEVisObject jeVisObject;
    double min = 0;
    double max = 0;


    public NetGraphDataRow(JEVisObject jeVisObject, double min, double max) {
        this.jeVisObject = jeVisObject;
        this.min = min;
        this.max = max;
    }

    public JEVisObject getJeVisObject() {
        return jeVisObject;
    }

    public void setJeVisObject(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
