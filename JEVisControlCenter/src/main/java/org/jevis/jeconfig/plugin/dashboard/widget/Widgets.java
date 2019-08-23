package org.jevis.jeconfig.plugin.dashboard.widget;

import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;

import java.util.ArrayList;
import java.util.List;

public class Widgets {

    public static List<Widget> getAvabableWidgets(DashboardControl conrol, WidgetPojo config) {
        List<Widget> widgetList = new ArrayList<>();

        widgetList.add(new PieWidget(conrol, config));
        widgetList.add(new TitleWidget(conrol, config));
        widgetList.add(new ChartWidget(conrol, config));
        widgetList.add(new ValueWidget(conrol, config));
        widgetList.add(new TableWidget(conrol, config));
        widgetList.add(new WebPieWidget(conrol, config));
        widgetList.add(new LinkerWidget(conrol, config));

        return widgetList;
    }


}
