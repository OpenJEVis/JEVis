/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.calculation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author broder
 */
public class SampleMerger {

    private static final Logger logger = LogManager.getLogger(SampleMerger.class);
    private final List<List<Sample>> allSamples = new ArrayList<>();
    private final List<Sample> constants = new ArrayList<>();
    private final List<List<Sample>> periodConstants = new ArrayList<>();

    private void addSamples(List<JEVisSample> jevisSamples, String variable) {
        List<Sample> samples = jevisSamples.stream().map(currentSample -> new Sample(currentSample, variable)).collect(Collectors.toList());
        allSamples.add(samples);
    }

    private void addConstant(JEVisSample constant, String variable) {
        Sample sample = new Sample(constant, variable);
        constants.add(sample);
    }

    private void addPeriodConstant(List<JEVisSample> jevisSamples, String variable) {
        List<Sample> samples = jevisSamples.stream().map(currentSample -> new Sample(currentSample, variable)).collect(Collectors.toList());
        periodConstants.add(samples);
    }

    public void addSamples(List<JEVisSample> jevisSamples, String variable, CalcInputType inputType) {
        switch (inputType) {
            case PERIODIC:
                addSamples(jevisSamples, variable);
                break;
            case STATIC:
                addConstant(jevisSamples.get(0), variable);
                break;
            case NON_PERIODIC:
                addPeriodConstant(jevisSamples, variable);
                break;
        }
    }

    public Map<DateTime, List<Sample>> merge() {
        Map<DateTime, List<Sample>> sampleMap = new TreeMap<>();

        insertAllSamples(sampleMap); //value changed for every sample

        insertConstants(sampleMap); //value is always the same

        insertPeriodicConstants(sampleMap); //value changed for specific periods

        int variableSize = allSamples.size() + constants.size() + periodConstants.size();
        Set<DateTime> removableKeys = sampleMap.entrySet().stream().filter(sampleEntry -> sampleEntry.getValue().size() != variableSize).map(Map.Entry::getKey).collect(Collectors.toSet());

        removableKeys.forEach(key -> {
            logger.debug("not every input data with datetime {}, will delete this datetime from calculation", key.toString(DateTimeFormat.fullDateTime()));
            sampleMap.remove(key);
        });
        return sampleMap;
    }

    private void insertAllSamples(Map<DateTime, List<Sample>> sampleMap) {
        allSamples.forEach(currentSamples -> currentSamples.forEach(sample -> {
            List<Sample> samples = sampleMap.getOrDefault(sample.getDate(), new ArrayList<>());
            samples.add(sample);
            sampleMap.put(sample.getDate(), samples);
        }));
    }

    private void insertConstants(Map<DateTime, List<Sample>> sampleMap) {
        sampleMap.forEach((key, value) -> value.addAll(constants));
    }

    private void insertPeriodicConstants(Map<DateTime, List<Sample>> sampleMap) {
        sampleMap.forEach((currentSampleTime, value) -> {
            for (List<Sample> periodicConstants : periodConstants) {
                Sample validConstant = null;
                for (Sample periodicConstant : periodicConstants) {
                    DateTime startDate = periodicConstant.getDate();
                    if (startDate.isBefore(currentSampleTime) || startDate.isEqual(currentSampleTime)) {
                        validConstant = periodicConstant;
                    } else if (startDate.isAfter(currentSampleTime)) {
                        break;
                    }
                }
                if (validConstant != null) {
                    value.add(validConstant);
                }
            }
        });
    }



}
