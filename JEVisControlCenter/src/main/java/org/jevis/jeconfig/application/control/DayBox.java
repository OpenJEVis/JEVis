package org.jevis.jeconfig.application.control;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class DayBox extends ComboBox<Integer> {

    public DayBox() {
        super();

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < 32; i++) {
            list.add(i);
        }

        ObservableList<Integer> enterDataTypes = FXCollections.observableArrayList(list);
        setItems(enterDataTypes);

        getSelectionModel().select(DateTime.now().getDayOfMonth());
    }

    public void setDays(int days) {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i < days + 1; i++) {
            list.add(i);
        }

        Platform.runLater(() -> getItems().setAll(list));
    }
}
