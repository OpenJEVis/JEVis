package org.jevis.jeconfig.plugin.dashboard.timeframe;

//import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.LocalDate;

public class TimeFrameEdior extends Popup {

    public ObjectProperty<Interval> intervalProperty;
    public TimeFrameFactory timeFrame;
    DatePicker datePicker;

    public TimeFrameEdior(TimeFrameFactory timeFrame, Interval interval) {
        super();
        this.intervalProperty = new SimpleObjectProperty<>(interval);
        this.timeFrame = timeFrame;
        this.datePicker = new DatePicker(LocalDate.now());
        this.datePicker.setShowWeekNumbers(true);
        DatePickerSkin datePickerSkin = new DatePickerSkin(this.datePicker);
        Node popupContent = datePickerSkin.getPopupContent();
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(popupContent);

        getContent().add(borderPane);
        this.datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            setIntervalResult();
            this.hide();
        });

    }

    public void setDate(DateTime date) {
        this.datePicker.setValue(toLocalDate(date));
    }


    private void setIntervalResult() {
        LocalDate localDate = this.datePicker.valueProperty().getValue();
        if (this.datePicker.valueProperty().getValue().isAfter(LocalDate.now())) {
            localDate = LocalDate.now();
            this.datePicker.setValue(localDate);
            return;
        }

        DateTime newDateTime = new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
        this.intervalProperty.setValue(this.timeFrame.getInterval(newDateTime));
    }

    public ObjectProperty<Interval> getIntervalProperty() {
        return this.intervalProperty;
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        LocalDate newLocalDate = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        return newLocalDate;
    }


}
