/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.unit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;

import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Objects;

/**
 * JEVisUnit implementation based on the javax.measure.unit
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisUnitImp implements JEVisUnit {

    private Unit _unit = Unit.ONE;
    private String _label = "";
    private Prefix _prefix = Prefix.NONE;
    private static final Logger logger = LogManager.getLogger(JEVisUnitImp.class);
//    private JsonUnit _json = null;

    public JEVisUnitImp(Unit unit) {
        _unit = unit;
        _label = unit.toString();
        _prefix = Prefix.NONE;//todo but how
    }

    public JEVisUnitImp() {
        _unit = Unit.ONE;
        _label = _unit.toString();
        _prefix = Prefix.NONE;//todo but how
    }

    public JEVisUnitImp(org.jevis.commons.ws.json.JsonUnit json) {

        Gson gson = new Gson();
        _label = json.getLabel();
        _prefix = UnitManager.getInstance().getPrefix(json.getPrefix(), Locale.getDefault());
        ParsePosition pp = new ParsePosition(0);
        _unit = UnitFormat.getInstance().parseObject(json.getFormula(), pp);

        try {
            UnitManager.getInstance().getUnitWithPrefix(_unit, getPrefix());
            _unit.toString();
        } catch (Exception ex) {
            logger.info("Warning! Could not parse unit from json: '" + gson.toJson(json) + "' " + ex.getMessage());
            _unit = Unit.ONE;
            _label = "Unknown";
            _prefix = Prefix.NONE;
        }

    }

    /**
     * Create an new JEVisUnit from an JEVisUnit string
     *
     * @param prefix
     * @param unit
     * @param label
     * @TODO example of an string
     */
    public JEVisUnitImp(String unit, String label, Prefix prefix) {
//        logger.info("new JEVisUnitImp2: " + unit + " - " + label + " - " + prefix);
        UnitFormula up = new UnitFormula(unit, label);
        _unit = up.getUnit();
        _label = label;
        _prefix = prefix;
    }

    public Unit toUnit() {

        return UnitManager.getInstance().getUnitWithPrefix(_unit, getPrefix());
//        return _unit;
    }

    /**
     * @param unit
     * @param label
     * @param prefix
     */
    public JEVisUnitImp(Unit unit, String label, Prefix prefix) {
//        logger.info("new JEVisUnitImp1: " + unit + " - " + label + " - " + prefix);
        try {
            _unit = unit;
        } catch (Exception ex) {
            logger.info("Warning! Could not parse unit: '" + unit + "' " + ex.getMessage());
        }
        _label = label;
        _prefix = prefix;
    }

    @Override
    public String toJSON() {
        Gson gson = new GsonBuilder().create();
        JsonUnit junit = JsonFactory.buildUnit(this);

        return gson.toJson(junit, JsonUnit.class);
    }

    @Override
    public void setPrefix(Prefix prefix) {
        //TODO remove pre prefix?
        _prefix = prefix;
    }

    @Override
    public Prefix getPrefix() {
        return _prefix;
    }

    public Unit getUnit() {
        return _unit;
    }

    @Override
    public String getLabel() {
//        if (_label == null || _label.isEmpty()) {
//            String formatted = _unit.toString().replace("·", "");
//
//            return UnitManager.getInstance().getPrefixChar(_prefix) + formatted;
//        }

        return _label;
    }

    @Override
    public void setLabel(String label) {
        _label = label;
    }

    @Override
    public String getFormula() {
        return _unit.toString();
    }

    @Override
    public double converteTo(JEVisUnit unit, double number) {
        //TODo check if unit is compatible
        try {
            Unit targetUnit = UnitManager.getInstance().getUnitWithPrefix(((JEVisUnitImp) unit).getUnit(), unit.getPrefix());
            Unit sourceUnit = UnitManager.getInstance().getUnitWithPrefix(_unit, getPrefix());

            UnitConverter uc = sourceUnit.getConverterTo(targetUnit);
            return uc.convert(number);
        } catch (Exception ex) {
            throw new ConversionException("Unit error: " + ex.getMessage());
        }

    }

    @Override
    public void setFormula(String formula) {
        ParsePosition pp = new ParsePosition(0);
        _unit = UnitFormat.getInstance().parseObject(formula, pp);
    }

    @Override
    public JEVisUnit plus(double offset) {
        Unit newUnit = getUnit().plus(offset);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit times(double factor) {
        Unit newUnit = getUnit().times(factor);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit times(JEVisUnit factor) {
        Unit newUnit = getUnit().times(((JEVisUnitImp) factor).getUnit());
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit divide(double factor) {
        Unit newUnit = getUnit().divide(factor);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit divide(JEVisUnit factor) {
        Unit newUnit = getUnit().divide(((JEVisUnitImp) factor).getUnit());
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public boolean isCompatible(JEVisUnit unit) {
        return getUnit().isCompatible(((JEVisUnitImp) unit).getUnit());
    }

    @Override
    public String toString() {
        return UnitManager.getInstance().formate(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this._unit);
        hash = 97 * hash + Objects.hashCode(this._label);
        hash = 97 * hash + Objects.hashCode(this._prefix);
        return hash;
    }


//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final JEVisUnitImp other = (JEVisUnitImp) obj;
//        if (!Objects.equals(this._unit, other._unit)) {
//            return false;
//        }
//        return true;
//        
//        
//        
//    }

//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final JEVisUnitImp other = (JEVisUnitImp) obj;
//        if (!Objects.equals(this._label, other._label)) {
//            return false;
//        }
//        if (!Objects.equals(this._unit, other._unit)) {
//            return false;
//        }
//        if (this._prefix != other._prefix) {
//            return false;
//        }
//        return true;
//    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof JEVisUnit) {

        } else {
            return false;
        }
        final JEVisUnit other = (JEVisUnit) obj;
        if (!Objects.equals(this.getLabel(), other.getLabel())) {
            return false;
        }
        if (!Objects.equals(this.getFormula(), other.getFormula())) {
            return false;
        }
        return Objects.equals(this.getPrefix(), other.getPrefix());
    }


    @Override
    protected Object clone() {
        JEVisUnit clone = new JEVisUnitImp(_unit);

        return clone;
    }
}
