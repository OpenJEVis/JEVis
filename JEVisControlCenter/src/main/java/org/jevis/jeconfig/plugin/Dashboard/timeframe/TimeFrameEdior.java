package org.jevis.jeconfig.plugin.Dashboard.timeframe;

import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.LocalDate;

public class TimeFrameEdior extends Popup {

    public final ObjectProperty<Interval> intervalProperty;
    public final ObjectProperty<TimeFrameFactory> timeFrameProperty;
    DatePicker datePicker;

    public TimeFrameEdior(ObjectProperty<TimeFrameFactory> timeFrameProperty, ObjectProperty<Interval> intervalProperty) {
        super();
        this.intervalProperty = intervalProperty;
        this.timeFrameProperty = timeFrameProperty;
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setShowWeekNumbers(true);
        DatePickerSkin datePickerSkin = new DatePickerSkin(datePicker);
        Node popupContent = datePickerSkin.getPopupContent();
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(popupContent);

        getContent().add(borderPane);
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            setIntervalresult();
            this.hide();
        });

    }

    public void setDate(DateTime date) {
        datePicker.setValue(toLocalDate(date));
    }


    private void setIntervalresult() {
        DateTime newDateTime = new DateTime(datePicker.valueProperty().getValue().getYear(), datePicker.valueProperty().getValue().getMonthValue(), datePicker.valueProperty().getValue().getDayOfMonth(), 0, 0);
        Interval newInterval = new Interval(newDateTime, newDateTime);
//        intervalProperty.setValue(newInterval);
        intervalProperty.setValue(timeFrameProperty.getValue().getInterval(newDateTime));
//
    }

    public ObjectProperty<Interval> getIntervalProperty() {
        return intervalProperty;
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        LocalDate newLocalDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        return newLocalDate;
    }


}
