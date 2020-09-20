package org.jevis.commons.dataprocessing;

public enum ManipulationMode {

    AVERAGE, MIN, MAX, MEDIAN, RUNNING_MEAN, CENTRIC_RUNNING_MEAN, SORTED_MIN, SORTED_MAX, CUMULATE, NONE;

    public static ManipulationMode get(String modeName) {
        String[] modeArray = modeName.split("_");
        String mode = NONE.name();
        if (modeArray.length == 2) {
            if (modeArray[0].equals("RUNNING") || modeArray[0].equals("SORTED")) {
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
            case ("NONE"):
            case ("None"):
                return NONE;
            case ("AVERAGE"):
            case ("Average"):
                return AVERAGE;
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
            default:
                return NONE;
        }
    }
}
