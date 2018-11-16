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
package org.jevis.application.jevistree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeRow {

    private final JEVisObject jevisObject;
    private final ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.BLUE);
    private final BooleanProperty objectSelectedProperty = new SimpleBooleanProperty(false);
    private final TYPE type;
    private final JEVisAttribute attribute;

    public JEVisTreeRow(JEVisObject objects) {
        this.jevisObject = objects;
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

    public BooleanProperty getObjectSelectedProperty() {
        return objectSelectedProperty;
    }

    public ObjectProperty<Color> getColorProperty() {
        return colorProperty;
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
        if (obj != null && obj instanceof JEVisTreeRow) {
            JEVisTreeRow otherRow = (JEVisTreeRow) obj;
            if (otherRow.getJEVisObject().equals(getJEVisObject())) {
                if (getType() == TYPE.ATTRIBUTE) {
                    return otherRow.getJEVisAttribute().equals(getJEVisAttribute());
                } else {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "JEVisTreeRow [type: '" + getType() + "' Object: '" + getJEVisObject() + "' Attribute: '" + getJEVisAttribute() + "']";
    }

    public enum TYPE {

        OBJECT, ATTRIBUTE
    }
}
