package org.jevis.jecc.plugin.nonconformities.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

public class TableFilter {


    private final ObservableList<String> status = FXCollections.observableArrayList();
    private final ObservableList<String> medium = FXCollections.observableArrayList();
    private final ObservableList<String> field = FXCollections.observableArrayList();
    private DATE_COMPARE plannedDateComp = DATE_COMPARE.EQUALS;
    private String plannedDateFilter = "";

    public boolean show(NonconformityData data) {
        boolean isOneTrue = false;

        if (!plannedDateFilter.isEmpty()) {
            DateTime now = new DateTime();
            if (plannedDateComp == DATE_COMPARE.EQUALS) {
                boolean isBigger = (data.deadLineProperty().get().getYear() + "").contains(plannedDateFilter);


                if ((data.deadLineProperty().get().getMonthOfYear() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

                if ((data.deadLineProperty().get().getDayOfMonth() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

                if (isBigger) {
                    isOneTrue = true;
                }
            }
        }

        return isOneTrue;
    }

    public void setPlannedDateComp(DATE_COMPARE plannedDateComp) {
        this.plannedDateComp = plannedDateComp;
    }

    public void setPlannedDateFilter(String plannedDateFilter) {
        this.plannedDateFilter = plannedDateFilter;
    }

    public enum DATE_COMPARE {
        BIGGER_THAN, SMALLER_THAN, EQUALS
    }


}
