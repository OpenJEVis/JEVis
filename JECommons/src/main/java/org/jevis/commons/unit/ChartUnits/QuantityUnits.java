package org.jevis.commons.unit.ChartUnits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ForecastDataObject;
import org.jevis.commons.dataprocessing.MathDataObject;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
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

    private final Unit _mg = SI.MILLI(SI.GRAM);
    private final JEVisUnit mg = new JEVisUnitImp(_mg);
    private final Unit _g = SI.GRAM;
    private final JEVisUnit g = new JEVisUnitImp(_g);
    private final Unit _kg = SI.KILOGRAM;
    private final JEVisUnit kg = new JEVisUnitImp(_kg);
    private final Unit _kkg = SI.KILO(SI.KILOGRAM);
    private final JEVisUnit kkg = new JEVisUnitImp(_kkg);
    private final Unit _t = NonSI.METRIC_TON;
    private final JEVisUnit t = new JEVisUnitImp(_t);
    private final Unit _l = NonSI.LITER;
    private final JEVisUnit l = new JEVisUnitImp(_l);

    private final Unit _lpers = NonSI.LITER.divide(SI.SECOND);

    private final JEVisUnit lpers = new JEVisUnitImp(_lpers);

    private final Unit _lpermin = NonSI.LITER.divide(NonSI.MINUTE);

    private final JEVisUnit lpermin = new JEVisUnitImp(_lpermin);

    private final Unit _lperh = NonSI.LITER.divide(NonSI.HOUR);

    private final JEVisUnit lperh = new JEVisUnitImp(_lperh);
    private final Unit _m3 = SI.CUBIC_METRE;
    private final JEVisUnit m3 = new JEVisUnitImp(_m3);

    private final Unit _m3pers = SI.CUBIC_METRE.divide(SI.SECOND);

    private final JEVisUnit m3pers = new JEVisUnitImp(_m3pers);

    private final Unit _m3permin = SI.CUBIC_METRE.divide(NonSI.MINUTE);

    private final JEVisUnit m3permin = new JEVisUnitImp(_m3permin);

    private final Unit _m3perh = SI.CUBIC_METRE.divide(NonSI.HOUR);

    private final JEVisUnit m3perh = new JEVisUnitImp(_m3perh);

    private final Unit _nm3 = SI.CUBIC_METRE.alternate("Nm³");

    private final JEVisUnit nm3 = new JEVisUnitImp(_nm3);

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

    private final Unit _va = SI.WATT.alternate("va");
    private final JEVisUnit va = new JEVisUnitImp(_va, "va", "NONE");
    private final Unit _kva = SI.KILO(_va);
    private final JEVisUnit kva = new JEVisUnitImp(_kva, "kva", "KILO");

    private final Unit _var = SI.WATT.alternate("var");
    private final JEVisUnit var = new JEVisUnitImp(_var, "var", "NONE");
    private final Unit _kvar = SI.KILO(_var);
    private final JEVisUnit kvar = new JEVisUnitImp(_kvar, "kvar", "KILO");

    private final Unit _vah = SI.WATT.alternate("va").times(NonSI.HOUR);
    private final JEVisUnit vah = new JEVisUnitImp(_vah, "vah", "NONE");
    private final Unit _kvah = SI.KILO(_vah);
    private final JEVisUnit kvah = new JEVisUnitImp(_kvah, "kvah", "KILO");

    private final Unit _varh = SI.WATT.alternate("var").times(NonSI.HOUR);
    private final JEVisUnit varh = new JEVisUnitImp(_varh, "varh", "NONE");
    private final Unit _kvarh = SI.KILO(_varh);
    private final JEVisUnit kvarh = new JEVisUnitImp(_kvarh, "kvarh", "KILO");

    private final Unit _bar = NonSI.BAR;

    private final JEVisUnit bar = new JEVisUnitImp(_bar);

    private final Unit _atm = NonSI.ATMOSPHERE;

    private final JEVisUnit atm = new JEVisUnitImp(_atm);

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
    private final List<JEVisUnit> energyPowerUnits;

    private final List<JEVisUnit> energyUnits;

    private final List<JEVisUnit> volumeUnits;

    private final List<JEVisUnit> massUnits;

    private final List<JEVisUnit> pressureUnits;

    private final List<JEVisUnit> volumeFlowUnits;

    private final List<JEVisUnit> moneyUnits;

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
        energyPowerUnits = new ArrayList<>(Arrays.asList(W, kW, MW, GW, va, kva, var, kvar));

        energyUnits = new ArrayList<>(Arrays.asList(W, kW, MW, GW, va, kva, var, kvar, Wh, kWh, MWh, GWh, vah, varh, kvah, kvarh));
        volumeUnits = new ArrayList<>(Arrays.asList(l, m3, nm3));
        massUnits = new ArrayList<>(Arrays.asList(mg, g, kg, kkg, t));
        pressureUnits = new ArrayList<>(Arrays.asList(bar, atm));
        volumeFlowUnits = new ArrayList<>(Arrays.asList(lpers, lpermin, lperh, m3pers, m3permin, m3perh));
        moneyUnits = new ArrayList<>(Arrays.asList(eur, usd, gbp, jpy, aud, cad, cny, krw, twd));

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
                _l, _m3, _nm3,
                _Wh, _kWh, _MWh, _GWh,
                _vah, _varh, _kvah, _kvarh,
                _eur, _usd, _gbp, _jpy, _aud, _cad, _cny, _krw, _twd
        ));

        jeVisUnitArrayList = new ArrayList<>(Arrays.asList(
                mg, g,
                kg, kkg, t,
                l, m3, nm3,
                Wh, kWh, MWh, GWh,
                vah, varh, kvah, kvarh,
                eur, usd, gbp, jpy, aud, cad, cny, krw, twd
        ));
    }

    public boolean isSumCalculable(JEVisUnit unit) {
        return energyPowerUnits.contains(unit) || jeVisUnitArrayList.contains(unit);
    }

    public JEVisUnit getSumUnit(JEVisUnit unit) {
        if (unit.equals(W) || unit.getLabel().equals(W.getLabel())) {
            return Wh;
        } else if (unit.equals(kW) || unit.getLabel().equals(kW.getLabel())) {
            return kWh;
        } else if (unit.equals(MW) || unit.getLabel().equals(MW.getLabel())) {
            return MWh;
        } else if (unit.equals(GW) || unit.getLabel().equals(GW.getLabel())) {
            return GWh;
        } else if (unit.equals(va) || unit.getLabel().equals(va.getLabel())) {
            return vah;
        } else if (unit.equals(var) || unit.getLabel().equals(var.getLabel())) {
            return varh;
        } else if (unit.equals(kva) || unit.getLabel().equals(kva.getLabel())) {
            return kvah;
        } else if (unit.equals(kvar) || unit.getLabel().equals(kvar.getLabel())) {
            return kvarh;
        } else if (unit.equals(Wh) || unit.getLabel().equals(Wh.getLabel())) {
            return Wh;
        } else if (unit.equals(kWh) || unit.getLabel().equals(kWh.getLabel())) {
            return kWh;
        } else if (unit.equals(MWh) || unit.getLabel().equals(MWh.getLabel())) {
            return MWh;
        } else if (unit.equals(GWh) || unit.getLabel().equals(GWh.getLabel())) {
            return GWh;
        } else if (unit.equals(vah) || unit.getLabel().equals(vah.getLabel())) {
            return vah;
        } else if (unit.equals(varh) || unit.getLabel().equals(varh.getLabel())) {
            return varh;
        } else if (unit.equals(kvah) || unit.getLabel().equals(kvah.getLabel())) {
            return kvah;
        } else if (unit.equals(kvarh) || unit.getLabel().equals(kvarh.getLabel())) {
            return kvarh;
        } else if (unit.equals(mg) || unit.getLabel().equals(mg.getLabel())) {
            return mg;
        } else if (unit.equals(g) || unit.getLabel().equals(g.getLabel())) {
            return g;
        } else if (unit.equals(kg) || unit.getLabel().equals(kg.getLabel())) {
            return kg;
        } else if (unit.equals(kkg) || unit.getLabel().equals(kkg.getLabel())) {
            return kkg;
        } else if (unit.equals(t) || unit.getLabel().equals(t.getLabel())) {
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
                } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (object != null) {
                if (object.getJevisClass().equals(CleanDataObject.CLASS_NAME)) {
                    isQuantity = isQuantity(sqlDataSource, object, isQuantity);
                } else if (object.getJevisClass().equals(ForecastDataObject.CLASS_NAME) || object.getJevisClass().equals(MathDataObject.CLASS_NAME)) {
                    for (JsonRelationship jsonRelationship : sqlDataSource.getRelationships(object.getId())) {
                        if (jsonRelationship.getType() == 1 && jsonRelationship.getFrom() == object.getId()) {
                            try {
                                JsonObject parent = sqlDataSource.getObject(jsonRelationship.getTo());
                                isQuantity = isQuantity(sqlDataSource, parent, isQuantity);
                                break;
                            } catch (Exception e) {
                                logger.error("Could not get parent {} of object {}", jsonRelationship.getTo(), object.getId());
                            }
                        }
                    }
                } else if (object.getJevisClass().equals(CleanDataObject.DATA_CLASS_NAME)) {
                    for (JsonRelationship jsonRelationship : sqlDataSource.getRelationships(object.getId())) {
                        if (jsonRelationship.getType() == 1 && jsonRelationship.getTo() == object.getId()) {
                            try {
                                JsonObject child = sqlDataSource.getObject(jsonRelationship.getTo());
                                isQuantity = isQuantity(sqlDataSource, child, isQuantity);
                                break;
                            } catch (Exception e) {
                                logger.error("Could not get parent {} of object {}", jsonRelationship.getTo(), object.getId());
                            }
                        }
                    }
                }
            }
        }
        return isQuantity;
    }

    private boolean isQuantity(SQLDataSource sqlDataSource, JsonObject object, boolean isQuantity) {
        boolean isQuantityTemp = isQuantity;
        if (object.getJevisClass().equals(CleanDataObject.CLASS_NAME)) {
            for (JsonAttribute jsonAttribute : sqlDataSource.getAttributes(object.getId())) {
                if (jsonAttribute.getType().equals(CleanDataObject.AttributeName.VALUE_QUANTITY.getAttributeName())) {
                    if (jsonAttribute.getLatestValue() != null) {
                        JsonSample latestSample = jsonAttribute.getLatestValue();
                        isQuantityTemp = Boolean.parseBoolean(latestSample.getValue());
                        break;
                    }
                }
            }
        }

        return isQuantityTemp;
    }

    public boolean isDiffPrefix(JEVisUnit inputUnit, JEVisUnit unit) {
        if (inputUnit.equals(W)
                && (unit.equals(kW) || unit.equals(MW) || unit.equals(GW))) {
            return true;
        } else if (inputUnit.equals(kW)
                && (unit.equals(W) || unit.equals(MW) || unit.equals(GW))) {
            return true;
        } else if (inputUnit.equals(MW)
                && (unit.equals(W) || unit.equals(kW) || unit.equals(GW))) {
            return true;
        } else if (inputUnit.equals(GW)
                && (unit.equals(W) || unit.equals(kW) || unit.equals(MW))) {
            return true;
        } else if (inputUnit.equals(Wh)
                && (unit.equals(kWh) || unit.equals(MWh) || unit.equals(GWh))) {
            return true;
        } else if (inputUnit.equals(kWh)
                && (unit.equals(Wh) || unit.equals(MWh) || unit.equals(GWh))) {
            return true;
        } else if (inputUnit.equals(MWh)
                && (unit.equals(Wh) || unit.equals(kWh) || unit.equals(GWh))) {
            return true;
        } else if (inputUnit.equals(GWh)
                && (unit.equals(Wh) || unit.equals(kWh) || unit.equals(MWh))) {
            return true;
        } else if (inputUnit.equals(mg)
                && (unit.equals(g) || unit.equals(kg) || unit.equals(kkg) || unit.equals(t))) {
            return true;
        } else if (inputUnit.equals(g)
                && (unit.equals(mg) || unit.equals(kg) || unit.equals(kkg) || unit.equals(t))) {
            return true;
        } else if (inputUnit.equals(kg)
                && (unit.equals(mg) || unit.equals(g) || unit.equals(kkg) || unit.equals(t))) {
            return true;
        } else if (inputUnit.equals(kkg)
                && (unit.equals(mg) || unit.equals(g) || unit.equals(kg) || unit.equals(t))) {
            return true;
        } else return inputUnit.equals(t)
                && (unit.equals(mg) || unit.equals(g) || unit.equals(kg) || unit.equals(kkg));
    }

    public boolean isEnergyUnit(JEVisUnit currentUnit) {
        return energyUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getEnergyUnits() {
        return energyUnits;
    }

    public boolean isVolumeUnit(JEVisUnit currentUnit) {
        return volumeUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getVolumeUnits() {
        return volumeUnits;
    }

    public boolean isMassUnit(JEVisUnit currentUnit) {
        return massUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getMassUnits() {
        return massUnits;
    }

    public boolean isPressureUnit(JEVisUnit currentUnit) {
        return pressureUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getPressureUnits() {
        return pressureUnits;
    }

    public boolean isVolumeFlowUnit(JEVisUnit currentUnit) {
        return volumeFlowUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getVolumeFlowUnits() {
        return volumeFlowUnits;
    }

    public Boolean isMoneyUnit(JEVisUnit currentUnit) {
        return moneyUnits.contains(currentUnit);
    }

    public List<JEVisUnit> getMoneyUnits() {
        return moneyUnits;
    }
}
