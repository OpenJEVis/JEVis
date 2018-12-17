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
package org.jevis.jeconfig.plugin.object.attribute;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface AttributeEditor {

    boolean hasChanged();

    //    void setAttribute(JEVisAttribute att);
    void commit() throws JEVisException;

    Node getEditor();

    BooleanProperty getValueChangedProperty();

    void setReadOnly(boolean canRead);

    JEVisAttribute getAttribute();

    boolean isValid();

    void update();
}
