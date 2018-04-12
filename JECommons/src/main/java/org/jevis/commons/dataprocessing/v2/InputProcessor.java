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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;

/**
 *
 * @author Florian Simon
 */
public class InputProcessor implements Function {

    private JEVisObject dpObject;
    private JEVisOption option;
    private List<Result> inputs;
    private DataWorkflow workflow;

    @Override
    public String getID() {
        return DataProcessing.GetDataProcessorID(dpObject);
    }

    @Override
    public void setWorkflow(DataWorkflow workflow) {
        this.workflow = workflow;
    }

    @Override
    public Result getResult() {
        Result result = new BasicResult();
        List<JEVisSample> samples = new ArrayList<>();

//        JEVisObject sourceObject;
        try {
            JEVisAttribute sourceAttribute = this.workflow.getAttribute();
//            sourceObject = dpObject.getDataSource().getObject(1484l);
//            sourceAttribute = sourceObject.getAttribute("Value");

            samples = sourceAttribute.getAllSamples();

        } catch (Exception ex) {
            ex.printStackTrace();

        }

        System.out.println("sample.size: " + samples.size());
        result.setSamples(samples);
        return result;
    }

    @Override
    public void setOptions(JEVisOption option) {
        this.option = option;
    }

    @Override
    public void setObject(JEVisObject object) {
        this.dpObject = object;
    }

    @Override
    public void setInput(List<Result> results) {
        this.inputs = results;
    }

}
