package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class ChartTypeComboBox extends DisabledItemsComboBox<String> {

    public ChartTypeComboBox(ChartSetting currentChartSetting) {
        super(ChartType.getlistNamesChartTypes());

        this.getSelectionModel().select(ChartType.parseChartIndex(currentChartSetting.getChartType()));

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                ChartType type = ChartType.parseChartType(this.getSelectionModel().getSelectedIndex());
                currentChartSetting.setChartType(type);
            }
        });
    }

    public ChartTypeComboBox(ChartType chartType) {
        super(ChartType.getlistNamesChartTypes());

        this.getSelectionModel().select(ChartType.parseChartIndex(chartType));

    }
}
