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
import java.util.ArrayList;
import java.util.List;

/**
 * This Class is used to represents an JEVisObject in JSON by the WebService
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@XmlRootElement(name = "Object")
public class JsonObject {

    private String name;
    private long id = -1;
    private String jevisClass;
    private long parent;
    private List<JsonRelationship> relationships;
    private List<JsonObject> objects;
    private List<JsonAttribute> attributes;
    private boolean isPublic;

    public JsonObject() {
    }

    @XmlElement(name = "attributes")
    public List<JsonAttribute> getAttributes() {
        if(attributes==null){
            attributes= new  ArrayList<>();
        }
        return attributes;
    }

    public void setAttributes(List<JsonAttribute> attributes) {
        this.attributes = attributes;
    }

    @XmlElement(name = "objects")
    public List<JsonObject> getObjects() {
        return objects;
    }

    public void setObjects(List<JsonObject> objects) {
        this.objects = objects;
    }

    /**
     * Returns the name of this JEVisObject
     *
     * @return name of the JEVisObject
     */
    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    /**
     * Set the name of the JEVisObject
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the JEVisObject.
     *
     * @return the unique id of this JEVisObject
     */
    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    /**
     * Set the name of the JEVisObject. The ID will be give by the Database and
     * will be ignored in the most cases.
     *
     * @param id Unique identifier of this JEVisObject
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns true is the object has public access
     * @return
     */
    @XmlElement(name = "isPublic")
    public boolean getisPublic() {
        return isPublic;
    }

    /**
     * Set if the objetc allows public access
     * @param isPublic
     */
    public void setisPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Returns the JEVisClass of this JEVisObject.
     *
     * @return JEVisClass of this JEVisObject.
     */
    @XmlElement(name = "jevisClass")
    public String getJevisClass() {
        return jevisClass;
    }

    /**
     * Set the JEVisClass of this JEVisObject.
     *
     * @param jevisclass
     */
    public void setJevisClass(String jevisClass) {
        this.jevisClass = jevisClass;
    }

    /**
     * Returns an list of all JEVisRelationships
     *
     * @return list of all JEVisRelationships
     */
    @XmlElement(name = "relationships")
    public List<JsonRelationship> getRelationships() {
        if(relationships==null){
            relationships=  new ArrayList<>();
        }
        return relationships;
    }

    /**
     * Set the List of JEVisRelationships.
     *
     *
     * @param relations
     */
    public void setRelationships(List<JsonRelationship> relationships) {
        this.relationships = relationships;
    }

    /**
     * Returns the Parent of this JEVisObject.
     *
     * @return Parent of this JEVisObject.
     */
    @XmlElement(name = "parent")
    public long getParent() {
        return parent;
    }

    /**
     * Set the Parent of this JEVisObject.
     *
     * @param parent
     */
    public void setParent(long parent) {
        this.parent = parent;
    }


    @Override
    public String toString() {
        return "JsonObject{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", jevisClass='" + jevisClass + '\'' +
                ", parent=" + parent +
                ", relationships=" + relationships +
                ", objects=" + objects +
                ", attributes=" + attributes +
                ", isPublic=" + isPublic +
                '}';
    }
}
