package org.jevis.jecc.application.Chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public enum ChartType {
    AREA, LOGICAL, LINE, BAR, COLUMN, BUBBLE, SCATTER, PIE, TABLE, HEAT_MAP, DEFAULT, TABLE_V, STACKED_AREA, STACKED_COLUMN;

    public static ChartType parseChartType(Integer chartTypeIndex) {
        switch (chartTypeIndex) {
            case (0):
                return AREA;
            case (1):
                return LOGICAL;
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
            case (10):
                return DEFAULT;
            case (11):
                return TABLE_V;
            case (12):
                return STACKED_AREA;
            case (13):
                return STACKED_COLUMN;
            case (2):
            default:
                return LINE;
        }
    }

    public static ChartType parseChartType(String chartType) {
        if (chartType != null) {
            switch (chartType) {
                case ("LOGICAL"):
                    return LOGICAL;
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
                case ("TABLE_V"):
                    return TABLE_V;
                case ("HEAT_MAP"):
                    return HEAT_MAP;
                case ("DEFAULT"):
                    return DEFAULT;
                case ("AREA"):
                    return AREA;
                case ("STACKED_AREA"):
                    return STACKED_AREA;
                case ("STACKED_COLUMN"):
                    return STACKED_COLUMN;
                case ("LINE"):
                default:
                    return LINE;
            }
        } else return LINE;
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
                case ("DEFAULT"):
                    return 10;
                case ("TABLE_V"):
                    return 11;
                case ("STACKED_AREA"):
                    return 12;
                case ("STACKED_COLUMN"):
                    return 13;
                default:
                    return 2;
            }
        } else return 2;
    }

    public static ObservableList<String> getListNamesChartTypes() {
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
                case ("TABLE_V"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.tablev.name"));
                    break;
                case ("HEAT_MAP"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.heatmap.name"));
                    break;
                case ("DEFAULT"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.default.name"));
                    break;
                case ("STACKED_AREA"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.stackedarea.name"));
                    break;
                case ("STACKED_COLUMN"):
                    tempList.add(I18n.getInstance().getString("plugin.graph.charttype.stackedcolumn.name"));
                    break;

            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
