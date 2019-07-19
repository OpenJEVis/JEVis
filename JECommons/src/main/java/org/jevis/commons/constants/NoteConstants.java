package org.jevis.commons.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteConstants {
    public interface Alignment {
        String ALIGNMENT_YES = "alignment(yes";
        String ALIGNMENT_NO = "alignment(no";
    }

    public interface Differential {
        String DIFFERENTIAL_ON = "diff";
        String COUNTER_OVERFLOW = "cof";
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

    public interface Calc {
        String CALC_INFINITE = "calc(infinite)";
    }

    public interface User {
        String USER_NOTES = "userNotes";
    }
}
