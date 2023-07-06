package org.jevis.jeconfig.plugin.dashboard.config2;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

public class GaugeSectionPojo {
    private DoubleProperty start = new SimpleDoubleProperty();
    private DoubleProperty end = new SimpleDoubleProperty();
    private Color color = Color.DARKBLUE;

    public GaugeSectionPojo(double end, Color color,DoubleProperty doubleProperty ) {
        this.end.setValue(end);
        this.color = color;
        startProperty().bindBidirectional(doubleProperty);
    }

    public GaugeSectionPojo(double start, double end, Color color) {
        this.start.setValue(start);
        this.end.setValue(end);
        this.color = color;
    }

    public GaugeSectionPojo() {
    }

    public GaugeSectionPojo(DoubleProperty doubleProperty) {
        startProperty().bindBidirectional(doubleProperty);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "GaugeSection{" +
                "minimum=" + start +
                ", maximum=" + end +
                ", color=" + color +
                '}';
    }


    public StringProperty getStartasStringProperty() {
        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bindBidirectional(start, new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return number.toString();
            }

            @Override
            public Number fromString(String s) {
                return Double.valueOf(s);
            }
        });

        return stringProperty;

    }
    public StringProperty getEndAsStringProperty() {
        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bindBidirectional(end, new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return number.toString();
            }

            @Override
            public Number fromString(String s) {
                return Double.valueOf(s);
            }
        });

        return stringProperty;

    }


    public double getStart() {
        return start.get();
    }

    public DoubleProperty startProperty() {
        return start;
    }

    public void setStart(double start) {
        this.start.set(start);
    }

    public double getEnd() {
        return end.get();
    }

    public DoubleProperty endProperty() {
        return end;
    }

    public void setEnd(double end) {
        this.end.set(end);
    }
}
