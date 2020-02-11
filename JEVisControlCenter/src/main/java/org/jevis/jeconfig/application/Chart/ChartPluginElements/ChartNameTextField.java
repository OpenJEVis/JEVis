package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.scene.control.TextField;
import org.jevis.jeconfig.application.Chart.ChartSetting;

public class ChartNameTextField extends TextField {

    public ChartNameTextField(ChartSetting chartSetting) {
        super();

        this.setPrefWidth(114);
        this.setText(chartSetting.getName());

        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                chartSetting.setName(newValue);
            }
        });
    }
}
