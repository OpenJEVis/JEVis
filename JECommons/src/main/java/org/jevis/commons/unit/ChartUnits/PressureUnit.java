package org.jevis.commons.unit.ChartUnits;

public enum PressureUnit {
    bar("bar"), atm("atm");

    private final String name;

    PressureUnit(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
