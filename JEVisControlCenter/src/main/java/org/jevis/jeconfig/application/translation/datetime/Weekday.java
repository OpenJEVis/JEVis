package org.jevis.jeconfig.application.translation.datetime;

import org.jevis.commons.i18n.I18n;

public enum Weekday {

    MONDAY(I18n.getInstance().getString("graph.datehelper.weekday.monday")), TUESDAY(I18n.getInstance().getString("graph.datehelper.weekday.tuesday")),
    WEDNESDAY(I18n.getInstance().getString("graph.datehelper.weekday.wednesday")), THURSDAY(I18n.getInstance().getString("graph.datehelper.weekday.thursday")),
    FRIDAY(I18n.getInstance().getString("graph.datehelper.weekday.friday")), SATURDAY(I18n.getInstance().getString("graph.datehelper.weekday.saturday")),
    SUNDAY(I18n.getInstance().getString("graph.datehelper.weekday.sunday"));

    private final String weekdayName;

    Weekday(String weekdayName) {
        this.weekdayName = weekdayName;
    }

    public String getWeekdayName() {
        return weekdayName;
    }
}