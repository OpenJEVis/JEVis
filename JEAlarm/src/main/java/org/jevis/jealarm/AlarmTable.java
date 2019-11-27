package org.jevis.jealarm;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import javax.measure.unit.Unit;
import java.text.NumberFormat;
import java.util.List;

public class AlarmTable extends org.jevis.commons.alarm.AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmTable.class);
    private final JEVisDataSource ds;
    private final List<Alarm> alarms;

    public AlarmTable(JEVisDataSource ds, List<Alarm> alarms) {
        super(ds);
        this.ds = ds;
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

        sb.append("<h2>Limits</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Organisation</th>");
        sb.append("    <th>Building</th>");
        sb.append("    <th>Raw Datapoint</th>");
        sb.append("    <th>Clean Datapoint Class</th>");
        sb.append("    <th>Time Stamp</th>");
        sb.append("    <th>Value</th>");
        sb.append("    <th>Alarm Type</th>");
        sb.append("  </tr>");//border=\"0\"

        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        JEVisClass buildingClass = ds.getJEVisClass("Monitored Object");
        JEVisClass rawDataClass = ds.getJEVisClass("Data");

        boolean odd = false;
        boolean empty = true;
        for (Alarm currentAlarm : alarms) {
            if (currentAlarm.getAlarmType().equals(AlarmType.L1) || currentAlarm.getAlarmType().equals(AlarmType.L2)) {
                empty = false;
                String name = currentAlarm.getObject().getName() + ":" + currentAlarm.getObject().getID().toString();
                String nameRaw = "";

                boolean hasRawDataObject = false;
                JEVisObject currentRawDataObject = null;
                for (JEVisObject parent : currentAlarm.getObject().getParents()) {
                    try {
                        JEVisClass parentClass = parent.getJEVisClass();
                        if (parentClass != null && parentClass.equals(rawDataClass)) {
                            hasRawDataObject = true;
                            currentRawDataObject = parent;
                            break;
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }

                String currentUnit = null;
                try {
                    currentUnit = UnitManager.getInstance().format(currentAlarm.getAttribute().getDisplayUnit());
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString()))
                        currentUnit = currentAlarm.getAttribute().getDisplayUnit().getLabel();
                } catch (Exception e) {
                    logger.error("Could not parse Unit.");
                }

                if (hasRawDataObject) {
                    nameRaw = currentRawDataObject.getName() + ":" + currentRawDataObject.getID().toString();
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
                sb.append(getParentName(currentAlarm.getObject(), organizationClass));
                sb.append("</td>");
                /**
                 * Building Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(getParentName(currentAlarm.getObject(), buildingClass));
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
                JEVisSample currentSample = null;
                try {
                    currentSample = getCurrentSample(currentAlarm);
                } catch (Exception e) {
                    logger.error("Could not get current Value.");
                }
                if (currentSample != null) {
                    sb.append(currentSample.getTimestamp().toString("yyyy-MM-dd HH:mm:ss"));
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
        List<JEVisSample> values = currentAlarm.getAttribute().getSamples(dt.minusMillis(1), dt.plusMillis(1));

        CleanDataObject clean = new CleanDataObject(currentAlarm.getObject(), new ObjectHandler(ds));
        List<JsonLimitsConfig> configList = clean.getLimitsConfig();

        if (values.size() == 1) {
            return values.get(0);
        }
        return null;
    }

    private Double getLimitAlarmValue(Alarm currentAlarm) throws JEVisException {

        DateTime dt = currentAlarm.getAlarmSample().getTimestamp();
        List<JEVisSample> values = currentAlarm.getAttribute().getSamples(dt.minusMillis(1), dt.plusMillis(1));

        CleanDataObject clean = new CleanDataObject(currentAlarm.getObject(), new ObjectHandler(ds));
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

        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        JEVisClass buildingClass = ds.getJEVisClass("Monitored Object");
        JEVisClass rawDataClass = ds.getJEVisClass("Data");

        sb.append("<h2>Alarms</h2>");

        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Organisation</th>");
        sb.append("    <th>Building</th>");
        sb.append("    <th>Raw Datapoint</th>");
        sb.append("    <th>Clean Datapoint Class</th>");
        sb.append("    <th>Time Stamp</th>");
        sb.append("    <th>Is Value</th>");
        sb.append("    <th>Should-be Value</th>");
        sb.append("    <th>Alarm Type</th>");
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
                JEVisObject currentRawDataObject = null;
                for (JEVisObject parent : currentAlarm.getObject().getParents()) {
                    try {
                        JEVisClass parentClass = parent.getJEVisClass();
                        if (parentClass != null && parentClass.equals(rawDataClass)) {
                            hasRawDataObject = true;
                            currentRawDataObject = parent;
                            break;
                        }
                    } catch (JEVisException e) {
                        e.printStackTrace();
                    }
                }

                if (hasRawDataObject) {
                    nameRaw = currentRawDataObject.getName() + ":" + currentRawDataObject.getID().toString();
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
                sb.append(getParentName(currentAlarm.getObject(), organizationClass));
                sb.append("</td>");
                /**
                 * Building Column
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(getParentName(currentAlarm.getObject(), buildingClass));
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
                sb.append(currentAlarm.getTimeStamp().toString("yyyy-MM-dd HH:mm:ss"));
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
                    if (currentUnit.equals("") || currentUnit.equals(Unit.ONE.toString()))
                        currentUnit = currentAlarm.getAttribute().getDisplayUnit().getLabel();
                } catch (Exception e) {
                    logger.error("Could not parse Unit.");
                }
                if (currentUnit != null) sb.append(" ").append(currentUnit);
                sb.append("</td>");
                /**
                 * Should-be Value
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(nf.format(currentAlarm.getShouldBeValue()));
                if (currentUnit != null) sb.append(" ").append(currentUnit);
                sb.append("</td>");
                /**
                 * Alarm Type
                 */
                sb.append("<td style=\"");
                sb.append(css);
                sb.append("\">");
                sb.append(currentAlarm.getAlarmType());
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
