package org.jevis.commons.dataprocessing;

public enum AggregationPeriod {

    NONE, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY;

    public static AggregationPeriod get(String modusName) {
        String period = modusName.split("_")[0];
        return valueOf(period);
    }

    public static AggregationPeriod parseAggregation(String aggregation) {
        switch (aggregation) {
            case ("None"):
                return NONE;
            case ("Hourly"):
                return HOURLY;
            case ("Daily"):
                return DAILY;
            case ("Weekly"):
                return WEEKLY;
            case ("Monthly"):
                return MONTHLY;
            case ("Quarterly"):
                return QUARTERLY;
            case ("Yearly"):
                return YEARLY;
            default:
                return NONE;
        }
    }
}