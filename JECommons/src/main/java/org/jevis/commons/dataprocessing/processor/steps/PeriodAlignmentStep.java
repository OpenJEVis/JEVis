/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.dataprocessing.processor.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStepN;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gschutz
 */
public class PeriodAlignmentStep implements ProcessStepN {

    private static final Logger logger = LogManager.getLogger(PeriodAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {

        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        Map<DateTime, JEVisSample> userDataMap = resourceManager.getUserDataMap();

        if (!cleanDataObject.getIsPeriodAligned()) {
            logger.debug("No period alignment enabled");
            return;
        }

        Map<Integer, JEVisSample> replacementMap = new HashMap<>();

        for (JEVisSample rawSample : rawSamples) {
            DateTime rawSampleTS = rawSample.getTimestamp();
            VirtualSample resultSample = new VirtualSample(rawSampleTS, rawSample.getValueAsDouble());
            resultSample.setNote(rawSample.getNote());
            Period periodForRawSample = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSampleTS);
            if (periodForRawSample.equals(Period.ZERO)) {
                logger.debug("Asynchronous period, no alignment possible, continuing");
                continue;
            }

            JEVisSample userDataSample = userDataMap.get(rawSampleTS);
            if (userDataSample != null) {
                resultSample.setValue(userDataSample.getValueAsDouble());
                if (!userDataSample.getNote().isEmpty()) {
                    resultSample.setNote(resultSample.getNote() + "," + userDataSample.getNote() + "," + NoteConstants.User.USER_VALUE);
                }
            }

            DateTime lowerTS = null;
            DateTime higherTS = null;

            if (periodForRawSample.equals(Period.minutes(1))) {
                lowerTS = rawSampleTS.minusSeconds(30).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusSeconds(30).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (periodForRawSample.getMinutes() == 5) {
                if (rawSampleTS.getMinuteOfHour() == 0) {
                    lowerTS = rawSampleTS.minusMinutes(5).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 5) {
                    lowerTS = rawSampleTS.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 10) {
                    lowerTS = rawSampleTS.withMinuteOfHour(5).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 15) {
                    lowerTS = rawSampleTS.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 20) {
                    lowerTS = rawSampleTS.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 25) {
                    lowerTS = rawSampleTS.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(25).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 30) {
                    lowerTS = rawSampleTS.withMinuteOfHour(25).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 35) {
                    lowerTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(35).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 40) {
                    lowerTS = rawSampleTS.withMinuteOfHour(35).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 45) {
                    lowerTS = rawSampleTS.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 50) {
                    lowerTS = rawSampleTS.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 55) {
                    lowerTS = rawSampleTS.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(55).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 60) {
                    lowerTS = rawSampleTS.withMinuteOfHour(55).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
            } else if (periodForRawSample.getMinutes() == 15) {
                if (rawSampleTS.getMinuteOfHour() == 0) {
                    lowerTS = rawSampleTS.minusMinutes(15).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 15) {
                    lowerTS = rawSampleTS.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 30) {
                    lowerTS = rawSampleTS.withMinuteOfHour(15).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 45) {
                    lowerTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    lowerTS = rawSampleTS.withMinuteOfHour(45).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
            } else if (periodForRawSample.getMinutes() == 30) {
                if (rawSampleTS.getMinuteOfHour() == 0) {
                    lowerTS = rawSampleTS.minusMinutes(30).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 30) {
                    lowerTS = rawSampleTS.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    lowerTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
            } else if (periodForRawSample.getHours() == 1) {
                lowerTS = rawSampleTS.minusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusMinutes(30).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (periodForRawSample.getDays() == 1) {
                lowerTS = rawSampleTS.minusHours(12).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusHours(12).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (periodForRawSample.getWeeks() == 1) {
                lowerTS = rawSampleTS.minusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (periodForRawSample.getMonths() == 1) {
                lowerTS = rawSampleTS.minusHours(363).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusHours(363).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (periodForRawSample.getMonths() == 3) {
                if (rawSampleTS.getMonthOfYear() <= 3) {
                    lowerTS = rawSampleTS.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMonthOfYear(3).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMonthOfYear() <= 6) {
                    lowerTS = rawSampleTS.withMonthOfYear(3).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMonthOfYear(6).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMonthOfYear() <= 9) {
                    lowerTS = rawSampleTS.withMonthOfYear(6).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMonthOfYear(9).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMonthOfYear() <= 12) {
                    lowerTS = rawSampleTS.withMonthOfYear(9).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMonthOfYear(12).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
            } else if (periodForRawSample.getYears() == 1) {
                lowerTS = rawSampleTS.minusDays(182).minusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusDays(182).plusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            }

            if (lowerTS != null && higherTS != null) {
                long lowerDiff = rawSampleTS.getMillis() - lowerTS.getMillis();
                long higherDiff = higherTS.getMillis() - rawSampleTS.getMillis();

                if (!resultSample.getNote().equals("")) {
                    resultSample.setNote(resultSample.getNote() + ",");
                }
                if (lowerDiff < higherDiff && !lowerTS.equals(rawSampleTS)) {
                    resultSample.setTimeStamp(lowerTS);
                    resultSample.setNote(resultSample.getNote() + NoteConstants.Alignment.ALIGNMENT_YES_NEG + lowerDiff / 1000 + NoteConstants.Alignment.ALIGNMENT_YES_CLOSE);
                } else if (higherDiff < lowerDiff && !higherTS.equals(rawSampleTS)) {
                    resultSample.setTimeStamp(higherTS);
                    resultSample.setNote(resultSample.getNote() + NoteConstants.Alignment.ALIGNMENT_YES_POS + higherDiff / 1000 + NoteConstants.Alignment.ALIGNMENT_YES_CLOSE);
                } else {
                    resultSample.setNote(resultSample.getNote() + NoteConstants.Alignment.ALIGNMENT_NO);
                }
            } else {
                resultSample.setNote(resultSample.getNote() + "," + NoteConstants.Alignment.ALIGNMENT_NO);
                logger.warn("Could not identify period {}", periodForRawSample);
            }

            replacementMap.put(rawSamples.indexOf(rawSample), resultSample);
        }

        for (Map.Entry<Integer, JEVisSample> entry : replacementMap.entrySet()) {
            resourceManager.getRawSamplesDown().set(entry.getKey(), entry.getValue());
        }
    }
}
