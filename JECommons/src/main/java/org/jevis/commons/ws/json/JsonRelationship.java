/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jevis.api.JEVisConstants;

/**
 * This Class is used to represents an JEVisRelationship in JSON by the
 * WebService
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement
public class JsonRelationship {

    private long from;
    private long to;
    private int type;

    public JsonRelationship() {
    }

    /**
     * Return the start JEVisObject in this relationship.
     *
     *
     * @return ID if the left object in the relationship
     */
    @XmlElement(name = "from")
    public long getFrom() {
        return from;
    }

    /**
     * Set the start JEVisObject in this relationship.
     *
     * @param start
     */
    public void setFrom(long start) {
        this.from = start;
    }

    /**
     * Returns the end JEVisObject in this relationship.
     *
     * @return
     */
    @XmlElement(name = "to")
    public long getTo() {
        return to;
    }

    /**
     * Set the end JEVisObject in this relationship.
     *
     * @param end
     */
    public void setTo(long end) {
        this.to = end;
    }

    /**
     * returns the type of this relationship
     *
     * @see JEVisConstants
     * @return
     */
    @XmlElement(name = "type")
    public int getType() {
        return type;
    }

    /**
     * Set the type of this relationhsip
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }
}
