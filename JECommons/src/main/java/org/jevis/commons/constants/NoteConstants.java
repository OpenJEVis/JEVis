package org.jevis.commons.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteConstants {
    public interface Alignment {
        String ALIGNMENT_YES = "alignment(yes,";
        String ALIGNMENT_YES_CLOSE = "s)";
        String ALIGNMENT_NO = "alignment(no)";
    }

    public interface Differential {
        String DIFFERENTIAL_ON = "diff";
        String COUNTER_OVERFLOW = "cof";
        String COUNTER_CHANGE = "cc";
        String RESET_BY_PERIOD = "rbp";
        String WRONG_COUNTER_VALUE = "wcv";
    }

    public interface Scaling {
        String SCALING_ON = "scale";
    }

    public interface Limits {
        String LIMIT_STEP1 = "limit(Step1)";
        String LIMIT_DEFAULT = "limit(Default)";
        String LIMIT_STATIC = "limit(Static)";
        String LIMIT_AVERAGE = "limit(Average)";
        String LIMIT_MEDIAN = "limit(Median)";
        String LIMIT_INTERPOLATION = "limit(Interpolation)";
        String LIMIT_MIN = "limit(Minimum)";
        String LIMIT_MAX = "limit(Maximum)";

        List<String> ALL_LIMITS_2 = new ArrayList<>(Arrays.asList(LIMIT_AVERAGE, LIMIT_STATIC, LIMIT_MIN, LIMIT_DEFAULT,
                LIMIT_INTERPOLATION, LIMIT_MEDIAN, LIMIT_MAX));
    }

    public interface Deltas {
        String DELTA_STEP1 = "delta(Step1)";
        String DELTA_DEFAULT = "delta(Default)";
        String DELTA_STATIC = "delta(Static)";
        String DELTA_AVERAGE = "delta(Average)";
        String DELTA_MEDIAN = "delta(Median)";
        String DELTA_INTERPOLATION = "delta(Interpolation)";
        String DELTA_MIN = "delta(Minimum)";
        String DELTA_MAX = "delta(Maximum)";

        List<String> ALL_DELTAS_2 = new ArrayList<>(Arrays.asList(DELTA_AVERAGE, DELTA_STATIC, DELTA_MIN, DELTA_DEFAULT,
                DELTA_INTERPOLATION, DELTA_MEDIAN, DELTA_MAX));
    }

    public interface Gap {
        String GAP = "gap(";
        String GAP_DEFAULT = "gap(Default)";
        String GAP_STATIC = "gap(Static)";
        String GAP_AVERAGE = "gap(Average)";
        String GAP_MEDIAN = "gap(Median)";
        String GAP_INTERPOLATION = "gap(Interpolation)";
        String GAP_MIN = "gap(Minimum)";
        String GAP_MAX = "gap(Maximum)";

        List<String> ALL_GAPS = new ArrayList<>(Arrays.asList(GAP_AVERAGE, GAP_STATIC, GAP_MIN, GAP_DEFAULT,
                GAP_INTERPOLATION, GAP_MEDIAN, GAP_MAX));
    }

    public interface Forecast {
        String FORECAST = "forecast";
        String FORECAST_1 = "forecast1";
        String FORECAST_2 = "forecast2";
        String FORECAST_AVERAGE = "(Average)";
        String FORECAST_MEDIAN = "(Median)";
        String FORECAST_MIN = "(Minimum)";
        String FORECAST_MAX = "(Maximum)";
    }

    public interface Calc {
        String CALC_INFINITE = "calc(infinite)";
    }

    public interface User {
        String USER_NOTES = "userNotes";
        String USER_VALUE = "userValue";
    }
}
