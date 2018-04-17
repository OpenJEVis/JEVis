/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class PeriodicIntervalCalc implements IntervalCalculator {

    private static final Map<PeriodModus, Interval> intervalMap = new ConcurrentHashMap<>();
    private static boolean isInit = false;
    private final SampleHandler samplesHandler;

    @Inject
    public PeriodicIntervalCalc(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public Interval getInterval(PeriodModus modus) {
        return intervalMap.get(modus);
    }

    public synchronized boolean getIsInit() {
        return isInit;
    }

    public synchronized void setIsInitTrue() {
        isInit = true;
    }

    private void initializeIntervalMap(JEVisObject reportObject) {
        String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", ReportProperty.ReportSchedule.DAILY.toString());
        ReportProperty.ReportSchedule schedule = ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
        String startRecordString = samplesHandler.getLastSampleAsString(reportObject, "Start Record");
        DateTime start = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
        for (PeriodModus modus : PeriodModus.values()) {
            DateTime startRecord = calcStartRecord(start, schedule, modus);
            DateTime endRecord = DateHelper.calcEndRecord(startRecord, schedule);
            Interval interval = new Interval(startRecord, endRecord);
            intervalMap.put(modus, interval);
        }
    }

    private DateTime calcStartRecord(DateTime startRecord, ReportProperty.ReportSchedule schedule, PeriodModus modus) {
        DateTime resultStartRecord = startRecord;
        switch (modus) {
            case LAST:
                resultStartRecord = DateHelper.getPriorStartRecord(startRecord, schedule);
                break;
            default:
                break;
        }
        return resultStartRecord;
    }

    @Override
    public void buildIntervals(JEVisObject reportObject) {
        initializeIntervalMap(reportObject);
    }

}
