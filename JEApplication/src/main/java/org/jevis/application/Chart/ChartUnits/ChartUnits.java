package org.jevis.application.Chart.ChartUnits;

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

        Unit _W = SI.WATT;
        Unit _kW = SI.KILO(SI.WATT);
        Unit _MW = SI.MEGA(SI.WATT);
        Unit _GW = SI.GIGA(SI.WATT);
        Unit _Wh = SI.WATT.times(NonSI.HOUR);
        Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
        Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
        Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);

        final JEVisUnit kg = new JEVisUnitImp(_kg);
        final JEVisUnit t = new JEVisUnitImp(_t);

        final JEVisUnit l = new JEVisUnitImp(_l);
        final JEVisUnit l2 = new JEVisUnitImp(_l2);
        final JEVisUnit m3 = new JEVisUnitImp(_m3);

        final JEVisUnit W = new JEVisUnitImp(_W);
        final JEVisUnit kW = new JEVisUnitImp(_kW);
        final JEVisUnit MW = new JEVisUnitImp(_MW);
        final JEVisUnit GW = new JEVisUnitImp(_GW);
        final JEVisUnit Wh = new JEVisUnitImp(_Wh);
        final JEVisUnit kWh = new JEVisUnitImp(_kWh);
        final JEVisUnit MWh = new JEVisUnitImp(_MWh);
        final JEVisUnit GWh = new JEVisUnitImp(_GWh);

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
            case "L":
                result = l;
                break;
            default:
                break;
        }
        return result;
    }
}
