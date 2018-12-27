/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report.periodic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author broder
 */
public class PeriodicIntervalCalc implements IntervalCalculator {

    private static final Logger logger = LogManager.getLogger(PeriodicIntervalCalc.class);
    private static final Map<PeriodMode, Interval> intervalMap = new ConcurrentHashMap<>();
    private static boolean isInit = false;
    private final SampleHandler samplesHandler;
    private JEVisObject reportObject = null;

    @Inject
    public PeriodicIntervalCalc(SampleHandler samplesHandler) {
        this.samplesHandler = samplesHandler;
    }

    @Override
    public Interval getInterval(PeriodMode modus) {
        return intervalMap.get(modus);
    }

    public synchronized boolean getIsInit() {
        return isInit;
    }

    public synchronized void setIsInitTrue() {
        isInit = true;
    }

    private void initializeIntervalMap(JEVisObject reportObject) {
        this.reportObject = reportObject;
        String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", ReportProperty.ReportSchedule.DAILY.toString());
        ReportProperty.ReportSchedule schedule = ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
        String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
        DateTime start = JEVisDates.DEFAULT_DATE_FORMAT.parseDateTime(startRecordString);
        for (PeriodMode mode : PeriodMode.values()) {
            DateTime startRecord = calcStartRecord(start, schedule, mode);
            DateTime endRecord = DateHelper.calcEndRecord(startRecord, schedule);
            Interval interval = new Interval(startRecord, endRecord);
            intervalMap.put(mode, interval);
        }

        logger.info("Initialized Interval Map. Created " + intervalMap.size() + " entries.");
    }

    private DateTime calcStartRecord(DateTime startRecord, ReportProperty.ReportSchedule schedule, PeriodMode modus) {
        DateTime resultStartRecord = startRecord;
        switch (modus) {
            case LAST:
                resultStartRecord = DateHelper.getPriorStartRecord(startRecord, schedule);
                break;
            case ALL:
                resultStartRecord = samplesHandler.getTimestampFromFirstSample(reportObject, "Start Record");
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
