package org.jevis.commons.unit.ChartUnits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jscience.economics.money.Currency;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuantityUnits {
    private static final Logger logger = LogManager.getLogger(QuantityUnits.class);

    private final Unit _mg = SI.GRAM.divide(1000);
    private final JEVisUnit mg = new JEVisUnitImp(_mg);
    private final Unit _g = SI.GRAM;
    private final JEVisUnit g = new JEVisUnitImp(_g);
    private final Unit _kg = SI.KILOGRAM;
    private final JEVisUnit kg = new JEVisUnitImp(_kg);
    private final Unit _kkg = SI.KILOGRAM.times(1000);
    private final JEVisUnit kkg = new JEVisUnitImp(_kkg);
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

    private final Unit _eur = Currency.EUR;
    private final Unit _usd = Currency.USD;
    private final Unit _gbp = Currency.GBP;
    private final Unit _jpy = Currency.JPY;
    private final Unit _aud = Currency.AUD;
    private final Unit _cad = Currency.CAD;
    private final Unit _cny = Currency.CNY;
    private final Unit _krw = Currency.KRW;
    private final Unit _twd = Currency.TWD;
    private final JEVisUnit eur = new JEVisUnitImp(_eur);
    private final JEVisUnit usd = new JEVisUnitImp(_usd);
    private final JEVisUnit gbp = new JEVisUnitImp(_gbp);
    private final JEVisUnit jpy = new JEVisUnitImp(_jpy);
    private final JEVisUnit aud = new JEVisUnitImp(_aud);
    private final JEVisUnit cad = new JEVisUnitImp(_cad);
    private final JEVisUnit cny = new JEVisUnitImp(_cny);
    private final JEVisUnit krw = new JEVisUnitImp(_krw);
    private final JEVisUnit twd = new JEVisUnitImp(_twd);

    private final ArrayList<JEVisUnit> jeVisUnitArrayList;

    private final ArrayList<Unit> unitArrayList;

    private final ArrayList<String> stringArrayList;
    private final List<JEVisUnit> energyUnits;

    public boolean isQuantityUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : jeVisUnitArrayList) {
            try {
                if (jeVisUnit.equals(unit)) return true;
            } catch (Exception e) {
            }
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
                mg.getLabel(), g.getLabel(),
                kg.getLabel(), kkg.getLabel(), t.getLabel(),
                l.getLabel(), m3.getLabel(),
                Wh.getLabel(), kWh.getLabel(), MWh.getLabel(), GWh.getLabel(),
                vah.getLabel(), varh.getLabel(), kvah.getLabel(), kvarh.getLabel(),
                eur.getLabel(), usd.getLabel(), gbp.getLabel(), jpy.getLabel(), aud.getLabel(), cad.getLabel(), cny.getLabel(), krw.getLabel(), twd.getLabel()
        ));

        unitArrayList = new ArrayList<>(Arrays.asList(
                _mg, _g,
                _kg, _kkg, _t,
                _l, _m3,
                _Wh, _kWh, _MWh, _GWh,
                _vah, _varh, _kvah, _kvarh,
                _eur, _usd, _gbp, _jpy, _aud, _cad, _cny, _krw, _twd
        ));

        jeVisUnitArrayList = new ArrayList<>(Arrays.asList(
                mg, g,
                kg, kkg, t,
                l, m3,
                Wh, kWh, MWh, GWh,
                vah, varh, kvah, kvarh,
                eur, usd, gbp, jpy, aud, cad, cny, krw, twd
        ));
    }

    public boolean isSumCalculable(JEVisUnit unit) {
        return energyUnits.contains(unit) || jeVisUnitArrayList.contains(unit);
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
        } else if (unit.equals(mg)) {
            return mg;
        } else if (unit.equals(g)) {
            return g;
        } else if (unit.equals(kg)) {
            return kg;
        } else if (unit.equals(kkg)) {
            return kkg;
        } else if (unit.equals(t)) {
            return t;
        } else return null;
    }

    public boolean isQuantityIfCleanData(JEVisAttribute attribute, boolean isQuantity) {
        if (attribute != null) {
            JEVisObject object = attribute.getObject();
            if (object != null) {
                try {
                    if (object.getJEVisClassName().equals(CleanDataObject.CLASS_NAME)) {
                        JEVisAttribute quantityAttribute = object.getAttribute(CleanDataObject.AttributeName.VALUE_QUANTITY.getAttributeName());
                        if (quantityAttribute != null && quantityAttribute.hasSample()) {
                            JEVisSample latestSample = quantityAttribute.getLatestSample();
                            if (latestSample != null) {
                                isQuantity = latestSample.getValueAsBoolean();
                            }
                        }
                    }
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
        }
        return isQuantity;
    }

    public boolean isQuantityIfCleanData(SQLDataSource sqlDataSource, JsonAttribute attribute, boolean isQuantity) {
        if (attribute != null) {
            JsonObject object = null;
            try {
                object = sqlDataSource.getObject(attribute.getObjectID());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            if (object != null) {
                if (object.getJevisClass().equals(CleanDataObject.CLASS_NAME)) {
                    for (JsonAttribute jsonAttribute : object.getAttributes()) {
                        if (jsonAttribute.getType().equals(CleanDataObject.AttributeName.VALUE_QUANTITY.getAttributeName())) {
                            if (jsonAttribute.getLatestValue() != null) {
                                JsonSample latestSample = jsonAttribute.getLatestValue();
                                isQuantity = Boolean.parseBoolean(latestSample.getValue());
                            }
                        }
                    }
                }
            }
        }
        return isQuantity;
    }
}
