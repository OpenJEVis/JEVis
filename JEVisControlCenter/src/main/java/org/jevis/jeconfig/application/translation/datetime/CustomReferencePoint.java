package org.jevis.jeconfig.application.translation.datetime;

import org.jevis.jeconfig.tool.I18n;

public enum CustomReferencePoint {

    NOW(I18n.getInstance().getString("graph.datehelper.referencepoint.now")), CUSTOM_PERIOD(I18n.getInstance().getString("graph.datehelper.referencepoint.customperiod")),
    WEEKDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.weekday")), MONTH(I18n.getInstance().getString("graph.datehelper.referencepoint.month")),
    STARTTIMEDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.starttimeday")), EDNTIMEDAY(I18n.getInstance().getString("graph.datehelper.referencepoint.endtimeday"));

    private final String referencePointName;

    CustomReferencePoint(String referencePointName) {
        this.referencePointName = referencePointName;
    }

    public String getReferencePointName() {
        return referencePointName;
    }
}