package org.jevis.jecc.application.Chart.Charts.regression;

public interface TrendLine {
    void setValues(double[] y, double[] x); // y ~ f(x)

    double predict(double x); // get a predicted y for a given x
}
