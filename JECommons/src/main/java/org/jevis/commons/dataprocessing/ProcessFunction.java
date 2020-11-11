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
package org.jevis.commons.dataprocessing;

import org.jevis.api.JEVisSample;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;

import java.util.List;

/**
 * An ProcessFunction can process the samples from an task an return the result.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface ProcessFunction {

    /**
     * Rertuns the result list from this processor.
     *
     * @param task Task with the options and previous tasks
     * @return List of JEVisSamples with the result
     */
    List<JEVisSample> getResult(Process task);

    /**
     * Returns the unique name of this processor wich is used to identify it.
     *
     * @return unique name
     */
    String getName();

    /**
     * Request an reset od the result
     */
    void resetResult();

    List<ProcessOption> getAvailableOptions();

    List<JsonSample> getJsonResult(BasicProcess basicProcess);

    void setJsonSampleGenerator(JsonSampleGenerator jsonSampleGenerator);
}
