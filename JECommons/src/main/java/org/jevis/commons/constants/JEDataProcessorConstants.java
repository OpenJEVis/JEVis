package org.jevis.commons.constants;

public class JEDataProcessorConstants {
    public interface GapFillingType {
        String NONE = "";
        String STATIC = "static";
        String INTERPOLATION = "interpolation";
        String DEFAULT_VALUE = "default value";
        String MINIMUM = "minimum";
        String MAXIMUM = "maximum";
        String MEDIAN = "median";
        String AVERAGE = "average";
    }

    public interface GapFillingReferencePeriod {
        String DAY = "day";
        String WEEK = "week";
        String MONTH = "month";
        String YEAR = "year";
        String NONE = "";
    }

    public interface GapFillingBoundToSpecific {
        String NONE = "";
        String WEEKDAY = "weekday";
        String WEEKOFYEAR = "weekofyear";
        String MONTHOFYEAR = "monthofyear";
    }
}
