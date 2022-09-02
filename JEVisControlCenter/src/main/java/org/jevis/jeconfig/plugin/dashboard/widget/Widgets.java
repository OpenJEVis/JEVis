package org.jevis.jeconfig.plugin.dashboard.widget;

import javafx.scene.image.ImageView;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Widgets {

    public static Map<String, WidgetSelection> availableWidgets = new HashMap<String, WidgetSelection>() {
        {
            put(TitleWidget.WIDGET_ID, new WidgetSelection(
                    TitleWidget.class.getName(), TitleWidget.WIDGET_ID,
                    I18n.getInstance().getString("dashboard.widget.title"), TitleWidget.ICON));
            put(ChartWidget.WIDGET_ID, new WidgetSelection(ChartWidget.class.getName(), ChartWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.chart"), TitleWidget.ICON));
            put(PieWidget.WIDGET_ID, new WidgetSelection(PieWidget.class.getName(), PieWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.pie"), TitleWidget.ICON));
            put(TableWidget.WIDGET_ID, new WidgetSelection(TableWidget.class.getName(), TableWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.table"), TitleWidget.ICON));
            put(LinkerWidget.WIDGET_ID, new WidgetSelection(LinkerWidget.class.getName(), LinkerWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.analyselinker"), TitleWidget.ICON));
            put(ValueWidget.WIDGET_ID, new WidgetSelection(ValueWidget.class.getName(), ValueWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.value"), TitleWidget.ICON));
            put(ValueEditWidget.WIDGET_ID, new WidgetSelection(ValueEditWidget.class.getName(), ValueEditWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.valueeditor"), TitleWidget.ICON));
            put(DashboadLinkWidget.WIDGET_ID, new WidgetSelection(DashboadLinkWidget.class.getName(), DashboadLinkWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.dashboardlinker"), TitleWidget.ICON));
            put(ArrowWidget.WIDGET_ID, new WidgetSelection(ArrowWidget.class.getName(), ArrowWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.arrow"), TitleWidget.ICON));
            //put(SankeyWidget.WIDGET_ID, new WidgetSelection(TitleWidget.class.getName(), TitleWidget.WIDGET_ID, TitleWidget.ICON));
            put(ImageWidget.WIDGET_ID, new WidgetSelection(ImageWidget.class.getName(), ImageWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.image"), TitleWidget.ICON));
            put(GaugeWidget.WIDGET_ID, new WidgetSelection(GaugeWidget.class.getName(), GaugeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.gauge"), TitleWidget.ICON));
            put(LinearGaugeWidget.WIDGET_ID, new WidgetSelection(LinearGaugeWidget.class.getName(), LinearGaugeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.lineargauge"), TitleWidget.ICON));

            put(ShapeWidget.WIDGET_ID, new WidgetSelection(ShapeWidget.class.getName(), ShapeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.shape"), TitleWidget.ICON));
            put(TimeFrameWidget.WIDGET_ID, new WidgetSelection(TimeFrameWidget.class.getName(), TimeFrameWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.timeframe"), TitleWidget.ICON));

        }
    };
    private static EmptyValueWidget emptyValueWidget = null;

    public static ImageView getImage(String widgetType, double height, double width) {
        ImageView image = new ImageView(availableWidgets.get(widgetType).getIcon());
        image.fitHeightProperty().set(height);
        image.fitWidthProperty().set(width);
        return image;
    }

    public static Widget createWidget(String widgetType, DashboardControl control, WidgetPojo config) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String className = availableWidgets.get(widgetType).getClassname();

        Class<?> clazz = Class.forName(className);
        Object instance;

        Constructor<?> constructor = clazz.getConstructor(DashboardControl.class, WidgetPojo.class);//
        instance = constructor.newInstance(control, config);


        return (Widget) instance;
    }

    public static EmptyValueWidget emptyValueWidget(DashboardControl dashboardControl) {
        if (emptyValueWidget == null) {
            emptyValueWidget = new EmptyValueWidget(dashboardControl, null);
        }

        return emptyValueWidget;
    }


}
