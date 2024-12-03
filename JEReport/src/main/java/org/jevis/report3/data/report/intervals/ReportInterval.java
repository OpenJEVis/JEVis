package org.jevis.report3.data.report.intervals;

import org.joda.time.DateTime;

public class ReportInterval {
    private final DateTime start;
    private final DateTime end;

    public ReportInterval(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
    }

    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }
}
