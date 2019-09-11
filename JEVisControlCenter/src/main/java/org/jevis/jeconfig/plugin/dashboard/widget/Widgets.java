package org.jevis.jeconfig.plugin.dashboard.widget;

import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Widgets {

    public static Map<String, String> availableWidgets = new HashMap<String, String>() {
        {
            put(ChartWidget.WIDGET_ID, ChartWidget.class.getName());
            put(PieWidget.WIDGET_ID, PieWidget.class.getName());
            put(TableWidget.WIDGET_ID, TableWidget.class.getName());
            put(LinkerWidget.WIDGET_ID, LinkerWidget.class.getName());
            put(TitleWidget.WIDGET_ID, TitleWidget.class.getName());
            put(ValueWidget.WIDGET_ID, ValueWidget.class.getName());

        }
    };

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

    public static Widget createWidget(DashboardControl control, WidgetPojo config) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = availableWidgets.get(config.getType());

        Class<?> clazz = Class.forName(className);
        Constructor<?> constructor = clazz.getConstructor(DashboardControl.class, WidgetPojo.class);
        Object instance = constructor.newInstance(control, config);

        return (Widget) instance;
    }


}
