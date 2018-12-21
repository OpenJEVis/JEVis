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
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisRelationshipWS.class);
    private JsonRelationship json;

    public JEVisRelationshipWS(JEVisDataSourceWS ds, JsonRelationship json) {
//        logger.trace("New Relationship: {}->{}", json.getFrom(), json.getTo());
        this.ds = ds;
        this.json = json;

        type = json.getType();

    }

    @Override
    public JEVisObject getStartObject() {
        return ds.getObject(json.getFrom());
    }

    @Override
    public JEVisObject getEndObject() {
        return ds.getObject(json.getTo());
    }

    @Override
    public JEVisObject[] getObjects() {
        return new JEVisObject[]{getStartObject(), getEndObject()};
    }

    @Override
    public JEVisObject getOtherObject(JEVisObject object) {
        if (object.getID() == getStartID()) {
            return getEndObject();
        } else {
            return getStartObject();
        }
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public boolean isType(int type) {
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
