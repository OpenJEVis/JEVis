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
package org.jevis.commons.json;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@XmlRootElement(name = "Object")
public class JsonObject {

    private String name;
    private long id;
    private String jevisclass;
    private long parent;
    private List<JsonAttribute> attributes = new ArrayList<JsonAttribute>();
    private List<JsonObject> children = new ArrayList<JsonObject>();
//    private List<JsonRelationship> relations;

    public JsonObject() {
    }

    @XmlElement(name = "parent")
    public long getParent() {
        return parent;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @XmlElement(name = "class")
    public String getJevisClass() {
        return jevisclass;
    }

    public void setJevisClass(String jevisclass) {
        this.jevisclass = jevisclass;
    }

//    public List<JsonRelationship> getRelations() {
//        return relations;
//    }
//
//    public void setRelations(List<JsonRelationship> relations) {
//        this.relations = relations;
//    }
    @XmlElement(name = "attributes")
    public List<JsonAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<JsonAttribute> attributes) {
        this.attributes = attributes;
    }

    @XmlElement(name = "children")
    public List<JsonObject> getChildren() {
        return children;
    }

    public void setChildren(List<JsonObject> children) {
        this.children = children;
    }

}
