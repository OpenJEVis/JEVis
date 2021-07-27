/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanInterval;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * change aggregation of values according to setting if possible
 *
 * @author gerrit
 */
public class AggregationAlignmentStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(AggregationAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        Map<DateTime, JEVisSample> notesMap = resourceManager.getNotesMap();
        List<CleanInterval> intervals = resourceManager.getIntervals();

        boolean downSampling = true;
        Boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

        BigDecimal offset = new BigDecimal(resourceManager.getCleanDataObject().getOffset().toString());
        List<JEVisSample> listMultipliers = resourceManager.getCleanDataObject().getMultiplier();

        /**
         * calc the sample per interval if possible depending on aggregation mode (avg or sum value)
         */

        List<CleanInterval> needToBeRemoved = new ArrayList<>();

        double lastValue = 0d;
        for (int j = 0, intervalsSize = intervals.size(); j < intervalsSize; j++) {
            CleanInterval currentInterval = intervals.get(j);
            //align the raw samples to the intervals
            Period outputPeriod = currentInterval.getOutputPeriod();
            Period inputPeriod = currentInterval.getInputPeriod();

            int compare = currentInterval.getCompare();
            int indexOfCurrentInterval = intervals.indexOf(currentInterval);
            // if clean data period is longer (e.g. 1 day) or equal than raw data period (e.g. 15 minutes)
            // the down sampling method will be used, else the other

            if (compare < 0) {
                downSampling = false;
            }

            if (compare == 0) { //no aggregation change
                JEVisSample sample = currentInterval.getResult();
                DateTime date = sample.getTimestamp();
                String note = "";
                try {
                    note = sample.getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(no)";
                } else {
                    note += ",agg(no)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                sample.setNote(note);
            } else if (downSampling && !valueIsQuantity) {

                CleanInterval lastInterval = null;
                if (indexOfCurrentInterval > 0) {
                    lastInterval = intervals.get(indexOfCurrentInterval - 1);
                }

                String note = "";
                if ((lastInterval == null || lastInterval.getRawSamples().isEmpty()) && currentInterval.isDifferential() && !currentInterval.getRawSamples().isEmpty()) {
                    if (lastInterval != null && lastInterval.getRawSamples().isEmpty()) {
                        //Should only be possible with broken timestamps, e.g. time shift for summer->winter time
                        int i = intervals.indexOf(currentInterval);
                        for (int i2 = i; i2 >= 0; i2--) {
                            CleanInterval prevInterval = intervals.get(i2);
                            if (!prevInterval.getRawSamples().isEmpty()) {
                                lastInterval = prevInterval;
                                break;
                            }
                        }
                    } else {
                        lastInterval = new CleanInterval(currentInterval.getInterval(), currentInterval.getDate());
                        lastInterval.getRawSamples().add(currentInterval.getRawSamples().get(0));
                        note += "ex-li-m";
                    }
                }

                Double valueAsDouble = calcAvgSample(currentInterval.getRawSamples(), currentInterval.isDifferential(), lastInterval);
                currentInterval.getResult().setValue(getScaledValue(listMultipliers, currentInterval.getDate(), valueAsDouble, offset).doubleValue());

                try {
                    note += currentInterval.getResult().getNote();
                } catch (Exception e) {
                    note += "";
                }
                if (note.equals("")) {
                    note += "agg(yes," + currentInterval.getRawSamples().size() + ",avg)";
                } else {
                    note += ",agg(yes," + currentInterval.getRawSamples().size() + ",avg)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(currentInterval.getResult().getTimestamp());
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                currentInterval.getResult().setNote(note);

            } else if (downSampling) {

                CleanInterval lastInterval = null;
                if (indexOfCurrentInterval > 0) {
                    lastInterval = intervals.get(indexOfCurrentInterval - 1);
                }

                String note = "";
                if ((lastInterval == null || lastInterval.getRawSamples().isEmpty()) && currentInterval.isDifferential() && !currentInterval.getRawSamples().isEmpty()) {
                    if (lastInterval != null && lastInterval.getRawSamples().isEmpty()) {
                        //Should only be possible with broken timestamps, e.g. time shift for summer->winter time
                        int i = intervals.indexOf(currentInterval);
                        for (int i2 = i; i2 >= 0; i2--) {
                            CleanInterval prevInterval = intervals.get(i2);
                            if (!prevInterval.getRawSamples().isEmpty()) {
                                lastInterval = prevInterval;
                                break;
                            }
                        }
                    } else {
                        lastInterval = new CleanInterval(currentInterval.getInterval(), currentInterval.getDate());
                        lastInterval.getRawSamples().add(currentInterval.getRawSamples().get(0));
                        note += "ex-li-m";
                    }
                }

                Double currentValue = calcSumSampleDownscale(currentInterval.getRawSamples(), currentInterval.isDifferential(), lastInterval);
                currentInterval.getResult().setValue(getScaledValue(listMultipliers, currentInterval.getDate(), currentValue, offset).doubleValue());

                try {
                    note += currentInterval.getRawSamples().get(currentInterval.getRawSamples().size() - 1).getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note.equals("")) {
                    note += "agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                } else {
                    note += ",agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(currentInterval.getResult().getTimestamp());
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                currentInterval.getResult().setNote(note);
            } else if (!downSampling && !valueIsQuantity) {

                Double nextValue = null;
                int nextAdd = 0;
                Double value = null;

                if (currentInterval.getRawSamples() == null || currentInterval.getRawSamples().isEmpty()) {

                    String note = "";
                    if (j < intervals.size() - 1) {
                        for (int i = j; i < intervals.size(); i++) {
                            if (intervals.get(i).getRawSamples() != null && !intervals.get(i).getRawSamples().isEmpty()) {
                                nextValue = intervals.get(i).getResult().getValueAsDouble();
                                note = intervals.get(i).getResult().getNote();
                                nextAdd++;
                                break;
                            }
                            nextAdd++;
                        }

                    } else needToBeRemoved.add(currentInterval);

                    if (note == null || note.equals("")) {
                        note = "agg(yes,up," + nextAdd + ",avg)";
                    } else {
                        note = note + ",agg(yes,up," + nextAdd + ",avg)";
                    }

                    if (!notesMap.isEmpty()) {
                        JEVisSample noteSample = notesMap.get(currentInterval.getResult().getTimestamp());
                        if (noteSample != null) {
                            note += ",userNotes";
                        }
                    }

                    if (nextValue != null) {
                        value = nextValue;
                        for (int i = j; i < j + nextAdd; i++) {
                            if (i < intervalsSize) {
                                JEVisSample sample1 = intervals.get(i).getResult();
                                sample1.setValue(value);
                                sample1.setNote(note);
                            }
                        }
                    }

                    if (nextAdd > 1) {
                        j += nextAdd - 1;
                    }
                }
            } else {
                Double nextValue = null;
                int nextAdd = 0;
                Double value = null;

                if (currentInterval.getRawSamples() == null || currentInterval.getRawSamples().isEmpty()) {

                    String note = "";
                    if (j < intervals.size() - 1) {
                        for (int i = j; i < intervals.size(); i++) {
                            if (intervals.get(i).getRawSamples() != null && !intervals.get(i).getRawSamples().isEmpty()) {
                                nextValue = intervals.get(i).getResult().getValueAsDouble();
                                note = intervals.get(i).getResult().getNote();
                                nextAdd++;
                                break;
                            }
                            nextAdd++;
                        }

                    } else needToBeRemoved.add(currentInterval);

                    if (note == null || note.equals("")) {
                        note = "agg(yes,up," + nextAdd + ",sum)";
                    } else {
                        note += ",agg(yes,up," + nextAdd + ",sum)";
                    }
                    if (!notesMap.isEmpty()) {
                        JEVisSample noteSample = notesMap.get(currentInterval.getResult().getTimestamp());
                        if (noteSample != null) {
                            note += ",userNotes";
                        }
                    }

                    if (nextValue != null) {
                        value = nextValue / nextAdd;
                        for (int i = j; i < j + nextAdd; i++) {
                            JEVisSample sample1 = intervals.get(i).getResult();
                            sample1.setValue(value);
                            sample1.setNote(note);
                        }
                    }
                }

                if (nextAdd > 1) {
                    j += nextAdd - 1;
                }

            }
        }

        intervals.removeAll(needToBeRemoved);
    }

    private BigDecimal getScaledValue(List<JEVisSample> listMultipliers, DateTime date, Double currentValue, BigDecimal offset) {
        BigDecimal productDec = new BigDecimal(0);

        try {
            BigDecimal rawValueDec = new BigDecimal(currentValue.toString());
            productDec = productDec.add(rawValueDec);
            productDec = productDec.multiply(ScalingStep.getCurrentMultiplier(listMultipliers, date));
            productDec = productDec.add(offset);
        } catch (Exception e) {
        }

        return productDec;
    }


    private Double calcAvgSample(List<JEVisSample> currentTmpSamples, Boolean differential, CleanInterval
            lastInterval) throws Exception {
        Double value = 0.0;
        if (!differential) {
            for (JEVisSample sample : currentTmpSamples) {
                Double valueAsDouble = sample.getValueAsDouble();
                value += valueAsDouble;
            }
        } else {
            Double lastValue = lastInterval.getRawSamples().get(lastInterval.getRawSamples().size() - 1).getValueAsDouble();
            List<Double> tmpValues = new ArrayList<>();

            for (JEVisSample sample : currentTmpSamples) {
                Double valueAsDouble = sample.getValueAsDouble() - lastValue;
                tmpValues.add(valueAsDouble);
                lastValue = sample.getValueAsDouble();
            }

            for (Double d : tmpValues) {
                value += d;
            }
        }
        return (value / currentTmpSamples.size());
    }

    private Double calcSumSampleDownscale(List<JEVisSample> currentTmpSamples, Boolean
            differential, CleanInterval lastInterval) throws Exception {
        double value = 0.0;
        if (currentTmpSamples.size() > 0) {
            if (!differential) {
                for (JEVisSample sample : currentTmpSamples) {
                    value += sample.getValueAsDouble();
                }
            } else {
                Double lastValue = currentTmpSamples.get(currentTmpSamples.size() - 1).getValueAsDouble();
                Double firstValue = lastInterval.getRawSamples().get(lastInterval.getRawSamples().size() - 1).getValueAsDouble();
                value = lastValue - firstValue;
            }
        }
        return value;
    }
}
