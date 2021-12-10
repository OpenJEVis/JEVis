package org.jevis.jealarm;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.alarm.*;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.json.JsonAlarm;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jenotifier.mode.SendNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.jevis.jenotifier.notifier.Email.EmailServiceProperty;
import org.joda.time.DateTime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

            boolean hasData = true;
            for (JEVisObject obj : allCleanDataObjects) {
                CleanDataObject cleanDataObject = new CleanDataObject(obj);
                boolean alarmEnabled = false;
                JEVisAttribute alarmEnabledAttribute = cleanDataObject.getAlarmEnabledAttribute();
                if (alarmEnabledAttribute != null) {
                    JEVisSample latestSample = alarmEnabledAttribute.getLatestSample();
                    if (latestSample != null) {
                        try {
                            alarmEnabled = latestSample.getValueAsBoolean();

                            if (!alarmEnabled) continue;
                        } catch (JEVisException e) {
                            logger.error("Error while getting alarm enabled attribute sample", e);
                        }
                    }
                }
                DateTime lastTS = cleanDataObject.getFirstDate();
                if (alarmEnabled && !lastTS.isAfter(end)) {
                    hasData = false;
                    break;
                }
            }

            if (hasData) {
                List<Alarm> activeAlarms = getActiveAlarms(allCleanDataObjects);

                if (!activeAlarms.isEmpty()) {
                    AlarmTable alarmTable = new AlarmTable(ds, activeAlarms);

                    boolean sentAlarm = sendAlarm(alarmTable);

                    if (sentAlarm) logger.info("Sent notification.");
                    else logger.info("Did not send notification.");

                    if (start != null && end != null && end.isAfter(start)) {
                        try {

                            List<JsonAlarm> jsonAlarmList = new ArrayList<>();
                            for (Alarm alarm : activeAlarms) {
                                JsonAlarm jsonAlarm = new JsonAlarm();
                                jsonAlarm.setAlarmType(alarm.getAlarmType());
                                jsonAlarm.setAttribute(alarm.getAttribute().getName());
                                jsonAlarm.setIsValue(alarm.getIsValue());
                                jsonAlarm.setOperator(alarm.getOperator());
                                jsonAlarm.setLogValue(alarm.getLogValue());
                                jsonAlarm.setObject(alarm.getObject().getID());
                                jsonAlarm.setShouldBeValue(alarm.getSetValue());
                                jsonAlarm.setTimeStamp(alarm.getTimeStamp().toString());
                                jsonAlarm.setTolerance(alarm.getTolerance());
                                jsonAlarmList.add(jsonAlarm);
                            }
                            try {
                                JEVisFileImp jsonFile = new JEVisFileImp(
                                        alarmConfiguration.getName() + "_" + DateTime.now().toString("yyyyMMddHHmm") + ".json"
                                        , JsonTools.prettyObjectMapper().writeValueAsString(jsonAlarmList).getBytes(StandardCharsets.UTF_8));
                                JEVisSample newSample = alarmConfiguration.getFileLogAttribute().buildSample(new DateTime(), jsonFile);
                                newSample.commit();
                            } catch (JsonProcessingException e) {
                                logger.error("JsonProcessingException. Could not build sample with new time stamp.", e);
                            } catch (FileNotFoundException e) {
                                logger.error("FileNotFoundException. Could not build sample with new time stamp.", e);
                            } catch (IOException e) {
                                logger.error("IOException. Could not build sample with new time stamp.", e);
                            }

                            alarmConfiguration.setChecked(false);
                        } catch (JEVisException e) {
                            logger.error("Could not build sample with new time stamp.");
                        }
                    }
                }

                try {
                    finish();
                } catch (Exception e) {
                    logger.error("Could not set new start record.");
                }
            } else {
                logger.warn("No new Data.");
            }
        }
    }

    private void finish() throws JEVisException {

        DateTime newStartRecordTime = PeriodHelper.getNextPeriod(start, alarmConfiguration.getAlarmPeriod(), 1, null);
        alarmConfiguration.getTimeStampAttribute().buildSample(new DateTime(), newStartRecordTime.toString()).commit();
    }

    public boolean sendAlarm(AlarmTable alarmTable) {

        try {
            EmailNotification emailNotification = new EmailNotification();
            emailNotification.setNotificationObject(getNotificationObject());
            emailNotification.setIsHTML(true);

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


    private List<Alarm> getActiveAlarms(List<JEVisObject> allCleanDataObjects) {
        List<Alarm> activeAlarms = new ArrayList<>();

        for (JEVisObject cleanData : allCleanDataObjects) {
            try {

                JEVisAttribute valueAtt = cleanData.getAttribute(VALUE_ATTRIBUTE);
                JEVisAttribute alarmLogAttribute = cleanData.getAttribute(ALARM_LOG_ATTRIBUTE);

                List<JEVisSample> valueSamples = valueAtt.getSamples(start, end);

                CleanDataObject cleanDataObject = new CleanDataObject(cleanData);
                List<JsonLimitsConfig> cleanDataObjectLimitsConfig = cleanDataObject.getLimitsConfig();
                Double shouldBeValue1Min = null;
                Double shouldBeValue2Min = null;
                Double shouldBeValue1Max = null;
                Double shouldBeValue2Max = null;
                for (JsonLimitsConfig jsonLimitsConfig : cleanDataObjectLimitsConfig) {
                    int index = cleanDataObjectLimitsConfig.indexOf(jsonLimitsConfig);
                    if (index == 0) {
                        try {
                            shouldBeValue1Min = Double.parseDouble(jsonLimitsConfig.getMin());
                            shouldBeValue1Max = Double.parseDouble(jsonLimitsConfig.getMax());
                        } catch (Exception e) {
                            logger.error("Could not parse limit step 1 configuration from object {}:{}", cleanData.getName(), cleanData.getID(), e);
                        }
                    } else if (index == 1) {
                        try {
                            shouldBeValue2Min = Double.parseDouble(jsonLimitsConfig.getMin());
                            shouldBeValue2Max = Double.parseDouble(jsonLimitsConfig.getMax());
                        } catch (Exception e) {
                            logger.error("Could not parse limit step 2 configuration from object {}:{}", cleanData.getName(), cleanData.getID(), e);
                        }
                    }
                }

                for (JEVisSample sample : valueSamples) {
                    String note = sample.getNote();

                    if (note.contains(NoteConstants.Limits.LIMIT_STEP1) && (note.contains(NoteConstants.Limits.LIMIT_DEFAULT)
                            || note.contains(NoteConstants.Limits.LIMIT_STATIC) || note.contains(NoteConstants.Limits.LIMIT_AVERAGE)
                            || note.contains(NoteConstants.Limits.LIMIT_MEDIAN) || note.contains(NoteConstants.Limits.LIMIT_INTERPOLATION)
                            || note.contains(NoteConstants.Limits.LIMIT_MIN) || note.contains(NoteConstants.Limits.LIMIT_MAX))) {
                        if (shouldBeValue2Min != null && sample.getValueAsDouble() < shouldBeValue2Min) {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, sample, sample.getTimestamp(), sample.getValueAsDouble(), "<", shouldBeValue2Min, AlarmType.L2, 0));
                        } else if (shouldBeValue2Max != null && sample.getValueAsDouble() > shouldBeValue2Max) {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, sample, sample.getTimestamp(), sample.getValueAsDouble(), ">", shouldBeValue2Max, AlarmType.L2, 0));
                        }
                    } else if (note.contains(NoteConstants.Limits.LIMIT_STEP1)) {
                        if (shouldBeValue1Min != null && sample.getValueAsDouble() < shouldBeValue1Min) {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, sample, sample.getTimestamp(), sample.getValueAsDouble(), "<", shouldBeValue1Min, AlarmType.L1, 0));
                        } else if (shouldBeValue1Max != null && sample.getValueAsDouble() > shouldBeValue1Max) {
                            activeAlarms.add(new Alarm(cleanData, valueAtt, sample, sample.getTimestamp(), sample.getValueAsDouble(), ">", shouldBeValue1Max, AlarmType.L1, 0));
                        }
                    }
                }

                CleanDataAlarm cleanDataAlarm = new CleanDataAlarm(cleanData);
                if (cleanDataAlarm.isValidAlarmConfiguration()) {
                    Double tolerance = cleanDataAlarm.getTolerance();
                    AlarmType alarmType = cleanDataAlarm.getAlarmType();
                    Double limit = null;
                    List<UsageSchedule> usageSchedules = cleanDataAlarm.getUsageSchedules();
                    List<JEVisSample> comparisonSamples;
                    Map<DateTime, JEVisSample> compareMap = new HashMap<>();

                    boolean dynamicAlarm = alarmType.equals(AlarmType.DYNAMIC);

                    DateTime firstTimeStamp = end;
                    if (dynamicAlarm) {
                        comparisonSamples = cleanDataAlarm.getSamples(start, end);
                        for (JEVisSample sample : comparisonSamples) {
                            if (sample.getTimestamp().isBefore(firstTimeStamp)) firstTimeStamp = sample.getTimestamp();
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
                                    while (compareSample == null && (dt.equals(firstTimeStamp) || dt.isAfter(firstTimeStamp))) {
                                        compareSample = compareMap.get(dt);
                                        dt = dt.minusSeconds(1);
                                    }

                                    if (compareSample == null) {
                                        logger.error("Could not find sample to compare with value: {}", ts);
                                        continue;
                                    }
                                }

                                if (tolerance != 0d) {
                                    diff = compareSample.getValueAsDouble() * (tolerance / 100);
                                } else {
                                    diff = 0d;
                                }
                                lowerValue = compareSample.getValueAsDouble() - diff;
                                upperValue = compareSample.getValueAsDouble() + diff;
                                sampleAlarmType = AlarmType.DYNAMIC;
                            } else {
                                if (tolerance != 0d) {
                                    diff = limit * (tolerance / 100);
                                } else {
                                    diff = 0d;
                                }
                                lowerValue = limit - diff;
                                upperValue = limit + diff;
                                sampleAlarmType = AlarmType.STATIC;
                            }

                            boolean isAlarm = false;
                            boolean upper = true;
                            String operator = "";
                            switch (cleanDataAlarm.getOperator()) {
                                case BIGGER:
                                    if (value > upperValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.BIGGER);
                                    }
                                    break;
                                case BIGGER_EQUALS:
                                    if (value >= upperValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.BIGGER_EQUALS);
                                    }
                                    break;
                                case EQUALS:
                                    if (value.equals(upperValue)) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.EQUALS);
                                    }
                                    break;
                                case NOT_EQUALS:
                                    if (!value.equals(upperValue)) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.NOT_EQUALS);
                                    }
                                    break;
                                case SMALLER:
                                    if (value < lowerValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.SMALLER);
                                    }
                                    upper = false;
                                    break;
                                case SMALLER_EQUALS:
                                    if (value <= lowerValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.SMALLER_EQUALS);
                                    }
                                    upper = false;
                                    break;
                            }

                            if (isAlarm) {
                                int logVal = 0;

                                logVal = ScheduleService.getValueForLog(ts, usageSchedules);
                                JEVisSample alarmSample = new VirtualSample(ts, (long) logVal);
                                alarmLogs.add(alarmSample);

                                if (upper) {
                                    activeAlarms.add(new Alarm(cleanData, valueAtt, alarmSample, ts, value, operator, upperValue, sampleAlarmType, logVal));
                                } else {
                                    activeAlarms.add(new Alarm(cleanData, valueAtt, alarmSample, ts, value, operator, lowerValue, sampleAlarmType, logVal));
                                }
                            }
                        }

                        alarmLogAttribute.addSamples(alarmLogs);
                    }
                }
            } catch (Exception e) {
                String parentName = "";
                try {
                    parentName = CommonMethods.getFirstParentalDataObject(cleanData).getName();
                } catch (Exception ex) {
                    logger.error("Could not get parental data object of object {}:{}", cleanData.getName(), cleanData.getID(), ex);
                }
                logger.error("Error while creating alarm for {} / {}:{}", parentName, cleanData.getName(), cleanData.getID(), e);
            }
        }

        return activeAlarms;
    }


    private List<JEVisObject> getAllCorrespondingCleanDataObjects() throws JEVisException {
        switch (getAlarmConfiguration().getAlarmScope()) {
            case COMPLETE:
                return getCompleteList(null, getAlarmConfiguration().getObject());
            case SELECTED:
                return getListFromSelectedObjects();
            case WITHOUT_SELECTED:
                List<JEVisObject> completeList = getCompleteList(null, getAlarmConfiguration().getObject());
                List<JEVisObject> unselectedList = getListFromSelectedObjects();
                if (unselectedList != null) {
                    completeList.removeAll(unselectedList);
                    return completeList;
                }
                return new ArrayList<>();
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
        return alarmConfiguration.getAlarmObjects();
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
