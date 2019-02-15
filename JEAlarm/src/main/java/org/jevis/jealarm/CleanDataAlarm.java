package org.jevis.jealarm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.json.JsonAlarmConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class CleanDataAlarm {
    private static final Logger logger = LogManager.getLogger(CleanDataAlarm.class);
    private final JEVisObject cleanDataObject;
    private JEVisObject limitDataObject;
    private Double limit;
    private JEVisAttribute limitDataAttribute;
    private AlarmConstants.Operator operator;
    private UsagePeriod silentTime;
    private UsagePeriod standByTime;
    private Double tolerance;
    private List<JsonAlarmConfig> jsonList = new ArrayList<>();
    private AlarmType alarmType;

    public CleanDataAlarm(JEVisObject cleanDataObject) throws JEVisException {
        this.cleanDataObject = cleanDataObject;

        createJsonList(cleanDataObject);

        if (!parseJsonList(cleanDataObject)) {
            throw new JEVisException("Could not parse Json.", 4214218);
        }
    }

    private boolean parseJsonList(JEVisObject cleanDataObject) throws JEVisException {
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

                    if (jac.getOperator() != null)
                        operator = AlarmConstants.Operator.parse(jac.getOperator());

                    if (jac.getSilentTime() != null) {
                        silentTime = new UsagePeriod(UsagePeriodType.SILENT);
                        silentTime.setPeriod(Long.parseLong(jac.getSilentTime()));
                    }

                    if (jac.getStandbyTime() != null) {
                        standByTime = new UsagePeriod(UsagePeriodType.STANDBY);
                        standByTime.setPeriod(Long.parseLong(jac.getSilentTime()));
                    }

                    if (jac.getTolerance() != null)
                        tolerance = Double.parseDouble(jac.getTolerance());
                    return ((limitDataObject != null && limitDataAttribute != null) || limit != null) && operator != null
                            && silentTime != null && standByTime != null && tolerance != null;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    private void createJsonList(JEVisObject cleanDataObject) {
        try {
            JEVisAttribute alarmConfigAtt = cleanDataObject.getAttribute("Alarm Config");
            if (alarmConfigAtt != null) {
                JEVisSample latestSample = alarmConfigAtt.getLatestSample();
                if (latestSample != null) {
                    String latestSampleString = latestSample.getValueAsString();
                    if (latestSampleString.startsWith("[")) {
                        jsonList = new Gson().fromJson(latestSampleString, new TypeToken<List<JsonAlarmConfig>>() {
                        }.getType());
                    } else {
                        jsonList.add(new Gson().fromJson(latestSampleString, JsonAlarmConfig.class));
                    }
                }
            }
        } catch (JEVisException e) {
            e.printStackTrace();
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

    public UsagePeriod getSilentTime() {
        return silentTime;
    }

    public UsagePeriod getStandByTime() {
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
}
