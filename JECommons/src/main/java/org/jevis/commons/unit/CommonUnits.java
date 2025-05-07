package org.jevis.commons.unit;

import org.jevis.api.JEVisUnit;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;


public interface CommonUnits {

    interface kWH {
        Unit unit = MetricPrefix.KILO(Units.WATT.multiply(Units.HOUR));
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface MWh {
        Unit unit = MetricPrefix.MEGA(Units.WATT.multiply(Units.HOUR));
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface kW {
        Unit unit = MetricPrefix.KILO(Units.WATT);
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface m3 {
        Unit unit = Units.CUBIC_METRE;
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }

    interface celsius {
        Unit unit = Units.CELSIUS;
        JEVisUnit jevisUnit = new JEVisUnitImp(unit);
    }
}
