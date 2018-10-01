package org.jevis.commons.dataprocessing;

public enum AggregationPeriod {

    NONE, HOURLY, DAILY, MONTHLY, WEEKLY, QUARTERLY, YEARLY;

    public static AggregationPeriod get(String modusName) {
        String period = modusName.split("_")[0];
        return valueOf(period);
    }
}