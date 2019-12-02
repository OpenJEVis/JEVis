package org.jevis.commons.constants;

public enum GapFillingReferencePeriod {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL,
    NONE;

    public static GapFillingReferencePeriod parse(String referencePeriod) {
        if (referencePeriod != null) {
            switch (referencePeriod.toUpperCase()) {
                case ("DAY"):
                    return DAY;
                case ("WEEK"):
                    return WEEK;
                case ("MONTH"):
                    return MONTH;
                case ("YEAR"):
                    return YEAR;
                case ("ALL"):
                    return ALL;
                case ("NONE"):
                default:
                    return NONE;
            }
        } else return NONE;
    }
}
