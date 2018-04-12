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
package org.jevis.commons.dataprocessing.v2;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;

/**
 *
 * @author Florian Simon
 */
public class BasicDataWorkflow implements DataWorkflow {

    private JEVisOption options;
    private JEVisObject workflowObject;
    private Task tasks;
    private JEVisAttribute attribute;

    public BasicDataWorkflow() {
    }

    @Override
    public JEVisAttribute getAttribute() {
        return this.attribute;
    }

    @Override
    public void setAttribute(JEVisAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public JEVisObject getWorkflowObject() {
        return this.workflowObject;
    }

    @Override
    public void setObject(JEVisObject object) {
        System.out.println("!!!!!!BasicDataWorkflow.setObject: " + object.getName() + " id:" + object.getID());
        this.workflowObject = object;
    }

    @Override
    public String getID() {
        return this.workflowObject.getName();
    }

    @Override
    public String getDescription() {
        try {
            return this.workflowObject.getAttribute(ATTRIBUTE_DESCRIPTION).getLatestSample().getValueAsString();
        } catch (JEVisException ex) {
            Logger.getLogger(BasicDataWorkflow.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    @Override
    public JEVisOption getOption() {
        return this.options;
    }

    @Override
    public void setOption(JEVisOption options) {
        this.options = options;
    }

    @Override
    public Result getResult() {
        if (tasks == null) {
            System.out.println("Task is null build new");
            try {
                System.out.println("Null?: " + this.workflowObject);
                System.out.println("2: " + this.workflowObject.getDataSource());
                JEVisClass processorClass = this.workflowObject.getDataSource().getJEVisClass(Function.JEVIS_CLASS);
                System.out.println("first DP: " + processorClass.getName());

                for (JEVisObject processor : this.workflowObject.getChildren(processorClass, true)) {
                    System.out.println("whhhafirst DP: " + processor.getName());
                    //should have only one......
                    //TODO: more checks and exeption handling
                    this.tasks = DataProcessing.BuildTask(null, processor, options, this);
                }
            } catch (JEVisException ex) {
                Logger.getLogger(BasicDataWorkflow.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("hgggggggggggggg");
                ex.printStackTrace();
            }

        }

        return this.tasks.getResult();
    }

}
