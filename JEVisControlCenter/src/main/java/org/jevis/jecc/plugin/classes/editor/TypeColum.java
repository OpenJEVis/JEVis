/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jecc.plugin.classes.editor;

import javafx.beans.property.SimpleStringProperty;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisConstants.Direction;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.commons.i18n.I18n;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class TypeColum {

    private SimpleStringProperty otherClass = new SimpleStringProperty("Error");
    private SimpleStringProperty type = new SimpleStringProperty("Error");
    private SimpleStringProperty direction = new SimpleStringProperty("Error");

    /**
     *
     * @param relation
     * @param jclass
     */
    public TypeColum(JEVisType type) {
        try {
            this.otherClass = new SimpleStringProperty(type.getName());
            this.type = new SimpleStringProperty(getTypeName(type.getPrimitiveType()));
            this.direction = new SimpleStringProperty(type.getGUIDisplayType());
        } catch (JEVisException ex) {
        }
    }

    private String getDirectionName(int direction) {
        switch (direction) {
            case Direction.BACKWARD:
                return "Backward";
            case Direction.FORWARD:
                return "Forward";
            default:
                return "Unknown";
        }

    }

    private String getTypeName(int type) {
        switch (type) {
            case JEVisConstants.PrimitiveType.STRING:
                return I18n.getInstance().getString("plugin.classes.relationship.prim_type.string");
            case JEVisConstants.PrimitiveType.DOUBLE:
                return I18n.getInstance().getString("plugin.classes.relationship.prim_type.double");
            case JEVisConstants.PrimitiveType.BOOLEAN:
                return I18n.getInstance().getString("plugin.classes.relationship.prim_type.boolean");
            default:
                return I18n.getInstance().getString("plugin.classes.relationship.prim_type.unknwn");
        }

    }

    public String getOtherClass() {
        return otherClass.get();
    }

    public void setOtherClass(String fName) {
        otherClass.set(fName);
    }

    public String getType() {
        return type.get();
    }

    public void setType(String fName) {
        type.set(fName);
    }

    public String getDirection() {
        return direction.get();
    }

    public void setDirection(String fName) {
        direction.set(fName);
    }
}
