/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.api;

import java.util.EventObject;

/**
 * @author fs
 */
public class JEVisEvent extends EventObject {

    private final TYPE type;
    private final Object object;

    public JEVisEvent(Object source, TYPE type, Object object) {
        super(source);
        this.type = type;
        this.object = object;
    }

    public TYPE getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    public enum TYPE {
        OBJECT_BUILD_CHILD, OBJECT_NEW_CHILD, OBJECT_DELETE, OBJECT_DELETE_BIN, OBJECT_CHILD_DELETED, OBJECT_UPDATE, OBJECT_UPDATED, OBJECT_MOVED, OBJECT_COPIED, OBJECT_LINKED,
        CLASS_DELETE, CLASS_CHILD_DELETE, CLASS_DELETE_TYPE, CLASS_BUILD_CHILD, CLASS_UPDATE, ATTRIBUTE_UPDATE
    }


}
