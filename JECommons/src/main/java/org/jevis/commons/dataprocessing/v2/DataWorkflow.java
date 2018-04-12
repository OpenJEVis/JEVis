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
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;

/**
 *
 * @author Florian Simon
 */
public interface DataWorkflow {

    public static final String DATA_WORKFLOW_CLASS = "Data Workflow";
    public static final String ATTRIBUTE_DESCRIPTION = "Description";

    String getID();

    void setAttribute(JEVisAttribute attribute);

    JEVisAttribute getAttribute();

    void setOption(JEVisOption options);

    JEVisOption getOption();

    String getDescription();

    void setObject(JEVisObject object);

    JEVisObject getWorkflowObject();

    Result getResult();

}
