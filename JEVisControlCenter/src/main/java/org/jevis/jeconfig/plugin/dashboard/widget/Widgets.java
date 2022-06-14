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
            put(ValueEditWidget.WIDGET_ID, ValueEditWidget.class.getName());
            put(DashboadLinkWidget.WIDGET_ID, DashboadLinkWidget.class.getName());
            put(ArrowWidget.WIDGET_ID, ArrowWidget.class.getName());
            put(SankeyWidget.WIDGET_ID, SankeyWidget.class.getName());
            put(ImageWidget.WIDGET_ID, ImageWidget.class.getName());
            put(GaugeWidget.WIDGET_ID, GaugeWidget.class.getName());
            put(LinearGaugeWidget.WIDGET_ID, LinearGaugeWidget.class.getName());

        }
    };

    public static List<Widget> getAvailableWidgets(DashboardControl control) {
        List<Widget> widgetList = new ArrayList<>();

        int i = -1;
        widgetList.add(new PieWidget(control, new WidgetPojo(i--)));
        widgetList.add(new TitleWidget(control, new WidgetPojo(i--)));
        widgetList.add(new ChartWidget(control, new WidgetPojo(i--)));
        widgetList.add(new ValueWidget(control, new WidgetPojo(i--)));
        widgetList.add(new ValueEditWidget(control, new WidgetPojo(i--)));
        widgetList.add(new TableWidget(control, new WidgetPojo(i--)));
        //disabled because its not working on the correct thread
//        widgetList.add(new WebPieWidget(control, new WidgetPojo(i--)));
        widgetList.add(new LinkerWidget(control, new WidgetPojo(i--)));
        widgetList.add(new DashboadLinkWidget(control, new WidgetPojo(i--)));
        widgetList.add(new ArrowWidget(control, new WidgetPojo(i--)));
        //widgetList.add(new SankeyWidget(control, config));
        widgetList.add(new ImageWidget(control, new WidgetPojo(i--)));
        widgetList.add(new GaugeWidget(control, new WidgetPojo(i--)));
        widgetList.add(new LinearGaugeWidget(control, new WidgetPojo(i--)));

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
