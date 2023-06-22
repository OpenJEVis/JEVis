package org.jevis.jeconfig.plugin.dashboard.config2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;
import org.jevis.jeconfig.plugin.dashboard.DashboardControl;
import org.jevis.jeconfig.plugin.dashboard.widget.Widget;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.List;

public class TimeFrameWidgetObject extends Widget {

    private BooleanProperty selected = new SimpleBooleanProperty(false);

    private ObjectProperty<Start> startObjectProperty = new SimpleObjectProperty<>(Start.NONE);
    private ObjectProperty<End> endObjectProperty = new SimpleObjectProperty<>(End.NONE);


    public TimeFrameWidgetObject(DashboardControl control, WidgetPojo config) {
        super(control, config);
    }

    @Override
    public void debug() {

    }

    @Override
    public WidgetPojo createDefaultConfig() {
        return null;
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
        return false;
    }

    @Override
    public List<DateTime> getMaxTimeStamps() {
        List<DateTime> dateTimes = new ArrayList<>();
        super.sampleHandler.getMaxTimeStamps();
        System.out.println("test");
        return dateTimes;

    }

    @Override
    public void init() {

    }

    @Override
    public String typeID() {
        return null;
    }

    @Override
    public ObjectNode toNode() {
        return null;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public Start getStartObjectProperty() {
        return startObjectProperty.get();
    }

    public ObjectProperty<Start> startObjectPropertyProperty() {
        return startObjectProperty;
    }

    public void setStartObjectProperty(Start startObjectProperty) {
        this.startObjectProperty.set(startObjectProperty);
    }

    public End getEndObjectProperty() {
        return endObjectProperty.get();
    }

    public ObjectProperty<End> endObjectPropertyProperty() {
        return endObjectProperty;
    }

    public void setEndObjectProperty(End endObjectProperty) {
        this.endObjectProperty.set(endObjectProperty);
    }

    @Override
    public String toString() {
        return "TimeFrameWidgetObject{" +
                "selected=" + selected +
                ", startObjectProperty=" + startObjectProperty +
                ", endObjectProperty=" + endObjectProperty +
                ", config=" + config +
                '}';
    }

    public enum Start {
        PERIODE_FROM, NONE

    }

    public enum End {
        LAST_TS, PERIODE_UNTIL, NONE

    }
}

