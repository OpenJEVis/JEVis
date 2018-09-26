/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement
public class JsonRelationship {
    private static final Logger logger = LogManager.getLogger(JsonRelationship.class);

    private String from;
    private String to;
    private int type;
//    private static String pathObj = "/api/rest/objects/";

    public JsonRelationship() {
    }

    public JsonRelationship(JEVisClassRelationship rel) {
        try {
            from = rel.getStart().getName();
            to = rel.getEnd().getName();
            type = rel.getType();
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

    }

    @XmlElement(name = "from")
    public String getFrom() {
//        return start;
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @XmlElement(name = "to")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @XmlElement(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "JsonRelationship{" + "from=" + from + ", to=" + to + ", type=" + type + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonRelationship that = (JsonRelationship) o;

        if (type != that.type) return false;
        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        return to != null ? to.equals(that.to) : that.to == null;
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }
}
