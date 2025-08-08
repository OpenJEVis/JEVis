package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.unit.JEVisUnitImp;
import org.joda.time.Period;
import systems.uom.common.Imperial;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.function.MultiplyConverter;
import tech.units.indriya.unit.TransformedUnit;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import java.util.Currency;

import static tech.units.indriya.unit.Units.KILOGRAM;

public class ChartUnits {

    public static JEVisUnit parseUnit(String unit) {

        JEVisUnit result = null;
        Unit _mg = MetricPrefix.MILLI(Units.GRAM);
        Unit _g = Units.GRAM;
        Unit _kg = Units.KILOGRAM;
        Unit _kkg = new TransformedUnit<>("kkg", "Kiloton", KILOGRAM, MultiplyConverter.ofRational(1000, 1));
        Unit _t = Imperial.METRIC_TON;

        Unit _l2 = Units.LITRE;
        Unit _l = Units.LITRE.alternate("L");

        Unit _m3 = Units.CUBIC_METRE;
        Unit _Nm3 = Units.CUBIC_METRE.alternate("Nm³");

        Unit _literPerSecond = _l2.divide(Units.SECOND);
        Unit _literPerMinute = _l2.divide(Units.MINUTE);
        Unit _literPerHour = _l2.divide(Units.HOUR);

        Unit _cubicMeterPerSecond = Units.CUBIC_METRE.divide(Units.SECOND);
        Unit _cubicMeterPerMinute = Units.CUBIC_METRE.divide(Units.MINUTE);
        Unit _cubicMeterPerHour = Units.CUBIC_METRE.divide(Units.HOUR);

        Unit _kgPerSecond = Units.KILOGRAM.divide(Units.SECOND);
        Unit _kgPerMinute = Units.KILOGRAM.divide(Units.MINUTE);
        Unit _kgPerHour = Units.KILOGRAM.divide(Units.HOUR);

        Unit _tPerSecond = _t.divide(Units.SECOND);
        Unit _tPerMinute = _t.divide(Units.MINUTE);
        Unit _tPerHour = _t.divide(Units.HOUR);

        Unit _bar = new TransformedUnit<>("bar", "Bar", Units.PASCAL, MultiplyConverter.ofRational(100000, 1));
        Unit _atm = new TransformedUnit<>("atm", "Atmosphere", Units.PASCAL, MultiplyConverter.ofRational(101300, 1));

        Unit _W = Units.WATT;
        Unit _kW = MetricPrefix.KILO(Units.WATT);
        Unit _MW = MetricPrefix.MEGA(Units.WATT);
        Unit _GW = MetricPrefix.GIGA(Units.WATT);
        Unit _Wh = Units.WATT.multiply(Units.HOUR);
        Unit _kWh = MetricPrefix.KILO(_Wh);
        Unit _MWh = MetricPrefix.MEGA(_Wh);
        Unit _GWh = MetricPrefix.GIGA(_Wh);

        for (Currency currency : Currency.getAvailableCurrencies()) {
            Unit<Dimensionless> cu = AbstractUnit.ONE.alternate(currency.getSymbol());

            JEVisUnit jeVisUnit = new JEVisUnitImp(cu);

            if (currency.getSymbol().equals(unit)) {
                return jeVisUnit;
            }
        }

        Unit _one = AbstractUnit.ONE;

        final JEVisUnit mg = new JEVisUnitImp(_mg);
        final JEVisUnit g = new JEVisUnitImp(_g);
        final JEVisUnit kkg = new JEVisUnitImp(_kkg);
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

        final JEVisUnit kgPerSecond = new JEVisUnitImp(_kgPerSecond);
        final JEVisUnit kgPerMinute = new JEVisUnitImp(_kgPerMinute);
        final JEVisUnit kgPerHour = new JEVisUnitImp(_kgPerHour);

        final JEVisUnit tPerSecond = new JEVisUnitImp(_tPerSecond);
        final JEVisUnit tPerMinute = new JEVisUnitImp(_tPerMinute);
        final JEVisUnit tPerHour = new JEVisUnitImp(_tPerHour);

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

        final Unit _va = Units.WATT.alternate("va");
        final JEVisUnit va = new JEVisUnitImp(_va, "va", "NONE");
        final Unit _kva = MetricPrefix.KILO(_va);
        final JEVisUnit kva = new JEVisUnitImp(_kva, "kva", "KILO");

        final Unit _var = Units.WATT.alternate("var");
        final JEVisUnit var = new JEVisUnitImp(_var, "var", "NONE");
        final Unit _kvar = MetricPrefix.KILO(_var);
        final JEVisUnit kvar = new JEVisUnitImp(_kvar, "kvar", "KILO");

        final Unit _vah = _va.multiply(Units.HOUR);
        final JEVisUnit vah = new JEVisUnitImp(_vah, "vah", "NONE");
        final Unit _kvah = MetricPrefix.KILO(_vah);
        final JEVisUnit kvah = new JEVisUnitImp(_kvah, "kvah", "KILO");

        final Unit _varh = _var.multiply(Units.HOUR);
        final JEVisUnit varh = new JEVisUnitImp(_varh, "varh", "NONE");
        final Unit _kvarh = MetricPrefix.KILO(_varh);
        final JEVisUnit kvarh = new JEVisUnitImp(_kvarh, "kvarh", "KILO");

        final JEVisUnit cal = new JEVisUnitImp(_one);
        cal.setLabel("cal");
        final JEVisUnit kcal = new JEVisUnitImp(_one);
        kcal.setLabel("kcal");

        final JEVisUnit one = new JEVisUnitImp(_one);


        if (unit != null) {
            switch (unit) {
                case "mg":
                    result = mg;
                    break;
                case "g":
                    result = g;
                    break;
                case "kg":
                    result = kg;
                    break;
                case "kkg":
                    result = kkg;
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
                case "kg/s":
                    result = kgPerSecond;
                    break;
                case "kg/min":
                    result = kgPerMinute;
                    break;
                case "kg/h":
                    result = kgPerHour;
                    break;
                case "t/s":
                    result = tPerSecond;
                    break;
                case "t/min":
                    result = tPerMinute;
                    break;
                case "t/h":
                    result = tPerHour;
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
        } else return one;
    }

    public Double scaleValue(Period inputPeriod, String inputUnit, Period outputPeriod, String outputUnit) {
        Double factor = 1.0;
        switch (outputUnit) {
            case "W":
                switch (inputUnit) {
                    case "kW":
                    case "kWh":
                        factor = 1000d;
                        break;
                    case "MW":
                    case "MWh":
                        factor = 1000000d;
                        break;
                    case "GW":
                    case "GWh":
                        factor = 1000000000d;
                        break;
                    case "Wh":
                        factor = 1d;
                        break;
                }
                break;
            case "kW":
                switch (inputUnit) {
                    case "W":
                        factor = 1d / 1000;
                        break;
                    case "MW":
                    case "MWh":
                        factor = 1000d;
                        break;
                    case "GW":
                    case "GWh":
                        factor = 1000000d;
                        break;
                    case "Wh":
                        factor = 1d / 1000d;
                        break;
                    case "kWh":
                        factor = 1d;
                        break;
                }
                break;
            case "MW":
                switch (inputUnit) {
                    case "W":
                    case "Wh":
                        factor = 1d / 1000000d;
                        break;
                    case "kW":
                    case "kWh":
                        factor = 1d / 1000d;
                        break;
                    case "GW":
                    case "GWh":
                        factor = 1000d;
                        break;
                    case "MWh":
                        factor = 1d;
                        break;
                }
                break;
            case "GW":
                switch (inputUnit) {
                    case "W":
                    case "Wh":
                        factor = 1d / 1000000000d;
                        break;
                    case "kW":
                    case "kWh":
                        factor = 1d / 1000000d;
                        break;
                    case "MW":
                    case "MWh":
                        factor = 1d / 1000d;
                        break;
                    case "GWh":
                        factor = 1d;
                        break;
                }
                break;
            case "Wh":
                switch (inputUnit) {
                    case "kWh":
                    case "kW":
                        factor = 1000d;
                        break;
                    case "MWh":
                    case "MW":
                        factor = 1000000d;
                        break;
                    case "GWh":
                    case "GW":
                        factor = 1000000000d;
                        break;
                    case "W":
                        factor = 1d;
                        break;
                }
                break;
            case "kWh":
                switch (inputUnit) {
                    case "Wh":
                    case "W":
                        factor = 1d / 1000d;
                        break;
                    case "MWh":
                    case "MW":
                        factor = 1000d;
                        break;
                    case "GWh":
                    case "GW":
                        factor = 1000000d;
                        break;
                    case "kW":
                        factor = 1d;
                        break;
                }
                break;
            case "MWh":
                switch (inputUnit) {
                    case "Wh":
                    case "W":
                        factor = 1d / 1000000d;
                        break;
                    case "kWh":
                    case "kW":
                        factor = 1d / 1000d;
                        break;
                    case "GWh":
                    case "GW":
                        factor = 1000d;
                        break;
                    case "MW":
                        factor = 1d;
                        break;
                }
                break;
            case "GWh":
                switch (inputUnit) {
                    case "Wh":
                    case "W":
                        factor = 1d / 1000000000d;
                        break;
                    case "kWh":
                    case "kW":
                        factor = 1d / 1000000d;
                        break;
                    case "MWh":
                    case "MW":
                        factor = 1d / 1000d;
                        break;
                    case "GW":
                        factor = 1d;
                        break;
                }
                break;
            case "L":
                switch (inputUnit) {
                    case "m³":
                    case "Nm³":
                        factor = 1000d;
                        break;
                }
                break;
            case "m³":
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
                    case "kkg":
                        factor = 1000d;
                        break;
                    case "mg":
                        factor = 1 / 1000000d;
                        break;
                    case "g":
                        factor = 1 / 1000d;
                        break;
                }
                break;
            case "t":
                switch (inputUnit) {
                    case "kg":
                        factor = 1d / 1000d;
                        break;
                    case "mg":
                        factor = 1 / 1000000000d;
                        break;
                    case "g":
                        factor = 1 / 1000000d;
                        break;
                    case "kkg":
                        factor = 1d;
                        break;
                }
                break;
            case "kkg":
                switch (inputUnit) {
                    case "kg":
                        factor = 1d / 1000d;
                        break;
                    case "mg":
                        factor = 1 / 1000000000d;
                        break;
                    case "g":
                        factor = 1 / 1000000d;
                        break;
                    case "t":
                        factor = 1d;
                        break;
                }
                break;
            case "g":
                switch (inputUnit) {
                    case "kg":
                        factor = 1000d;
                        break;
                    case "mg":
                        factor = 1 / 1000d;
                        break;
                    case "kkg":
                    case "t":
                        factor = 1000000d;
                        break;
                }
                break;
            case "mg":
                switch (inputUnit) {
                    case "kg":
                        factor = 1000000d;
                        break;
                    case "g":
                        factor = 1000d;
                        break;
                    case "kkg":
                    case "t":
                        factor = 1000000000d;
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
                        factor = 1.01325;
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
            case "t/s":
                switch (inputUnit) {
                    case "t/min":
                        factor = 1d / 60D;
                        break;
                    case "t/h":
                        factor = 1d / 3600d;
                        break;
                    case "kg/s":
                        factor = 1d / 1000d;
                        break;
                    case "kg/min":
                        factor = 1d / 60000d;
                        break;
                    case "kg/h":
                        factor = 1d / 3600000;
                        break;
                }
                break;
            case "t/min":
                switch (inputUnit) {
                    case "t/s":
                        factor = 60d;
                        break;
                    case "t/h":
                        factor = 1d / 60d;
                        break;
                    case "kg/s":
                        factor = 1000d * 60d;
                        break;
                    case "kg/min":
                        factor = 1000d;
                        break;
                    case "kg/h":
                        factor = 1000d / 60d;
                        break;
                }
                break;
            case "t/h":
                switch (inputUnit) {
                    case "t/s":
                        factor = 60d * 60d;
                        break;
                    case "t/min":
                        factor = 1d / 60d;
                        break;
                    case "kg/s":
                        factor = 1000d / 3600d;
                        break;
                    case "kg/min":
                        factor = 1000d / 60d;
                        break;
                    case "kg/h":
                        factor = 1000d;
                        break;
                }
                break;
            case "kg/s":
                switch (inputUnit) {
                    case "t/s":
                        factor = 1d / 1000d;
                        break;
                    case "t/min":
                        factor = 60d / 1000d;
                        break;
                    case "t/h":
                        factor = 3600d / 10000;
                        break;
                    case "kg/min":
                        factor = 60d;
                        break;
                    case "kg/h":
                        factor = 3600d;
                        break;
                }
                break;
            case "kg/min":
                switch (inputUnit) {
                    case "t/s":
                        factor = 1d / 60000d;
                        break;
                    case "t/min":
                        factor = 1d / 1000d;
                        break;
                    case "t/h":
                        factor = 60d / 1000d;
                        break;
                    case "kg/s":
                        factor = 1 / 60d;
                        break;
                    case "kg/h":
                        factor = 60d;
                        break;
                }
                break;
            case "kg/h":
                switch (inputUnit) {
                    case "t/s":
                        factor = 1d / 3600000d;
                        break;
                    case "t/min":
                        factor = 1d / 60000d;
                        break;
                    case "t/h":
                        factor = 1d / 1000d;
                        break;
                    case "kg/s":
                        factor = 3600d;
                        break;
                    case "kg/min":
                        factor = 60d;
                        break;
                }
                break;
            case "va":
                switch (inputUnit) {
                    case "vah":
                        factor = 1d;
                        break;
                    case "kva":
                        factor = 1000d;
                        break;
                    case "kvah":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "kva":
                switch (inputUnit) {
                    case "vah":
                        factor = 1000d;
                        break;
                    case "va":
                        factor = 1d / 1000d;
                        break;
                    case "kvah":
                        factor = 1d;
                        break;
                }
                break;
            case "vah":
                switch (inputUnit) {
                    case "va":
                        factor = 1d;
                        break;
                    case "kva":
                    case "kvah":
                        factor = 1000d;
                        break;
                }
                break;
            case "kvah":
                switch (inputUnit) {
                    case "va":
                        factor = 1000d;
                        break;
                    case "kva":
                        factor = 1d;
                        break;
                    case "vah":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "var":
                switch (inputUnit) {
                    case "varh":
                        factor = 1d;
                        break;
                    case "kvar":
                        factor = 1000d;
                        break;
                    case "kvarh":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            case "kvar":
                switch (inputUnit) {
                    case "varh":
                        factor = 1000d;
                        break;
                    case "var":
                        factor = 1d / 1000d;
                        break;
                    case "kvarh":
                        factor = 1d;
                        break;
                }
                break;
            case "varh":
                switch (inputUnit) {
                    case "var":
                        factor = 1d;
                        break;
                    case "kvar":
                    case "kvarh":
                        factor = 1000d;
                        break;
                }
                break;
            case "kvarh":
                switch (inputUnit) {
                    case "var":
                        factor = 1000d;
                        break;
                    case "kvar":
                        factor = 1d;
                        break;
                    case "varh":
                        factor = 1d / 1000d;
                        break;
                }
                break;
            default:
                break;
        }

        PeriodComparator comparator = new PeriodComparator();
        int compare = comparator.compare(inputPeriod, outputPeriod);

        if (compare <= 0 && inputPeriod.equals(Period.minutes(15))) {
            switch (inputUnit) {
                case "kWh":
                case "Wh":
                case "MWh":
                case "GWh":
                case "kvah":
                case "vah":
                case "varh":
                case "kvarh":
                    switch (outputUnit) {
                        case "W":
                        case "kW":
                        case "MW":
                        case "GW":
                        case "va":
                        case "kva":
                        case "var":
                        case "kvar":
                            factor *= 4d;
                            break;
                    }
                    break;
                case "W":
                case "kW":
                case "MW":
                case "GW":
                case "va":
                case "kva":
                case "var":
                case "kvar":
                    switch (outputUnit) {
                        case "kWh":
                        case "Wh":
                        case "MWh":
                        case "GWh":
                        case "kvah":
                        case "vah":
                        case "varh":
                        case "kvarh":
                            factor *= 1 / 4d;
                            break;
                    }
                    break;
            }
        }

        return factor;
    }

    public boolean areComplementary(String inputUnit, String outputUnit) {

        switch (inputUnit) {
            case "Wh":
            case "W":
                return (inputUnit.equals("Wh") && outputUnit.equals("W"))
                        || (inputUnit.equals("W") && outputUnit.equals("Wh"));
            case "kWh":
            case "kW":
                return (inputUnit.equals("kWh") && outputUnit.equals("kW"))
                        || (inputUnit.equals("kW") && outputUnit.equals("kWh"));
            case "MWh":
            case "MW":
                return (inputUnit.equals("MWh") && outputUnit.equals("MW"))
                        || (inputUnit.equals("MW") && outputUnit.equals("MWh"));
            case "GWh":
            case "GW":
                return (inputUnit.equals("GWh") && outputUnit.equals("GW"))
                        || (inputUnit.equals("GW") && outputUnit.equals("GWh"));
            case "vah":
            case "va":
                return (inputUnit.equals("vah") && outputUnit.equals("va"))
                        || (inputUnit.equals("va") && outputUnit.equals("vah"));
            case "kvah":
            case "kva":
                return (inputUnit.equals("kvah") && outputUnit.equals("kva"))
                        || (inputUnit.equals("kva") && outputUnit.equals("kvah"));
            case "varh":
            case "var":
                return (inputUnit.equals("varh") && outputUnit.equals("var"))
                        || (inputUnit.equals("var") && outputUnit.equals("varh"));
            case "kvarh":
            case "kvar":
                return (inputUnit.equals("kvarh") && outputUnit.equals("kvar"))
                        || (inputUnit.equals("kvar") && outputUnit.equals("kvarh"));
        }


        return false;
    }
}
