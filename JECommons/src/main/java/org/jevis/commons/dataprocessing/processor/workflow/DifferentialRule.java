package org.jevis.commons.dataprocessing.processor.workflow;

import org.joda.time.DateTime;

public class DifferentialRule {
    private final DateTime startOfPeriod;
    private final Boolean isDifferential;

    public DifferentialRule(DateTime startOfPeriod, Boolean isDifferential) {
        this.startOfPeriod = startOfPeriod;
        this.isDifferential = isDifferential;
    }

    public DateTime getStartOfPeriod() {
        return startOfPeriod;
    }

    public Boolean isDifferential() {
        return isDifferential;
    }
}
