package org.jevis.jecc.plugin.dashboard.timeframe;

import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.YearMonth;

public class TimeFrameEditor extends Popup {

    private static Method getPopupContent;

    static {
        //TODO JFX17
//        try {
//            getPopupContent = MFXDatePickerSkin.class.getDeclaredMethod("getPopupContent");
//            getPopupContent.setAccessible(true);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
    }

    public ObjectProperty<Interval> intervalProperty;
    public TimeFrame timeFrame;
    MFXDatePicker datePicker;

    public TimeFrameEditor(TimeFrame timeFrame, Interval interval) {
        super();
        this.intervalProperty = new SimpleObjectProperty<>(interval);
        this.timeFrame = timeFrame;
        this.datePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
        //TODO JFX17
        //this.datePicker.setShowWeekNumbers(true);
//        JFXDatePickerSkin datePickerSkin = new JFXDatePickerSkin(this.datePicker);
//        ComboBoxPopupControl<LocalDate> comboBoxPopupControl = datePickerSkin;
//        Node popupContent = null;
//        try {
//            popupContent = (Node) TimeFrameEditor.getPopupContent.invoke(datePickerSkin);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }


        BorderPane borderPane = new BorderPane();
        // borderPane.setCenter(popupContent);

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

        if (this.timeFrame != null) {
            this.intervalProperty.setValue(this.timeFrame.getInterval(newDateTime));
        }
    }

    public ObjectProperty<Interval> getIntervalProperty() {
        return this.intervalProperty;
    }

    public void setIntervalProperty(Interval interval) {
        intervalProperty.setValue(interval);
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }


}
