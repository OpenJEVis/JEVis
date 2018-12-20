package org.jevis.jeconfig.application.Chart;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuantityUnits {

    Unit _kg = SI.KILOGRAM;
    final JEVisUnit kg = new JEVisUnitImp(_kg);
    Unit _t = NonSI.METRIC_TON;
    final JEVisUnit t = new JEVisUnitImp(_t);
    Unit _l = NonSI.LITER;
    final JEVisUnit l = new JEVisUnitImp(_l);
    Unit _m3 = SI.CUBIC_METRE;
    final JEVisUnit m3 = new JEVisUnitImp(_m3);
    Unit _Wh = SI.WATT.times(NonSI.HOUR);
    final JEVisUnit Wh = new JEVisUnitImp(_Wh);
    Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
    final JEVisUnit kWh = new JEVisUnitImp(_kWh);
    Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
    final JEVisUnit MWh = new JEVisUnitImp(_MWh);
    Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);
    final JEVisUnit GWh = new JEVisUnitImp(_GWh);
    Unit _vah = Unit.ONE.alternate("vah");
    final JEVisUnit vah = new JEVisUnitImp(_vah);
    Unit _varh = Unit.ONE.alternate("varh");
    final JEVisUnit varh = new JEVisUnitImp(_varh);

    public List<JEVisUnit> get() {
        return new ArrayList<>(Arrays.asList(kg, t, l, m3, Wh, kWh, MWh, GWh, vah, varh));
    }

}
