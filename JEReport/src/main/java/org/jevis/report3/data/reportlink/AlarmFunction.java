/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.PeriodMode;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author broder
 */
public class AlarmFunction implements ReportData {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmFunction.class);
    private static String COLUMN_FROM = "from";
    private static String COLUMN_UNTIL = "until";
    private static String COLUMN_DIFF = "diff";
    private static String COLUMN_SHOULDBE = "reff";
    private static String COLUMN_TOLERANCE = "tol";
    private static String COLUMN_IS = "value";
    private static String COLUMN_OBJECTNAME = "objectname";
    private String templateName = "";
    private String alarmLinkName = "";
    private JEVisObject alarmObj = null;
    private JEVisObject linkObject;

    public AlarmFunction(JEVisObject functionObject) {
        try {

            this.linkObject = functionObject;
            templateName = functionObject.getAttribute("Template Name").getLatestSample().getValueAsString();
            alarmLinkName = functionObject.getAttribute("Alarm Link").getLatestSample().getValueAsString();

            TargetHelper th = new TargetHelper(functionObject.getDataSource(), functionObject.getAttribute("Alarm Link"));
            if (th.hasObject()) {
                if (!th.getObject().isEmpty()) {
                    alarmObj = th.getObject().get(0);
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    /**
     * Called form outside
     *
     * @param property
     * @param intervalCalc
     * @return
     */
    @Override
    public ConcurrentHashMap<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc) {
        ConcurrentHashMap<String, Object> functionMap = new ConcurrentHashMap<>();
        Interval interval = intervalCalc.getInterval(PeriodMode.CURRENT);
        DateTime start = interval.getStart();
        DateTime end = interval.getEnd();

        try {
            logger.debug("\n\nCheck Alarm: [" + alarmObj.getID() + "]" + alarmObj.getName());

            CleanDataObject cleanDataObject = new CleanDataObject(alarmObj, new ObjectHandler(alarmObj.getDataSource()));
            cleanDataObject.getAttributes();
            CleanDataAlarm cleanDataAlarm = new CleanDataAlarm(alarmObj);

            AlarmType alarmType = cleanDataAlarm.getAlarmType();

            List<JEVisSample> listLogs = cleanDataObject.getAlarmLogAttribute().getSamples(interval.getStart(), interval.getEnd());
            Map<DateTime, JEVisSample> isValues = new HashMap<>();
            Map<DateTime, JEVisSample> shouldBeValues = new HashMap<>();

            cleanDataObject.getValueAttribute().getSamples(start, end).forEach(sample -> {
                try {
                    isValues.put(sample.getTimestamp(), sample);
                } catch (JEVisException e) {
                    logger.error("Could not get timestamp of value sample.");
                }
            });

            if (alarmType.equals(AlarmType.DYNAMIC)) {
                cleanDataAlarm.getLimitDataAttribute().getSamples(start, end).forEach(sample -> {
                    try {
                        shouldBeValues.put(sample.getTimestamp(), sample);
                    } catch (JEVisException e) {
                        logger.error("Could not get timestamp of comparison sample.");
                    }
                });
            }


            List<Alarm> alarmList = new ArrayList<>();

            for (JEVisSample smp : listLogs) {
                if (smp.getValueAsLong() < 4L) {
                    Alarm alarm = new Alarm(alarmObj, null, smp, smp.getTimestamp(), isValues.get(smp.getTimestamp()).getValueAsDouble(),
                            shouldBeValues.get(smp.getTimestamp()).getValueAsDouble(), alarmType, smp.getValueAsLong().intValue());
                    alarm.setTolerance(cleanDataAlarm.getTolerance());
                    alarmList.add(alarm);
                }
            }

            int limit = 0;

            List<Object> content = new ArrayList<>();
            for (Alarm alarm : alarmList) {
                if (limit++ >= 12) {
                    break;
                }
                content.add(getElement(alarm));
            }
            functionMap.put(templateName, content);

        } catch (Exception ex) {
            logger.error("Error while creating Dynamic Alarm");
        }

        return functionMap;
    }

    private Map<String, Object> getElement(Alarm alarm) {
        //hier is das mit .value/.timestamp usw
        Map<String, Object> tmpMap = new HashMap<>();
        DateTime ts = null;
        try {
            ts = alarm.getAlarmSample().getTimestamp();
        } catch (JEVisException e) {
            logger.error("could not get sample time stamp.");
        }
        tmpMap.put(COLUMN_FROM, PeriodHelper.transformTimestampsToExcelTime(ts));
        tmpMap.put(COLUMN_UNTIL, PeriodHelper.transformTimestampsToExcelTime(ts));
        tmpMap.put(COLUMN_IS, alarm.getIsValue());
        tmpMap.put(COLUMN_SHOULDBE, alarm.getShouldBeValue());
        tmpMap.put(COLUMN_DIFF, alarm.getIsValue() - alarm.getShouldBeValue());
        tmpMap.put(COLUMN_TOLERANCE, alarm.getTolerance());
        tmpMap.put(COLUMN_OBJECTNAME, alarm.getObject().getName());

        return tmpMap;
    }

    @Override
    public JEVisObject getDataObject() {
        return alarmObj;
    }

    @Override
    public LinkStatus getReportLinkStatus(DateTime end) {
        AlarmConfiguration alarmConfiguration = getCorrespondingAlarmConfiguration(alarmObj);
        if (alarmConfiguration != null) {
            JEVisAttribute timeStampAttribute = alarmConfiguration.getTimeStampAttribute();
            if (timeStampAttribute != null) {
                JEVisSample latestSample = timeStampAttribute.getLatestSample();
                if (latestSample != null) {
                    try {
                        DateTime ts = new DateTime(latestSample.getValueAsString());
                        if (ts.isAfter(end)) {
                            return new LinkStatus(true, "ok");
                        }
                    } catch (JEVisException e) {
                        logger.error("Could not parse time stamp: " + e);
                    }
                }
            }
        }
        return new LinkStatus(false, "nok");
    }

    private AlarmConfiguration getCorrespondingAlarmConfiguration(JEVisObject alarmObj) {
        try {
            final JEVisClass alarmDirectoryClass = alarmObj.getDataSource().getJEVisClass("Alarm Directory");
            final JEVisDataSource ds = alarmObj.getDataSource();

            return getAlarmConfiguration(ds, alarmObj, alarmDirectoryClass);
        } catch (JEVisException e) {
            logger.error("Could not get corresponding alarm configuration: " + e);
        }
        return null;
    }

    private AlarmConfiguration getAlarmConfiguration(JEVisDataSource ds, JEVisObject alarmObj, JEVisClass alarmDirectoryClass) throws JEVisException {
        for (JEVisObject parent : alarmObj.getParents()) {
            for (JEVisObject child : parent.getChildren(alarmDirectoryClass, true)) {
                for (JEVisObject alarmDirectoryChild : child.getChildren()) {
                    AlarmConfiguration alarmConfiguration = new AlarmConfiguration(ds, alarmDirectoryChild);
                    if (alarmConfiguration.getAlarmScope().equals(AlarmScope.COMPLETE)) {
                        return alarmConfiguration;
                    } else if (alarmConfiguration.getAlarmScope().equals(AlarmScope.SELECTED)) {
                        if (alarmConfiguration.getAlarmObjects().contains(alarmDirectoryChild)) {
                            return alarmConfiguration;
                        }
                    } else if (alarmConfiguration.getAlarmScope().equals(AlarmScope.WITHOUT_SELECTED)) {
                        if (!alarmConfiguration.getAlarmObjects().contains(alarmDirectoryChild)) {
                            return alarmConfiguration;
                        }
                    }
                }
            }
            return getAlarmConfiguration(ds, parent, alarmDirectoryClass);
        }
        return null;
    }


}