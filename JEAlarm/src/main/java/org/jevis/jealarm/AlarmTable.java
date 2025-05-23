package org.jevis.jealarm;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.classes.JC;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import tech.units.indriya.AbstractUnit;

import java.text.NumberFormat;
import java.util.List;

public class AlarmTable extends org.jevis.commons.alarm.AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmTable.class);
    private final JEVisDataSource ds;
    private final List<Alarm> alarms;
    private final SampleHandler sampleHandler;

    public AlarmTable(JEVisDataSource ds, List<Alarm> alarms) {
        super(ds);
        this.ds = ds;
        this.sampleHandler = new SampleHandler();
        this.alarms = alarms;

        try {
            getLimitTableString();
        } catch (JEVisException e) {
            logger.error("Could not initialize.");
        }
    }

    private void getLimitTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();

        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append(I18n.getInstance().getString("alarms.table.title.limits")).append("</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.organisation")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.building")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.rawdata")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.cleandata")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.timestamp")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.currentvalue")).append("</th>");
        sb.append("    <th></th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.setvalue")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.alarmtype")).append("</th>");
        sb.append("  </tr>");//border=\"0\"

        boolean odd = false;
        boolean empty = true;
        for (Alarm currentAlarm : alarms) {
            if (currentAlarm.getAlarmType().equals(AlarmType.L1) || currentAlarm.getAlarmType().equals(AlarmType.L2)) {
                empty = false;
                String name = currentAlarm.getObject().getName() + ":" + currentAlarm.getObject().getID().toString();
                String nameRaw = "";

                boolean hasRawDataObject = false;
                JEVisObject currentRawDataObject = CommonMethods.getFirstParentalDataObject(currentAlarm.getObject());
                JEVisObject currentSiteObject = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), JC.MonitoredObject.Building.name);
                DateTimeZone dateTimeZone = sampleHandler.getLastSample(currentSiteObject, JC.MonitoredObject.Building.a_Timezone, DateTimeZone.UTC);
                Period periodForDate = CleanDataObject.getPeriodForDate(currentAlarm.getObject(), currentAlarm.getTimeStamp());
                String formatString = PeriodHelper.getFormatString(periodForDate, false);

                String currentUnit = null;
                try {
                    currentUnit = UnitManager.getInstance().format(currentAlarm.getAttribute().getDisplayUnit());
                    if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString()))
                        currentUnit = currentAlarm.getAttribute().getDisplayUnit().getLabel();
                } catch (Exception e) {
                    logger.error("Could not parse Unit.");
                }

                if (!currentRawDataObject.equals(currentAlarm.getObject())) {
                    nameRaw = currentRawDataObject.getName() + ":" + currentRawDataObject.getID().toString();
                    hasRawDataObject = true;
                }

                String css = rowCss;
                if (odd) {
                    css += highlight;
                }
                odd = !odd;

                sb.append("<tr>");
                /**
                 * Organisation Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                JEVisObject organization = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), JC.Organization.name);
                if (organization != null) {
                    sb.append(organization.getName());
                }
                sb.append("</td>");
                /**
                 * Building Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                JEVisObject building = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), "Building");
                if (building != null) {
                    sb.append(building.getName());
                }
                sb.append("</td>");
                /**
                 * Raw Datapoint Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                if (hasRawDataObject) {
                    JEVisObject dataObject = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), JC.Data.name);
                    if (dataObject != null) {
                        sb.append(dataObject.getName());
                    }
                }
                sb.append("</td>");
                /**
                 * Clean Datapoint Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(name);
                sb.append("</td>");
                /**
                 * Time Stamp
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                JEVisSample currentSample = null;
                try {
                    currentSample = getCurrentSample(currentAlarm);
                } catch (Exception e) {
                    logger.error("Could not get current Value.");
                }
                if (currentSample != null) {
                    sb.append(currentSample.getTimestamp().withZone(dateTimeZone).toString(formatString));
                }
                sb.append("</td>");

                /**
                 * Is Value
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                if (currentSample != null) {
                    sb.append(currentSample.getValueAsDouble());
                    if (currentUnit != null) {
                        sb.append(" ").append(currentUnit);
                    }
                }
                sb.append("</td>");
                /**
                 * Operator
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                if (currentAlarm.getOperator() != null) {
                    sb.append(currentAlarm.getOperator());
                }
                sb.append("</td>");
                /**
                 * Should-be Value
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                Double limitValue = null;
                try {
                    limitValue = getLimitAlarmValue(currentAlarm);
                } catch (Exception e) {
                    logger.error("Could not get current Value.");
                }
                if (limitValue != null) {
                    sb.append(limitValue);
                    if (currentUnit != null) {
                        sb.append(" ").append(currentUnit);
                    }
                }
                sb.append("</td>");
                /**
                 * Alarm Type
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(currentAlarm.getAlarmType());
                JEVisSample smp = currentAlarm.getAlarmSample();
                if (smp != null) {
                    String currentNote = smp.getNote();
                    sb.append(" ");
                    if (currentNote.contains(NoteConstants.Limits.LIMIT_DEFAULT)) {
                        sb.append(NoteConstants.Limits.LIMIT_DEFAULT);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_STATIC)) {
                        sb.append(NoteConstants.Limits.LIMIT_STATIC);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_AVERAGE)) {
                        sb.append(NoteConstants.Limits.LIMIT_AVERAGE);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_MEDIAN)) {
                        sb.append(NoteConstants.Limits.LIMIT_MEDIAN);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_INTERPOLATION)) {
                        sb.append(NoteConstants.Limits.LIMIT_INTERPOLATION);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_MIN)) {
                        sb.append(NoteConstants.Limits.LIMIT_MIN);
                    } else if (currentNote.contains(NoteConstants.Limits.LIMIT_MAX)) {
                        sb.append(NoteConstants.Limits.LIMIT_MAX);
                    }
                }
                sb.append("</td>");

                sb.append("</tr>");// style=\"border: 1px solid #D9E4E6;\">");
            }
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");

        if (empty) {
            setTableString("");
        } else {
            setTableString(sb.toString());
        }
    }

    private JEVisSample getCurrentSample(Alarm currentAlarm) throws JEVisException {
        DateTime dt = currentAlarm.getAlarmSample().getTimestamp();
        List<JEVisSample> values = currentAlarm.getAttribute().getSamples(dt, dt);

        if (values.size() == 1) {
            return values.get(0);
        }
        return null;
    }

    private Double getLimitAlarmValue(Alarm currentAlarm) throws JEVisException {

        DateTime dt = currentAlarm.getAlarmSample().getTimestamp();
        List<JEVisSample> values = currentAlarm.getAttribute().getSamples(dt.minusMillis(1), dt.plusMillis(1));

        CleanDataObject clean = new CleanDataObject(currentAlarm.getObject());
        List<JsonLimitsConfig> configList = clean.getLimitsConfig();

        if (values.size() == 1) {
            double value = values.get(0).getValueAsDouble();
            Double min = null;
            Double max = null;

            switch (currentAlarm.getAlarmType()) {
                case L1:
                    min = Double.parseDouble(configList.get(0).getMin());
                    max = Double.parseDouble(configList.get(0).getMax());
                    break;
                case L2:
                    min = Double.parseDouble(configList.get(1).getMin());
                    max = Double.parseDouble(configList.get(1).getMax());
                    break;
            }

            if (min != null && max != null) {
                if (value < min) {
                    return min;
                } else if (value > max) {
                    return max;
                }
            }
        }
        return null;
    }

    public String getAlarmTable() throws JEVisException {
        StringBuilder sb = new StringBuilder();

        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append(I18n.getInstance().getString("alarms.table.title.alarms")).append("</h2>");

        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.organisation")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.building")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.rawdata")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.cleandata")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.timestamp")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.currentvalue")).append("</th>");
        sb.append("    <th></th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.setvalue")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("alarms.table.captions.alarmtype")).append("</th>");
        sb.append("  </tr>");//border=\"0\"

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        boolean odd = false;
        boolean empty = true;
        for (Alarm currentAlarm : alarms) {
            if (currentAlarm.getAlarmType().equals(AlarmType.DYNAMIC) || currentAlarm.getAlarmType().equals(AlarmType.STATIC)
                    && currentAlarm.getLogValue() == 1) {
                empty = false;
                String name = currentAlarm.getObject().getName() + ":" + currentAlarm.getObject().getID().toString();
                String nameRaw = "";

                boolean hasRawDataObject = false;
                JEVisObject currentRawDataObject = CommonMethods.getFirstParentalDataObject(currentAlarm.getObject());
                JEVisObject currentSiteObject = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), JC.MonitoredObject.Building.name);
                DateTimeZone dateTimeZone = sampleHandler.getLastSample(currentSiteObject, JC.MonitoredObject.Building.a_Timezone, DateTimeZone.UTC);
                Period periodForDate = CleanDataObject.getPeriodForDate(currentAlarm.getObject(), currentAlarm.getTimeStamp());
                String formatString = PeriodHelper.getFormatString(periodForDate, false);

                if (!currentRawDataObject.equals(currentAlarm.getObject())) {
                    nameRaw = currentRawDataObject.getName() + ":" + currentRawDataObject.getID().toString();
                    hasRawDataObject = true;
                }

                String css = rowCss;
                if (odd) {
                    css += highlight;
                }
                odd = !odd;

                sb.append("<tr>");
                /**
                 * Organisation Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                JEVisObject organization = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), JC.Organization.name);
                if (organization != null) {
                    sb.append(organization.getName());
                }
                sb.append("</td>");
                /**
                 * Building Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                JEVisObject building = CommonMethods.getFirstParentalObjectOfClass(currentAlarm.getObject(), "Building");
                if (building != null) {
                    sb.append(building.getName());
                }
                sb.append("</td>");
                /**
                 * Raw Datapoint Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                if (hasRawDataObject) {
                    sb.append(nameRaw);
                }
                sb.append("</td>");
                /**
                 * Clean Datapoint Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(name);
                sb.append("</td>");
                /**
                 * Time Stamp
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(currentAlarm.getTimeStamp().withZone(dateTimeZone).toString(formatString));
                sb.append("</td>");
                /**
                 * Is Value
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(nf.format(currentAlarm.getIsValue()));
                String currentUnit = null;
                try {
                    currentUnit = UnitManager.getInstance().format(currentAlarm.getAttribute().getDisplayUnit());
                    if (currentUnit.equals("") || currentUnit.equals(AbstractUnit.ONE.toString()))
                        currentUnit = currentAlarm.getAttribute().getDisplayUnit().getLabel();
                } catch (Exception e) {
                    logger.error("Could not parse Unit.");
                }
                if (currentUnit != null) sb.append(" ").append(currentUnit);
                sb.append("</td>");
                /**
                 * Operator
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                if (currentAlarm.getOperator() != null) {
                    sb.append(currentAlarm.getOperator());
                }
                sb.append("</td>");
                /**
                 * Should-be Value
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(nf.format(currentAlarm.getSetValue()));
                if (currentUnit != null) sb.append(" ").append(currentUnit);
                sb.append("</td>");
                /**
                 * Alarm Type
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(currentAlarm.getTranslatedTypeName());
                sb.append("</td>");

                sb.append("</tr>");// style=\"border: 1px solid #D9E4E6;\">");
            }
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");

        if (empty) {
            return "";
        } else {
            return sb.toString();
        }
    }
}
