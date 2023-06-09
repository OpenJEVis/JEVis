package org.jevis.commons.dataprocessing;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.commons.i18n.I18n;

import java.util.ArrayList;
import java.util.List;

public enum ManipulationMode {

    NONE, AVERAGE, SUM, MIN, MAX, MEDIAN, RUNNING_MEAN, CENTRIC_RUNNING_MEAN, SORTED_MIN, SORTED_MAX, CUMULATE, GEOMETRIC_MEAN, FORMULA;

    public static ManipulationMode get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = NONE.name();
        if (modeArray.length == 2) {
            if (modeArray[0].equals("RUNNING") || modeArray[0].equals("SORTED") || modeArray[0].equals("GEOMETRIC")) {
                mode = modeName;
            } else {
                mode = modeArray[1];
            }
        } else if (modeArray.length == 3) {
            mode = modeArray[1] + "_" + modeArray[2];
        }
        return valueOf(mode);
    }

    public static ManipulationMode parseManipulation(String manipulation) {
        return switch (manipulation) {
            case ("AVERAGE"), ("Average"), ("average") -> AVERAGE;
            case ("SUM"), ("Sum"), ("sum") -> SUM;
            case ("MIN"), ("Min") -> MIN;
            case ("MAX"), ("Max") -> MAX;
            case ("MEDIAN"), ("Median") -> MEDIAN;
            case ("RUNNING MEAN"), ("RUNNING_MEAN"), ("Running_Mean"), ("Running Mean") -> RUNNING_MEAN;
            case ("GEOMETRIC MEAN"), ("GEOMETRIC_MEAN"), ("Geometric_Mean"), ("Geometric Mean") -> GEOMETRIC_MEAN;
            case ("CENTRIC RUNNING MEAN"), ("CENTRIC_RUNNING_MEAN"), ("Centric_Running_Mean"), ("Centric Running Mean") ->
                    CENTRIC_RUNNING_MEAN;
            case ("SORTED MIN"), ("SORTED_MIN"), ("Sorted Min"), ("Sorted_Min") -> SORTED_MIN;
            case ("SORTED MAX"), ("SORTED_MAX"), ("Sorted Max"), ("Sorted_Max") -> SORTED_MAX;
            case ("CUMULATE"), ("Cumulate") -> CUMULATE;
            case ("FORMULA"), ("Formula") -> FORMULA;
            default -> NONE;
        };
    }

    public static Integer parseManipulationIndex(ManipulationMode manipulationMode) {
        if (manipulationMode != null) {
            return switch (manipulationMode) {
                case AVERAGE -> 1;
                case SUM -> 2;
                case MIN -> 3;
                case MAX -> 4;
                case MEDIAN -> 5;
                case RUNNING_MEAN -> 6;
                case CENTRIC_RUNNING_MEAN -> 7;
                case SORTED_MIN -> 8;
                case SORTED_MAX -> 9;
                case CUMULATE -> 10;
                case GEOMETRIC_MEAN -> 11;
                case FORMULA -> 12;
                default -> 0;
            };
        } else return 0;
    }

    public static ManipulationMode parseManipulationIndex(Integer aggregationIndex) {
        return switch (aggregationIndex) {
            case (1) -> AVERAGE;
            case (2) -> SUM;
            case (3) -> MIN;
            case (4) -> MAX;
            case (5) -> MEDIAN;
            case (6) -> RUNNING_MEAN;
            case (7) -> CENTRIC_RUNNING_MEAN;
            case (8) -> SORTED_MIN;
            case (9) -> SORTED_MAX;
            case (10) -> CUMULATE;
            case (11) -> GEOMETRIC_MEAN;
            case (12) -> FORMULA;
            default -> NONE;
        };
    }

    public static ObservableList<String> getListNamesManipulationModes() {
        List<String> tempList = new ArrayList<>();

        for (ManipulationMode manipulationMode : ManipulationMode.values()) {
            switch (manipulationMode) {
                case AVERAGE ->
                        tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.average"));
                case SUM -> tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.total"));
                case MIN -> tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.min"));
                case MAX -> tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.max"));
                case MEDIAN ->
                        tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.median"));
                case RUNNING_MEAN ->
                        tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.runningmean"));
                case CENTRIC_RUNNING_MEAN ->
                        tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean"));
                case SORTED_MIN -> tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.sortedmin"));
                case SORTED_MAX -> tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.sortedmax"));
                case CUMULATE -> tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.cumulate"));
                case GEOMETRIC_MEAN ->
                        tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.geometricmean"));
                case FORMULA -> tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.formula"));
                case NONE -> tempList.add(I18n.getInstance().getString("plugin.graph.interval.preset"));
            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
