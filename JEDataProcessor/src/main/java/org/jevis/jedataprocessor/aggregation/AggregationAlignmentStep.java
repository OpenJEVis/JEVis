/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.aggregation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.jedataprocessor.data.CleanInterval;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.workflow.ProcessStep;
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
public class AggregationAlignmentStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(AggregationAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();

        Map<DateTime, JEVisSample> notesMap = resourceManager.getNotesMap();
        List<CleanInterval> intervals = resourceManager.getIntervals();

        //align the raw samples to the intervals
        Period periodCleanData = cleanDataObject.getCleanDataPeriodAlignment();
        Period periodRawData = cleanDataObject.getRawDataPeriodAlignment();


        boolean downSampling = true;
        Boolean valueIsQuantity = cleanDataObject.getValueIsQuantity();

        PeriodComparator periodComparator = new PeriodComparator();
        int compare = periodComparator.compare(periodCleanData, periodRawData);
        // if clean data period is longer (e.g. 1 day) or equal than raw data period (e.g. 15 minutes)
        // the down sampling method will be used, else the other

        if (compare < 0) {
            downSampling = false;
        }

        if (!downSampling) {

            boolean periodRawHasMonths = periodRawData.getMonths() > 0;
            boolean periodRawHasYear = periodRawData.getYears() > 0;
            boolean periodRawHasDays = periodRawData.getDays() > 0;
            boolean periodRawHasHours = periodRawData.getHours() > 0;
            boolean periodRawHasMinutes = periodRawData.getMinutes() > 0;
            boolean periodRawHasSeconds = periodRawData.getSeconds() > 0;

            boolean periodCleanHasMonths = periodCleanData.getMonths() > 0;
            boolean periodCleanHasYear = periodCleanData.getYears() > 0;
            boolean periodCleanHasDays = periodCleanData.getDays() > 0;
            boolean periodCleanHasHours = periodCleanData.getHours() > 0;
            boolean periodCleanHasMinutes = periodCleanData.getMinutes() > 0;
            boolean periodCleanHasSeconds = periodCleanData.getSeconds() > 0;

            if (!periodRawHasMonths && !periodRawHasYear && !periodCleanHasMonths && !periodCleanHasYear) {

                List<JEVisSample> list = new ArrayList<>();
                String lastNote = null;
                for (CleanInterval ci : intervals) {
                    if (!ci.getTmpSamples().isEmpty()) {
                        Double value = ci.getTmpSamples().get(ci.getTmpSamples().size() - 1).getValueAsDouble();
                        lastNote = ci.getTmpSamples().get(ci.getTmpSamples().size() - 1).getNote();
                        JEVisSample virtualSample = new VirtualSample(ci.getDate(), value);
                        virtualSample.setNote(lastNote);
                        list.add(virtualSample);
                    } else {
                        Double value = null;
                        JEVisSample virtualSample = new VirtualSample(ci.getDate(), value);
                        if (lastNote != null) {
                            virtualSample.setNote(lastNote);
                        }
                        list.add(virtualSample);
                        ci.addTmpSample(virtualSample);
                    }
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

                for (CleanInterval ci : intervals) {
                    for (JEVisSample sample : ci.getTmpSamples()) {
                        JEVisSample sample1 = map.get(ci.getDate());
                        if (sample1 != null) {
                            sample.setValue(sample1.getValueAsDouble());
                            sample.setNote(sample1.getNote());
                        }
                    }
                }
            } else {
                //TODO: implement periods greater than months
            }
        }


        /**
         * calc the sample per interval if possible depending on aggregation mode (avg or sum value)
         */

        for (CleanInterval currentInterval : intervals) {
            //logger.info("align {},last {}, sum {}, avg {}", cleanDataObject.getIsPeriodAligned(), last, sum, avg);
            List<JEVisSample> currentTmpSamples = currentInterval.getTmpSamples();
            if (currentTmpSamples.isEmpty()) {
                continue;
            }

            if (compare == 0) { //no aggregation change
                JEVisSample sample = currentTmpSamples.get(currentTmpSamples.size() - 1);
//                        Double valueAsDouble = sample.getValueAsDouble();
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
//                        JEVisSample sample = new VirtualSample(date, valueAsDouble);
                sample.setNote(note);
//                        currentInterval.addTmpSample(sample);
            } else if (downSampling && !valueIsQuantity) {
                DateTime date = currentInterval.getDate();

                Double valueAsDouble = calcAvgSample(currentTmpSamples);

                JEVisSample sample = new VirtualSample(date, valueAsDouble);
                String note = "";
                try {
                    note = currentTmpSamples.get(currentTmpSamples.size() - 1).getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentTmpSamples.size() + ",last)";
                } else {
                    note += ",agg(yes," + currentTmpSamples.size() + ",last)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                sample.setNote(note);
                currentInterval.addTmpSample(sample);

            } else if (downSampling) {

                Double currentValue = calcSumSampleDownscale(currentTmpSamples);

                DateTime date = currentInterval.getDate();
                JEVisSample sample = new VirtualSample(date, currentValue);
                String note = "";
                try {
                    note = currentTmpSamples.get(currentTmpSamples.size() - 1).getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentTmpSamples.size() + ",sum)";
                } else {
                    note += ",agg(yes," + currentTmpSamples.size() + ",sum)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                sample.setNote(note);
                currentInterval.getTmpSamples().clear();
                currentInterval.addTmpSample(sample);
            } else {
                JEVisSample sample1 = currentTmpSamples.get(currentTmpSamples.size() - 1);
                Double currentValue = sample1.getValueAsDouble();

                DateTime date = currentInterval.getDate();
                JEVisSample sample = new VirtualSample(date, currentValue);
                String note = "";
                try {
                    note = sample1.getNote();
                } catch (Exception e) {
                    note = "";
                }
                if (note == null || note.equals("")) {
                    note = "agg(yes," + currentTmpSamples.size() + ",sum)";
                } else {
                    note += ",agg(yes," + currentTmpSamples.size() + ",sum)";
                }
                if (!notesMap.isEmpty()) {
                    JEVisSample noteSample = notesMap.get(date);
                    if (noteSample != null) {
                        note += ",userNotes";
                    }
                }
                sample.setNote(note);
                currentInterval.getTmpSamples().clear();
                currentInterval.addTmpSample(sample);
            }
        }
    }


    private Double calcAvgSample(List<JEVisSample> currentTmpSamples) throws Exception {
        Double value = 0.0;
        for (JEVisSample sample : currentTmpSamples) {
            Double valueAsDouble = sample.getValueAsDouble();
            value += valueAsDouble;
        }
        return (value / currentTmpSamples.size());
    }

    private Double calcSumSampleDownscale(List<JEVisSample> currentTmpSamples) throws Exception {
        double value = 0.0;
        if (currentTmpSamples.size() > 0) {
            for (JEVisSample sample : currentTmpSamples) {
                value += sample.getValueAsDouble();
            }
        }
        return value;
    }
}
