package org.jevis.jeconfig.application.Chart.ChartUnits;

public enum VolumeFlowUnit {
    literPerSecond("L/s"),
    literPerMinute("L/min"),
    literPerHour("L/h"),

    cubicMeterPerSecond("m³/s"),
    cubicMeterPerMinute("m³/min"),
    cubicMeterPerHour("m³/h");


    private final String name;

    VolumeFlowUnit(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
