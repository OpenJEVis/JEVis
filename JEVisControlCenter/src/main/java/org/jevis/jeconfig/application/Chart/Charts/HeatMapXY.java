package org.jevis.jeconfig.application.Chart.Charts;

import org.jevis.commons.dataprocessing.AggregationPeriod;

public class HeatMapXY {
    private Long X;
    private Long Y;
    private AggregationPeriod aggregationPeriod;

    public HeatMapXY(Long x, Long y, AggregationPeriod aggregationPeriod) {
        this.X = x;
        this.Y = y;
        this.aggregationPeriod = aggregationPeriod;
    }

    public Long getX() {
        return X;
    }

    public Long getY() {
        return Y;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }
}
