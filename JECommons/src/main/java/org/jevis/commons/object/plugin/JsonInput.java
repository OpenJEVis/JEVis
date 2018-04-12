/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.object.plugin;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO to transform an Input into json.
 *
 * @author Florian Simon
 */
@XmlRootElement(name = "Input")
public class JsonInput {

    private long object;
    private String attribute;
    private String workflow;
    private String id;

    @XmlElement(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElement(name = "object")
    public long getObject() {
        return object;
    }

    @XmlElement(name = "attribute")
    public String getAttribute() {
        return attribute;
    }

    @XmlElement(name = "workflow")
    public String getWorkflow() {
        return workflow;
    }

    public void setObject(long object) {
        this.object = object;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

}
