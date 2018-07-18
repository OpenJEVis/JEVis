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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This Class represents an JEVisClass as Json
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "JEVisClass")
public class JsonJEVisClass {

    private String name;
    private String description;
//    private String inheritance;
    private boolean unique;
    private List<JsonClassRelationship> relationships= new ArrayList<>();
    private List<JsonType> types = new ArrayList<>();

    public JsonJEVisClass() {
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "types")
    public List<JsonType> getTypes() {
        return types;
    }

    public void setTypes(List<JsonType> types) {
        this.types = types;
    }

    
    
    
//    @XmlElement(name = "inheritance")
//    public String getInheritance() {
//        return inheritance;
//    }
//
//    public void setInheritance(String inheritance) {
//        this.inheritance = inheritance;
//    }
    @XmlElement(name = "unique")
    public boolean getUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @XmlElement(name = "relationships")
    public List<JsonClassRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<JsonClassRelationship> relationships) {
        this.relationships = relationships;
    }

    @Override
    public String toString() {
        return "JsonJEVisClass{" + "name=" + name + ", description=" + description + ", unique=" + unique + ", relationships=" + relationships + '}';
    }

}
