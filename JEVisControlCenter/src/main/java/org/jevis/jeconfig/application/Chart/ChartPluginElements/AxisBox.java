package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import org.jevis.jeconfig.application.Chart.ChartDataModel;

import java.util.ArrayList;
import java.util.List;

public class AxisBox {
    private ChoiceBox choiceBox;

    public AxisBox(final ChartDataModel data) {
        List<String> axisList = new ArrayList<>();

        final String keyPreset = "0";
        String one = "1";

        axisList.add(keyPreset);
        axisList.add(one);

        ChoiceBox choiceBox = new ChoiceBox(FXCollections.observableArrayList(axisList));

        choiceBox.setMinWidth(40);

        choiceBox.getSelectionModel().selectFirst();
        switch (data.getAxis()) {
            case 0:
                choiceBox.getSelectionModel().selectFirst();
                break;
            case 1:
                choiceBox.getSelectionModel().select(1);
                break;
        }

        choiceBox.valueProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {

            if (newValue.equals(keyPreset)) {
                data.setAxis(0);
            } else if (newValue.equals(one)) {
                data.setAxis(1);
            }
        });

        this.choiceBox = choiceBox;
    }

    public ChoiceBox getChoiceBox() {
        return choiceBox;
    }
}
