/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.application.cache;

import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;

/**
 *
 * @author fs
 */
public class CacheObjectEvent implements CacheEvent {

    private Object obj;
    private TYPE type;

    public CacheObjectEvent(JEVisObject obj, TYPE type) {
        this.obj = obj;
        this.type = type;
    }

    public CacheObjectEvent(JEVisClass obj, TYPE type) {
        this.obj = obj;
        this.type = type;
    }

    @Override
    public TYPE getType() {
        return type;
    }

    @Override
    public Object getObject() {
        return obj;
    }

    @Override
    public void fireEvent() {
        ;
    }

}
