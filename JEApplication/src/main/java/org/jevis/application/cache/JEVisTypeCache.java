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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;

/**
 *
 * @author fs
 */
public class JEVisTypeCache implements JEVisType {

    private final JEVisDataSourceCache cache;
    private final JEVisType otherDSType;
    private String jclass;
    private final Logger logger = LogManager.getLogger(JEVisTypeCache.class);
    private Integer primitiveType;
    private JEVisUnit unit = null;
    private String alternativSymbol = null;
    private String description = null;

    public JEVisTypeCache(JEVisDataSourceCache cache, JEVisType otherDSType, String jclass) {
//        logger.trace("==inti==");
        this.cache = cache;
        this.otherDSType = otherDSType;
        this.jclass = jclass;
    }

    @Override
    public String getName() throws JEVisException {
        return otherDSType.getName();
    }

    @Override
    public void setName(String name) throws JEVisException {
        otherDSType.setName(name);
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        if (primitiveType == null) {
            primitiveType = otherDSType.getPrimitiveType();
        }

        return primitiveType;
    }

    @Override
    public void setPrimitiveType(int type) throws JEVisException {
        primitiveType = type;
        otherDSType.setPrimitiveType(type);
    }

    @Override
    public String getGUIDisplayType() throws JEVisException {
        return otherDSType.getGUIDisplayType();
    }

    @Override
    public void setGUIDisplayType(String type) throws JEVisException {
        otherDSType.setGUIDisplayType(type);
    }

    @Override
    public void setGUIPosition(int pos) throws JEVisException {
        otherDSType.setGUIPosition(pos);
    }

    @Override
    public int getGUIPosition() throws JEVisException {
        return otherDSType.getGUIPosition();
    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return getJEVisClass().getName();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return cache.getJEVisClass(jclass);
    }

    @Override
    public int getValidity() throws JEVisException {
        return otherDSType.getValidity();
    }

    @Override
    public void setValidity(int validity) throws JEVisException {
        otherDSType.setValidity(validity);
    }

    @Override
    public String getConfigurationValue() throws JEVisException {
        return otherDSType.getConfigurationValue();
    }

    @Override
    public void setConfigurationValue(String value) throws JEVisException {
        otherDSType.setConfigurationValue(value);
    }

    @Override
    public void setUnit(JEVisUnit unit) throws JEVisException {
        otherDSType.setUnit(unit);
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        if (unit == null) {
            unit = otherDSType.getUnit();
        }
        return unit;
    }

    @Override
    public String getAlternativSymbol() throws JEVisException {
        if (alternativSymbol == null) {
            alternativSymbol = otherDSType.getAlternativSymbol();;
        }
        return alternativSymbol;
    }

    @Override
    public void setAlternativSymbol(String symbol) throws JEVisException {
        otherDSType.setAlternativSymbol(symbol);
    }

    @Override
    public String getDescription() throws JEVisException {
        if (description == null) {
            description = otherDSType.getDescription();
        }
        return description;
    }

    @Override
    public void setDescription(String discription) throws JEVisException {
        otherDSType.setDescription(discription);
    }

    @Override
    public boolean delete() throws JEVisException {
        return getJEVisClass().deleteType(getName());
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return cache;
    }

    @Override
    public void commit() throws JEVisException {
        logger.trace("Commit()");
        otherDSType.commit();
    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        logger.trace("is on use but not implemented");
        return false;//TODO this
    }

    @Override
    public int compareTo(JEVisType o) {
        try {
            return getName().compareTo(o.getName());
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public boolean isInherited() throws JEVisException {
        return otherDSType.isInherited();
    }

    @Override
    public void setInherited(boolean inherited) throws JEVisException {
        otherDSType.setInherited(inherited);
    }

    @Override
    public String toString() {
        return otherDSType.toString();
    }

}
