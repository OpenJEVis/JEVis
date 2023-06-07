package org.jevis.jecc.application.control;

import io.github.palexdev.materialfx.controls.MFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class YearBox extends MFXComboBox<Integer> {

    private MonthBox monthBox;
    private DayBox dayBox;

    public YearBox(DateTime nextTS) {
        super();

        List<Integer> list = new ArrayList<>();

        for (int i = 1990; i < 2050; i++) {
            list.add(i);
        }

        ObservableList<Integer> integers = FXCollections.observableArrayList(list);
        setItems(integers);

        Integer year = DateTime.now().getYear();

        if (nextTS != null) {
            selectItem(nextTS.getYear());
        } else {
            selectItem(year);
        }
    }

    public YearBox() {
        this(null);
    }

    public void setRelations(MonthBox monthBox, DayBox dayBox) {
        this.monthBox = monthBox;
        this.dayBox = dayBox;

        getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                Integer year = newValue;
                Integer month = monthBox.getSelectionModel().getSelectedIndex() + 1;
                YearMonth yearMonthObject = YearMonth.of(year, month);
                dayBox.setDays(yearMonthObject.lengthOfMonth());
            }
        });
    }

    public void setTS(DateTime nextTS) {
        if (nextTS != null) {
            Platform.runLater(() -> selectItem(Integer.valueOf(nextTS.getYear())));
        }
    }
}
