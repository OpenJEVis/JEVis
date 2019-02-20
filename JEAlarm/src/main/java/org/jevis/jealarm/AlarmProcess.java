package org.jevis.jealarm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.alarm.CleanDataAlarm;
import org.jevis.commons.alarm.UsageSchedule;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.jenotifier.mode.SendNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.jevis.jenotifier.notifier.Email.EmailServiceProperty;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */

public class AlarmProcess {
    private static final Logger logger = LogManager.getLogger(AlarmConfiguration.class);
    private static final String RAW_DATA_CLASS_NAME = "Data";
    private static final String CLEAN_DATA_CLASS_NAME = "Clean Data";
    private static final String DATA_DIRECTORY_NAME = "Data Directory";
    private static final String VALUE_ATTRIBUTE = "Value";
    private static final String ALARM_ENABLED_ATTRIBUTE = "Alarm Enabled";
    private static final String ALARM_LOG_ATTRIBUTE = "Alarm Log";
    private static final String NOTIFICATION_CLASS = "Notification";
    private final AlarmConfiguration alarmConfiguration;
    private final JEVisDataSource ds;
    private JEVisClass alarmConfigurationClass;
    private JEVisClass rawDataClass;
    private JEVisClass cleanDataClass;
    private JEVisClass dataDirectoryClass;
    private JEVisClass notificationClass;
    private DateTime start;
    private DateTime end;

