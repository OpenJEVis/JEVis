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
package org.jevis.commons.dataprocessing;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon
 */
public class ProcessChain {

    private final String ATTRIBUTE_DATA = "Data";

    final private JEVisObject obj;

    public ProcessChain(JEVisObject obj) {
        this.obj = obj;
    }

    public String getName() {
        return obj.getName();
    }

    public JEVisAttribute getData() throws JEVisException {
        return obj.getAttribute(ATTRIBUTE_DATA);
    }

    public JEVisObject getObject() {
        return obj;
    }

}
