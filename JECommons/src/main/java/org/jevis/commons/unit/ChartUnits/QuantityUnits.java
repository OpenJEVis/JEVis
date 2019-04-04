package org.jevis.commons.unit.ChartUnits;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final Unit _W = SI.WATT;
    private final JEVisUnit W = new JEVisUnitImp(_W);
    private final Unit _kW = SI.KILO(SI.WATT);
    private final JEVisUnit kW = new JEVisUnitImp(_kW);
    private final Unit _MW = SI.MEGA(SI.WATT);
    private final JEVisUnit MW = new JEVisUnitImp(_MW);
    private final Unit _GW = SI.GIGA(SI.WATT);
    private final JEVisUnit GW = new JEVisUnitImp(_GW);

    private final Unit _vah = Unit.ONE.alternate("vah");
    private final JEVisUnit vah = new JEVisUnitImp(_vah);
    private final Unit _kvah = Unit.ONE.alternate("kvah");
    private final JEVisUnit kvah = new JEVisUnitImp(_kvah);

    private final Unit _varh = Unit.ONE.alternate("varh");
    private final JEVisUnit varh = new JEVisUnitImp(_varh);
    private final Unit _kvarh = Unit.ONE.alternate("kvarh");
    private final JEVisUnit kvarh = new JEVisUnitImp(_kvarh);

    private final Unit _va = Unit.ONE.alternate("va");
    private final JEVisUnit va = new JEVisUnitImp(_va);
    private final Unit _kva = Unit.ONE.alternate("kva");
    private final JEVisUnit kva = new JEVisUnitImp(_kva);

    private final Unit _var = Unit.ONE.alternate("var");
    private final JEVisUnit var = new JEVisUnitImp(_var);
    private final Unit _kvar = Unit.ONE.alternate("kvar");
    private final JEVisUnit kvar = new JEVisUnitImp(_kvar);

    private final ArrayList<JEVisUnit> jeVisUnitArrayList;

    private final ArrayList<Unit> unitArrayList;

    private final ArrayList<String> stringArrayList;
    private final List<JEVisUnit> energyUnits;

    public boolean isQuantityUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : jeVisUnitArrayList) {
            try {
                if (jeVisUnit.getLabel().equals(unit.getLabel())) return true;
            } catch (Exception e) {
            }
            try {
                if (jeVisUnit.getFormula().equals(unit.getFormula())) return true;
            } catch (Exception e) {
            }
            try {
                if (UnitManager.getInstance().format(jeVisUnit).equals(UnitManager.getInstance().format(unit)))
                    return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean isQuantityUnit(Unit unit) {
        return unitArrayList.contains(unit);
    }

    public boolean isQuantityUnit(String unit) {
        return stringArrayList.contains(unit);
    }

    public QuantityUnits() {
        energyUnits = new ArrayList<>(Arrays.asList(W, kW, MW, GW));

        stringArrayList = new ArrayList<>(Arrays.asList(
                kg.getLabel(), t.getLabel(),
                l.getLabel(), m3.getLabel(),
                Wh.getLabel(), kWh.getLabel(), MWh.getLabel(), GWh.getLabel(),
                vah.getLabel(), varh.getLabel(), kvah.getLabel(), kvarh.getLabel()
        ));

        unitArrayList = new ArrayList<>(Arrays.asList(
                _kg, _t,
                _l, _m3,
                _Wh, _kWh, _MWh, _GWh,
                _vah, _varh, _kvah, _kvarh
        ));

        jeVisUnitArrayList = new ArrayList<>(Arrays.asList(
                kg, t,
                l, m3,
                Wh, kWh, MWh, GWh,
                vah, varh, kvah, kvarh
        ));
    }

    public boolean isSumCalculable(JEVisUnit unit) {
        return energyUnits.contains(unit);
    }

    public JEVisUnit getSumUnit(JEVisUnit unit) {
        if (unit.equals(W)) {
            return Wh;
        } else if (unit.equals(kW)) {
            return kWh;
        } else if (unit.equals(MW)) {
            return MWh;
        } else if (unit.equals(GW)) {
            return GWh;
        } else if (unit.equals(va)) {
            return vah;
        } else if (unit.equals(var)) {
            return varh;
        } else if (unit.equals(kva)) {
            return kvah;
        } else if (unit.equals(kvar)) {
            return kvarh;
        } else if (unit.equals(Wh)) {
            return Wh;
        } else if (unit.equals(kWh)) {
            return kWh;
        } else if (unit.equals(MWh)) {
            return MWh;
        } else if (unit.equals(GWh)) {
            return GWh;
        } else if (unit.equals(vah)) {
            return vah;
        } else if (unit.equals(varh)) {
            return varh;
        } else if (unit.equals(kvah)) {
            return kvah;
        } else if (unit.equals(kvarh)) {
            return kvarh;
        } else return null;
    }
}
