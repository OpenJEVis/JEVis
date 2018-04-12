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
package org.jevis.commons.dataprocessing.function;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.ProcessOption;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.Process;
import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class InputFunction implements ProcessFunction {

    public final static String NAME = "Input";
    public final static String OBJECT_ID = "object-id";
    public final static String ATTRIBUTE_ID = "attribute-id";
    private List<JEVisSample> _result = null;

    public InputFunction() {
    }

    public InputFunction(List<JEVisSample> resultSamples) {
        _result = resultSamples;
    }

    @Override
    public void resetResult() {
        _result = null;
    }

    @Override
    public List<JEVisSample> getResult(Process task) {
        if (_result != null) {
            return _result;
        } else {
            _result = new ArrayList<>();

            JEVisObject object = null;
            if (ProcessOptions.ContainsOption(task, OBJECT_ID)) {
                long oid = Long.valueOf((ProcessOptions.GetLatestOption(task, OBJECT_ID, new BasicProcessOption(OBJECT_ID, "")).getValue()));
                try {
                    object = task.getJEVisDataSource().getObject(oid);
                } catch (JEVisException ex) {
                    Logger.getLogger(InputFunction.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (task.getObject() != null) {
                try {
                    object = task.getObject().getParents().get(0);//TODO make save
                } catch (JEVisException ex) {
                }
            }

            if (object != null && ProcessOptions.ContainsOption(task, ATTRIBUTE_ID)) {

                try {
                    System.out.println("Parent object: " + object);
//                    long oid = Long.valueOf(task.getOptions().get(OBJECT_ID));
//                    JEVisObject object = task.getJEVisDataSource().getObject(oid);

                    JEVisAttribute att = object.getAttribute(ProcessOptions.GetLatestOption(task, ATTRIBUTE_ID, new BasicProcessOption(ATTRIBUTE_ID, "")).getValue());

                    DateTime[] startEnd = ProcessOptions.getStartAndEnd(task);
                    System.out.println("start: " + startEnd[0] + " end: " + startEnd[1]);

                    _result = att.getSamples(startEnd[0], startEnd[1]);
                    System.out.println("Input result: " + _result.size());
                } catch (JEVisException ex) {
                    Logger.getLogger(InputFunction.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("Missing options " + OBJECT_ID + " and " + ATTRIBUTE_ID);
            }
        }
        return _result;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<ProcessOption> getAvailableOptions() {
        List<ProcessOption> options = new ArrayList<>();

        options.add(new BasicProcessOption("Object"));
        options.add(new BasicProcessOption("Attribute"));
        options.add(new BasicProcessOption("Workflow"));

        return options;
    }
}
