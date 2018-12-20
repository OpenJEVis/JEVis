/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.unit;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.UnitManager;

import java.util.Locale;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitObject {

    public enum Type {

        Quntity, SIUnit, AltSymbol, FakeRoot, NonSIUnit
    }

    private JEVisUnit _unit;
    private Type _type;
    private String _id;
    private String _name;

    public UnitObject(Type type, JEVisUnit unit, String id) {
        _unit = unit;
        _type = type;
        _id = id;
    }

    public String getID() {
        return _id;

    }

    public String getName() {

        switch (_type) {
            case Quntity:
                if (_id.equals("Custom")) {
                    return _id;
                } else {
                    return UnitManager.getInstance().getQuantitiesName(_unit, Locale.ENGLISH) + " - "
                            + UnitManager.getInstance().getUnitName(_unit, Locale.ENGLISH) + " [ " + _unit.toString() + " ]";
                }

            default:
                return UnitManager.getInstance().getUnitName(_unit, Locale.ENGLISH) + " [ " + _unit.toString() + " ]";
        }
    }

    public Type getType() {
        return _type;
    }

    public JEVisUnit getUnit() {
        return _unit;
    }

}
