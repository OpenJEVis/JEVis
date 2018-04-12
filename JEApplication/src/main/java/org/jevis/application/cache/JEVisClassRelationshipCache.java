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
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

/**
 *
 * @author fs
 */
public class JEVisClassRelationshipCache implements org.jevis.api.JEVisClassRelationship {

    private final JEVisClassRelationship otherRel;
    private final JEVisDataSourceCache cache;
    private JEVisClass start;
    private JEVisClass end;

    public JEVisClassRelationshipCache(JEVisDataSourceCache cache, JEVisClassRelationship otherRel) {
        this.otherRel = otherRel;
        this.cache = cache;
    }

    @Override
    public JEVisClass getStart() throws JEVisException {
        if (start == null) {
            start = cache.getJEVisClass(otherRel.getStartName());
        }
        return start;
    }

    @Override
    public String getStartName() throws JEVisException {
        return otherRel.getStartName();
    }

    @Override
    public JEVisClass getEnd() throws JEVisException {
        if (end == null) {
            end = cache.getJEVisClass(otherRel.getEndName());
        }
        return end;
    }

    @Override
    public String getEndName() throws JEVisException {
        return otherRel.getEndName();
    }

    @Override
    public int getType() throws JEVisException {
        return otherRel.getType();
    }

    @Override
    public JEVisClass[] getJEVisClasses() throws JEVisException {
        return new JEVisClass[]{getStart(), getEnd()};
    }

    @Override
    public JEVisClass getOtherClass(JEVisClass jclass) throws JEVisException {
        if (getStartName().equals(jclass.getName())) {
            return getEnd();
        } else {
            return getStart();
        }
    }

    @Override
    public String getOtherClassName(String name) throws JEVisException {
        if (getStartName().equals(name)) {
            return getEndName();
        } else {
            return getStartName();
        }
    }

    @Override
    public boolean isType(int type) throws JEVisException {
        return type == getType();
    }

    @Override
    public boolean isInHerited() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return cache;
    }

    @Override
    public String toString() {
        try {
            return "JEVisClassRelationshipCache{ Type=" + getType() + ", from=" + getStartName() + ", to=" + getEndName() + "}";
        } catch (JEVisException ex) {

            return "";
        }
    }

}
