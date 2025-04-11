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
import si.uom.SIServiceProvider;
import systems.uom.common.spi.CommonServiceProvider;
import tech.units.indriya.AbstractUnit;
import tech.units.indriya.spi.DefaultServiceProvider;

import javax.measure.Prefix;
import javax.measure.Unit;
import javax.measure.spi.ServiceProvider;
import java.text.ParsePosition;
import java.util.Objects;

/**
 * JEVisUnit implementation based on the javax.measure.unit
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisUnitImp implements JEVisUnit {

    private static final Logger logger = LogManager.getLogger(JEVisUnitImp.class);
    private static final ServiceProvider SISP = new SIServiceProvider();
    private static final ServiceProvider DSP = new DefaultServiceProvider();
    private static final ServiceProvider CSP = new CommonServiceProvider();
    private Unit unit;
    private String label;
    private String prefix;

    public JEVisUnitImp(Unit unit) {
        this();
        this.unit = unit;

        String uString = "";
        try {
            uString = SISP.getFormatService().getUnitFormat().format(unit);
        } catch (Exception e) {
            logger.info("Could not parse unit {} with SI Service Provider", unit, e);
            try {
                uString = DSP.getFormatService().getUnitFormat().format(unit);
            } catch (Exception e1) {
                logger.info("Could not parse unit {} with Default Service Provider", unit, e);
                try {
                    uString = CSP.getFormatService().getUnitFormat().format(unit);
                } catch (Exception e2) {
                    logger.error("Could not parse unit {} with Common Service Provider", unit, e);
                }
            }
        }
        this.label = UnitManager.getInstance().format(uString);
        Prefix prefix = UnitManager.getInstance().prefixForUnit(unit);

        if (prefix != null) {
            this.prefix = prefix.getName();
        } else this.prefix = "";
    }

    public JEVisUnitImp(org.jevis.commons.ws.json.JsonUnit json) {
        this();
        this.label = json.getLabel();
        this.prefix = json.getPrefix();
        try {
            if (json.getFormula() != null) {
                try {
                    ParsePosition parsePosition = new ParsePosition(0);
                    this.unit = SISP.getFormatService().getUnitFormat().parse(json.getFormula(), parsePosition);
                } catch (Exception | Error pe) {
                    try {
                        if (!json.getLabel().isEmpty()) {
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
        this.unit = AbstractUnit.ONE;
        this.prefix = "";
        this.label = "";
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
        this();

        try {
            ParsePosition parsePosition = new ParsePosition(0);
            this.unit = SISP.getFormatService().getUnitFormat().parse(unit, parsePosition);
        } catch (Exception | Error pe) {
            try {
                if (!unit.isEmpty()) {
                    JEVisUnit jeVisUnit = ChartUnits.parseUnit(unit);
                    this.unit = jeVisUnit.getUnit();
                }
            } catch (Exception e) {
                logger.info("Warning! Could not parse unit from string: {}", unit, pe);
            }
        }
        this.label = label;
        this.prefix = prefix;
    }

    /**
     * @param unit
     * @param label
     * @param prefix
     */
    public JEVisUnitImp(Unit unit, String label, String prefix) {
        this();
        try {
            this.unit = unit;
        } catch (Exception ex) {
            logger.info("Warning! Could not parse unit: '" + unit + "' " + ex.getMessage());
        }
        this.label = label;
        this.prefix = prefix;
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
        return UnitManager.getInstance().prefixForUnit(unit);
    }

    @Override
    public void setPrefix(Prefix prefix) {
        this.prefix = prefix.toString();
        //TODO set prefix to unit
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
        if (unit != null) {
            return this.unit.toString();
        } else return "";
    }

    @Override
    public void setFormula(String formula) {
        ParsePosition pp = new ParsePosition(0);
        this.unit = SISP.getFormatService().getUnitFormat().parse(formula, pp);
    }

    @Override
    public double convertTo(JEVisUnit unit, double number) {
        //TODo check if unit is compatible
        try {
            return this.unit.getConverterTo(unit.getUnit()).convert(number);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return number;
    }

    @Override
    public JEVisUnit plus(double offset) {
        Unit newUnit = getUnit().shift(offset);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit times(double factor) {
        Unit newUnit = getUnit().multiply(factor);
        return new JEVisUnitImp(newUnit);
    }

    @Override
    public JEVisUnit times(JEVisUnit factor) {
        Unit newUnit = getUnit().multiply(factor.getUnit());
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
