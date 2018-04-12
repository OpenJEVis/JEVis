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

import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;

/**
 *
 * @author fs
 */
public class JEVisRelationshipCache implements JEVisRelationship {

    private final JEVisDataSourceCache cache;
    private JEVisObject start;
    private JEVisObject end;
    private final JEVisRelationship otherRel;

    public JEVisRelationshipCache(JEVisDataSourceCache cache, JEVisRelationship otherRel) {
        this.cache = cache;
        this.otherRel = otherRel;
    }

    @Override
    public JEVisObject getStartObject() throws JEVisException {
        if (start == null) {
            start = cache.getObject(otherRel.getStartID());
        }
        return start;
    }

    @Override
    public long getStartID() {
        return otherRel.getStartID();
    }

    @Override
    public long getEndID() {
        return otherRel.getEndID();
    }

    @Override
    public JEVisObject getEndObject() throws JEVisException {
        if (end == null) {
            end = cache.getObject(otherRel.getEndID());
        }
        return end;
    }

    @Override
    public JEVisObject[] getObjects() throws JEVisException {
        return new JEVisObject[]{getStartObject(), getEndObject()};
    }

    @Override
    public JEVisObject getOtherObject(JEVisObject object) throws JEVisException {
        if (getStartID() == object.getID()) {
            return getEndObject();
        } else {
            return getStartObject();
        }
    }

    @Override
    public int getType() throws JEVisException {
        return otherRel.getType();
    }

    @Override
    public boolean isType(int type) throws JEVisException {
        return getType() == type;
    }

    @Override
    public void delete() {
        otherRel.delete();
    }

}
