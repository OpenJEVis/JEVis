package org.jevis.jeconfig.application.Chart.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisUnit;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class ValueWithDateTime {
    private final SimpleDoubleProperty value = new SimpleDoubleProperty(this, "value", 0d);
    private final SimpleListProperty<DateTime> dateTime = new SimpleListProperty<>(this, "dateTime", FXCollections.observableArrayList(new ArrayList<DateTime>()));
    private final SimpleObjectProperty<JEVisUnit> unit = new SimpleObjectProperty<>(this, "unit");

    public ValueWithDateTime(Double value) {
        this.value.set(value);
    }

    public ValueWithDateTime(DateTime dateTime, Double value) {
        this.dateTime.get().add(dateTime);
        this.value.set(value);
    }

    public Double getValue() {
        return value.get();
    }

    public void setValue(Double value) {
        this.value.set(value);
    }

    public SimpleDoubleProperty valueProperty() {
        return value;
    }

    public ObservableList<DateTime> getDateTime() {
        return dateTime.get();
    }

    public void setDateTime(ObservableList<DateTime> dateTime) {
        this.dateTime.set(dateTime);
    }

    public SimpleListProperty<DateTime> dateTimeProperty() {
        return dateTime;
    }

    public JEVisUnit getUnit() {
        return unit.get();
    }

    public void setUnit(JEVisUnit unit) {
        this.unit.set(unit);
    }

    public SimpleObjectProperty<JEVisUnit> unitProperty() {
        return unit;
    }

    public void minCheck(DateTime dateTime, Double currentValue) {
        Double min = Math.min(getValue(), currentValue);
        if (!getValue().equals(min)) {
            setValue(min);
            getDateTime().clear();
            getDateTime().add(dateTime);
        } else if (getValue().equals(currentValue) && getDateTime().size() < 40) {
            getDateTime().add(dateTime);
        }
    }

    public void maxCheck(DateTime dateTime, Double currentValue) {
        Double max = Math.max(getValue(), currentValue);
        if (!getValue().equals(max)) {
            setValue(max);
            getDateTime().clear();
            getDateTime().add(dateTime);
        } else if (getValue().equals(currentValue) && getDateTime().size() < 40) {
            getDateTime().add(dateTime);
        }
    }
}
