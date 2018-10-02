package org.jevis.application.Chart.ChartUnits;

public enum VolumeUnit {
    L("L"), m3("m³");

    private final String name;

    VolumeUnit(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}