    public AlarmProcess(AlarmConfiguration alarmConfiguration) {
        this.alarmConfiguration = alarmConfiguration;
        this.ds = alarmConfiguration.getDs();

        try {
            this.alarmConfigurationClass = ds.getJEVisClass(AlarmConfiguration.CLASS_NAME);
            this.rawDataClass = ds.getJEVisClass(RAW_DATA_CLASS_NAME);
            this.cleanDataClass = ds.getJEVisClass(CLEAN_DATA_CLASS_NAME);
            this.dataDirectoryClass = ds.getJEVisClass(DATA_DIRECTORY_NAME);
            this.notificationClass = ds.getJEVisClass(NOTIFICATION_CLASS);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }


    public void start() {
        List<JEVisObject> allCleanDataObjects = new ArrayList<>();

        DateHelper dateHelper = null;
        start = alarmConfiguration.getTimeStamp();
        dateHelper = PeriodHelper.getDateHelper(alarmConfiguration.getObject(), alarmConfiguration.getAlarmPeriod(), dateHelper, start);
        end = PeriodHelper.calcEndRecord(start, alarmConfiguration.getAlarmPeriod(), dateHelper);

        if (end.isBefore(DateTime.now())) {
            try {
                allCleanDataObjects = getAllCorrespondingCleanDataObjects();
            } catch (JEVisException e) {
                logger.error("Could not get list of all Clean Data Objects.");
            }

            List<Alarm> activeAlarms = new ArrayList<>();
            try {
                activeAlarms = getActiveAlarms(allCleanDataObjects);

            } catch (JEVisException e) {
                logger.error("Could not get list of all active alarms.");
            }

            AlarmTable alarmTable = new AlarmTable(ds, activeAlarms);


            if (sendAlarm(alarmTable)) logger.info("Sent notification.");
            else logger.info("Did not send notification.");

            if (start != null && end != null && end.isAfter(start)) {
                try {
                    JEVisSample timeStampSample = alarmConfiguration.getTimeStampAttribute().buildSample(DateTime.now(), end.plusMillis(1));
                    timeStampSample.commit();

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append("<br>");
                    sb.append("<br>");
                    sb.append(alarmTable.getTableString());
                    sb.append("<br>");
                    sb.append("<br>");
                    sb.append(alarmTable.getAlarmTable());
                    sb.append("<br>");
                    sb.append("<br>");
                    sb.append("</html>");

                    JEVisSample logSample = alarmConfiguration.getLogAttribute().buildSample(DateTime.now(), sb.toString());
                    logSample.commit();
                } catch (JEVisException e) {
                    logger.error("Could not build sample with new time stamp.");
                }
            }

            try {
                finish();
            } catch (Exception e) {
                logger.error("Could not set new start record.");
            }
        }
    }

    private void finish() throws JEVisException {

        DateTime newStartRecordTime = PeriodHelper.getNextPeriod(start, alarmConfiguration.getAlarmPeriod(), 1);
        alarmConfiguration.getTimeStampAttribute().buildSample(new DateTime(), newStartRecordTime.toString()).commit();
    }

    public boolean sendAlarm(AlarmTable alarmTable) {

        try {
            EmailNotification emailNotification = new EmailNotification();
            emailNotification.setNotificationObject(getNotificationObject());

            StringBuilder sb = new StringBuilder();

            sb.append("<html>");

            sb.append("<br>");
            sb.append("<br>");
            sb.append(emailNotification.getMessage());
            sb.append("<br>");
            sb.append("<br>");

            try {
                sb.append(alarmTable.getAlarmTable());
            } catch (Exception e) {
                logger.error("Could not get alarm table string.");
            }

            sb.append("<br>");
            sb.append("<br>");

            sb.append("</html>");

            emailNotification.setMessage(sb.toString());

            sendNotification(emailNotification);
            return true;
        } catch (Exception e) {
            logger.warn("No notification object.");
            return false;
        }
    }

    private JEVisObject getNotificationObject() {
        try {
            List<JEVisObject> notificationObjects = alarmConfiguration.getObject().getChildren(notificationClass, true);
            if (notificationObjects.size() == 1) {
                return notificationObjects.get(0);
            } else {
                throw new IllegalStateException("Too many or no Notification Object for Object: id: " + alarmConfiguration.getObject().getID() + " and name: " + alarmConfiguration.getObject().getName());
            }
        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing Notification Object for Object: id: " + alarmConfiguration.getObject().getID() + " and name: " + alarmConfiguration.getObject().getName(), ex);
        }
    }

    private void sendNotification(EmailNotification emailNotification) {
        try {

            EmailServiceProperty service = getReportService();

            JEVisObject emailNotificationDriverObject = ds.getObject(service.getMailID());

            EmailNotificationDriver emailNotificationDriver = new EmailNotificationDriver();
            emailNotificationDriver.setNotificationDriverObject(emailNotificationDriverObject);

            SendNotification sn = new SendNotification(emailNotification, emailNotificationDriver);
            sn.run();

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private EmailServiceProperty getReportService() {

        EmailServiceProperty service = new EmailServiceProperty();
        try {
            JEVisClass jeVisClass = ds.getJEVisClass("JEReport");
            List<JEVisObject> reportServies = ds.getObjects(jeVisClass, true);
            if (reportServies.size() == 1) {
                service.initialize(reportServies.get(0));
            }
        } catch (JEVisException ex) {
            logger.error("error while getting report service", ex);
        }
        return service;
    }


    private List<Alarm> getActiveAlarms(List<JEVisObject> allCleanDataObjects) throws JEVisException {
        List<Alarm> activeAlarms = new ArrayList<>();

        for (JEVisObject cleanData : allCleanDataObjects) {

            JEVisAttribute valueAtt = cleanData.getAttribute(VALUE_ATTRIBUTE);

            JEVisAttribute alarmLogAttribute = cleanData.getAttribute(ALARM_LOG_ATTRIBUTE);

            List<JEVisSample> valueSamples = valueAtt.getSamples(start, end);
            for (JEVisSample sample : valueSamples) {
                String note = sample.getNote();

                if (note.contains(NoteConstants.Limits.LIMIT_STEP1) && (note.contains(NoteConstants.Limits.LIMIT_DEFAULT)
                        || note.contains(NoteConstants.Limits.LIMIT_STATIC) || note.contains(NoteConstants.Limits.LIMIT_AVERAGE)
                        || note.contains(NoteConstants.Limits.LIMIT_MEDIAN) || note.contains(NoteConstants.Limits.LIMIT_INTERPOLATION)
                        || note.contains(NoteConstants.Limits.LIMIT_MIN) || note.contains(NoteConstants.Limits.LIMIT_MAX))) {
                    activeAlarms.add(new Alarm(cleanData, valueAtt, sample, 0d, 0d, AlarmType.L2, 0));
                } else if (note.contains(NoteConstants.Limits.LIMIT_STEP1)) {
                    activeAlarms.add(new Alarm(cleanData, valueAtt, sample, 0d, 0d, AlarmType.L1, 0));
                }
            }


            CleanDataAlarm cleanDataAlarm = new CleanDataAlarm(cleanData);
            Double tolerance = cleanDataAlarm.getTolerance();
            AlarmType alarmType = cleanDataAlarm.getAlarmType();
            Double limit = null;
            List<UsageSchedule> usageSchedules = cleanDataAlarm.getUsageSchedules();
            List<JEVisSample> comparisonSamples;
            Map<DateTime, JEVisSample> compareMap = new HashMap<>();

            boolean dynamicAlarm = alarmType.equals(AlarmType.DYNAMIC);

            if (dynamicAlarm) {
                comparisonSamples = cleanDataAlarm.getSamples(start, end);
                for (JEVisSample sample : comparisonSamples) {
                    compareMap.put(sample.getTimestamp(), sample);
                }
            } else limit = cleanDataAlarm.getLimit();


            if (!valueSamples.isEmpty()) {
                List<JEVisSample> alarmLogs = new ArrayList<>();
                for (JEVisSample valueSample : valueSamples) {
                    DateTime ts = valueSample.getTimestamp();
                    JEVisSample compareSample = null;
                    Double value = valueSample.getValueAsDouble();
                    AlarmType sampleAlarmType;

                    Double diff = null;
                    Double lowerValue = null;
                    Double upperValue = null;
                    if (dynamicAlarm) {
                        compareSample = compareMap.get(ts);

                        if (compareSample == null) {

                            DateTime dt = ts.minusSeconds(1);
                            while (compareSample == null) {
                                compareSample = compareMap.get(dt);
                                dt = dt.minusSeconds(1);
                            }

//                            logger.error("Could not find sample to compare with value." + ts);
//                            continue;
                        }

                        diff = compareSample.getValueAsDouble() * (tolerance / 100);
                        lowerValue = compareSample.getValueAsDouble() - diff;
                        upperValue = compareSample.getValueAsDouble() + diff;
                        sampleAlarmType = AlarmType.DYNAMIC;
                    } else {
                        diff = limit * (tolerance / 100);
                        lowerValue = limit - diff;
                        upperValue = limit + diff;
                        sampleAlarmType = AlarmType.STATIC;
                    }

                    boolean isAlarm = false;
                    boolean upper = true;
                    switch (cleanDataAlarm.getOperator()) {
                        case BIGGER:
                            if (value > upperValue) isAlarm = true;
                            break;
                        case BIGGER_EQUALS:
                            if (value >= upperValue) isAlarm = true;
                            break;
                        case EQUALS:
                            if (value.equals(upperValue)) isAlarm = true;
                            break;
                        case NOT_EQUALS:
                            if (!value.equals(upperValue)) isAlarm = true;
                            break;
                        case SMALLER:
                            if (value < lowerValue) isAlarm = true;
                            upper = false;
                            break;
                        case SMALLER_EQUALS:
                            if (value <= lowerValue) isAlarm = true;
                            upper = false;
                            break;
                    }

                    if (isAlarm) {
                        int logVal = 0;

                        logVal = ScheduleService.getValueForLog(ts, usageSchedules);
                        JEVisSample alarmSample = new VirtualSample(ts, (long) logVal);
                        alarmLogs.add(alarmSample);

                        if (upper) {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, alarmSample, value, upperValue, sampleAlarmType, logVal));
                        } else {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, alarmSample, value, lowerValue, sampleAlarmType, logVal));
                        }
                    }
                }
                alarmLogAttribute.addSamples(alarmLogs);

            }
        }

