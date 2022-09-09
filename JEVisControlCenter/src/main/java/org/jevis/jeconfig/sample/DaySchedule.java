package org.jevis.jeconfig.sample;

import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.time.LocalTime;
import java.time.format.FormatStyle;

public class DaySchedule {

    private final BooleanProperty selectedProperty;
    private final ObjectProperty<LocalTime> startTime;
    private final ObjectProperty<LocalTime> endTime;

    private final int dayOfWeek;
    private final ToggleButton dayButton;
    private final VBox startVBox;
    private final JFXTimePicker start;
    private final VBox endVBox;
    private final JFXTimePicker end;

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

        start = new JFXTimePicker(LocalTime.of(0, 0, 0));
        start.setPrefWidth(100d);
        start.setMaxWidth(100d);
        start.set24HourView(true);
        start.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        startTime = start.valueProperty();

        Label endLabel = new Label(I18n.getInstance().getString("plugin.graph.changedate.enddate"));
        endLabel.setAlignment(Pos.CENTER);
        endVBox = new VBox(endLabel);
        endVBox.setAlignment(Pos.CENTER);

        end = new JFXTimePicker(LocalTime.of(23, 59, 59, 999999999));
        end.setPrefWidth(100d);
        end.setMaxWidth(100d);
        end.set24HourView(true);
        end.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));
        endTime = end.valueProperty();
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

    public JFXTimePicker getStart() {
        return start;
    }

    public VBox getEndVBox() {
        return endVBox;
    }

    public JFXTimePicker getEnd() {
        return end;
    }
}
