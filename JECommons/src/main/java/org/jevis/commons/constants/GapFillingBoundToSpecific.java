package org.jevis.commons.constants;

public enum GapFillingBoundToSpecific {
    NONE,
    WEEKDAY,
    WEEKOFYEAR,
    MONTHOFYEAR;

    public static GapFillingBoundToSpecific parse(String boundToSpecific) {
        if (boundToSpecific != null) {
            switch (boundToSpecific.toUpperCase()) {
                case ("WEEKDAY"):
                    return WEEKDAY;
                case ("WEEKOFYEAR"):
                    return WEEKOFYEAR;
                case ("MONTHOFYEAR"):
                    return MONTHOFYEAR;
                case ("NONE"):
                default:
                    return NONE;
            }
        } else return NONE;
    }
}
