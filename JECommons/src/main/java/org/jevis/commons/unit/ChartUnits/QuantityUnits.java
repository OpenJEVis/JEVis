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
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Unit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

public class QuantityUnits {
    private static final Logger logger = LogManager.getLogger(QuantityUnits.class);

    /**
     * Time Units
     */
    private final Unit _sec = Units.SECOND;
    private final JEVisUnit sec = new JEVisUnitImp(_sec);
    private final Unit _min = Units.MINUTE;
    private final JEVisUnit min = new JEVisUnitImp(_min);
    private final Unit _hour = Units.HOUR;
    private final JEVisUnit hour = new JEVisUnitImp(_hour);
    private final Unit _day = Units.DAY;
    private final JEVisUnit day = new JEVisUnitImp(_day);
    private final Unit _week = Units.WEEK;
    private final JEVisUnit week = new JEVisUnitImp(_week);
    private final Unit _month = Units.MONTH;
    private final JEVisUnit month = new JEVisUnitImp(_month);
    private final Unit _year = Units.YEAR;
    private final JEVisUnit year = new JEVisUnitImp(_year);

    /**
     * Mass Units
     */
    private final Unit _mg = MetricPrefix.MILLI(Units.GRAM);
    private final JEVisUnit mg = new JEVisUnitImp(_mg);
    private final Unit _g = Units.GRAM;
    private final JEVisUnit g = new JEVisUnitImp(_g);
    private final Unit _kg = Units.KILOGRAM;
    private final JEVisUnit kg = new JEVisUnitImp(_kg);
    private final Unit _kkg = _kg.alternate("kkg").multiply(1000);
    private final JEVisUnit kkg = new JEVisUnitImp(_kkg);
    private final Unit _t = _kg.alternate("t").multiply(1000);
    private final JEVisUnit t = new JEVisUnitImp(_t);
    private final Unit _tpers = _t.divide(Units.SECOND);
    private final Unit _tpermin = _t.divide(Units.MINUTE);

    private final Unit _tperh = _t.divide(Units.HOUR);
    /**
     * Volume Units
     */
    private final Unit _l = Units.LITRE;
    private final JEVisUnit l = new JEVisUnitImp(_l);
    private final Unit _m3 = Units.CUBIC_METRE;
    private final JEVisUnit m3 = new JEVisUnitImp(_m3);
    private final Unit _nm3 = Units.CUBIC_METRE.alternate("Nm³");
    private final JEVisUnit nm3 = new JEVisUnitImp(_nm3);
    /**
     * Volume Flow Units
     */
    private final Unit _lpers = _l.divide(Units.SECOND);
    private final JEVisUnit lpers = new JEVisUnitImp(_lpers);
    private final Unit _lpermin = _l.divide(Units.MINUTE);
    private final JEVisUnit lpermin = new JEVisUnitImp(_lpermin);
    private final Unit _lperh = _l.divide(Units.HOUR);
    private final JEVisUnit lperh = new JEVisUnitImp(_lperh);
    private final Unit _m3pers = Units.CUBIC_METRE.divide(Units.SECOND);
    private final JEVisUnit m3pers = new JEVisUnitImp(_m3pers);
    private final Unit _m3permin = Units.CUBIC_METRE.divide(Units.MINUTE);
    private final JEVisUnit m3permin = new JEVisUnitImp(_m3permin);
    private final Unit _m3perh = Units.CUBIC_METRE.divide(Units.HOUR);
    private final JEVisUnit m3perh = new JEVisUnitImp(_m3perh);
    /**
     * Mass Flow Units
     */
    private final Unit _kgpers = Units.KILOGRAM.divide(Units.SECOND);
    private final JEVisUnit kgpers = new JEVisUnitImp(_kgpers);
    private final JEVisUnit tpers = new JEVisUnitImp(_tpers);
    private final Unit _kgpermin = Units.KILOGRAM.divide(Units.MINUTE);
    private final JEVisUnit kgpermin = new JEVisUnitImp(_kgpermin);
    private final JEVisUnit tpermin = new JEVisUnitImp(_tpermin);
    private final Unit _kgperh = Units.KILOGRAM.divide(Units.HOUR);
    private final JEVisUnit kgperh = new JEVisUnitImp(_kgperh);
    private final JEVisUnit tperh = new JEVisUnitImp(_tperh);
    /**
     * Energy Units
     */
    private final Unit _Wh = Units.WATT.multiply(Units.HOUR);
    private final JEVisUnit Wh = new JEVisUnitImp(_Wh);
    private final Unit _kWh = MetricPrefix.KILO(_Wh);
    private final JEVisUnit kWh = new JEVisUnitImp(_kWh);
    private final Unit _MWh = MetricPrefix.MEGA(_Wh);
    private final JEVisUnit MWh = new JEVisUnitImp(_MWh);
    private final Unit _GWh = MetricPrefix.GIGA(_Wh);
    private final JEVisUnit GWh = new JEVisUnitImp(_GWh);
    private final Unit _W = Units.WATT;
    private final JEVisUnit W = new JEVisUnitImp(_W);
    private final Unit _kW = MetricPrefix.KILO(Units.WATT);
    private final JEVisUnit kW = new JEVisUnitImp(_kW);
    private final Unit _MW = MetricPrefix.MEGA(Units.WATT);
    private final JEVisUnit MW = new JEVisUnitImp(_MW);
    private final Unit _GW = MetricPrefix.GIGA(Units.WATT);
    private final JEVisUnit GW = new JEVisUnitImp(_GW);

