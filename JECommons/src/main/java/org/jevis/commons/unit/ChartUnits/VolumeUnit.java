package org.jevis.commons.unit.ChartUnits;

public enum VolumeUnit {
    L("L"), m3("m³"), Nm3("Nm³");

    private final String name;

    VolumeUnit(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}