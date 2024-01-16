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
import org.jevis.commons.dataprocessing.processor.workflow.ProcessStep;
import org.jevis.commons.dataprocessing.processor.workflow.ResourceManager;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gschutz
 */
public class PeriodAlignmentStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(PeriodAlignmentStep.class);

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        DateTime benchStart = new DateTime();
        CleanDataObject cleanDataObject = resourceManager.getCleanDataObject();
        List<JEVisSample> rawSamples = resourceManager.getRawSamplesDown();
        Map<DateTime, JEVisSample> userDataMap = resourceManager.getUserDataMap();

        if (!cleanDataObject.getIsPeriodAligned()) {
            logger.info("No period alignment enabled");
            return;
        }

        WorkDays workDays = new WorkDays(cleanDataObject.getCleanObject());
        DateTimeZone timeZone = workDays.getDateTimeZone();

        Map<Integer, JEVisSample> replacementMap = new HashMap<>();

        for (JEVisSample rawSample : rawSamples) {
            DateTime rawSampleUTCTS = rawSample.getTimestamp();
            DateTime rawSampleTS = rawSampleUTCTS.withZone(timeZone);
            VirtualSample resultSample = new VirtualSample(rawSampleUTCTS, rawSample.getValueAsDouble());
            resultSample.setNote(rawSample.getNote());
            Period periodForRawSample = CleanDataObject.getPeriodForDate(cleanDataObject.getRawDataPeriodAlignment(), rawSampleUTCTS);
            if (periodForRawSample.equals(Period.ZERO)) {
                logger.debug("Asynchronous period, no alignment possible, continuing");
                continue;
            }

            JEVisSample userDataSample = userDataMap.get(rawSampleUTCTS);
            if (userDataSample != null) {
                resultSample.setValue(userDataSample.getValueAsDouble());
                if (!userDataSample.getNote().isEmpty()) {
                    resultSample.setNote(resultSample.getNote() + "," + userDataSample.getNote() + "," + NoteConstants.User.USER_VALUE);
                } else {
                    resultSample.setNote(resultSample.getNote() + "," + NoteConstants.User.USER_VALUE);
                }
            }

            DateTime lowerTS = null;
            DateTime higherTS = null;
            boolean isGreaterThenDays = false;

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
                } else {
                    lowerTS = rawSampleTS.withMinuteOfHour(55).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                }
            } else if (periodForRawSample.getMinutes() == 10) {
                if (rawSampleTS.getMinuteOfHour() == 0) {
                    lowerTS = rawSampleTS.minusMinutes(10).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 10) {
                    lowerTS = rawSampleTS.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 20) {
                    lowerTS = rawSampleTS.withMinuteOfHour(10).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 30) {
                    lowerTS = rawSampleTS.withMinuteOfHour(20).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 40) {
                    lowerTS = rawSampleTS.withMinuteOfHour(30).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
                } else if (rawSampleTS.getMinuteOfHour() < 50) {
                    lowerTS = rawSampleTS.withMinuteOfHour(40).withSecondOfMinute(0).withMillisOfSecond(0);
                    higherTS = rawSampleTS.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
                } else {
                    lowerTS = rawSampleTS.withMinuteOfHour(50).withSecondOfMinute(0).withMillisOfSecond(0);
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
                isGreaterThenDays = true;
            } else if (periodForRawSample.getWeeks() == 1) {
                lowerTS = rawSampleTS.minusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusHours(84).withDayOfWeek(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                isGreaterThenDays = true;
            } else if (periodForRawSample.getMonths() == 1) {
                int halfHoursOfMonth = rawSampleTS.dayOfMonth().getMaximumValue() * 12;
                lowerTS = rawSampleTS.minusHours(halfHoursOfMonth).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(1);
                higherTS = rawSampleTS.plusHours(halfHoursOfMonth).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                isGreaterThenDays = true;
            } else if (periodForRawSample.getMonths() == 3) {
                isGreaterThenDays = true;
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
                isGreaterThenDays = true;
                int halfDaysOfYear = rawSampleTS.dayOfYear().getMaximumValue() / 2;
                lowerTS = rawSampleTS.minusDays(halfDaysOfYear).minusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
                higherTS = rawSampleTS.plusDays(halfDaysOfYear).plusHours(15).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                try {
                    long halfPeriod = periodForRawSample.toStandardDuration().getMillis();
                    lowerTS = rawSampleTS.minus(halfPeriod);
                    higherTS = rawSampleTS.plus(halfPeriod);

                } catch (Exception e) {
                    logger.error("Could not determine period duration", e);
                }
            }

            if (isGreaterThenDays && lowerTS != null) {
                LocalTime workdayStart = workDays.getWorkdayStart();
                lowerTS = lowerTS.withHourOfDay(workdayStart.getHour())
                        .withMinuteOfHour(workdayStart.getMinute())
                        .withSecondOfMinute(workdayStart.getSecond());

                higherTS = higherTS.withHourOfDay(workdayStart.getHour())
                        .withMinuteOfHour(workdayStart.getMinute())
                        .withSecondOfMinute(workdayStart.getSecond());

                LocalTime workdayEnd = workDays.getWorkdayEnd();
                if (workdayEnd.isBefore(workdayStart)) {
                    lowerTS = lowerTS.minusDays(1);
                    higherTS = higherTS.minusDays(1);
                }
            }

            if (lowerTS != null && higherTS != null) {
                long lowerDiff = rawSampleTS.getMillis() - lowerTS.getMillis();
                long higherDiff = higherTS.getMillis() - rawSampleTS.getMillis();

                if (!resultSample.getNote().equals("")) {
                    resultSample.setNote(resultSample.getNote() + ",");
                }
                if (lowerDiff < higherDiff && !lowerTS.equals(rawSampleTS)) {
                    resultSample.setTimeStamp(lowerTS.withZone(DateTimeZone.UTC));
                    resultSample.setNote(resultSample.getNote() + NoteConstants.Alignment.ALIGNMENT_YES + lowerDiff / 1000 + NoteConstants.Alignment.ALIGNMENT_YES_CLOSE);
                } else if (higherDiff < lowerDiff && !higherTS.equals(rawSampleTS)) {
                    resultSample.setTimeStamp(higherTS.withZone(DateTimeZone.UTC));
                    resultSample.setNote(resultSample.getNote() + NoteConstants.Alignment.ALIGNMENT_YES + higherDiff / 1000 + NoteConstants.Alignment.ALIGNMENT_YES_CLOSE);
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

        logger.debug("{} finished in {}", this.getClass().getSimpleName(), new Period(benchStart, new DateTime()).toString(PeriodFormat.wordBased(I18n.getInstance().getLocale())));
    }
}
