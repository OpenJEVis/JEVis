package org.jevis.jecc.plugin.loadprofile.data;

import de.jollyday.HolidayManager;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class SyntheticLoadProfile {

    private final ListProperty<Long> inputDataRows = new SimpleListProperty<>(this, "inputDataRows");
    private final LongProperty outputDataRow = new SimpleLongProperty(this, "outputDataRow", -1L);
    private final ObjectProperty<HolidayManager> holidayManager = new SimpleObjectProperty<>(this, "holidayManager");
    private final BooleanProperty holidaysEnabled = new SimpleBooleanProperty(this, "holidaysEnabled", false);

    //TODO list of scheduled events/disturbances


    public ObservableList<Long> getInputDataRows() {
        return inputDataRows.get();
    }

    public void setInputDataRows(ObservableList<Long> inputDataRows) {
        this.inputDataRows.set(inputDataRows);
    }

    public ListProperty<Long> inputDataRowsProperty() {
        return inputDataRows;
    }

    public long getOutputDataRow() {
        return outputDataRow.get();
    }

    public void setOutputDataRow(long outputDataRow) {
        this.outputDataRow.set(outputDataRow);
    }

    public LongProperty outputDataRowProperty() {
        return outputDataRow;
    }

    public HolidayManager getHolidayManager() {
        return holidayManager.get();
    }

    public void setHolidayManager(HolidayManager holidayManager) {
        this.holidayManager.set(holidayManager);
    }

    public ObjectProperty<HolidayManager> holidayManagerProperty() {
        return holidayManager;
    }

    public boolean isHolidaysEnabled() {
        return holidaysEnabled.get();
    }

    public void setHolidaysEnabled(boolean holidaysEnabled) {
        this.holidaysEnabled.set(holidaysEnabled);
    }

    public BooleanProperty holidaysEnabledProperty() {
        return holidaysEnabled;
    }
}
