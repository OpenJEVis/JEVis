package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Arrays;

public class QuantityUnits {

    private final Unit _kg = SI.KILOGRAM;
    private final JEVisUnit kg = new JEVisUnitImp(_kg);
    private final Unit _t = NonSI.METRIC_TON;
    private final JEVisUnit t = new JEVisUnitImp(_t);
    private final Unit _l = NonSI.LITER;
    private final JEVisUnit l = new JEVisUnitImp(_l);
    private final Unit _m3 = SI.CUBIC_METRE;
    private final JEVisUnit m3 = new JEVisUnitImp(_m3);

    private final Unit _Wh = SI.WATT.times(NonSI.HOUR);
    private final JEVisUnit Wh = new JEVisUnitImp(_Wh);
    private final Unit _kWh = SI.KILO(SI.WATT).times(NonSI.HOUR);
    private final JEVisUnit kWh = new JEVisUnitImp(_kWh);
    private final Unit _MWh = SI.MEGA(SI.WATT).times(NonSI.HOUR);
    private final JEVisUnit MWh = new JEVisUnitImp(_MWh);
    private final Unit _GWh = SI.GIGA(SI.WATT).times(NonSI.HOUR);
    private final JEVisUnit GWh = new JEVisUnitImp(_GWh);

    private final Unit _vah = Unit.ONE.alternate("vah");
    private final JEVisUnit vah = new JEVisUnitImp(_vah);
    private final Unit _kvah = Unit.ONE.alternate("kvah");
    private final JEVisUnit kvah = new JEVisUnitImp(_kvah);

    private final Unit _varh = Unit.ONE.alternate("varh");
    private final JEVisUnit varh = new JEVisUnitImp(_varh);
    private final Unit _kvarh = Unit.ONE.alternate("kvarh");
    private final JEVisUnit kvarh = new JEVisUnitImp(_kvarh);

    private final ArrayList<JEVisUnit> jeVisUnitArrayList = new ArrayList<>(Arrays.asList(kg, t, l, m3, Wh, kWh, MWh,
            GWh, vah, varh, kvah, kvarh));

    private final ArrayList<Unit> unitArrayList = new ArrayList<>(Arrays.asList(_kg, _t, _l, _m3, _Wh, _kWh, _MWh,
            _GWh, _vah, _varh, _kvah, _kvarh));

    private final ArrayList<String> stringArrayList = new ArrayList<>(Arrays.asList(kg.getLabel(), t.getLabel(),
            l.getLabel(), m3.getLabel(), Wh.getLabel(), kWh.getLabel(), MWh.getLabel(), GWh.getLabel(), vah.getLabel(),
            varh.getLabel(), kvah.getLabel(), kvarh.getLabel()));

    public boolean isQuantityUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : jeVisUnitArrayList) {
            if (jeVisUnit.getLabel().equals(unit.getLabel())) return true;
            if (jeVisUnit.getFormula().equals(unit.getFormula())) return true;
            if (UnitManager.getInstance().format(jeVisUnit).equals(UnitManager.getInstance().format(unit))) return true;
        }
        return false;
    }

    public boolean isQuantityUnit(Unit unit) {
        return unitArrayList.contains(unit);
    }

    public boolean isQuantityUnit(String unit) {
        return stringArrayList.contains(unit);
    }
}
