package org.jevis.jeconfig.plugin.dashboard.timeframe;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.skins.JFXDatePickerSkin;
import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;

public class TimeFrameEditor extends Popup {

    public ObjectProperty<Interval> intervalProperty;
    public TimeFrameFactory timeFrame;
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

    public TimeFrameEditor(TimeFrameFactory timeFrame, Interval interval) {
        super();
        this.intervalProperty = new SimpleObjectProperty<>(interval);
        this.timeFrame = timeFrame;
        this.datePicker = new JFXDatePicker(LocalDate.now());
        this.datePicker.setShowWeekNumbers(true);
        JFXDatePickerSkin datePickerSkin = new JFXDatePickerSkin(this.datePicker);
        ComboBoxPopupControl<LocalDate> comboBoxPopupControl = datePickerSkin;
        Node popupContent = null;
        try {
            popupContent = (Node) TimeFrameEditor.getPopupContent.invoke(datePickerSkin);
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

    public void setTimeFrame(TimeFrameFactory timeFrame) {
        this.timeFrame = timeFrame;
    }

    public void setIntervalProperty(Interval interval) {
        intervalProperty.setValue(interval);
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
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }


}
