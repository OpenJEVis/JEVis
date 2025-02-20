package org.jevis.jecc.plugin.loadprofile.data;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.joda.time.DateTime;

public class MixedLoadProfile {
    private final ObjectProperty<DateTime> dateTime = new SimpleObjectProperty<>(this, "dateTime");
    private final ObjectProperty<Object> workdayValue = new SimpleObjectProperty<>(this, "workdayValue");
    private final ObjectProperty<Object> holidayValue = new SimpleObjectProperty<>(this, "holidayValue");

    public MixedLoadProfile(DateTime dateTime, Object workdayValue, Object holidayValue) {
        this.dateTime.set(dateTime);
        this.workdayValue.set(workdayValue);
        this.holidayValue.set(holidayValue);
    }

    public DateTime getDateTime() {
        return dateTime.get();
    }

    public ObjectProperty<DateTime> dateTimeProperty() {
        return dateTime;
    }

    public Object getWorkdayValue() {
        return workdayValue.get();
    }

    public ObjectProperty<Object> workdayValueProperty() {
        return workdayValue;
    }

    public Object getHolidayValue() {
        return holidayValue.get();
    }

    public ObjectProperty<Object> holidayValueProperty() {
        return holidayValue;
    }
}
