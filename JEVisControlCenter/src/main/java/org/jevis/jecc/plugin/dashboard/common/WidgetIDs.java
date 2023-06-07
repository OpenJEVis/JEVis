package org.jevis.jecc.plugin.dashboard.common;

import org.jevis.jecc.plugin.dashboard.widget.Widget;

import java.util.List;

public class WidgetIDs {

    public static int getNetxFreeeID(List<Widget> widgets) {
        int nextFreeID = 1;
        for (Widget widget : widgets) {
            if (widget.getConfig().getUuid() >= nextFreeID) {
                nextFreeID = widget.getConfig().getUuid() + 1;
            }
        }

        return nextFreeID;
    }
}
