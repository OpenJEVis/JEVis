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

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.commons.ws.json.JsonRelationship;

/**
 *
 * @author fs
 */
public class JEVisRelationshipWS implements JEVisRelationship {

    private JEVisObject startObj = null;
    private JEVisObject endObject = null;
    private int type;
    private JEVisDataSourceWS ds;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisRelationshipWS.class);
    JsonRelationship json;

    public JEVisRelationshipWS(JEVisDataSourceWS ds, JsonRelationship json) throws JEVisException {
//        logger.trace("New Relationship: {}->{}", json.getFrom(), json.getTo());
        this.ds = ds;
        this.json = json;

        type = json.getType();

    }

    @Override
    public JEVisObject getStartObject() throws JEVisException {
        return ds.getObject(json.getFrom());
    }

    @Override
    public JEVisObject getEndObject() throws JEVisException {
        return ds.getObject(json.getTo());
    }

    @Override
    public JEVisObject[] getObjects() throws JEVisException {
        return new JEVisObject[]{startObj, endObject};
    }

    @Override
    public JEVisObject getOtherObject(JEVisObject object) throws JEVisException {
        if (object.equals(startObj)) {
            return endObject;
        } else {
            return startObj;
        }
    }

    @Override
    public int getType() throws JEVisException {
        return type;
    }

    @Override
    public boolean isType(int type) throws JEVisException {
        return (this.type == type);
    }

    @Override
    public long getStartID() {
        return json.getFrom();
    }

    @Override
    public long getEndID() {
        return json.getTo();
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "JEVisRelationshipWS{" + "startobj=" + json.getFrom() + ", endobj=" + json.getTo() + ", type=" + json.getType() + '}';
    }

}
