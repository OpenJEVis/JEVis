/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.object.selectiontree;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UserSelection {

    private JEVisObject _obj;
    private JEVisAttribute _att;
    private DateTime _startDate;
    private DateTime _endDate;
    private SelectionType _type;

    public static enum SelectionType {

        Object, Attribute, AttributeAndTime
    };

    public UserSelection(SelectionType _type, JEVisObject _obj) {
        this._obj = _obj;
        this._type = _type;
    }

    public UserSelection(SelectionType type, JEVisAttribute _att, DateTime _startDate, DateTime _endDate) {
        _type = type;
        this._att = _att;
        this._startDate = _startDate;
        this._endDate = _endDate;
    }

    public DateTime getEndDate() {
        return _endDate;
    }

    public DateTime getStartDate() {
        return _startDate;
    }

    public JEVisAttribute getSelectedAttribute() {
        return _att;
    }

    public JEVisObject getSelectedObject() {
        return _obj;
    }

}