    private final Unit _va = Units.WATT.alternate("va");
    private final JEVisUnit va = new JEVisUnitImp(_va, "va", "NONE");
    private final Unit _kva = MetricPrefix.KILO(_va);
    private final JEVisUnit kva = new JEVisUnitImp(_kva, "kva", "KILO");

    private final Unit _var = Units.WATT.alternate("var");
    private final JEVisUnit var = new JEVisUnitImp(_var, "var", "NONE");
    private final Unit _kvar = MetricPrefix.KILO(_var);
    private final JEVisUnit kvar = new JEVisUnitImp(_kvar, "kvar", "KILO");

    private final Unit _vah = Units.WATT.alternate("va").multiply(Units.HOUR);
    private final JEVisUnit vah = new JEVisUnitImp(_vah, "vah", "NONE");
    private final Unit _kvah = MetricPrefix.KILO(_vah);
    private final JEVisUnit kvah = new JEVisUnitImp(_kvah, "kvah", "KILO");

    private final Unit _varh = Units.WATT.alternate("var").multiply(Units.HOUR);
    private final JEVisUnit varh = new JEVisUnitImp(_varh, "varh", "NONE");
    private final Unit _kvarh = MetricPrefix.KILO(_varh);
    private final JEVisUnit kvarh = new JEVisUnitImp(_kvarh, "kvarh", "KILO");

    /**
     * Pressure Units
     */
    private final Unit _bar = Units.PASCAL.alternate("bar").divide(100000);
    private final JEVisUnit bar = new JEVisUnitImp(_bar);
    private final Unit _atm = Units.PASCAL.alternate("atm").divide(101300);
    private final JEVisUnit atm = new JEVisUnitImp(_atm);

    /**
     * Unit Lists
     */
    private final ArrayList<JEVisUnit> quantityUnitsJEVisUnit;
    private final ArrayList<Unit> quantityUnitsUnit;
    private final ArrayList<String> quantityUnitsLabel;
    private final List<JEVisUnit> energyPowerUnits;
    private final List<JEVisUnit> timeUnits;
    private final List<JEVisUnit> energyUnits;
    private final List<JEVisUnit> volumeUnits;
    private final List<JEVisUnit> massUnits;
    private final List<JEVisUnit> pressureUnits;
    private final List<JEVisUnit> volumeFlowUnits;
    private final List<JEVisUnit> massFlowUnits;
    private final List<JEVisUnit> moneyUnits;

