package org.jevis.jeconfig.application.Chart;

import org.jevis.commons.datetime.DateHelper;

public enum TimeFrame {
    CUSTOM,
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    THIS_WEEK,
    LAST_WEEK,
    LAST_30_DAYS,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    LAST_YEAR,
    CUSTOM_START_END,
    PREVIEW;

    public static DateHelper.TransformType parseTransformType(TimeFrame timeFrame) {
        switch (timeFrame) {
            case CUSTOM:
                return DateHelper.TransformType.CUSTOM;
            case TODAY:
                return DateHelper.TransformType.TODAY;
            case YESTERDAY:
                return DateHelper.TransformType.YESTERDAY;
            case LAST_7_DAYS:
                return DateHelper.TransformType.LAST7DAYS;
            case THIS_WEEK:
                return DateHelper.TransformType.THISWEEK;
            case LAST_WEEK:
                return DateHelper.TransformType.LASTWEEK;
            case LAST_30_DAYS:
                return DateHelper.TransformType.LAST30DAYS;
            case THIS_MONTH:
                return DateHelper.TransformType.THISMONTH;
            case LAST_MONTH:
                return DateHelper.TransformType.LASTMONTH;
            case THIS_YEAR:
                return DateHelper.TransformType.THISYEAR;
            case LAST_YEAR:
                return DateHelper.TransformType.LASTYEAR;
            case CUSTOM_START_END:
                return DateHelper.TransformType.CUSTOM_PERIOD;
            case PREVIEW:
                return DateHelper.TransformType.PREVIEW;
        }
        return null;
    }
}
