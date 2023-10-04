package org.jevis.commons.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public enum BubbleType {
    NONE, X, Y, SIZE;

    public static BubbleType parseBubbleType(String bubbleType) {
        switch (bubbleType) {
            case ("X"):
                return X;
            case ("Y"):
                return Y;
            case ("SIZE"):
                return SIZE;
            default:
                return NONE;
        }
    }

    public static Integer parseBubbleIndex(BubbleType bubbleType) {
        if (bubbleType != null) {
            switch (bubbleType.toString()) {
                default:
                    return 0;
                case ("X"):
                    return 1;
                case ("Y"):
                    return 2;
                case ("SIZE"):
                    return 3;

            }
        }
        return 0;
    }

    public static BubbleType parseBubbleIndex(Integer bubbleIndex) {
        if (bubbleIndex != null) {
            switch (bubbleIndex) {
                default:
                    return NONE;
                case (1):
                    return X;
                case (2):
                    return Y;
                case (3):
                    return SIZE;

            }
        }
        return NONE;
    }

    public static ObservableList<String> getListNamesBubbleTypes() {
        List<String> tempList = new ArrayList<>();
        for (BubbleType bt : BubbleType.values()) {
            tempList.add(bt.toString());
        }

        return FXCollections.observableArrayList(tempList);
    }

    @Override
    public String toString() {
        switch (this) {
            default:
            case NONE:
                return "NONE";
            case X:
                return "X";
            case Y:
                return "Y";
            case SIZE:
                return "SIZE";
        }
    }
}
