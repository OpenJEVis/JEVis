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
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.ProcessOption;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.ProcessFunction;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.VirtualAttribute;
import org.jevis.commons.dataprocessing.VirtuelSample;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ConverterFunction implements ProcessFunction {

    public final static String NAME = "Converter";
    public static final String MULTIPLAYER = "multiplier";
    public static final String OFFSET = "offset";
    private List<JEVisSample> _result;

    @Override
    public void resetResult() {
        _result = null;
    }

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        if (_result != null) {
            return _result;
        } else {
            _result = new ArrayList<>();

            double m = 1;
            double b = 0;

            m = Double.parseDouble(ProcessOptions.GetLatestOption(mainTask, MULTIPLAYER, new BasicProcessOption(MULTIPLAYER, "1")).getValue());//TYPO MULTIPLAYER
            b = Double.parseDouble(ProcessOptions.GetLatestOption(mainTask, OFFSET, new BasicProcessOption(OFFSET, "0")).getValue());

            if (mainTask.getSubProcesses().size() != 1) {
                System.out.println("Waring Counter processor can only handel one input. using first only!");
            }

            System.out.println("Using M:" + m + "  B:" + b);
            for (JEVisSample sample : mainTask.getSubProcesses().get(0).getResult()) {

                try {
                    double sum = (sample.getValueAsDouble() * m) + b;
//                    System.out.println("TS: " + sample.getTimestamp() + " new Value: " + sum);
                    _result.add(new VirtuelSample(sample.getTimestamp(), sum, mainTask.getJEVisDataSource(), new VirtualAttribute(null)));
                } catch (JEVisException ex) {
                    Logger.getLogger(ConverterFunction.class.getName()).log(Level.SEVERE, null, ex);
                }
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

        options.add(new BasicProcessOption(MULTIPLAYER));
        options.add(new BasicProcessOption(OFFSET));

        return options;
    }

}
