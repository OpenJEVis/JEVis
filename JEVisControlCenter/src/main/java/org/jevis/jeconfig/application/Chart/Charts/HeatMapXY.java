package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.commons.dataprocessing.AggregationPeriod;

public class HeatMapXY {
    private Long X;
    private String X_FORMAT;
    private Long Y;
    private String Y_FORMAT;
    private AggregationPeriod aggregationPeriod;

    public HeatMapXY(Long x, String x_Format, Long y, String y_FORMAT, AggregationPeriod aggregationPeriod) {
        this.X = x;
        this.X_FORMAT = x_Format;
        this.Y = y;
        this.Y_FORMAT = y_FORMAT;
        this.aggregationPeriod = aggregationPeriod;
    }

    public Long getX() {
        return X;
    }

    public String getX_FORMAT() {
        return X_FORMAT;
    }

    public Long getY() {
        return Y;
    }

    public String getY_FORMAT() {
        return Y_FORMAT;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }
}
