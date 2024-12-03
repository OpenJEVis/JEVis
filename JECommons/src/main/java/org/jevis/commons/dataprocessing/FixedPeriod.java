package org.jevis.commons.dataprocessing;

public enum FixedPeriod {

    NONE, QUARTER_HOUR, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR, THREEYEARS, FIVEYEARS, TENYEARS;

    public static FixedPeriod get(String modeName) {
        return valueOf(modeName);
    }

    public static FixedPeriod parseFixedPeriod(String fixedPeriod) {
        switch (fixedPeriod) {
            case "Quarter Hour":
            case "Quarter Hourly":
            case "Quarterly Hour":
            case "Quarterly Hourly":
            case "QUARTER_HOUR":
            case "QUARTER_HOURLY":
            case "QUARTERLY_HOURLY":
                return QUARTER_HOUR;
            case "HOUR":
            case "HOURLY":
            case "Hour":
            case "Hourly":
                return HOUR;
            case "DAY":
            case "DAILY":
            case "Day":
            case "Daily":
                return DAY;
            case "WEEK":
            case "WEEKLY":
            case "Week":
            case "Weekly":
                return WEEK;
            case "MONTH":
            case "MONTHLY":
            case "Month":
            case "Monthly":
                return MONTH;
            case "QUARTER":
            case "QUARTERLY":
            case "Quarter":
            case "Quarterly":
                return QUARTER;
            case "YEAR":
            case "YEARLY":
            case "Year":
            case "Yearly":
                return YEAR;
            case ("Three Years"):
            case ("Three_Years"):
            case ("THREE YEARS"):
            case ("THREE_YEARS"):
            case ("THREEYEARS"):
                return THREEYEARS;
            case ("Five Years"):
            case ("Five_Years"):
            case ("FIVE YEARS"):
            case ("FIVE_YEARS"):
            case ("FIVEYEARS"):
                return FIVEYEARS;
            case ("Ten Years"):
            case ("Ten_Years"):
            case ("TEN YEARS"):
            case ("TEN_YEARS"):
            case ("TENYEARS"):
                return TENYEARS;
            case ("None"):
            case ("NONE"):
            default:
                return NONE;
        }
    }
}