package org.jevis.commons.object.plugin;

import java.util.ArrayList;
import java.util.List;

public class RangingValues {

    private final List<RangingValue> rangingValues = new ArrayList<>();

    public List<RangingValue> getRangingValues() {
        return rangingValues;
    }

    public void setRangingValues(List<RangingValue> rangingValues) {
        this.rangingValues.clear();
        this.rangingValues.addAll(rangingValues);
    }

    public void reset() {
        this.rangingValues.clear();
    }
}
