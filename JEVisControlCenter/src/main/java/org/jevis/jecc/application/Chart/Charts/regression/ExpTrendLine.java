package org.jevis.jecc.application.Chart.Charts.regression;


public class ExpTrendLine extends OLSTrendLine {
    @Override
    protected double[] xVector(double x) {
        return new double[]{1, x};
    }

    @Override
    protected boolean logY() {
        return true;
    }
}
