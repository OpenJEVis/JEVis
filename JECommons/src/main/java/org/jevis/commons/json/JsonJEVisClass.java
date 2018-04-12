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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "Class")
public class JsonJEVisClass {

    private String name;
    private String description;
    private String inheritance;
    private boolean unique;
    @XmlElementWrapper(name = "JsonType")
    private List<JsonType> types;
    @XmlElementWrapper(name = "JsonRelationship")
    private List<JsonRelationship> relationships;
    JEVisClass jclass;

    public JsonJEVisClass() {

    }

    public JsonJEVisClass(JEVisClass jclass) {
        try {
            name = jclass.getName();
            description = jclass.getDescription();
            if (jclass.getInheritance() != null) {
                inheritance = jclass.getInheritance().getName();
            }
            unique = jclass.isUnique();

            types = new ArrayList<>();
            for (JEVisType type : jclass.getTypes()) {
                types.add(new JsonType(type));
            }

            relationships = new ArrayList<>();
            for (JEVisClassRelationship rel : jclass.getRelationships()) {
                relationships.add(new JsonRelationship(rel));
            }

        } catch (JEVisException ex) {
            Logger.getLogger(JsonJEVisClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @XmlElement(name = "relationships")
    public List<JsonRelationship> getRelationships() {
        return relationships;
    }

    @XmlElementWrapper(name = "JsonRelationship")
    public void setRelationships(List<JsonRelationship> relationships) {
//        System.out.println("setrelationship: " + relationships);
        this.relationships = relationships;
    }

    @XmlElement(name = "types")
    public List<JsonType> getTypes() {
        return types;
    }

    @XmlElementWrapper(name = "JsonType")
    public void setTypes(List<JsonType> types) {
        this.types = types;
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

    @XmlElement(name = "inheritance")
    public String getInheritance() {
        return inheritance;
    }

    public void setInheritance(String inheritance) {
        this.inheritance = inheritance;
    }

    @XmlElement(name = "unique")
    public boolean getUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    @Override
    public String toString() {
        return "JsonJEVisClass{" + "name=" + name + ", description=" + description + ", inheritance=" + inheritance + ", unique=" + unique + ", types=" + types + ", relationships=" + relationships + ", jclass=" + jclass + '}';
    }

}
