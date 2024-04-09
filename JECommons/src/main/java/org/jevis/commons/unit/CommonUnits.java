package org.jevis.commons.unit;

import org.jevis.api.JEVisUnit;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public interface CommonUnits {

    interface kWH {
        Unit unit = SI.KILO(SI.WATT).times(NonSI.HOUR);
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface MWh {
        Unit unit = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface kW {
        Unit unit = SI.KILO(SI.WATT);
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface m3 {
        Unit unit = SI.CUBIC_METRE;
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface celsius {
        Unit unit = SI.CELSIUS;
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }
}
