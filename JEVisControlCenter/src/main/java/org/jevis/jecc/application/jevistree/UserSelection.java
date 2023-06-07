package org.jevis.jecc.application.jevistree;

/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.joda.time.DateTime;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class UserSelection {

    private final JEVisObject obj;
    private final SelectionType type;
    private JEVisAttribute att;
    private DateTime startDate;
    private DateTime endDate;

    public UserSelection(SelectionType type, JEVisObject obj) {
        this.obj = obj;
        this.type = type;
    }

    public UserSelection(SelectionType type, JEVisAttribute att, DateTime startDate, DateTime endDate) {
        this.type = type;
        this.att = att;
        this.obj = att.getObject();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public JEVisAttribute getSelectedAttribute() {
        return att;
    }

    public JEVisObject getSelectedObject() {
        return obj;
    }

    public SelectionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "UserSelection{" + "_obj=" + obj + ", _att=" + att + ", _startDate=" + startDate + ", _endDate=" + endDate + ", _type=" + type + '}';
    }

    public enum SelectionType {

        Object, Attribute, AttributeAndTime
    }

}
