package org.jevis.jecc.plugin.action.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import static org.jevis.jecc.plugin.action.data.TableFilter.DATE_COMPARE.EQUALS;

public class TableFilter {
    private static final Logger logger = LogManager.getLogger(TableFilter.class);

    private final ObservableList<String> status = FXCollections.observableArrayList();
    private final ObservableList<String> medium = FXCollections.observableArrayList();
    private final ObservableList<String> field = FXCollections.observableArrayList();
    private DATE_COMPARE plannedDateComp = EQUALS;
    private String plannedDateFilter = "";

    public boolean show(ActionData data) {
        boolean isOneTrue = false;

        if (!plannedDateFilter.isEmpty()) {
            DateTime now = new DateTime();
            if (plannedDateComp == EQUALS) {
                logger.debug("Is bigger than");
                boolean isBigger = (data.plannedDateProperty().get().getYear() + "").contains(plannedDateFilter);


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
        logger.debug("Filter: " + isOneTrue);

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
