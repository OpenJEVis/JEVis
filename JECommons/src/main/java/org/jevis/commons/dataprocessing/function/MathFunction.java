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
package org.jevis.commons.dataprocessing.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.dataprocessing.Process;
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class MathFunction implements ProcessFunction {
    private static final Logger logger = LogManager.getLogger(MathFunction.class);
    private final String AVERAGE = "average";
    private final String MIN = "min";
    private final String MAX = "max";
    private final String MEDIAN = "median";
    private final String RUNNINGMEAN = "runningmean";

    public static final String NAME = "Math Processor";
    private final String mode;

    public MathFunction(String mode) {
        this.mode = mode;
    }

    @Override
    public void resetResult() {
    }

    @Override
    public List<JEVisSample> getResult(Process mainTask) {
        logger.info("get Result for " + mainTask.getID() + " with function " + mode);
        List<JEVisSample> result = new ArrayList<>();

        List<JEVisSample> allSamples = new ArrayList<>();
        for (Process task : mainTask.getSubProcesses()) {
            allSamples.addAll(task.getResult());
        }

        Boolean hasSamples = null;
        Double value = 0d;
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        List<Double> listMedian = new ArrayList<>();
        JEVisUnit unit = null;
        DateTime dateTime = null;

        List<JEVisSample> listRunningMean = new ArrayList<>();

        if (mode.equals(AVERAGE) || mode.equals(MIN) || mode.equals(MAX) || mode.equals(MEDIAN)) {
            for (JEVisSample smp : allSamples) {
                try {
                    Double currentValue = smp.getValueAsDouble();
                    value += currentValue;
                    min = Math.min(min, currentValue);
                    max = Math.max(max, currentValue);
                    listMedian.add(currentValue);

                    if (hasSamples == null) hasSamples = true;
                    if (unit == null) unit = smp.getUnit();
                    if (dateTime == null) dateTime = smp.getTimestamp();
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        } else if (mode.equals(RUNNINGMEAN)) {
            if (allSamples.size() > 3) {
                double lastValue;
                for (int i = 0; i < allSamples.size(); i++) {
                    if (i % 3 == 0) {
                        try {
                            Double currentValue =
                                    1 / 3 * (allSamples.get(i - 2).getValueAsDouble()
                                            + allSamples.get(i - 1).getValueAsDouble()
                                            + allSamples.get(i).getValueAsDouble());

                            DateTime newTS = allSamples.get(i - 1).getTimestamp();


                            if (unit == null) unit = allSamples.get(i).getUnit();
                            JEVisSample smp = new VirtualSample(newTS, currentValue, unit);
                            listRunningMean.add(smp);

                            if (hasSamples == null) hasSamples = true;

                        } catch (JEVisException ex) {
                            logger.fatal(ex);
                        }
                    }
                }
            }
        }

        if (hasSamples) {
            if (mode.equals(AVERAGE)) {
                value = value / (double) allSamples.size();
            } else if (mode.equals(MIN)) {
                value = min;
            } else if (mode.equals(MAX)) {
                value = max;
            } else if (mode.equals(MEDIAN)) {
                if (listMedian.size() > 1)
                    listMedian.sort(Comparator.naturalOrder());
                value = listMedian.get((listMedian.size() - 1) / 2);
            }
        }

        if (!mode.equals(RUNNINGMEAN))
            result.add(new VirtualSample(dateTime, value, unit, mainTask.getJEVisDataSource(), new VirtualAttribute(null)));
        else {
            result.addAll(listRunningMean);
        }
        logger.info("Result is: " + dateTime + " : " + value + " " + UnitManager.getInstance().formate(unit));

        return result;

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
