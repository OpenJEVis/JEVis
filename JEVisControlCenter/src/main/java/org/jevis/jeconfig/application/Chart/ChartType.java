package org.jevis.jeconfig.application.Chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public enum ChartType {
    AREA, LOGICAL, LINE, BAR, COLUMN, BUBBLE, SCATTER, PIE, TABLE, HEAT_MAP;

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
                return COLUMN;
            case (5):
                return BUBBLE;
            case (6):
                return SCATTER;
            case (7):
                return PIE;
            case (8):
                return TABLE;
            case (9):
                return HEAT_MAP;
            default:
                return AREA;
        }
    }

    public static ChartType parseChartType(String chartType) {
        switch (chartType) {
            case ("LOGICAL"):
                return LOGICAL;
            case ("LINE"):
                return LINE;
            case ("BAR"):
                return BAR;
            case ("COLUMN"):
                return COLUMN;
            case ("BUBBLE"):
                return BUBBLE;
            case ("SCATTER"):
                return SCATTER;
            case ("PIE"):
                return PIE;
            case ("TABLE"):
                return TABLE;
            case ("HEAT_MAP"):
                return HEAT_MAP;
            case ("AREA"):
            default:
                return AREA;
        }
    }

    public static Integer parseChartIndex(ChartType chartType) {
        if (chartType != null) {
            switch (chartType.toString()) {
                case ("AREA"):
                    return 0;
                case ("LOGICAL"):
                    return 1;
                case ("LINE"):
                    return 2;
                case ("BAR"):
                    return 3;
                case ("COLUMN"):
                    return 4;
                case ("BUBBLE"):
                    return 5;
                case ("SCATTER"):
                    return 6;
                case ("PIE"):
                    return 7;
                case ("TABLE"):
                    return 8;
                case ("HEAT_MAP"):
                    return 9;
                default:
                    return 2;
            }
        } else return 2;
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
                case ("COLUMN"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.column.name"));
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
                case ("TABLE"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.table.name"));
                    break;
                case ("HEAT_MAP"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.heatmap.name"));
                default:
                    break;
            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
