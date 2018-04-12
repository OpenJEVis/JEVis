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

/**
 * This Class represents an JEVisType as Json
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@XmlRootElement(name = "JEVisType")
public class JsonType {

    private String name;
    private int primitiveType;
    private String guiType;
    private int GUIPosition;
    private int validity;
    private String description;
    private boolean inherited;
    private String jevisClass;

    public JsonType() {
    }

    @XmlElement(name = "jevisClass")
    public String getJevisClass() {
        return jevisClass;
    }

    public void setJevisclass(String jevisClass) {
        this.jevisClass = jevisClass;
    }
    
    

    @XmlElement(name = "inherited")
    public boolean getInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "primitiveType")
    public int getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(int primitiveType) {
        this.primitiveType = primitiveType;
    }

    @XmlElement(name = "guiType")
    public String getGuiType() {
        return guiType;
    }

    public void setGuiType(String guiType) {
        this.guiType = guiType;
    }

    @XmlElement(name = "guiPosition")
    public int getGUIPosition() {
        return GUIPosition;
    }

    public void setGUIPosition(int GUIPosition) {
        this.GUIPosition = GUIPosition;
    }

    @XmlElement(name = "validity")
    public int getValidity() {
        return validity;
    }

    public void setValidity(int validity) {
        this.validity = validity;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "JsonType{" + "name=" + name + ", primitiveType=" + primitiveType + ", GUIDisplayType=" + guiType + ", GUIPosition=" + GUIPosition + ", validity=" + validity + ", description=" + description + '}';
    }

}
