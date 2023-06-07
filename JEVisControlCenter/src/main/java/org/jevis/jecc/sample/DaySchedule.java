package org.jevis.jecc.sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import jfxtras.scene.control.LocalTimePicker;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.util.Map;

public class DaySchedule {

    private final BooleanProperty selectedProperty;
    private final ObjectProperty<LocalTime> startTime;
    private final ObjectProperty<LocalTime> endTime;

    private final int dayOfWeek;
    private final ToggleButton dayButton;
    private final VBox startVBox;
    private final LocalTimePicker start;
    private final VBox endVBox;
    private final LocalTimePicker end;

    public DaySchedule(int dayOfWeek) {

        this.dayOfWeek = dayOfWeek;

        DateTime dateTime = new DateTime().withDayOfWeek(dayOfWeek);
        dayButton = new ToggleButton(dateTime.dayOfWeek().getAsText(I18n.getInstance().getLocale()));
        dayButton.setSelected(true);
        selectedProperty = dayButton.selectedProperty();

        Label startLabel = new Label(I18n.getInstance().getString("plugin.graph.changedate.startdate"));
        startLabel.setAlignment(Pos.CENTER);
        startVBox = new VBox(startLabel);
        startVBox.setAlignment(Pos.CENTER);

        start = new LocalTimePicker(LocalTime.of(0, 0, 0));
        start.setPrefWidth(100d);
        start.setMaxWidth(100d);
//        start.set24HourView(true);
//        start.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        startTime = start.localTimeProperty();

        Label endLabel = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        endLabel.setAlignment(Pos.CENTER);
        endVBox = new VBox(endLabel);
        endVBox.setAlignment(Pos.CENTER);

        end = new LocalTimePicker(LocalTime.of(23, 59, 59, 999999999));
        end.setPrefWidth(100d);
        end.setMaxWidth(100d);
//        end.set24HourView(true);
//        end.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        endTime = end.localTimeProperty();
    }

    public static boolean dateCheck(DateTime date, Map<Integer, DaySchedule> dayScheduleMap) {
        if (dayScheduleMap.isEmpty()) {
            return true;
        } else {
            DaySchedule daySchedule = dayScheduleMap.get(date.getDayOfWeek());
            if (daySchedule.isSelected()) {
                LocalTime dateTime = LocalTime.of(date.getHourOfDay(), date.getMinuteOfHour(), date.getSecondOfMinute(), date.getMillisOfSecond() * 100000);
                return dateTime.equals(daySchedule.getStartTime())
                        || (dateTime.isAfter(daySchedule.getStartTime()) && dateTime.isBefore(daySchedule.getEndTime()))
                        || dateTime.equals(daySchedule.getEndTime());
            }
        }

        return false;
    }

    public boolean isSelected() {
        return selectedProperty.get();
    }

    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    public LocalTime getStartTime() {
        return startTime.get();
    }

    public ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime.get();
    }

    public ObjectProperty<LocalTime> endTimeProperty() {
        return endTime;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public ToggleButton getDayButton() {
        return dayButton;
    }

    public VBox getStartVBox() {
        return startVBox;
    }

    public LocalTimePicker getStart() {
        return start;
    }

    public VBox getEndVBox() {
        return endVBox;
    }

    public LocalTimePicker getEnd() {
        return end;
    }
}
