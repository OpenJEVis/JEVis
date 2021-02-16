package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;

import java.util.ArrayList;
import java.util.List;

public class AxisBox extends JFXComboBox<String> {


    public AxisBox(final ChartDataRow data) {
        List<String> axisList = new ArrayList<>();

        final String y1 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y1");
        final String y2 = I18n.getInstance().getString("plugin.graph.chartplugin.axisbox.y2");

        axisList.add(y1);
        axisList.add(y2);

        setItems(FXCollections.observableArrayList(axisList));

        this.setMinWidth(40);

        this.getSelectionModel().selectFirst();
        switch (data.getAxis()) {
            case 0:
                this.getSelectionModel().selectFirst();
                break;
            case 1:
                this.getSelectionModel().select(1);
                break;
        }

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(y1)) {
                data.setAxis(0);
            } else if (newValue.equals(y2)) {
                data.setAxis(1);
            }
        });
    }
}
