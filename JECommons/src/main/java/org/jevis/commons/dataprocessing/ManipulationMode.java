package org.jevis.commons.dataprocessing;

public enum ManipulationMode {

    TOTAL, AVERAGE, MIN, MAX, MEDIAN, RUNNING_MEAN, CENTRIC_RUNNING_MEAN, SORTED_MIN, SORTED_MAX, NONE;

    public static ManipulationMode get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = TOTAL.name();
        if (modeArray.length == 2) {
            mode = modeArray[1];
        }
        return valueOf(mode);
    }
}
