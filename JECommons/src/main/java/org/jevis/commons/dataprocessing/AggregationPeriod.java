package org.jevis.commons.dataprocessing;

public enum AggregationPeriod {

    NONE, QUARTER_HOURLY, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY;

    public static AggregationPeriod get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = NONE.name();
        if (modeArray.length == 2) {
            if (modeArray[0].toUpperCase().equals("QUARTER")) {
                mode = modeArray[0] + "_" + modeArray[1];
            } else {
                mode = modeArray[0];
            }
        } else {
            mode = modeArray[0];
        }
        return valueOf(mode);
    }

    public static AggregationPeriod parseAggregation(String aggregation) {
        switch (aggregation) {
            case ("Quarter Hourly"):
            case ("Quarterly Hourly"):
            case ("QUARTER_HOURLY"):
            case ("QUARTERLY_HOURLY"):
                return QUARTER_HOURLY;
            case ("HOURLY"):
            case ("Hourly"):
                return HOURLY;
            case ("DAILY"):
            case ("Daily"):
                return DAILY;
            case ("WEEKLY"):
            case ("Weekly"):
                return WEEKLY;
            case ("MONTHLY"):
            case ("Monthly"):
                return MONTHLY;
            case ("QUARTERLY"):
            case ("Quarterly"):
                return QUARTERLY;
            case ("YEARLY"):
            case ("Yearly"):
                return YEARLY;
            case ("NONE"):
            case ("None"):
            default:
                return NONE;
        }
    }
}