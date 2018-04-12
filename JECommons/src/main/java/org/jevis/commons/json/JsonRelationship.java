/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.json;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement
public class JsonRelationship {

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
            Logger.getLogger(JsonRelationship.class.getName()).log(Level.SEVERE, null, ex);
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

}
