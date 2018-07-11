/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.application.jevistree;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEVisTreeRow {

    private final JEVisObject _obj;
    private JEVisAttribute _attribute = null;
    private final ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.BLUE);
    private final BooleanProperty objectSelecedProperty = new SimpleBooleanProperty(false);

    public enum TYPE {

        OBJECT, ATTRIBUTE
    }
    private final TYPE _type;

    public JEVisTreeRow(JEVisObject _obj) {
        this._obj = _obj;
        _type = TYPE.OBJECT;
    }

    public JEVisTreeRow(JEVisAttribute attribute) {
        _type = TYPE.ATTRIBUTE;
        _obj = attribute.getObject();
        _attribute = attribute;

    }

    public TYPE getType() {
        return _type;
    }

    public JEVisAttribute getJEVisAttribute() {
        return _attribute;
    }

    public JEVisObject getJEVisObject() {
        return _obj;
    }

    public BooleanProperty getObjectSelecedProperty() {
        return objectSelecedProperty;
    }

    public ObjectProperty<Color> getColorProperty() {
        return colorProperty;
    }

    public String getID() {

        if (_obj != null && _attribute != null) {
            return _obj.getID() + "." + _attribute.getName();
        } else if (_obj != null) {
            return _obj.getID().toString();
        } else {
            return "";
        }

    }

}
