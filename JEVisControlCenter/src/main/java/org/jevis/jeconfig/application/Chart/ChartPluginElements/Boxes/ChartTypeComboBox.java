package org.jevis.jeconfig.application.Chart.ChartPluginElements.Boxes;

import org.jevis.jeconfig.application.Chart.ChartSetting;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;

public class ChartTypeComboBox extends DisabledItemsComboBox {

    public ChartTypeComboBox(ChartSetting currentChartSetting) {
        super(ChartType.getlistNamesChartTypes());

        //I18n.getInstance().getString("plugin.graph.charttype.scatter.name"),
//        List<String> disabledItems = Arrays.asList(
//                I18n.getInstance().getString("plugin.graph.charttype.bubble.name"));
//        this.setDisabledItems(disabledItems);

        this.getSelectionModel().select(ChartType.parseChartIndex(currentChartSetting.getChartType()).intValue());

        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null || newValue != oldValue) {

                ChartType type = ChartType.parseChartType(this.getSelectionModel().getSelectedIndex());
                currentChartSetting.setChartType(type);

            }
        });
    }
}
