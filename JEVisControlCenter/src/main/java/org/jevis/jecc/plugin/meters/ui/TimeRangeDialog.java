package org.jevis.jecc.plugin.meters.ui;

import io.github.palexdev.materialfx.controls.MFXDatePicker;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jevis.commons.i18n.I18n;
import org.jevis.jecc.ControlCenter;
import org.joda.time.DateTime;

import java.time.LocalDate;
import java.time.YearMonth;

public class TimeRangeDialog extends Alert {

    MFXDatePicker fromDatePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    MFXDatePicker untilDatePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());

    public TimeRangeDialog(DateTime from, DateTime until) {
        super(AlertType.CONFIRMATION);
        setHeaderText("Zeitbereich auswählen");
        setTitle("Zeitbereich auswählen");
        initOwner(ControlCenter.getStage());
        setResizable(true);

        GridPane gridPane = new GridPane();

        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(15);
        gridPane.setHgap(15);


        if (from != null) {
            LocalDate date = LocalDate.of(from.getYear(), from.getMonthOfYear(), from.getDayOfMonth());
            fromDatePicker.valueProperty().set(date);
        }
        if (until != null) {
            LocalDate date = LocalDate.of(until.getYear(), until.getMonthOfYear(), until.getDayOfMonth());
            untilDatePicker.valueProperty().set(date);
        }
        gridPane.add(new Label("Ab: "), 0, 0);
        gridPane.add(fromDatePicker, 1, 0);

        gridPane.add(new Label("Ais: "), 0, 1);
        gridPane.add(untilDatePicker, 1, 1);

        getDialogPane().setContent(gridPane);
    }

    private DateTime toDateTime(LocalDate localDate) {
        return new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
    }

    public DateTime getFromDate() {
        return toDateTime(fromDatePicker.valueProperty().get());
    }

    public DateTime getUntilDate() {
        return toDateTime(untilDatePicker.valueProperty().get());
    }
}
