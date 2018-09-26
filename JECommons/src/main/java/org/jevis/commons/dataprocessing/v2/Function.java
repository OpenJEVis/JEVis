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

import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;

import java.util.List;

/**
 *
 * @author Florian Simon
 */
public interface Function {

    String JEVIS_CLASS = "Data Processor";
    String ATTRIBUTE_ID = "ID";
    String ATTRIBUTE_DEBUG_LEVEL = "Debug Level";

    //mybe its not the best to allow the implmentation to choose this
    String getID();

    Result getResult();

    void setOptions(JEVisOption option);

    void setObject(JEVisObject object);

    void setInput(List<Result> results);

    /**
     * Set the workflow this Data Processor belongs to
     *
     * @param workflow
     */
    void setWorkflow(DataWorkflow workflow);

//    void setLogLevel(Level level);
}
