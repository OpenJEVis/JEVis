package org.jevis.jeconfig.application.Chart.ChartPluginElements;

import javafx.scene.control.TextField;
import org.jevis.jeconfig.application.Chart.ChartSettings;

public class ChartNameTextField extends TextField {

    public ChartNameTextField(ChartSettings chartSettings) {
        super();

        this.setPrefWidth(114);
        this.setText(chartSettings.getName());

        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                chartSettings.setName(newValue);
            }
        });
    }
}
