package org.jevis.commons.dataprocessing;

public enum FixedPeriod {

    NONE, QUARTER_HOUR, HOUR, DAY, WEEK, MONTH, QUARTER, YEAR, THREEYEARS, FIVEYEARS, TENYEARS;

    public static FixedPeriod get(String modeName) {
        return valueOf(modeName);
    }

    public static FixedPeriod parseFixedPeriod(String fixedPeriod) {
        switch (fixedPeriod) {
            case ("Quarter Hour"):
                return QUARTER_HOUR;
            case ("Hour"):
                return HOUR;
            case ("Day"):
                return DAY;
            case ("Week"):
                return WEEK;
            case ("Month"):
                return MONTH;
            case ("Quarter"):
                return QUARTER;
            case ("Year"):
                return YEAR;
            case ("Three Years"):
                return THREEYEARS;
            case ("Five Years"):
                return FIVEYEARS;
            case ("Ten Years"):
                return TENYEARS;
            case ("None"):
            default:
                return NONE;
        }
    }
}