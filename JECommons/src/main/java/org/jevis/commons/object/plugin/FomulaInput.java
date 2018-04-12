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

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author Florian Simon
 */
public class FomulaInput implements Input {

    private JEVisObject object;
    private JEVisAttribute attribute;
    private String workflow;
    private String id;

    public FomulaInput() {
    }

    public FomulaInput(JEVisDataSource ds, JsonInput json) throws JEVisException {

        try {
            id = json.getId();

            System.out.println("Get object: " + json.getObject());
            object = ds.getObject(json.getObject());
            System.out.println("found Object: " + object);
            System.out.println("Get Attribute: " + json.getAttribute());
            attribute = object.getAttribute(json.getAttribute());
            System.out.println("done attribute: " + attribute);
            workflow = json.getWorkflow();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getWorkflowID() {
        return workflow;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public JEVisObject getObject() {
        return object;
    }

    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public void setAttribute(JEVisAttribute attribute) {
        this.attribute = attribute;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setWorkflowID(String workflow) {
        this.workflow = workflow;
    }

}
