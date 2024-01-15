package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.unit.JEVisUnitImp;
import org.joda.time.Period;
import org.jscience.economics.money.Currency;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class ChartUnits {

    public static JEVisUnit parseUnit(String unit) {

        JEVisUnit result = null;
        Unit _mg = SI.GRAM.divide(1000);
        Unit _g = SI.GRAM;
        Unit _kg = SI.KILOGRAM;
        Unit _kkg = SI.KILOGRAM.alternate("kkg").times(1000);
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

        Unit _kgPerSecond = SI.KILOGRAM.divide(SI.SECOND);
        Unit _kgPerMinute = SI.KILOGRAM.divide(NonSI.MINUTE);
        Unit _kgPerHour = SI.KILOGRAM.divide(NonSI.HOUR);

        Unit _tPerSecond = NonSI.METRIC_TON.divide(SI.SECOND);
        Unit _tPerMinute = NonSI.METRIC_TON.divide(NonSI.MINUTE);
        Unit _tPerHour = NonSI.METRIC_TON.divide(NonSI.HOUR);

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

        Unit _eur = Currency.EUR;
        Unit _usd = Currency.USD;
        Unit _gbp = Currency.GBP;
        Unit _jpy = Currency.JPY;
        Unit _aud = Currency.AUD;
        Unit _cad = Currency.CAD;
        Unit _cny = Currency.CNY;
        Unit _krw = Currency.KRW;
        Unit _twd = Currency.TWD;

        Unit _one = Unit.ONE;

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

        final Unit _va = SI.WATT.alternate("va");
        final JEVisUnit va = new JEVisUnitImp(_va, "va", "NONE");
        final Unit _kva = SI.KILO(_va);
        final JEVisUnit kva = new JEVisUnitImp(_kva, "kva", "KILO");

        final Unit _var = SI.WATT.alternate("var");
        final JEVisUnit var = new JEVisUnitImp(_var, "var", "NONE");
        final Unit _kvar = SI.KILO(_var);
        final JEVisUnit kvar = new JEVisUnitImp(_kvar, "kvar", "KILO");

        final Unit _vah = SI.WATT.alternate("va").times(NonSI.HOUR);
        final JEVisUnit vah = new JEVisUnitImp(_vah, "vah", "NONE");
        final Unit _kvah = SI.KILO(_vah);
        final JEVisUnit kvah = new JEVisUnitImp(_kvah, "kvah", "KILO");

        final Unit _varh = SI.WATT.alternate("var").times(NonSI.HOUR);
        final JEVisUnit varh = new JEVisUnitImp(_varh, "varh", "NONE");
        final Unit _kvarh = SI.KILO(_varh);
        final JEVisUnit kvarh = new JEVisUnitImp(_kvarh, "kvarh", "KILO");

        final JEVisUnit cal = new JEVisUnitImp(_one);
        cal.setLabel("cal");
        final JEVisUnit kcal = new JEVisUnitImp(_one);
        kcal.setLabel("kcal");

        final JEVisUnit one = new JEVisUnitImp(_one);

        final JEVisUnit eur = new JEVisUnitImp(_eur);
        final JEVisUnit usd = new JEVisUnitImp(_usd);
        final JEVisUnit gbp = new JEVisUnitImp(_gbp);
        final JEVisUnit jpy = new JEVisUnitImp(_jpy);
        final JEVisUnit aud = new JEVisUnitImp(_aud);
        final JEVisUnit cad = new JEVisUnitImp(_cad);
        final JEVisUnit cny = new JEVisUnitImp(_cny);
        final JEVisUnit krw = new JEVisUnitImp(_krw);
        final JEVisUnit twd = new JEVisUnitImp(_twd);

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
                case ("EUR"):
                    result = eur;
                    break;
                case ("USD"):
                    result = usd;
                    break;
                case ("GBP"):
                    result = gbp;
                    break;
                case ("JPY"):
                    result = jpy;
                    break;
                case ("AUD"):
                    result = aud;
                    break;
                case ("CAD"):
                    result = cad;
                    break;
                case ("CNY"):
                    result = cny;
                    break;
                case ("KRW"):
                    result = krw;
                    break;
                case ("TWD"):
                    result = twd;
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
