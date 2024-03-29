package org.jevis.jeconfig.plugin.dashboard.timeframe;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerContent;
import com.jfoenix.skins.JFXDatePickerSkin;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

public class TimeFrameEditor extends Popup {

    private final SimpleObjectProperty<Interval> intervalProperty;
    private TimeFrame timeFrame;
    private static Method getPopupContent;

    static {
        try {
            getPopupContent = JFXDatePickerSkin.class.getDeclaredMethod("getPopupContent");
            getPopupContent.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    JFXDatePicker datePicker;

    public TimeFrameEditor(TimeFrame timeFrame, Interval interval) {
        super();
        this.intervalProperty = new SimpleObjectProperty<>(this, "interval", interval);
        this.timeFrame = timeFrame;
        this.datePicker = new JFXDatePicker(LocalDate.now());
        this.datePicker.setShowWeekNumbers(true);
        JFXDatePickerSkin datePickerSkin = new JFXDatePickerSkin(this.datePicker);
        JFXDatePickerContent popupContent = null;
        try {
            popupContent = (JFXDatePickerContent) TimeFrameEditor.getPopupContent.invoke(datePickerSkin);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


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

    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    private void setIntervalResult() {
        LocalDate localDate = this.datePicker.valueProperty().getValue();
        if (this.datePicker.valueProperty().getValue().isAfter(LocalDate.now())) {
            localDate = LocalDate.now();
            this.datePicker.setValue(localDate);
            return;
        }

        DateTime newDateTime = new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
        setInterval(this.timeFrame.getInterval(newDateTime, false));
    }

    public SimpleObjectProperty<Interval> intervalProperty() {
        return intervalProperty;
    }

    public Interval getInterval() {
        return intervalProperty.get();
    }

    public void setInterval(Interval interval) {
        intervalProperty.setValue(interval);
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }


}
