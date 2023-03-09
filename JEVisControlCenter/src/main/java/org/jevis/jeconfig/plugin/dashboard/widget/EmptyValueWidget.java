package org.jevis.jeconfig.plugin.dashboard.widget;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.image.ImageView;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.config2.WidgetPojo;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;

public class EmptyValueWidget extends Widget {

    public String TYPE_ID = "EmptyValueWidget";

    public EmptyValueWidget(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void debug() {

    }

    @Override
    public WidgetPojo createDefaultConfig() {
        WidgetPojo widgetPojo = new WidgetPojo();
        widgetPojo.setTitle(I18n.getInstance().getString("plugin.dashboard.valuewidget.newname"));
        widgetPojo.setType(TYPE_ID);
        return widgetPojo;
    }

    @Override
    public ImageView getImagePreview() {
        return null;
    }

    @Override
    public void updateData(Interval interval) {

    }

    @Override
    public void updateLayout() {

    }

    @Override
    public void updateConfig() {

    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public List<DateTime> getMaxTimeStamps() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public String typeID() {
        return TYPE_ID;
    }

    @Override
    public ObjectNode toNode() {
        return null;
    }
}
