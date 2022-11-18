package org.jevis.commons.object.plugin;

import javafx.beans.property.SimpleDoubleProperty;

public class RangingValue {

    private final SimpleDoubleProperty from = new SimpleDoubleProperty(this, "from", -1);
    private final SimpleDoubleProperty to = new SimpleDoubleProperty(this, "to", -1);
    private final SimpleDoubleProperty value = new SimpleDoubleProperty(this, "value", -1);

    public double getFrom() {
        return from.get();
    }

    public void setFrom(double from) {
        this.from.set(from);
    }

    public SimpleDoubleProperty fromProperty() {
        return from;
    }

    public double getTo() {
        return to.get();
    }

    public void setTo(double to) {
        this.to.set(to);
    }

    public SimpleDoubleProperty toProperty() {
        return to;
    }

    public double getValue() {
        return value.get();
    }

    public void setValue(double value) {
        this.value.set(value);
    }

    public SimpleDoubleProperty valueProperty() {
        return value;
    }
}
