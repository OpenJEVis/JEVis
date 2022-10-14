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
        switch (manipulation) {
            case ("AVERAGE"):
            case ("Average"):
            case ("average"):
                return AVERAGE;
            case ("SUM"):
            case ("Sum"):
            case ("sum"):
                return SUM;
            case ("MIN"):
            case ("Min"):
                return MIN;
            case ("MAX"):
            case ("Max"):
                return MAX;
            case ("MEDIAN"):
            case ("Median"):
                return MEDIAN;
            case ("RUNNING MEAN"):
            case ("RUNNING_MEAN"):
            case ("Running_Mean"):
            case ("Running Mean"):
                return RUNNING_MEAN;
            case ("GEOMETRIC MEAN"):
            case ("GEOMETRIC_MEAN"):
            case ("Geometric_Mean"):
            case ("Geometric Mean"):
                return GEOMETRIC_MEAN;
            case ("CENTRIC RUNNING MEAN"):
            case ("CENTRIC_RUNNING_MEAN"):
            case ("Centric_Running_Mean"):
            case ("Centric Running Mean"):
                return CENTRIC_RUNNING_MEAN;
            case ("SORTED MIN"):
            case ("SORTED_MIN"):
            case ("Sorted Min"):
            case ("Sorted_Min"):
                return SORTED_MIN;
            case ("SORTED MAX"):
            case ("SORTED_MAX"):
            case ("Sorted Max"):
            case ("Sorted_Max"):
                return SORTED_MAX;
            case ("CUMULATE"):
            case ("Cumulate"):
                return CUMULATE;
            case ("FORMULA"):
            case ("Formula"):
                return FORMULA;
            case ("NONE"):
            case ("None"):
            case ("none"):
            default:
                return NONE;
        }
    }

    public static Integer parseManipulationIndex(ManipulationMode manipulationMode) {
        if (manipulationMode != null) {
            switch (manipulationMode) {
                case AVERAGE:
                    return 1;
                case SUM:
                    return 2;
                case MIN:
                    return 3;
                case MAX:
                    return 4;
                case MEDIAN:
                    return 5;
                case RUNNING_MEAN:
                    return 6;
                case CENTRIC_RUNNING_MEAN:
                    return 7;
                case SORTED_MIN:
                    return 8;
                case SORTED_MAX:
                    return 9;
                case CUMULATE:
                    return 10;
                case GEOMETRIC_MEAN:
                    return 11;
                case FORMULA:
                    return 12;
                default:
                case NONE:
                    return 0;
            }
        } else return 0;
    }

    public static ManipulationMode parseManipulationIndex(Integer aggregationIndex) {
        switch (aggregationIndex) {
            case (1):
                return AVERAGE;
            case (2):
                return SUM;
            case (3):
                return MIN;
            case (4):
                return MAX;
            case (5):
                return MEDIAN;
            case (6):
                return RUNNING_MEAN;
            case (7):
                return CENTRIC_RUNNING_MEAN;
            case (8):
                return SORTED_MIN;
            case (9):
                return SORTED_MAX;
            case (10):
                return CUMULATE;
            case (11):
                return GEOMETRIC_MEAN;
            case (12):
                return FORMULA;
            case (0):
            default:
                return NONE;
        }
    }

    public static ObservableList<String> getListNamesManipulationModes() {
        List<String> tempList = new ArrayList<>();

        for (ManipulationMode manipulationMode : ManipulationMode.values()) {
            switch (manipulationMode) {
                case AVERAGE:
                    tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.average"));
                    break;
                case SUM:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.total"));
                    break;
                case MIN:
                    tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.min"));
                    break;
                case MAX:
                    tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.max"));
                    break;
                case MEDIAN:
                    tempList.add(I18n.getInstance().getString("plugin.object.report.dialog.aggregation.median"));
                    break;
                case RUNNING_MEAN:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.runningmean"));
                    break;
                case CENTRIC_RUNNING_MEAN:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.centricrunningmean"));
                    break;
                case SORTED_MIN:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.sortedmin"));
                    break;
                case SORTED_MAX:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.sortedmax"));
                    break;
                case CUMULATE:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.cumulate"));
                    break;
                case GEOMETRIC_MEAN:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.geometricmean"));
                    break;
                case FORMULA:
                    tempList.add(I18n.getInstance().getString("plugin.graph.manipulation.formula"));
                    break;
                case NONE:
                    tempList.add(I18n.getInstance().getString("plugin.graph.interval.preset"));
            }
        }
        return FXCollections.observableArrayList(tempList);
    }
}
