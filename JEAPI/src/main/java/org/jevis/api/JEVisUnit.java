/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI.
 * <p>
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import javax.measure.unit.Unit;

/**
 * The JEVisUnit class handles the converting and labeling of the JEVisSamples
 * values.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisUnit {

    /**
     * Convert a double value with this unit into the given unit.
     *
     * @param unit unit to convert to
     * @param number number to convert
     * @return
     */
    double convertTo(JEVisUnit unit, double number);

    /**
     * Returns the label of this unit.
     *
     * @return
     */
    String getLabel();

    /**
     * Set the label for this unit.
     *
     * @param label
     */
    void setLabel(String label);

    /**
     * SI prefixes and NONE if no prefix is set
     */
    enum Prefix {

        NONE, ZETTA, EXA, PETA, TERA, GIGA, MEGA, NANO, PICO, KILO, HECTO, DEKA, DECI, CENTI, MILLI, MICRO, FEMTO, ATTO, ZEPTO, YOCTO, YOTTA
    }

    /**
     * Add an offset to this unit.
     *
     * @param offset
     * @return
     */
    JEVisUnit plus(double offset);

    /**
     * Returns a new Unit with the unit multiplied with the factor
     *
     * @param factor
     * @return
     */
    JEVisUnit times(double factor);

    /**
     * Returns a product with this unit combined with the specified.
     *
     * @param factor
     * @return
     */
    JEVisUnit times(JEVisUnit factor);

    /**
     * Returns an new Unit as an result of this unit divided by the factor.
     *
     * @param factor
     * @return
     */
    JEVisUnit divide(double factor);

    /**
     * Returns an new Unit as an result of this unit divided by the unit-factor.
     *
     * @param factor
     * @return
     */
    JEVisUnit divide(JEVisUnit factor);

    /**
     * Returns true if this unit can be converted into the given unit.
     *
     * @param unit
     * @return
     */
    boolean isCompatible(JEVisUnit unit);

    /**
     * Set the Prefix for this Unit eg. KILO, MEGA, GIGA, ...
     *
     * @param prefix
     */
    void setPrefix(Prefix prefix);

    /**
     * Returns the current Prefix of this unit
     *
     * @return Prefix for unit, returns Prefix.NONE if no prefix is set
     */
    Prefix getPrefix();

    /**
     * Returns a JSON representation of this Unit.
     *
     * @deprecated be careful using this function because it could be a
     * temporary solution until the JEVisUnit design is final
     * @return
     */
    String toJSON();

    /**
     * Returns an JSR-275 parsable string for this unit
     *
     * @return
     */
    String getFormula();

    /**
     * Set the formula for this unit (JSR-275 parsable)
     *
     * @param formula
     */
    void setFormula(String formula);

    /**
     * Returns JAVA unit
     *
     * @return
     */
    Unit getUnit();
}
