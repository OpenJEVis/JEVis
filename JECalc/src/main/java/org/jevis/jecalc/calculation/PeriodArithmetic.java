package org.jevis.jecalc.calculation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.joda.time.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.joda.time.DurationFieldType.*;

/**
 * @author Robert Elliot
 */

public class PeriodArithmetic {

    private static final long MILLIS_IN_DAY = Days.ONE.toStandardSeconds().getSeconds() * 1000L;
    private static final long MILLIS_IN_YEAR = Days.ONE.toStandardSeconds().getSeconds() * 365250L;
    private static final ImmutableMap<DurationFieldType, Long> averageLengthMillis
            = ImmutableMap.<DurationFieldType, Long>builder()
            .put(millis(), 1L)
            .put(seconds(), 1000L)
            .put(minutes(), Minutes.ONE.toStandardSeconds().getSeconds() * 1000L)
            .put(hours(), Hours.ONE.toStandardSeconds().getSeconds() * 1000L)
            .put(halfdays(), MILLIS_IN_DAY / 2)
            .put(days(), MILLIS_IN_DAY)
            .put(weeks(), Weeks.ONE.toStandardSeconds().getSeconds() * 1000L)
            .put(months(), MILLIS_IN_YEAR / 12)
            .put(years(), MILLIS_IN_YEAR)
            .put(weekyears(), MILLIS_IN_YEAR)
            .put(centuries(), MILLIS_IN_YEAR * 100)
            .put(eras(), Long.MAX_VALUE)
            .build();

    public static long periodsInAnInterval(Interval interval, Period period) {
        long averageMillis = toAverageMillis(period);
        int bestGuess;
        if (averageMillis > 0)
            bestGuess = (int) (interval.toDurationMillis() / averageMillis);
        else return 0;
        if (bestGuess < 0) return 0;
        if (startPlusScaledPeriodIsAfterEnd(interval, period, bestGuess + 1)) {
            return searchDownwards(interval, period, bestGuess);
        } else {
            return searchUpwards(interval, period, bestGuess);
        }
    }

    private static long searchDownwards(Interval interval, Period period, int currentGuess) {
        if (startPlusScaledPeriodIsAfterEnd(interval, period, currentGuess)) {
            return searchDownwards(interval, period, currentGuess - 1);
        } else {
            return currentGuess;
        }
    }

    private static long searchUpwards(Interval interval, Period period, int currentGuess) {
        if (!startPlusScaledPeriodIsAfterEnd(interval, period, currentGuess + 1)) {
            return searchUpwards(interval, period, currentGuess + 1);
        } else {
            return currentGuess;
        }
    }

    private static boolean startPlusScaledPeriodIsAfterEnd(Interval interval, Period period, int scalar) {
        return interval.getStart().plus(period.multipliedBy(scalar)).isAfter(interval.getEnd());
    }

    private static long toAverageMillis(Period period) {
        final Iterable<Long> milliValues = Arrays.stream(period.getFieldTypes()).map(toAverageMillisForFieldType(period)).collect(Collectors.toList());
        return total(milliValues);
    }

    private static java.util.function.Function<DurationFieldType, Long> toAverageMillisForFieldType(final Period period) {
        return (Function<DurationFieldType, Long>) durationFieldType -> {
            final Long averageDuration = averageLengthMillis.get(durationFieldType);
            return period.get(durationFieldType) * averageDuration;
        };
    }

    private static long total(Iterable<Long> milliValues) {
        long acc = 0;
        for (Long milliValue : milliValues) {
            acc += milliValue;
        }
        return acc;
    }
}