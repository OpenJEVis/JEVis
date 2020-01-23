package org.jevis.commons.constants;

public enum GapFillingType {
    NONE,
    STATIC,
    INTERPOLATION,
    DEFAULT_VALUE,
    MINIMUM,
    MAXIMUM,
    MEDIAN,
    AVERAGE,
    DELETE;

    public static GapFillingType parse(String type) {
        if (type != null) {
            switch (type.toUpperCase()) {
                case ("STATIC"):
                    return STATIC;
                case ("INTERPOLATION"):
                    return INTERPOLATION;
                case ("DEFAULT VALUE"):
                case ("DEFAULT_VALUE"):
                    return DEFAULT_VALUE;
                case ("MINIMUM"):
                    return MINIMUM;
                case ("MAXIMUM"):
                    return MAXIMUM;
                case ("MEDIAN"):
                    return MEDIAN;
                case ("AVERAGE"):
                    return AVERAGE;
                case ("DELETE"):
                    return DELETE;
                case ("NONE"):
                default:
                    return NONE;
            }
        } else {
            return NONE;
        }
    }
}
