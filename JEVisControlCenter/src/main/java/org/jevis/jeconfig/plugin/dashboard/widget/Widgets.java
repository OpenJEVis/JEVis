package org.jevis.jeconfig.plugin.dashboard.widget;

import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;

import java.util.ArrayList;
import java.util.List;

public class Widgets {

    public static List<Widget> getAvabableWidgets(DashboardControl control, WidgetPojo config) {
        List<Widget> widgetList = new ArrayList<>();

        widgetList.add(new PieWidget(control, config));
        widgetList.add(new TitleWidget(control, config));
        widgetList.add(new ChartWidget(control, config));
        widgetList.add(new ValueWidget(control, config));
        widgetList.add(new TableWidget(control, config));
//        widgetList.add(new WebPieWidget(control, config));
        widgetList.add(new LinkerWidget(control, config));

        return widgetList;
    }


}
