package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class ChartTypeComboBox extends DisabledItemsComboBox {

    public ChartTypeComboBox(ChartSetting currentChartSetting) {
        super(ChartType.getlistNamesChartTypes());

        this.getSelectionModel().select(ChartType.parseChartIndex(currentChartSetting.getChartType()).intValue());

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {

                ChartType type = ChartType.parseChartType(this.getSelectionModel().getSelectedIndex());
                currentChartSetting.setChartType(type);

            }
        });
    }

    public ChartTypeComboBox(ChartType chartType) {
        super(ChartType.getlistNamesChartTypes());

        this.getSelectionModel().select(ChartType.parseChartIndex(chartType).intValue());

    }
}
