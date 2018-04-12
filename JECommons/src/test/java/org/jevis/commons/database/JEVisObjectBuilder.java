/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.database;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 *
 * @author broder
 */
public class JEVisObjectBuilder {

    Set<String> attributes = new HashSet<>();

    public JEVisObjectBuilder withAttribute(String attribute) {
        attributes.add(attribute);
        return this;
    }

    public JEVisObject build() {
        JEVisObject obj = Mockito.mock(JEVisObject.class);
        for (String attribute : attributes) {
            JEVisAttribute attributeObj = Mockito.mock(JEVisAttribute.class);
            try {
                when(obj.getAttribute(attribute)).thenReturn(attributeObj);
            } catch (JEVisException ex) {
                Logger.getLogger(JEVisObjectBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }
}