    public QuantityUnits() {
        energyPowerUnits = new ArrayList<>(Arrays.asList(W, kW, MW, GW, va, kva, var, kvar));

        timeUnits = new ArrayList<>(Arrays.asList(sec, min, hour, day, week, month, year));
        energyUnits = new ArrayList<>(Arrays.asList(W, kW, MW, GW, va, kva, var, kvar, Wh, kWh, MWh, GWh, vah, varh, kvah, kvarh));
        volumeUnits = new ArrayList<>(Arrays.asList(l, m3, nm3));
        massUnits = new ArrayList<>(Arrays.asList(mg, g, kg, kkg, t));
        pressureUnits = new ArrayList<>(Arrays.asList(bar, atm));
        volumeFlowUnits = new ArrayList<>(Arrays.asList(lpers, lpermin, lperh, m3pers, m3permin, m3perh));
        massFlowUnits = new ArrayList<>(Arrays.asList(kgpers, kgpermin, kgperh, tpers, tpermin, tperh));
        moneyUnits = new ArrayList<>();
        Currency.getAvailableCurrencies().forEach(currency -> moneyUnits.add(new JEVisUnitImp(AbstractUnit.ONE.alternate(currency.getSymbol()))));

        quantityUnitsLabel = new ArrayList<>(Arrays.asList(
                sec.getLabel(), min.getLabel(), hour.getLabel(), day.getLabel(), week.getLabel(), month.getLabel(), year.getLabel(),
                mg.getLabel(), g.getLabel(), kg.getLabel(), kkg.getLabel(), t.getLabel(),
                l.getLabel(), m3.getLabel(), nm3.getLabel(),
                Wh.getLabel(), kWh.getLabel(), MWh.getLabel(), GWh.getLabel(),
                vah.getLabel(), varh.getLabel(), kvah.getLabel(), kvarh.getLabel()
        ));
        moneyUnits.forEach(jeVisUnit -> quantityUnitsLabel.add(jeVisUnit.getLabel()));

        quantityUnitsUnit = new ArrayList<>(Arrays.asList(
                _sec, _min, _hour, _day, _week, _month, _year,
                _mg, _g, _kg, _kkg, _t,
                _l, _m3, _nm3,
                _Wh, _kWh, _MWh, _GWh,
                _vah, _varh, _kvah, _kvarh
        ));
        moneyUnits.forEach(jeVisUnit -> quantityUnitsUnit.add(jeVisUnit.getUnit()));

        quantityUnitsJEVisUnit = new ArrayList<>(Arrays.asList(
                sec, min, hour, day, week, month, year,
                mg, g, kg, kkg, t,
                l, m3, nm3,
                Wh, kWh, MWh, GWh,
                vah, varh, kvah, kvarh
        ));

        quantityUnitsJEVisUnit.addAll(moneyUnits);
    }

    public boolean isQuantityUnit(JEVisUnit unit) {
        for (JEVisUnit jeVisUnit : quantityUnitsJEVisUnit) {
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
        return quantityUnitsUnit.contains(unit);
    }

    public boolean isQuantityUnit(String unit) {
        return quantityUnitsLabel.contains(unit);
    }

    public boolean isSumCalculable(JEVisUnit unit) {
        return energyPowerUnits.contains(unit) || quantityUnitsJEVisUnit.contains(unit);
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
        } else return unit;
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

    public boolean isTimeUnit(JEVisUnit currentUnit) {
        return timeUnits.contains(currentUnit) || timeUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getTimeUnits() {
        return timeUnits;
    }

    public boolean isEnergyUnit(JEVisUnit currentUnit) {
        return energyUnits.contains(currentUnit) || energyUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getEnergyUnits() {
        return energyUnits;
    }

    public boolean isVolumeUnit(JEVisUnit currentUnit) {
        return volumeUnits.contains(currentUnit) || volumeUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getVolumeUnits() {
        return volumeUnits;
    }

    public boolean isMassUnit(JEVisUnit currentUnit) {
        return massUnits.contains(currentUnit) || massUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getMassUnits() {
        return massUnits;
    }

    public boolean isPressureUnit(JEVisUnit currentUnit) {
        return pressureUnits.contains(currentUnit) || pressureUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getPressureUnits() {
        return pressureUnits;
    }

    public boolean isVolumeFlowUnit(JEVisUnit currentUnit) {
        return volumeFlowUnits.contains(currentUnit) || volumeFlowUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getVolumeFlowUnits() {
        return volumeFlowUnits;
    }

    public boolean isMassFlowUnit(JEVisUnit currentUnit) {
        return massFlowUnits.contains(currentUnit) || massFlowUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getMassFlowUnits() {
        return massFlowUnits;
    }

    public Boolean isMoneyUnit(JEVisUnit currentUnit) {
        return moneyUnits.contains(currentUnit) || moneyUnits.stream().anyMatch(jeVisUnit -> jeVisUnit.getLabel().equals(currentUnit.getLabel()));
    }

    public List<JEVisUnit> getMoneyUnits() {
        return moneyUnits;
    }
}
