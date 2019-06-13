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
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.report.PeriodMode;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.report3.data.report.IntervalCalculator;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import javax.inject.Inject;
import java.time.LocalTime;
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
    private LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);

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
        String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", Period.DAILY.toString());
        Period schedule = Period.valueOf(scheduleString.toUpperCase());
        String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
        DateTime start = JEVisDates.DEFAULT_DATE_FORMAT.parseDateTime(startRecordString);

        org.jevis.commons.datetime.DateHelper dateHelper = null;

        dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, start);

        for (PeriodMode mode : PeriodMode.values()) {
            DateTime startRecord = calcStartRecord(start, schedule, mode, dateHelper);
            DateTime endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateHelper);
            Interval interval = new Interval(startRecord, endRecord);
            intervalMap.put(mode, interval);
        }

        logger.info("Initialized Interval Map. Created " + intervalMap.size() + " entries.");
    }

    private DateTime calcStartRecord(DateTime startRecord, Period schedule, PeriodMode modus, org.jevis.commons.datetime.DateHelper dateHelper) {
        DateTime resultStartRecord = startRecord;
        switch (modus) {
            case LAST:
                resultStartRecord = PeriodHelper.getPriorStartRecord(startRecord, schedule, dateHelper);
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
