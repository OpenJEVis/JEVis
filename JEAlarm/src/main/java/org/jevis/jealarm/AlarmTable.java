package org.jevis.jealarm;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.constants.NoteConstants;

import java.util.List;

public class AlarmTable extends org.jevis.commons.alarm.AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmTable.class);
    private final JEVisDataSource ds;
    private final List<Alarm> alarms;

    public AlarmTable(JEVisDataSource ds, List<Alarm> alarms) {
        this.ds = ds;
        this.alarms = alarms;

        try {
            createTableString();
        } catch (JEVisException e) {
            logger.error("Could not initialize.");
        }
    }

    private void createTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();

        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>Alarm!</h2>");

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
        sb.append("    <th>Value</th>");
        sb.append("    <th>Alarm Type</th>");
        sb.append("  </tr>");//border=\"0\"

        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        JEVisClass buildingClass = ds.getJEVisClass("Monitored Object");
        JEVisClass rawDataClass = ds.getJEVisClass("Data");

        boolean odd = false;
        for (Alarm currentAlarm : alarms) {
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
             * Value
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(currentAlarm.getSample().getValueAsDouble());
            sb.append(" ").append(currentAlarm.getSample().getUnit());
            sb.append("</td>");
            /**
             * Alarm Type
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(currentAlarm.getAlarmType());
            JEVisSample smp = currentAlarm.getSample();
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

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");

        setTableString(sb.toString());
    }
}
