package org.jevis.commons.dataprocessing.processor.workflow;

import org.joda.time.DateTime;
import org.joda.time.Period;

public class PeriodRule {
    private final DateTime startOfPeriod;
    private final Period period;

    public PeriodRule(DateTime startOfPeriod, Period period) {
        this.startOfPeriod = startOfPeriod;
        this.period = period;
    }

    public DateTime getStartOfPeriod() {
        return startOfPeriod;
    }

    public Period getPeriod() {
        return period;
    }
}
