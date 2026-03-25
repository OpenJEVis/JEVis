package org.jevis.jeconfig.plugin.dashboard.widget;

import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Widgets {

    private static final Logger logger = LogManager.getLogger(Widgets.class);

    /**
     * Compile-time-safe factory map: widget type ID → constructor reference.
     * Eliminates Class.forName() and reflection per widget instantiation.
     */
    private static final Map<String, BiFunction<DashboardControl, WidgetPojo, Widget>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put(TitleWidget.WIDGET_ID, TitleWidget::new);
        FACTORIES.put(ChartWidget.WIDGET_ID, ChartWidget::new);
        FACTORIES.put(PieWidget.WIDGET_ID, PieWidget::new);
        FACTORIES.put(TableWidget.WIDGET_ID, TableWidget::new);
        FACTORIES.put(LinkerWidget.WIDGET_ID, LinkerWidget::new);
        FACTORIES.put(ValueWidget.WIDGET_ID, ValueWidget::new);
        FACTORIES.put(StringValueWidget.WIDGET_ID, StringValueWidget::new);
        FACTORIES.put(ValueEditWidget.WIDGET_ID, ValueEditWidget::new);
        FACTORIES.put(DashboadLinkWidget.WIDGET_ID, DashboadLinkWidget::new);
        FACTORIES.put(ArrowWidget.WIDGET_ID, ArrowWidget::new);
        FACTORIES.put(SankeyWidget.WIDGET_ID, SankeyWidget::new);
        FACTORIES.put(ImageWidget.WIDGET_ID, ImageWidget::new);
        FACTORIES.put(GaugeWidget.WIDGET_ID, GaugeWidget::new);
        FACTORIES.put(LinearGaugeWidget.WIDGET_ID, LinearGaugeWidget::new);
        FACTORIES.put(ShapeWidget.WIDGET_ID, ShapeWidget::new);
        FACTORIES.put(TimeFrameWidget.WIDGET_ID, TimeFrameWidget::new);
        FACTORIES.put(BatteryWidget.WIDGET_ID, BatteryWidget::new);
        FACTORIES.put(NetGraphWidget.WIDGET_ID, NetGraphWidget::new);
        FACTORIES.put(ToogleSwitchWidget.WIDGET_ID, ToogleSwitchWidget::new);
        FACTORIES.put(SliderWidget.WIDGET_ID, SliderWidget::new);
        FACTORIES.put(PlusMinusWidget.WIDGET_ID, PlusMinusWidget::new);
    }

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
            put(StringValueWidget.WIDGET_ID, new WidgetSelection(StringValueWidget.class.getName(), StringValueWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.stringvalue"), TitleWidget.ICON));
            put(ValueEditWidget.WIDGET_ID, new WidgetSelection(ValueEditWidget.class.getName(), ValueEditWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.valueeditor"), TitleWidget.ICON));
            put(DashboadLinkWidget.WIDGET_ID, new WidgetSelection(DashboadLinkWidget.class.getName(), DashboadLinkWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.dashboardlinker"), TitleWidget.ICON));
            put(ArrowWidget.WIDGET_ID, new WidgetSelection(ArrowWidget.class.getName(), ArrowWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.arrow"), TitleWidget.ICON));
            put(SankeyWidget.WIDGET_ID, new WidgetSelection(SankeyWidget.class.getName(), SankeyWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.sankey"), TitleWidget.ICON));
            put(ImageWidget.WIDGET_ID, new WidgetSelection(ImageWidget.class.getName(), ImageWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.image"), TitleWidget.ICON));
            put(GaugeWidget.WIDGET_ID, new WidgetSelection(GaugeWidget.class.getName(), GaugeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.gauge"), TitleWidget.ICON));
            put(LinearGaugeWidget.WIDGET_ID, new WidgetSelection(LinearGaugeWidget.class.getName(), LinearGaugeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.lineargauge"), TitleWidget.ICON));
            put(ShapeWidget.WIDGET_ID, new WidgetSelection(ShapeWidget.class.getName(), ShapeWidget.WIDGET_ID, I18n.getInstance().getString("dashboard.widget.shape"), TitleWidget.ICON));
            put(TimeFrameWidget.WIDGET_ID, new WidgetSelection(TimeFrameWidget.class.getName(), TimeFrameWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.timeframe"), TitleWidget.ICON));
            put(BatteryWidget.WIDGET_ID, new WidgetSelection(BatteryWidget.class.getName(), BatteryWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.battery"), TitleWidget.ICON));
            put(NetGraphWidget.WIDGET_ID, new WidgetSelection(NetGraphWidget.class.getName(), NetGraphWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.net"), TitleWidget.ICON));
            put(ToogleSwitchWidget.WIDGET_ID, new WidgetSelection(ToogleSwitchWidget.class.getName(), ToogleSwitchWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.toggleswitch"), TitleWidget.ICON));
            put(SliderWidget.WIDGET_ID, new WidgetSelection(SliderWidget.class.getName(), SliderWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.slider"), TitleWidget.ICON));
            put(PlusMinusWidget.WIDGET_ID, new WidgetSelection(PlusMinusWidget.class.getName(), PlusMinusWidget.WIDGET_ID, I18n.getInstance().getString("plugin.dashboard.plusminus"), TitleWidget.ICON));
        }
    };

    private static EmptyValueWidget emptyValueWidget = null;

    public static ImageView getImage(String widgetType, double height, double width) {
        ImageView image = new ImageView(availableWidgets.get(widgetType).getIcon());
        image.fitHeightProperty().set(height);
        image.fitWidthProperty().set(width);
        return image;
    }

    /**
     * Creates a widget instance for the given type using pre-resolved constructor references.
     * No reflection at runtime — all widget types are registered in the static FACTORIES map.
     *
     * @param widgetType the widget type identifier (e.g. {@link ValueWidget#WIDGET_ID})
     * @param control    the dashboard controller
     * @param config     the widget configuration POJO
     * @return the new widget instance
     * @throws IllegalArgumentException if the widget type is not registered
     */
    public static Widget createWidget(String widgetType, DashboardControl control, WidgetPojo config) {
        BiFunction<DashboardControl, WidgetPojo, Widget> factory = FACTORIES.get(widgetType);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown widget type: '" + widgetType + "'. Not registered in Widgets.FACTORIES.");
        }
        return factory.apply(control, config);
    }

    public static EmptyValueWidget emptyValueWidget(DashboardControl dashboardControl) {
        if (emptyValueWidget == null) {
            emptyValueWidget = new EmptyValueWidget(dashboardControl, null);
        }
        return emptyValueWidget;
    }
}
