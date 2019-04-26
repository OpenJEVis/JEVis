package org.jevis.jeconfig.plugin.Dashboard.widget;

import org.jevis.api.JEVisDataSource;
import org.jevis.jeconfig.plugin.Dashboard.config.WidgetConfig;

import java.util.ArrayList;
import java.util.List;

public class Widgets {

    public static List<Widget> getAvabableWidgets(JEVisDataSource jeVisDataSource, WidgetConfig config) {
        List<Widget> widgetList = new ArrayList<>();

        widgetList.add(new PieWidget(jeVisDataSource, config));
        widgetList.add(new TitleWidget(jeVisDataSource, config));
        widgetList.add(new ChartWidget(jeVisDataSource, config));
        widgetList.add(new ValueWidget(jeVisDataSource, config));
        widgetList.add(new TableWidget(jeVisDataSource, config));
        widgetList.add(new WebPieWidget(jeVisDataSource, config));
        return widgetList;
    }


}
