package org.jevis.jeconfig.plugin.nonconformities.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

public class TableFilter {


    private ObservableList<String> status = FXCollections.observableArrayList();
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> field = FXCollections.observableArrayList();
    private DATE_COMPARE plannedDateComp = DATE_COMPARE.EQUALS;
    private String plannedDateFilter = "";

    public boolean show(NonconformityData data) {
        boolean isOneTrue = false;

        if (!plannedDateFilter.isEmpty()) {
            DateTime now = new DateTime();
            if (plannedDateComp == DATE_COMPARE.EQUALS) {
                boolean isBigger = false;


                if ((data.deadLineProperty().get().getYear() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

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

    public static enum DATE_COMPARE {
        BIGGER_THAN, SMALLER_THAN, EQUALS
    }


}
