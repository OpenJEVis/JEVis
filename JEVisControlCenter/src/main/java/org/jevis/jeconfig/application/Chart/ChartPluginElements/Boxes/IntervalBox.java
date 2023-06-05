package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXTimePicker;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.converter.LocalTimeStringConverter;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;

public class IntervalBox extends HBox {


    private final MFXDatePicker datePicker = new MFXDatePicker();
    private final JFXTimePicker timePicker = new JFXTimePicker();
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return IntervalBox.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    public IntervalBox(String date) {
        datePicker.setPrefWidth(120d);
        timePicker.setPrefWidth(100d);
        timePicker.setMaxWidth(100d);
        timePicker.set24HourView(true);
        timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        if (date != null) {
            DateTime selectedStart = new DateTime(date);
            datePicker.setValue(LocalDate.of(selectedStart.getYear(), selectedStart.getMonthOfYear(), selectedStart.getDayOfMonth()));
            timePicker.setValue(LocalTime.of(selectedStart.getHourOfDay(), selectedStart.getMinuteOfHour(), selectedStart.getSecondOfMinute()));
        }

        getChildren().setAll(datePicker, timePicker);
    }

    private static String getItemText(Cell<String> cell) {
        if (cell != null && cell.getItem() != null) {
            return new DateTime(cell.getItem()).toString("yyyy-MM-dd HH:mm:ss");
        }
        return "";
    }

    public static IntervalBox createComboBox(final Cell<String> cell) {
        String item = cell.getItem();
        final IntervalBox intervalBox = new IntervalBox(item);

        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        intervalBox.setOnAction(event -> {

            cell.commitEdit(intervalBox.getDate());
            event.consume();
        });
        intervalBox.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return intervalBox;
    }

    public static void startEdit(final Cell<String> cell,
                                 final HBox hbox,
                                 final Node graphic,
                                 final IntervalBox intervalBox) {
        if (intervalBox != null) {
            intervalBox.setDate(cell.getItem());
        }
        cell.setText(null);

        if (graphic != null) {
            hbox.getChildren().setAll(graphic, intervalBox);
            cell.setGraphic(hbox);
        } else {
            cell.setGraphic(intervalBox);
        }

        // requesting focus so that key input can immediately go into the
        // TextField (see RT-28132)
        intervalBox.requestFocus();
    }

    public static void cancelEdit(Cell<String> cell, Node graphic) {
        cell.setText(getItemText(cell));
        cell.setGraphic(graphic);
    }

    public static void updateItem(final Cell<String> cell,
                                  final HBox hbox,
                                  final Node graphic,
                                  final IntervalBox intervalBox) {
        if (cell.isEmpty()) {
            cell.setText(null);
            cell.setGraphic(null);
        } else {
            if (cell.isEditing()) {
                if (cell != null && cell.getItem() != null) {
                    intervalBox.setDate(cell.getItem());
                }

                if (graphic != null) {
                    hbox.getChildren().setAll(graphic, intervalBox);
                    cell.setGraphic(hbox);
                } else {
                    cell.setGraphic(intervalBox);
                }
            } else {
                cell.setText(getItemText(cell));
                cell.setGraphic(graphic);
            }
        }
    }

    public String getDate() {
        return new DateTime(datePicker.getValue().getYear(), datePicker.getValue().getMonthValue(), datePicker.getValue().getDayOfMonth(),
                timePicker.getValue().getHour(), timePicker.getValue().getMinute(), timePicker.getValue().getSecond()).toString();
    }

    public void setDate(String dateString) {
        DateTime dateTime = new DateTime(dateString);
        datePicker.setValue(LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth()));
        timePicker.setValue(LocalTime.of(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute()));
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onActionProperty().get();
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set(value);
    }
}
