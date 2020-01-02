package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public class AxisBox {
    private ChoiceBox choiceBox;

    public AxisBox(final ChartDataModel data) {
        List<String> axisList = new ArrayList<>();

        final String y1 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y1");
        final String y2 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y2");

        axisList.add(y1);
        axisList.add(y2);

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

            if (newValue.equals(y1)) {
                data.setAxis(0);
            } else if (newValue.equals(y2)) {
                data.setAxis(1);
            }
        });

        this.choiceBox = choiceBox;
    }

    public ChoiceBox getChoiceBox() {
        return choiceBox;
    }
}
