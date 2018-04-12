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
import org.jevis.api.JEVisObject;

/**
 * Interface for an Attributes relationship. If finised this may move into the
 * JEAPI in some form.
 *
 * @author Florian Simon
 */
public interface Input {

    /**
     * Get the ID of the workflow to use.
     *
     * Emty for no workflow, Default for the default worklow.
     *
     * @return
     */
    String getWorkflowID();

    /**
     * Get the id of this input. This id is unique identifier the seperate
     * inputs.
     *
     * @return unique identifier
     */
    String getID();

    /**
     * Get the JEVisObject
     *
     * @return
     */
    JEVisObject getObject();

    /**
     * Get the JEVisAttribute
     *
     * @return
     */
    JEVisAttribute getAttribute();

    void setWorkflowID(String workflow);

}
