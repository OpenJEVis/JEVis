package org.jevis.jeconfig.application.Chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.jeconfig.tool.I18n;

import java.util.ArrayList;
import java.util.List;

public enum ChartType {
    AREA, LOGICAL, LINE, BAR, BUBBLE, SCATTER, PIE;

    public static ChartType parseChartType(Integer chartTypeIndex) {
        switch (chartTypeIndex) {
            case (0):
                return AREA;
            case (1):
                return LOGICAL;
            case (2):
                return LINE;
            case (3):
                return BAR;
            case (4):
                return BUBBLE;
            case (5):
                return SCATTER;
            case (6):
                return PIE;
            default:
                return AREA;
        }
    }

    public static ChartType parseChartType(String chartType) {
        switch (chartType) {
            case ("AREA"):
                return AREA;
            case ("LOGICAL"):
                return LOGICAL;
            case ("LINE"):
                return LINE;
            case ("BAR"):
                return BAR;
            case ("BUBBLE"):
                return BUBBLE;
            case ("SCATTER"):
                return SCATTER;
            case ("PIE"):
                return PIE;
            default:
                return AREA;
        }
    }

    public static Integer parseChartIndex(ChartType chartType) {

        switch (chartType.toString()) {
            case ("AREA"):
                return 0;
            case ("LOGICAL"):
                return 1;
            case ("LINE"):
                return 2;
            case ("BAR"):
                return 3;
            case ("BUBBLE"):
                return 4;
            case ("SCATTER"):
                return 5;
            case ("PIE"):
                return 6;
            default:
                return 0;
        }
    }

    public static ObservableList<String> getlistNamesChartTypes() {
        List<String> tempList = new ArrayList<>();
        for (ChartType ct : ChartType.values()) {
            switch (ct.toString()) {
                case ("AREA"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.area.name"));
                    break;
                case ("LOGICAL"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.logical.name"));
                    break;
                case ("LINE"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.line.name"));
                    break;
                case ("BAR"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.bar.name"));
                    break;
                case ("BUBBLE"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.bubble.name"));
                    break;
                case ("SCATTER"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.scatter.name"));
                    break;
                case ("PIE"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.pie.name"));
                    break;
                default:
                    break;
            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
