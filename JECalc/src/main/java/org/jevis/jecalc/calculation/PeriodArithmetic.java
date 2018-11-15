package org.jevis.jecalc.calculation;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import org.joda.time.*;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;
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
        int bestGuess = (int) (interval.toDurationMillis() / toAverageMillis(period));
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
        final Iterable<Long> milliValues = from(asList(period.getFieldTypes())).transform(toAverageMillisForFieldType(period));
        return total(milliValues);
    }

    private static Function<DurationFieldType, Long> toAverageMillisForFieldType(final Period period) {
        return new Function<DurationFieldType, Long>() {
            @Override
            public Long apply(DurationFieldType durationFieldType) {
                final Long averageDuration = averageLengthMillis.get(durationFieldType);
                return period.get(durationFieldType) * averageDuration;
            }
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