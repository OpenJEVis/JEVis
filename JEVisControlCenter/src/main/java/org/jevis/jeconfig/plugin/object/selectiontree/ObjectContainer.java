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

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ObjectContainer {

    private boolean isObject = false;
    private JEVisObject _obj;
//    private JEVisAttribute _att;
    private final ObjectSelectionTree _tree;

    public ObjectContainer(JEVisObject obj, ObjectSelectionTree tree) {
        isObject = true;
        _obj = obj;
        _tree = tree;
    }

//    public ObjectContainer(JEVisAttribute att, SelectionTree tree) {
//        isObject = false;
////        _att = att;
//        _tree = tree;
//    }
    public String getIdentifier() {
        return _obj.getID().toString();
    }

    public boolean isObject() {
        return isObject;
    }

    public JEVisObject getObject() {
        return _obj;
    }

    public ObjectSelectionTree getTree() {
        return _tree;
    }

}
