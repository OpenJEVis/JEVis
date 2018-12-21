/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import org.jevis.api.*;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonType;

import javax.measure.unit.Unit;

/**
 * @author fs
 */
public class JEVisTypeWS implements JEVisType {

    private String jclass = null;
    private JEVisDataSourceWS ds;
    private JsonType json;

    public JEVisTypeWS(JEVisDataSourceWS ds, JsonType json, String jclass) {
        this.jclass = jclass;
        this.ds = ds;
        this.json = json;
    }

    public JEVisTypeWS(JEVisDataSourceWS ds, String name, String jclass) {
        this.json = new JsonType();
        this.json.setName(name);
        this.jclass = jclass;
        this.ds = ds;
    }

    @Override
    public String getName() {
        return json.getName();
    }

    @Override
    public void setName(String name) {
        json.setName(name);
    }

    @Override
    public int getPrimitiveType() {
        return json.getPrimitiveType();
    }

    @Override
    public void setPrimitiveType(int type) {
        json.setPrimitiveType(type);
    }

    @Override
    public String getGUIDisplayType() {
        return json.getGuiType();
    }

    @Override
    public void setGUIDisplayType(String type) {
        json.setGuiType(type);
    }

    @Override
    public int getGUIPosition() {
        return json.getGUIPosition();
    }

    @Override
    public void setGUIPosition(int pos) {
        json.setGUIPosition(pos);
    }

    @Override
    public JEVisClass getJEVisClass() {
        return ds.getJEVisClass(jclass);
    }

    @Override
    public int getValidity() {
        return json.getValidity();
    }

    @Override
    public void setValidity(int validity) {
        json.setValidity(validity);
    }

    @Override
    public String getConfigurationValue() {
        return "";
    }

    @Override
    public void setConfigurationValue(String value) {
    }

    @Override
    public JEVisUnit getUnit() {
        return new JEVisUnitImp(Unit.ONE);
    }

    @Override
    public void setUnit(JEVisUnit unit) {
    }

    @Override
    public String getAlternativSymbol() {
        return "";
    }

    @Override
    public void setAlternativSymbol(String symbol) {
    }

    @Override
    public String getDescription() {
        return json.getDescription();
    }

    @Override
    public void setDescription(String discription) {
        json.setDescription(discription);
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        //TODO: has Changed
        return false;
    }

    @Override
    public int compareTo(JEVisType o
    ) {
        try {
            return getName().compareTo(o.getName());
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public String getJEVisClassName() {
        return jclass;
    }

    @Override
    public boolean isInherited() {
        return json.getInherited();
    }

    @Override
    public void setInherited(boolean inherited) {
        json.setInherited(inherited);
    }

    @Override
    public boolean equals(Object obj
    ) {
        try {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JEVisType other = (JEVisType) obj;
            if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
                return false;
            }
            return this.getJEVisClass() == other.getJEVisClass() || (this.getJEVisClass() != null && this.getJEVisClass().equals(other.getJEVisClass()));
        } catch (Exception ex) {
            return false;
        }
    }

}
