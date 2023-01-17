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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.MoneyUnit;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonUnit;

import javax.measure.MetricPrefix;
import javax.measure.converter.ConversionException;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Objects;

/**
 * JEVisUnit implementation based on the javax.measure.unit
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisUnitImp implements JEVisUnit {

    private Unit unit = Unit.ONE;
    private String label = "";
    private String prefix = "";
    private static final Logger logger = LogManager.getLogger(JEVisUnitImp.class);

    public JEVisUnitImp(Unit unit) {
        this.unit = unit;
        UnitFormat unitFormat = UnitFormat.getInstance();
        String uString = unitFormat.format(unit);
        this.label = UnitManager.getInstance().format(uString);
        this.prefix = getPrefixFromUnit(unit);
    }

    public JEVisUnitImp(org.jevis.commons.ws.json.JsonUnit json) {

        this.label = json.getLabel();
        this.prefix = json.getPrefix();
        try {
            try {
                this.unit = (Unit) UnitFormat.getInstance().parseObject(json.getFormula());
            } catch (ParseException pe) {
                try {
                    if (!json.getLabel().equals("")) {
                        for (MoneyUnit mu : MoneyUnit.values()) {
                            if (json.getFormula().equals(mu.toString())) {
                                JEVisUnit jeVisUnit = ChartUnits.parseUnit(json.getLabel());
                                this.unit = jeVisUnit.getUnit();
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Warning! Could not parse unit from json: '" + JsonTools.prettyObjectMapper().writeValueAsString(json) + "' " + pe.getMessage());
                }
            }
        } catch (JsonProcessingException ex) {
            try {
                logger.error("Warning! Could not create unit from json: '" + JsonTools.prettyObjectMapper().writeValueAsString(json) + "' " + ex.getMessage());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    public JEVisUnitImp() {
    }

    /**
     * Create a new JEVisUnit from an JEVisUnit string
     *
     * @param prefix
     * @param unit
     * @param label
     * @TODO example of a string
     */
    public JEVisUnitImp(String unit, String label, String prefix) {
        UnitFormula up = new UnitFormula(unit, label);
        this.unit = up.getUnit();
        this.label = label;
        this.prefix = prefix;
    }

    /**
     * @param unit
     * @param label
     * @param prefix
     */
    public JEVisUnitImp(Unit unit, String label, String prefix) {
        try {
            this.unit = unit;
        } catch (Exception ex) {
            logger.info("Warning! Could not parse unit: '" + unit + "' " + ex.getMessage());
        }
        this.label = label;
        this.prefix = prefix;
    }

    private String getPrefixFromUnit(Unit unit) {
        String unitString = unit.toString();
        if (unitString.length() > 1) {
            if (unitString.equals("m²") || unitString.equals("m³") || unitString.equals("min")) return "";

            String sub = unitString.substring(0, 1);
            MetricPrefix prefixFromShort = UnitManager.getInstance().getPrefixFromShort(sub);

            if (prefixFromShort != null) {
                return prefixFromShort.toString();
            } else return "";
        } else return "";
    }

    @Override
    public String toJSON() {
        JsonUnit junit = JsonFactory.buildUnit(this);

        try {
            return JsonTools.objectMapper().writeValueAsString(junit);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public Prefix getPrefix() {
        return UnitManager.getInstance().getJEVisUnitPrefix(this.prefix);
    }

    @Override
    public void setPrefix(Prefix prefix) {
        this.prefix = prefix.toString();
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getFormula() {
        return this.unit.toString();
    }

    @Override
    public void setFormula(String formula) {
        ParsePosition pp = new ParsePosition(0);
        this.unit = UnitFormat.getInstance().parseObject(formula, pp);
    }

    @Override
    public double convertTo(JEVisUnit unit, double number) {
        //TODo check if unit is compatible
        try {
            if (UnitManager.getInstance().getPrefix(this.prefix) instanceof MetricPrefix) {
                MetricPrefix metricPrefix = (MetricPrefix) UnitManager.getInstance().getPrefix(this.prefix);
                Unit targetUnit = UnitManager.getInstance().getUnitWithPrefix(unit.getUnit(), metricPrefix);
                Unit sourceUnit = UnitManager.getInstance().getUnitWithPrefix(this.unit, metricPrefix);

                UnitConverter uc = sourceUnit.getConverterTo(targetUnit);
                return uc.convert(number);
            }
        } catch (Exception ex) {
            throw new ConversionException("Unit error: " + ex.getMessage());
        }
        return number;
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
        Unit newUnit = getUnit().times(factor.getUnit());
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit divide(double factor) {
        Unit newUnit = getUnit().divide(factor);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit divide(JEVisUnit factor) {
        Unit newUnit = getUnit().divide(factor.getUnit());
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public boolean isCompatible(JEVisUnit unit) {
        return getUnit().isCompatible(unit.getUnit());
    }

    @Override
    public String toString() {
        return UnitManager.getInstance().format(this);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.unit);
        hash = 97 * hash + Objects.hashCode(this.label);
        hash = 97 * hash + Objects.hashCode(this.prefix);
        return hash;
    }

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

        return new JEVisUnitImp(this.unit);
    }
}
