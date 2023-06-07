package org.jevis.jecc.application.Chart.ChartPluginElements;

import io.github.palexdev.materialfx.controls.MFXTextField;
import org.jevis.jecc.application.Chart.ChartSetting;

public class ChartNameTextField extends MFXTextField {

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
