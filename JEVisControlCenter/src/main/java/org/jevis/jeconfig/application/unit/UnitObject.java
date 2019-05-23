/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.application.unit;

import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.tool.I18n;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UnitObject {

    public String getName() {

        switch (_type) {
            case Quantity:
                if (_id.equals("Custom")) {
                    return _id;
                } else {
                    return UnitManager.getInstance().getQuantitiesName(_unit, I18n.getInstance().getLocale()) + " - "
                            + UnitManager.getInstance().getUnitName(_unit, I18n.getInstance().getLocale()) + " [ " + _unit.toString() + " ]";
                }

            default:
                return UnitManager.getInstance().getUnitName(_unit, I18n.getInstance().getLocale()) + " [ " + _unit.toString() + " ]";
        }
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

    public enum Type {

        Quantity, SIUnit, AltSymbol, FakeRoot, NonSIUnit
    }

    public Type getType() {
        return _type;
    }

    public JEVisUnit getUnit() {
        return _unit;
    }

}
