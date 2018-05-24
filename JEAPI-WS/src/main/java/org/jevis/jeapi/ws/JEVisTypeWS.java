/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import com.google.gson.Gson;
import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonType;

/**
 *
 * @author fs
 */
public class JEVisTypeWS implements JEVisType {

    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisDataSourceWS.class);
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
    public String getName() throws JEVisException {
        return json.getName();
    }

    @Override
    public void setName(String name) throws JEVisException {
        json.setName(name);
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return json.getPrimitiveType();
    }

    @Override
    public void setPrimitiveType(int type) throws JEVisException {
        json.setPrimitiveType(type);
    }

    @Override
    public String getGUIDisplayType() throws JEVisException {
        return json.getGuiType();
    }

    @Override
    public void setGUIDisplayType(String type) throws JEVisException {
        json.setGuiType(type);
    }

    @Override
    public void setGUIPosition(int pos) throws JEVisException {
        json.setGUIPosition(pos);
    }

    @Override
    public int getGUIPosition() throws JEVisException {
        return json.getGUIPosition();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return ds.getJEVisClass(jclass);
    }

    @Override
    public int getValidity() throws JEVisException {
        return json.getValidity();
    }

    @Override
    public void setValidity(int validity) throws JEVisException {
        json.setValidity(validity);
    }

    @Override
    public String getConfigurationValue() throws JEVisException {
        return "";
    }

    @Override
    public void setConfigurationValue(String value) throws JEVisException {
        ;
    }

    @Override
    public void setUnit(JEVisUnit unit) throws JEVisException {
        ;
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return new JEVisUnitImp(Unit.ONE);
    }

    @Override
    public String getAlternativSymbol() throws JEVisException {
        return "";
    }

    @Override
    public void setAlternativSymbol(String symbol) throws JEVisException {
        ;
    }

    @Override
    public String getDescription() throws JEVisException {
        return json.getDescription();
    }

    @Override
    public void setDescription(String discription) throws JEVisException {
        json.setDescription(discription);
    }

    @Override
    public boolean delete() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
        try {

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.CLASSES.PATH
                    + jclass+ "/"
                    + REQUEST.CLASSES.TYPES.PATH
                    + getName();
//                    + getName();
            Gson gson = new Gson();
            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));

//            JsonType newJson = gson.fromJson(response.toString(), JsonType.class);
//            this.json = newJson;
               
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    @Override
    public void rollBack() throws JEVisException {
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
    public String getJEVisClassName() throws JEVisException {
        return jclass;
    }

    @Override
    public boolean isInherited() throws JEVisException {
        return json.getInherited();
    }

    @Override
    public void setInherited(boolean inherited) throws JEVisException {
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
            if (this.getJEVisClass() != other.getJEVisClass() && (this.getJEVisClass() == null || !this.getJEVisClass().equals(other.getJEVisClass()))) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
