package org.jevis.application.Chart.ChartUnits;

public enum VolumeFlowUnit {
    literPerSecond("L/s"),
    literPerMinute("L/m"),
    literPerHour("L/h"),

    cubicMeterPerSecond("m³/s"),
    cubicMeterPerMinute("m³/m"),
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
