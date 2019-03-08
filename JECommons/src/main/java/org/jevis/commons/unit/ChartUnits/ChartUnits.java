package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class ChartUnits {

    public static JEVisUnit parseUnit(String unit) {
        JEVisUnit result = null;
        Unit _kg = SI.KILOGRAM;
        Unit _t = NonSI.METRIC_TON;

        Unit _l = NonSI.LITER;
        Unit _l2 = NonSI.LITRE;
        Unit _m3 = SI.CUBIC_METRE;
        Unit _Nm3 = SI.CUBIC_METRE.alternate("Nm³");

        Unit _literPerSecond = NonSI.LITER.divide(SI.SECOND);
        Unit _literPerMinute = NonSI.LITER.divide(NonSI.MINUTE);
        Unit _literPerHour = NonSI.LITER.divide(NonSI.HOUR);

        Unit _cubicMeterPerSecond = SI.CUBIC_METRE.divide(SI.SECOND);
        Unit _cubicMeterPerMinute = SI.CUBIC_METRE.divide(NonSI.MINUTE);
        Unit _cubicMeterPerHour = SI.CUBIC_METRE.divide(NonSI.HOUR);

        Unit _bar = NonSI.BAR;
        Unit _atm = NonSI.ATMOSPHERE;

        Unit _W = SI.WATT;
        Unit _kW = SI.KILO(SI.WATT);
        Unit _MW = SI.MEGA(SI.WATT);
        Unit _GW = SI.GIGA(SI.WATT);
        Unit _Wh = SI.WATT.times(NonSI.HOUR);
        Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
        Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);

        Unit _one = Unit.ONE;

        final JEVisUnit kg = new JEVisUnitImp(_kg);
        final JEVisUnit t = new JEVisUnitImp(_t);

        final JEVisUnit l = new JEVisUnitImp(_l);
        final JEVisUnit l2 = new JEVisUnitImp(_l2);
        final JEVisUnit m3 = new JEVisUnitImp(_m3);
        final JEVisUnit Nm3 = new JEVisUnitImp(_Nm3);

        final JEVisUnit literPerSecond = new JEVisUnitImp(_literPerSecond);
        final JEVisUnit literPerMinute = new JEVisUnitImp(_literPerMinute);
        final JEVisUnit literPerHour = new JEVisUnitImp(_literPerHour);

        final JEVisUnit cubicMeterPerSecond = new JEVisUnitImp(_cubicMeterPerSecond);
        final JEVisUnit cubicMeterPerMinute = new JEVisUnitImp(_cubicMeterPerMinute);
        final JEVisUnit cubicMeterPerHour = new JEVisUnitImp(_cubicMeterPerHour);

        final JEVisUnit bar = new JEVisUnitImp(_bar);
        final JEVisUnit atm = new JEVisUnitImp(_atm);

        final JEVisUnit W = new JEVisUnitImp(_W);
        final JEVisUnit kW = new JEVisUnitImp(_kW);
        final JEVisUnit MW = new JEVisUnitImp(_MW);
        final JEVisUnit GW = new JEVisUnitImp(_GW);
        final JEVisUnit Wh = new JEVisUnitImp(_Wh);
        final JEVisUnit kWh = new JEVisUnitImp(_kWh);
        final JEVisUnit MWh = new JEVisUnitImp(_MWh);
        final JEVisUnit GWh = new JEVisUnitImp(_GWh);

        final JEVisUnit va = new JEVisUnitImp(_one);
        va.setLabel("va");
        final JEVisUnit kva = new JEVisUnitImp(_one);
        kva.setLabel("kva");

        final JEVisUnit var = new JEVisUnitImp(_one);
        var.setLabel("var");
        final JEVisUnit kvar = new JEVisUnitImp(_one);
        kvar.setLabel("kvar");

        final JEVisUnit vah = new JEVisUnitImp(_one);
        vah.setLabel("vah");
        final JEVisUnit kvah = new JEVisUnitImp(_one);
        kvah.setLabel("kvah");

        final JEVisUnit varh = new JEVisUnitImp(_one);
        varh.setLabel("varh");
        final JEVisUnit kvarh = new JEVisUnitImp(_one);
        kvarh.setLabel("kvarh");

        final JEVisUnit cal = new JEVisUnitImp(_one);
        cal.setLabel("cal");
        final JEVisUnit kcal = new JEVisUnitImp(_one);
        kcal.setLabel("kcal");

        switch (unit) {
            case "kg":
                result = kg;
                break;
            case "t":
                result = t;
                break;
            case "W":
                result = W;
                break;
            case "kW":
                result = kW;
                break;
            case "MW":
                result = MW;
                break;
            case "GW":
                result = GW;
                break;
            case "Wh":
                result = Wh;
                break;
            case "kWh":
                result = kWh;
                break;
            case "MWh":
                result = MWh;
                break;
            case "GWh":
                result = GWh;
                break;
            case "m³":
                result = m3;
                break;
            case "Nm³":
                result = Nm3;
                break;
            case "L":
                result = l;
                break;
            case "L/s":
                result = literPerSecond;
                break;
            case "L/min":
                result = literPerMinute;
                break;
            case "L/h":
                result = literPerHour;
                break;
            case "m³/s":
                result = cubicMeterPerSecond;
                break;
            case "m³/min":
                result = cubicMeterPerMinute;
                break;
            case "m³/h":
                result = cubicMeterPerHour;
                break;
            case "bar":
                result = bar;
                break;
            case "va":
                result = va;
                break;
            case "kva":
                result = kva;
                break;
            case "var":
                result = var;
                break;
            case "kvar":
                result = kvar;
                break;
            case "vah":
                result = vah;
                break;
            case "kvah":
                result = kvah;
                break;
            case "varh":
                result = varh;
                break;
            case "kvarh":
                result = kvarh;
                break;
            case "cal":
                result = cal;
                break;
            case "kcal":
                result = kcal;
                break;
            default:
                break;
        }
        return result;
    }

    public Double timeFactor() {
        Double factor = 1.0;


        return factor;
    }

    public Double scaleValue(String inputUnit, String outputUnit) {
        Double factor = 1.0;
        switch (outputUnit) {
            case "W":
                switch (inputUnit) {
                    case "kW":
                        factor = 1000d / 1d;
                        break;
                    case "MW":
                        factor = 1000000d / 1d;
                        break;
                    case "GW":
                        factor = 1000000000d / 1d;
                        break;
                    case "Wh":
                        factor = 4d / 1d;
                        break;
                    case "kWh":
                        factor = 4d / 1000d;
                        break;
                    case "MWh":
                        factor = 4d / 1000000d;
                        break;
                    case "GWh":
                        factor = 4d / 1000000000d;
                        break;
                }
                break;
            case "kW":
                switch (inputUnit) {
                    case "W":
                        factor = 1d / 1000;
                        break;
                    case "MW":
                        factor = 1d / 1000d;
                        break;
                    case "GW":
                        factor = 1d / 1000000d;
                        break;
                    case "Wh":
                        factor = 4d / 1000d;
                        break;
                    case "kWh":
                        factor = 4d / 1d;
                        break;
                    case "MWh":
                        factor = 4000d / 1d;
                        break;
                    case "GWh":
                        factor = 4000000d / 1d;
                        break;
                }
                break;
            case "MW":
                switch (inputUnit) {
                    case "W":
                        factor = 1d / 1000000d;
                        break;
                    case "kW":
                        factor = 1d / 1000d;
                        break;
                    case "GW":
                        factor = 1000d;
                        break;
                    case "Wh":
                        factor = 4d / 1000000d;
                        break;
                    case "kWh":
                        factor = 4d / 1000d;
                        break;
                    case "MWh":
                        factor = 4d / 1d;
                        break;
                    case "GWh":
                        factor = 4000d / 1d;
                        break;
                }
                break;
            case "GW":
                switch (inputUnit) {
                    case "W":
                        factor = 1d / 1000000000d;
                        break;
                    case "kW":
                        factor = 1d / 1000000d;
                        break;
                    case "MW":
                        factor = 1d / 1000d;
                        break;
                    case "Wh":
                        factor = 4d / 1000000000d;
                        break;
                    case "kWh":
                        factor = 4d / 1000000d;
                        break;
                    case "MWh":
                        factor = 4d / 1000d;
                        break;
                    case "GWh":
                        factor = 4d / 1d;
                        break;
                }
                break;
            case "Wh":
                switch (inputUnit) {
                    case "kWh":
                        factor = 1000d;
                        break;
                    case "MWh":
                        factor = 1000000d / 1d;
                        break;
                    case "GWh":
                        factor = 1000000000d / 1d;
                        break;
                    case "W":
                        factor = 1 / 4d;
                        break;
                    case "kW":
                        factor = 1000d / 4d;
                        break;
                    case "MW":
                        factor = 1000000d / 4d;
                        break;
                    case "GW":
                        factor = 1000000000d / 4d;
                        break;
                }
                break;
            case "kWh":
                switch (inputUnit) {
                    case "Wh":
                        factor = 1d / 1000d;
                        break;
                    case "MWh":
                        factor = 1000d;
                        break;
                    case "GWh":
                        factor = 1000000d;
                        break;
                    case "W":
                        factor = 1000d / 4d;
                        break;
                    case "kW":
                        factor = 1d / 4d;
                        break;
                    case "MW":
                        factor = 1d / 4000d;
                        break;
                    case "GW":
                        factor = 1d / 4000000d;
                        break;
                }
                break;
            case "MWh":
                switch (inputUnit) {
                    case "Wh":
                        factor = 1d / 1000000d;
                        break;
                    case "kWh":
                        factor = 1d / 1000d;
                        break;
                    case "GWh":
                        factor = 1000d;
                        break;
                    case "W":
                        factor = 1d / 4000000d;
                        break;
                    case "kW":
                        factor = 1d / 4000d;
                        break;
                    case "MW":
                        factor = 1d / 4d;
                        break;
                    case "GW":
                        factor = 1000d / 4d;
                        break;
                }
                break;
            case "GWh":
                switch (inputUnit) {
                    case "Wh":
                        factor = 1d / 1000000000d;
                        break;
                    case "kWh":
                        factor = 1d / 1000000d;
                        break;
                    case "MWh":
                        factor = 1d / 1000d;
                        break;
                    case "W":
                        factor = 1d / 4000000000d;
                        break;
                    case "kW":
                        factor = 1d / 4000000d;
                        break;
                    case "MW":
                        factor = 1d / 4000d;
                        break;
                    case "GW":
                        factor = 1d / 4d;
                        break;
                }
                break;
            case "L":
                switch (inputUnit) {
                    case "m³":
                        factor = 1000d;
                        break;
                    case "Nm³":
                        factor = 1000d;
                        break;
                }
                break;
            case "m³":
                switch (inputUnit) {
                    case "L":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "Nm³":
                switch (inputUnit) {
                    case "L":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "kg":
                switch (inputUnit) {
                    case "t":
                        factor = 1000d;
                        break;
                }
                break;
            case "t":
                switch (inputUnit) {
                    case "kg":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "bar":
                switch (inputUnit) {
                    case "atm":
                        factor = 1d / 1.01325;
                        break;
                }
                break;
            case "atm":
                switch (inputUnit) {
                    case "bar":
                        factor = 1.01325 / 1d;
                        break;
                }
                break;
            case "m³/s":
                switch (inputUnit) {
                    case "m³/min":
                        factor = 1d / 60D;
                        break;
                    case "m³/h":
                        factor = 1d / 3600d;
                        break;
                    case "l/s":
                        factor = 1d / 1000d;
                        break;
                    case "l/min":
                        factor = 1d / 60000d;
                        break;
                    case "l/h":
                        factor = 1d / 3600000;
                        break;
                }
                break;
            case "m³/min":
                switch (inputUnit) {
                    case "m³/s":
                        factor = 60d;
                        break;
                    case "m³/h":
                        factor = 1d / 60d;
                        break;
                    case "l/s":
                        factor = 1000d * 60d;
                        break;
                    case "l/min":
                        factor = 1000d;
                        break;
                    case "l/h":
                        factor = 1000d / 60d;
                        break;
                }
                break;
            case "m³/h":
                switch (inputUnit) {
                    case "m³/s":
                        factor = 60d * 60d;
                        break;
                    case "m³/min":
                        factor = 1d / 60d;
                        break;
                    case "l/s":
                        factor = 1000d / 3600d;
                        break;
                    case "l/min":
                        factor = 1000d / 60d;
                        break;
                    case "l/h":
                        factor = 1000d;
                        break;
                }
                break;
            case "l/s":
                switch (inputUnit) {
                    case "m³/s":
                        factor = 1d / 1000d;
                        break;
                    case "m³/min":
                        factor = 60d / 1000d;
                        break;
                    case "m³/h":
                        factor = 3600d / 10000;
                        break;
                    case "l/min":
                        factor = 60d;
                        break;
                    case "l/h":
                        factor = 3600d;
                        break;
                }
                break;
            case "l/min":
                switch (inputUnit) {
                    case "m³/s":
                        factor = 1d / 60000d;
                        break;
                    case "m³/min":
                        factor = 1d / 1000d;
                        break;
                    case "m³/h":
                        factor = 60d / 1000d;
                        break;
                    case "l/s":
                        factor = 1 / 60d;
                        break;
                    case "l/h":
                        factor = 60d;
                        break;
                }
                break;
            case "l/h":
                switch (inputUnit) {
                    case "m³/s":
                        factor = 1d / 3600000d;
                        break;
                    case "m³/min":
                        factor = 1d / 60000d;
                        break;
                    case "m³/h":
                        factor = 1d / 1000d;
                        break;
                    case "l/s":
                        factor = 3600d;
                        break;
                    case "l/min":
                        factor = 60d;
                        break;
                }
                break;
            case "va":
                switch (inputUnit) {
                    case "vah":
                        factor = 4d / 1d;
                        break;
                    case "kva":
                        factor = 1000d / 1d;
                        break;
                    case "kvah":
                        factor = 4d / 1000d;
                        break;
                }
                break;
            case "kva":
                switch (inputUnit) {
                    case "vah":
                        factor = 4000d / 1d;
                        break;
                    case "va":
                        factor = 1d / 1000d;
                        break;
                    case "kvah":
                        factor = 4d / 1d;
                        break;
                }
                break;
            case "vah":
                switch (inputUnit) {
                    case "va":
                        factor = 1 / 4d;
                        break;
                    case "kva":
                        factor = 1000d / 4d;
                        break;
                    case "kvah":
                        factor = 1000d;
                        break;
                }
                break;
            case "kvah":
                switch (inputUnit) {
                    case "va":
                        factor = 1000d / 4d;
                        break;
                    case "kva":
                        factor = 1d / 4d;
                        break;
                    case "vah":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "var":
                switch (inputUnit) {
                    case "varh":
                        factor = 4d / 1d;
                        break;
                    case "kvar":
                        factor = 1000d / 1d;
                        break;
                    case "kvarh":
                        factor = 4d / 1000d;
                        break;
                }
                break;
            case "kvar":
                switch (inputUnit) {
                    case "varh":
                        factor = 4000d / 1d;
                        break;
                    case "var":
                        factor = 1d / 1000d;
                        break;
                    case "kvarh":
                        factor = 4d / 1d;
                        break;
                }
                break;
            case "varh":
                switch (inputUnit) {
                    case "var":
                        factor = 1 / 4d;
                        break;
                    case "kvar":
                        factor = 1000d / 4d;
                        break;
                    case "kvarh":
                        factor = 1000d;
                        break;
                }
                break;
            case "kvarh":
                switch (inputUnit) {
                    case "var":
                        factor = 1000d / 4d;
                        break;
                    case "kvar":
                        factor = 1d / 4d;
                        break;
                    case "varh":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            default:
                break;
        }
        return factor;
    }
}
