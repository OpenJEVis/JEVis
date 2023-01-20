package org.jevis.jeconfig.plugin.action.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.joda.time.DateTime;

import static org.jevis.jeconfig.plugin.action.data.TableFilter.DATE_COMPARE.EQUALS;

public class TableFilter {


    private ObservableList<String> status = FXCollections.observableArrayList();
    private ObservableList<String> medium = FXCollections.observableArrayList();
    private ObservableList<String> field = FXCollections.observableArrayList();
    private DATE_COMPARE plannedDateComp = EQUALS;
    private String plannedDateFilter = "";

    public boolean show(ActionData data) {
        boolean isOneTrue = false;

        if (!plannedDateFilter.isEmpty()) {
            DateTime now = new DateTime();
            if (plannedDateComp == EQUALS) {
                System.out.println("Is bigger than");
                boolean isBigger = false;


                if ((data.plannedDateProperty().get().getYear() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

                if ((data.plannedDateProperty().get().getMonthOfYear() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

                if ((data.plannedDateProperty().get().getDayOfMonth() + "").contains(plannedDateFilter)) {
                    isBigger = true;
                }

                if (isBigger) {
                    isOneTrue = true;
                }
            }
        }
        System.out.println("Filter: " + isOneTrue);

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
