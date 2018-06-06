/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEWebService.
 * <p>
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.json;

import org.jevis.api.JEVisConstants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Class is used to represents an JEVisRelationship in JSON by the
 * WebService
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement
public class JsonClassRelationship {

    private String start;
    private String end;
    private int type;

    public JsonClassRelationship() {
    }

    /**
     * Return the start JEVisObject in this relationship.
     *
     * @return ID if the left object in the relationship
     */
    @XmlElement(name = "from")
    public String getStart() {
        return start;
    }

    /**
     * Set the start JEVisObject in this relationship.
     *
     * @param start
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * Returns the end JEVisObject in this relationship.
     *
     * @return
     */
    @XmlElement(name = "to")
    public String getEnd() {
        return end;
    }

    /**
     * Set the end JEVisObject in this relationship.
     *
     * @param end
     */
    public void setEnd(String end) {
        this.end = end;
    }

    /**
     * returns the type of this relationship
     *
     * @return
     * @see JEVisConstants
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonClassRelationship that = (JsonClassRelationship) o;

        if (type != that.type) return false;
        if (!start.equals(that.start)) return false;
        return end.equals(that.end);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + type;
        return result;
    }
}
