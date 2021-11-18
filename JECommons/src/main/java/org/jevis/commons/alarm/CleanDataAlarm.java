package org.jevis.commons.alarm;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datatype.scheduler.SchedulerHandler;
import org.jevis.commons.datatype.scheduler.cron.CronScheduler;
import org.jevis.commons.json.JsonAlarmConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CleanDataAlarm {
    private static final Logger logger = LogManager.getLogger(CleanDataAlarm.class);
    private final JEVisObject cleanDataObject;
    private JEVisObject limitDataObject;
    private Double limit;
    private JEVisAttribute limitDataAttribute;
    private AlarmConstants.Operator operator;
    private CronScheduler silentTime;
    private CronScheduler standByTime;
    private Double tolerance = 0d;
    private List<JsonAlarmConfig> jsonList = new ArrayList<>();
    private AlarmType alarmType;
    private final List<UsageSchedule> usageSchedules = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final boolean validAlarmConfiguration;
    private final boolean isEnabled;

    public CleanDataAlarm(JEVisObject cleanDataObject) throws JEVisException {
        this.cleanDataObject = cleanDataObject;

        CleanDataObject cdo = new CleanDataObject(cleanDataObject);
        isEnabled = cdo.isAlarmEnabled();

        createJsonList(cleanDataObject);

        validAlarmConfiguration = parseJsonList(cleanDataObject);
    }

    private boolean parseJsonList(JEVisObject cleanDataObject) {
        if (!jsonList.isEmpty()) {
            for (JsonAlarmConfig jac : jsonList) {
                try {
                    if (jac.getLimitData() != null) {
                        TargetHelper th = new TargetHelper(cleanDataObject.getDataSource(), jac.getLimitData());

                        limitDataObject = th.getObject().get(0);
                        limitDataAttribute = th.getAttribute().get(0);
                        alarmType = AlarmType.DYNAMIC;
                    }
                    if (jac.getLimit() != null) {
                        limit = Double.parseDouble(jac.getLimit());
                        alarmType = AlarmType.STATIC;
                    }

                    if (alarmType != null) {
                        if (jac.getOperator() != null)
                            operator = AlarmConstants.Operator.parse(jac.getOperator());

                        if (jac.getSilentTime() != null) {
                            String json = jac.getSilentTime().toString();
                            silentTime = SchedulerHandler.BuildScheduler(json);
                            usageSchedules.add(new UsageSchedule(silentTime, UsageScheduleType.SILENT));
                        }

                        if (jac.getStandbyTime() != null) {
                            String json = jac.getStandbyTime().toString();
                            standByTime = SchedulerHandler.BuildScheduler(json);
                            usageSchedules.add(new UsageSchedule(standByTime, UsageScheduleType.STANDBY));
                        }

                        if (jac.getTolerance() != null)
                            tolerance = Double.parseDouble(jac.getTolerance());
                    }
                } catch (Exception e) {
                    logger.error("Error parsing alarm configuration: " + e);
                }
            }
        }
        return (((limitDataObject != null && limitDataAttribute != null) || limit != null) && operator != null
                && tolerance != null);
    }

    private void createJsonList(JEVisObject cleanDataObject) {
        try {
            JEVisAttribute alarmConfigAtt = cleanDataObject.getAttribute("Alarm Config");
            if (alarmConfigAtt != null) {
                JEVisSample latestSample = alarmConfigAtt.getLatestSample();
                if (latestSample != null) {
                    String latestSampleString = latestSample.getValueAsString();
                    if (latestSampleString.startsWith("[")) {
                        jsonList = Arrays.asList(objectMapper.readValue(latestSampleString, JsonAlarmConfig[].class));
                    } else {
                        jsonList.add(objectMapper.readValue(latestSampleString, JsonAlarmConfig.class));
                    }
                }
            }
        } catch (JEVisException e) {
            logger.error("JEVisException. Error while parsing alarm configuration json from config {}", cleanDataObject.getID(), e);
        } catch (JsonParseException e) {
            logger.error("JsonParseException. Error while parsing alarm configuration json from config {}", cleanDataObject.getID(), e);
        } catch (JsonMappingException e) {
            logger.error("JsonMappingException. Error while parsing alarm configuration json from config {}", cleanDataObject.getID(), e);
        } catch (IOException e) {
            logger.error("IOException. Error while parsing alarm configuration json from config {}", cleanDataObject.getID(), e);
        }
    }

    public JEVisObject getCleanDataObject() {
        return cleanDataObject;
    }

    public JEVisObject getLimitDataObject() {
        return limitDataObject;
    }

    public Double getLimit() {
        return limit;
    }

    public JEVisAttribute getLimitDataAttribute() {
        return limitDataAttribute;
    }

    public CronScheduler getSilentTime() {
        return silentTime;
    }

    public CronScheduler getStandByTime() {
        return standByTime;
    }

    public Double getTolerance() {
        return tolerance;
    }

    public AlarmConstants.Operator getOperator() {
        return operator;
    }

    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        return limitDataAttribute != null ? limitDataAttribute.getSamples(from, to) : new ArrayList<>();
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public List<UsageSchedule> getUsageSchedules() {
        return usageSchedules;
    }

    public boolean isValidAlarmConfiguration() {
        return validAlarmConfiguration;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
