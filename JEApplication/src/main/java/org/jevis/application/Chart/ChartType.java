package org.jevis.application.Chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;

import java.util.ArrayList;
import java.util.List;

public enum ChartType {
    AREA, LINE, BAR, BUBBLE, SCATTER, PIE;

    private static SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());

    public static ChartType parseChartType(Integer chartTypeIndex) {
        switch (chartTypeIndex) {
            case (0):
                return AREA;
            case (1):
                return LINE;
            case (2):
                return BAR;
            case (3):
                return BUBBLE;
            case (4):
                return SCATTER;
            case (5):
                return PIE;
            default:
                return AREA;
        }
    }

    public static ChartType parseChartType(String chartType) {
        switch (chartType) {
            case ("AREA"):
                return AREA;
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
            case ("LINE"):
                return 1;
            case ("BAR"):
                return 2;
            case ("BUBBLE"):
                return 3;
            case ("SCATTER"):
                return 4;
            case ("PIE"):
                return 5;
            default:
                return 0;
        }
    }

    public static ObservableList<String> getlistNamesChartTypes() {
        List<String> tempList = new ArrayList<>();
        for (ChartType ct : ChartType.values()) {
            switch (ct.toString()) {
                case ("AREA"):
                    tempList.add(rb.getString("plugin.graph.charttype.area.name"));
                    break;
                case ("LINE"):
                    tempList.add(rb.getString("plugin.graph.charttype.line.name"));
                    break;
                case ("BAR"):
                    tempList.add(rb.getString("plugin.graph.charttype.bar.name"));
                    break;
                case ("BUBBLE"):
                    tempList.add(rb.getString("plugin.graph.charttype.bubble.name"));
                    break;
                case ("SCATTER"):
                    tempList.add(rb.getString("plugin.graph.charttype.scatter.name"));
                    break;
                case ("PIE"):
                    tempList.add(rb.getString("plugin.graph.charttype.pie.name"));
                    break;
                default:
                    break;
            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
