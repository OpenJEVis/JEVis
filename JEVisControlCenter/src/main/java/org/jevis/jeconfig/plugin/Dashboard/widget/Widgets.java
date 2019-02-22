package org.jevis.jeconfig.plugin.Dashboard.widget;

import org.jevis.api.JEVisDataSource;

import java.util.ArrayList;
import java.util.List;

public class Widgets {

    public static List<Widget> getAvabableWidgets(JEVisDataSource jeVisDataSource) {
        List<Widget> widgetList = new ArrayList<>();
        widgetList.add(new DonutChart(jeVisDataSource));
        widgetList.add(new HighLowWidget(jeVisDataSource));
        widgetList.add(new NumberWidget(jeVisDataSource));
        widgetList.add(new StockWidget(jeVisDataSource));
        widgetList.add(new LabelWidget(jeVisDataSource));
        widgetList.add(new ChartWidget(jeVisDataSource));
        widgetList.add(new PieWidget(jeVisDataSource));
        return widgetList;
    }
}
