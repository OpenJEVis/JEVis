package org.jevis.jecc.application.Chart;

import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;

public enum TimeFrame {
    CUSTOM, CURRENT, TODAY, YESTERDAY, LAST_7_DAYS, THIS_WEEK, LAST_WEEK, LAST_30_DAYS, THIS_MONTH, LAST_MONTH, THIS_YEAR, LAST_YEAR, THE_YEAR_BEFORE_LAST, CUSTOM_START_END, PREVIEW;

    public static DateHelper.TransformType parseTransformType(TimeFrame timeFrame) {
        switch (timeFrame) {
            case CUSTOM:
                return DateHelper.TransformType.CUSTOM;
            case CURRENT:
                return DateHelper.TransformType.CURRENT;
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
            case THE_YEAR_BEFORE_LAST:
                return DateHelper.TransformType.THEYEARBEFORELAST;
            case CUSTOM_START_END:
                return DateHelper.TransformType.CUSTOM_PERIOD;
            case PREVIEW:
                return DateHelper.TransformType.PREVIEW;
        }
        return null;
    }

    public String getLocalName() {
        return switch (this) {
            case CUSTOM -> I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
            case CURRENT -> I18n.getInstance().getString("plugin.graph.changedate.buttoncurrent");
            case TODAY -> I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
            case YESTERDAY -> I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
            case LAST_7_DAYS -> I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
            case THIS_WEEK -> I18n.getInstance().getString("plugin.graph.changedate.buttonthisweek");
            case LAST_WEEK -> I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
            case LAST_30_DAYS -> I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
            case THIS_MONTH -> I18n.getInstance().getString("plugin.graph.changedate.buttonthismonth");
            case LAST_MONTH -> I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
            case THIS_YEAR -> I18n.getInstance().getString("plugin.graph.changedate.buttonthisyear");
            case LAST_YEAR -> I18n.getInstance().getString("plugin.graph.changedate.buttonlastyear");
            case THE_YEAR_BEFORE_LAST ->
                    I18n.getInstance().getString("plugin.graph.changedate.buttontheyearbeforelast");
            case CUSTOM_START_END -> I18n.getInstance().getString("plugin.graph.changedate.buttoncustomstartend");
            case PREVIEW -> I18n.getInstance().getString("plugin.graph.changedate.preview");
        };
    }
}
