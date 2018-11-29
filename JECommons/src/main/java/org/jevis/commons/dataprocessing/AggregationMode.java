package org.jevis.commons.dataprocessing;

public enum AggregationMode {

    TOTAL, AVERAGE, MIN, MAX, MEDIAN, RUNNINGMEAN, NONE;

    public static AggregationMode get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = TOTAL.name();
        if (modeArray.length == 2) {
            mode = modeArray[1];
        }
        return valueOf(mode);
    }
}
