package org.jevis.jecc.application.translation.datetime;

import org.jevis.commons.i18n.I18n;

public enum Month {

    JANUARY(I18n.getInstance().getString("graph.datehelper.months.january")), FEBRUARY(I18n.getInstance().getString("graph.datehelper.months.february")),
    MARCH(I18n.getInstance().getString("graph.datehelper.months.march")), APRIL(I18n.getInstance().getString("graph.datehelper.months.april")),
    MAY(I18n.getInstance().getString("graph.datehelper.months.may")), JUNE(I18n.getInstance().getString("graph.datehelper.months.june")),
    JULY(I18n.getInstance().getString("graph.datehelper.months.july")), AUGUST(I18n.getInstance().getString("graph.datehelper.months.august")),
    SEPTEMBER(I18n.getInstance().getString("graph.datehelper.months.september")), OCTOBER(I18n.getInstance().getString("graph.datehelper.months.october")),
    NOVEMBER(I18n.getInstance().getString("graph.datehelper.months.november")), DECEMBER(I18n.getInstance().getString("graph.datehelper.months.december"));

    private final String monthName;

    Month(String monthName) {
        this.monthName = monthName;
    }

    public String getMonthName() {
        return monthName;
    }
}
