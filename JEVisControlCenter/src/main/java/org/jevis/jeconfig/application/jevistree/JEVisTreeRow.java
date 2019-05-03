/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.application.jevistree;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeRow {

    private final JEVisObject jevisObject;
    private Boolean selected = false;
    private final TYPE type;
    private final JEVisAttribute attribute;
    private Map<String, Object> additinalData;

    public JEVisTreeRow(JEVisObject object) {
        this.jevisObject = object;
        this.type = TYPE.OBJECT;
        this.attribute = null;
    }

    public JEVisTreeRow(JEVisAttribute attribute) {
        this.type = TYPE.ATTRIBUTE;
        this.jevisObject = attribute.getObject();
        this.attribute = attribute;

    }

    public TYPE getType() {
        return type;
    }

    public JEVisAttribute getJEVisAttribute() {
        return attribute;
    }

    public JEVisObject getJEVisObject() {
        return jevisObject;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getID() {

        if (jevisObject != null && attribute != null) {
            return jevisObject.getID() + "." + attribute.getName();
        } else if (jevisObject != null) {
            return jevisObject.getID().toString();
        } else {
            return "";
        }

    }

    @Override
    public boolean equals(Object obj) {
        try {
            if (obj != null && obj instanceof JEVisTreeRow) {
                JEVisTreeRow otherRow = (JEVisTreeRow) obj;
                if (otherRow.getJEVisObject().equals(getJEVisObject())) {
                    if (getType() == TYPE.ATTRIBUTE && otherRow.getJEVisAttribute() != null && getJEVisAttribute() != null) {
                        return otherRow.getJEVisAttribute().equals(getJEVisAttribute());
                    } else {
                        return true;
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        String objectName = "JEVisTreeRow [type: '" + getType() + " NoName" + "]";
        try {
            objectName = "ObjectID: '" + getJEVisObject().getID() + "'";
            if (getType() == TYPE.OBJECT) {
                objectName += " Name: '" + getJEVisObject().getName() + "' (" + getJEVisObject().getJEVisClassName() + ")";
            } else {
                objectName += " Attribute: '" + getJEVisAttribute().getName() + "'";
            }
        } catch (Exception ex) {

        }
        return objectName;
    }

    public void clearData() {
        additinalData.clear();
        additinalData = null;
    }

    public Object getDataObject(String key, Object defaultObject) {
        if (additinalData == null) additinalData = new HashMap<>();
        return additinalData.getOrDefault(key, defaultObject);
    }

    public void setDataObject(String key, Object object) {
        if (additinalData == null) additinalData = new HashMap<>();
        additinalData.put(key, object);
    }

    public enum TYPE {

        OBJECT, ATTRIBUTE
    }
}