        return activeAlarms;
    }


    private List<JEVisObject> getAllCorrespondingCleanDataObjects() throws JEVisException {
        switch (getAlarmConfiguration().getAlarmScope()) {
            case COMPLETE:
                return filterForEnabled(getCompleteList(null, getAlarmConfiguration().getObject()));
            case SELECTED:
                List<JEVisObject> completeList = getCompleteList(null, getAlarmConfiguration().getObject());
                List<JEVisObject> unselectedList = getListFromSelectedObjects();
                if (unselectedList != null) {
                    completeList.removeAll(unselectedList);
                    return filterForEnabled(completeList);
                }
                return new ArrayList<>();
            case WITHOUT_SELECTED:
                return getListFromSelectedObjects();
            case NONE:
                return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }

    private List<JEVisObject> filterForEnabled(List<JEVisObject> completeList) {
        List<JEVisObject> enabledOjects = new ArrayList<>();
        completeList.forEach(jeVisObject -> {
            JEVisAttribute alarmEnabledAttribute = null;
            try {
                alarmEnabledAttribute = jeVisObject.getAttribute(ALARM_ENABLED_ATTRIBUTE);
            } catch (JEVisException e) {
                logger.error("Could not get Attribute.");
            }
            JEVisSample lastSampleAlarmEnabled = alarmEnabledAttribute.getLatestSample();
            boolean alarmEnabled = false;
            if (lastSampleAlarmEnabled != null) {
                try {
                    alarmEnabled = lastSampleAlarmEnabled.getValueAsBoolean();
                } catch (JEVisException e) {
                    logger.error("could not get last Value as boolean.");
                }
            }
            if (alarmEnabled) enabledOjects.add(jeVisObject);
        });
        return enabledOjects;
    }

    private List<JEVisObject> getListFromSelectedObjects() {
        return null;
    }

    private List<JEVisObject> getCompleteList(List<JEVisObject> listObjects, JEVisObject currentObject) throws JEVisException {
        if (listObjects == null) listObjects = new ArrayList<>();


        if (currentObject.getJEVisClass().equals(alarmConfigurationClass)) {
            JEVisObject alarmDir = currentObject.getParents().get(0);
            JEVisObject building = alarmDir.getParents().get(0);

            for (JEVisObject obj : building.getChildren()) {
                if (obj.getJEVisClass().equals(dataDirectoryClass)) getCleanDataChildrenRecursive(listObjects, obj);
            }
        }

        return listObjects;
    }

    private void getCleanDataChildrenRecursive(List<JEVisObject> listObjects, JEVisObject obj) throws JEVisException {
        if (obj.getJEVisClass().equals(cleanDataClass)) listObjects.add(obj);
        else if (obj.getJEVisClass().equals(dataDirectoryClass) || obj.getJEVisClass().equals(rawDataClass)) {
            for (JEVisObject child : obj.getChildren()) {
                try {
                    getCleanDataChildrenRecursive(listObjects, child);
                } catch (JEVisException e) {
                    logger.error("Could not get Children.");
                }
            }
        }
    }

    public AlarmConfiguration getAlarmConfiguration() {
        return alarmConfiguration;
    }
}
