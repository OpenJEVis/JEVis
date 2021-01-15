/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.CleanIntervalN;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManagerN;
import org.jevis.commons.datetime.PeriodComparator;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * change aggregation of values according to setting if possible
 *
 * @author gerrit
 */
public class AggregationAlignmentStepN implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(AggregationAlignmentStepN.class);

    @Override
    public void run(ResourceManagerN resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        Map<DateTime, JEVisSample> notesMap = resourceManager.getNotesMap();
        List<CleanIntervalN> intervals = resourceManager.getIntervals();

        boolean downSampling = true;
        Boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

        PeriodComparator periodComparator = new PeriodComparator();

        /**
         * calc the sample per interval if possible depending on aggregation mode (avg or sum value)
         */

        for (CleanIntervalN currentInterval : intervals) {
            //align the raw samples to the intervals
            Period outputPeriod = currentInterval.getOutputPeriod();
            Period inputPeriod = currentInterval.getInputPeriod();

            int compare = periodComparator.compare(outputPeriod, inputPeriod);
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
                DateTime date = currentInterval.getDate();

                CleanIntervalN lastInterval = null;
                if (indexOfCurrentInterval > 0) {
                    lastInterval = intervals.get(indexOfCurrentInterval - 1);
                }

                Double valueAsDouble = calcAvgSample(currentInterval.getRawSamples(), currentInterval.isDifferential(), lastInterval);

                currentInterval.getResult().setTimeStamp(date);
                currentInterval.getResult().setValue(valueAsDouble);
                String note = "";
                try {
                    note = currentInterval.getResult().getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentInterval.getRawSamples().size() + ",avg)";
                } else {
                    note += ",agg(yes," + currentInterval.getRawSamples().size() + ",avg)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                currentInterval.getResult().setNote(note);

            } else if (downSampling) {

                CleanIntervalN lastInterval = null;
                if (indexOfCurrentInterval > 0) {
                    lastInterval = intervals.get(indexOfCurrentInterval - 1);
                }

                Double currentValue = calcSumSampleDownscale(currentInterval.getRawSamples(), currentInterval.isDifferential(), lastInterval);

                DateTime date = currentInterval.getDate();
                currentInterval.getResult().setTimeStamp(date);
                currentInterval.getResult().setValue(currentValue);
                String note = "";
                try {
                    note = currentInterval.getRawSamples().get(currentInterval.getRawSamples().size() - 1).getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                } else {
                    note += ",agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                currentInterval.getResult().setNote(note);
            } else if (!downSampling) {

                boolean periodRawHasMonths = inputPeriod.getMonths() > 0;
                boolean periodRawHasYear = inputPeriod.getYears() > 0;
                boolean periodRawHasDays = inputPeriod.getDays() > 0;
                boolean periodRawHasHours = inputPeriod.getHours() > 0;
                boolean periodRawHasMinutes = inputPeriod.getMinutes() > 0;
                boolean periodRawHasSeconds = inputPeriod.getSeconds() > 0;

                boolean periodCleanHasMonths = outputPeriod.getMonths() > 0;
                boolean periodCleanHasYear = outputPeriod.getYears() > 0;
                boolean periodCleanHasDays = outputPeriod.getDays() > 0;
                boolean periodCleanHasHours = outputPeriod.getHours() > 0;
                boolean periodCleanHasMinutes = outputPeriod.getMinutes() > 0;
                boolean periodCleanHasSeconds = outputPeriod.getSeconds() > 0;

                if (!periodRawHasMonths && !periodRawHasYear && !periodCleanHasMonths && !periodCleanHasYear) {

                    List<JEVisSample> list = new ArrayList<>();
                    for (CleanIntervalN ci : intervals) {
                        list.add(ci.getResult());
                    }

                    for (JEVisSample sample : list) {
                        int index = list.indexOf(sample);

                        Double previousValue = null;
                        Double nextValue = null;
                        int nextAdd = 0;
                        Double value = null;

                        if (sample.getValue() == null) {
                            if (index > 0) {
                                previousValue = list.get(index - 1).getValueAsDouble();
                            }

                            String note = "";
                            if (index < list.size() - 1) {
                                for (int i = index; i < list.size(); i++) {
                                    if (list.get(i).getValueAsDouble() != null) {
                                        nextValue = list.get(i).getValueAsDouble();
                                        note = list.get(i).getNote();
                                        nextAdd++;
                                        break;
                                    }
                                    nextAdd++;
                                }

                            }

                            if (nextValue != null && valueIsQuantity) {
                                value = nextValue / nextAdd;
                                for (int i = index; i < index + nextAdd; i++) {
                                    JEVisSample sample1 = list.get(i);
                                    sample1.setValue(value);
                                    if (note == null || note.equals("")) {
                                        note = "agg(yes,up," + nextAdd + ")";
                                    } else {
                                        note = note + ",agg(yes,up," + nextAdd + ")";
                                    }
                                    sample1.setNote(note);
                                }
                            } else if (previousValue != null && nextValue != null) {
                                value = (previousValue + nextValue) / 2;
                                for (int i = index - 1; i < index + nextAdd; i++) {
                                    JEVisSample sample1 = list.get(i);
                                    sample1.setValue(value);
                                    if (note == null || note.equals("")) {
                                        note = "agg(yes,up," + (nextAdd + 1) + ")";
                                    } else {
                                        note = note + ",agg(yes,up," + (nextAdd + 1) + ")";
                                    }
                                    sample1.setNote(note);
                                }
                            }
                        }
                    }

                    HashMap<DateTime, JEVisSample> map = new HashMap<>();
                    for (JEVisSample jeVisSample : list) {
                        try {
                            map.put(jeVisSample.getTimestamp(), jeVisSample);
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    }

                    for (CleanIntervalN ci : intervals) {
                        JEVisSample sample = ci.getResult();
                        JEVisSample sample1 = map.get(ci.getDate());
                        if (sample1 != null) {
                            sample.setValue(sample1.getValueAsDouble());
                            sample.setNote(sample1.getNote());
                        }
                    }
                } else {
                    //TODO: implement periods greater than months
                }
            } else {
                JEVisSample sample1 = currentInterval.getRawSamples().get(currentInterval.getRawSamples().size() - 1);
                Double currentValue = sample1.getValueAsDouble();

                DateTime date = currentInterval.getDate();
                currentInterval.getResult().setTimeStamp(date);
                currentInterval.getResult().setValue(currentValue);
                String note = "";
                try {
                    note = sample1.getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                } else {
                    note += ",agg(yes," + currentInterval.getRawSamples().size() + ",sum)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                currentInterval.getResult().setNote(note);
            }
        }
    }


    private Double calcAvgSample(List<JEVisSample> currentTmpSamples, Boolean differential, CleanIntervalN lastInterval) throws Exception {
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

    private Double calcSumSampleDownscale(List<JEVisSample> currentTmpSamples, Boolean differential, CleanIntervalN lastInterval) throws Exception {
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
