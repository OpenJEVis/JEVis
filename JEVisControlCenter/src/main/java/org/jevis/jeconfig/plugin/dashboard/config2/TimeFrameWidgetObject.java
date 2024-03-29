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

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private final ObjectProperty<Start> startObjectProperty = new SimpleObjectProperty<>(Start.NONE);
    private final ObjectProperty<End> endObjectProperty = new SimpleObjectProperty<>(End.NONE);

    private final BooleanProperty countOfSamples = new SimpleBooleanProperty();


    public TimeFrameWidgetObject(DashboardControl control, WidgetPojo config) {
        super(control, config);
        addListner();
    }

    private void addListner() {

        countOfSamples.addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                startObjectProperty.set(Start.NONE);
                endObjectProperty.set(End.NONE);
            }
        });
        startObjectProperty.addListener((observableValue, start, t1) -> {
            System.out.println(t1);
            if (!t1.equals(Start.NONE)) {
                countOfSamples.set(false);

            }
        });
        endObjectProperty.addListener((observableValue, start, t1) -> {
            System.out.println(t1);
            if (!t1.equals(End.NONE)) {
                countOfSamples.set(false);

            }
        });



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

    public boolean getCountOfSamples() {
        return countOfSamples.get();
    }

    public void setCountOfSamples(boolean countOfSamples) {
        this.countOfSamples.set(countOfSamples);
    }

    public BooleanProperty countOfSamplesProperty() {
        return countOfSamples;
    }

    public enum Start {
        PERIODE_FROM, NONE

    }

    public enum End {
        LAST_TS, PERIODE_UNTIL, NONE

    }
}

