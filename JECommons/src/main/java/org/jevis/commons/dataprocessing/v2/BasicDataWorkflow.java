/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing.v2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

/**
 *
 * @author Florian Simon
 */
public class BasicDataWorkflow implements DataWorkflow {
    private static final Logger logger = LogManager.getLogger(BasicDataWorkflow.class);

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
        logger.info("!!!!!!BasicDataWorkflow.setObject: " + object.getName() + " id:" + object.getID());
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
            logger.fatal(ex);
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
            logger.info("Task is null build new");
            try {
                logger.info("Null?: " + this.workflowObject);
                logger.info("2: " + this.workflowObject.getDataSource());
                JEVisClass processorClass = this.workflowObject.getDataSource().getJEVisClass(Function.JEVIS_CLASS);
                logger.info("first DP: " + processorClass.getName());

                for (JEVisObject processor : this.workflowObject.getChildren(processorClass, true)) {
                    logger.info("whhhafirst DP: " + processor.getName());
                    //should have only one......
                    //TODO: more checks and exeption handling
                    this.tasks = DataProcessing.BuildTask(null, processor, options, this);
                }
            } catch (JEVisException ex) {
                logger.fatal(ex);
            }

        }

        return this.tasks.getResult();
    }

}
