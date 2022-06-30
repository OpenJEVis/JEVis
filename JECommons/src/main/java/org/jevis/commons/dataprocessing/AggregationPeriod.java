package org.jevis.commons.dataprocessing;

public enum AggregationPeriod {

    NONE, MINUTELY, QUARTER_HOURLY, HOURLY, DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, THREEYEARS, FIVEYEARS, TENYEARS, CUSTOM, CUSTOM2;

    public boolean isGreaterThenDays() {
        AggregationPeriod aggregationPeriod = this;
        return aggregationPeriod == AggregationPeriod.DAILY || aggregationPeriod == AggregationPeriod.WEEKLY || aggregationPeriod == AggregationPeriod.MONTHLY
                || aggregationPeriod == AggregationPeriod.QUARTERLY || aggregationPeriod == AggregationPeriod.YEARLY || aggregationPeriod == AggregationPeriod.THREEYEARS
                || aggregationPeriod == AggregationPeriod.FIVEYEARS || aggregationPeriod == AggregationPeriod.TENYEARS;
    }

    public static AggregationPeriod get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = NONE.name();
        if (modeArray.length == 2) {
            if (modeArray[0].equalsIgnoreCase("QUARTER")) {
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
            case ("Minute"):
            case ("MINUTE"):
            case ("Minutely"):
            case ("MINUTELY"):
                return MINUTELY;
            case ("Quarter Hour"):
            case ("Quarter Hourly"):
            case ("Quarterly Hour"):
            case ("Quarterly Hourly"):
            case ("QUARTER_HOUR"):
            case ("QUARTER_HOURLY"):
            case ("QUARTERLY_HOURLY"):
                return QUARTER_HOURLY;
            case ("HOUR"):
            case ("HOURLY"):
            case ("Hour"):
            case ("Hourly"):
                return HOURLY;
            case ("DAY"):
            case ("DAILY"):
            case ("Day"):
            case ("Daily"):
                return DAILY;
            case ("WEEK"):
            case ("WEEKLY"):
            case ("Week"):
            case ("Weekly"):
                return WEEKLY;
            case ("MONTH"):
            case ("MONTHLY"):
            case ("Month"):
            case ("Monthly"):
                return MONTHLY;
            case ("QUARTER"):
            case ("QUARTERLY"):
            case ("Quarter"):
            case ("Quarterly"):
                return QUARTERLY;
            case ("YEAR"):
            case ("YEARLY"):
            case ("Year"):
            case ("Yearly"):
                return YEARLY;
            case ("THREEYEARS"):
            case ("THREE_YEARS"):
            case ("Threeyears"):
            case ("Three years"):
                return THREEYEARS;
            case ("FIVEYEARS"):
            case ("FIVE_YEARS"):
            case ("Fiveyears"):
            case ("Five years"):
                return FIVEYEARS;
            case ("TENYEARS"):
            case ("TEN_YEARS"):
            case ("Tenyears"):
            case ("Ten years"):
                return TENYEARS;
            case ("CUSTOM"):
            case ("Custom"):
                return CUSTOM;
            case ("CUSTOM2"):
            case ("Custom2"):
                return CUSTOM2;
            case ("NONE"):
            case ("None"):
            default:
                return NONE;

        }
    }
}