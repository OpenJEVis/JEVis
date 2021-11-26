package org.jevis.jeconfig.application.Chart.ChartElements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.alarm.Alarm;
import org.jevis.commons.alarm.AlarmType;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;

import static org.jevis.commons.constants.NoteConstants.Calc.CALC_INFINITE;
import static org.jevis.commons.constants.NoteConstants.Differential.COUNTER_OVERFLOW;
import static org.jevis.commons.constants.NoteConstants.Forecast.FORECAST_1;
import static org.jevis.commons.constants.NoteConstants.Forecast.FORECAST_2;
import static org.jevis.commons.constants.NoteConstants.Gap.GAP;
import static org.jevis.commons.constants.NoteConstants.Limits.*;
import static org.jevis.commons.constants.NoteConstants.User.USER_NOTES;
import static org.jevis.commons.constants.NoteConstants.User.USER_VALUE;

public class Note {

    private static final Logger logger = LogManager.getLogger(Note.class);
    private String noteString = null;

    public Note(JEVisSample sample, JEVisSample noteSample, Alarm alarm) throws JEVisException {
        String note = sample.getNote();

        if (noteSample != null && !note.contains(USER_NOTES)) {
            note += "," + USER_NOTES;
        }

        if (alarm != null) {
            note += "," + alarm.getAlarmType();
        }

        if (note != null) {

            boolean changed = false;
            StringBuilder sb = new StringBuilder();
            int noOfNotes = 0;
            String toolTipString = "";

            if (note.contains(LIMIT_STEP1)) {
                try {
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit1"));
                    noOfNotes++;
                    changed = true;

                    try {
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject());
                        JsonLimitsConfig l1Config = cleanDataObject.getLimitsConfig().get(0);
                        toolTipString += "L1 Min: " + l1Config.getMin() + " L1 Max: " + l1Config.getMax();
                    } catch (Exception e) {

                    }
                } catch (Exception e) {
                }
            }

            if (note.contains(LIMIT_DEFAULT) || note.contains(LIMIT_STATIC) || note.contains(LIMIT_AVERAGE)
                    || note.contains(LIMIT_MEDIAN) || note.contains(LIMIT_INTERPOLATION) || note.contains(LIMIT_MIN) || note.contains(LIMIT_MAX)) {
                try {
                    if (noOfNotes > 0) {
                        sb = new StringBuilder();
                        toolTipString += " ";
                    }
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit2"));
                    noOfNotes++;
                    changed = true;

                    try {
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject());
                        JsonLimitsConfig l2Config = cleanDataObject.getLimitsConfig().get(1);
                        toolTipString += "L2 Min: " + l2Config.getMin() + " L2 Max: " + l2Config.getMax();
                    } catch (Exception e) {

                    }
                } catch (Exception e) {
                }
            }

            if (note.contains(GAP)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.gap"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains(FORECAST_1)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.forecast1"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains(FORECAST_2)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.forecast2"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains(COUNTER_OVERFLOW)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.counteroverflow"));
                    noOfNotes++;
                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains(CALC_INFINITE)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.div0"));
                    noOfNotes++;

                    changed = true;

                    toolTipString += I18n.getInstance().getString("plugin.graph.chart.note.div0.long");
                } catch (Exception e) {
                }
            }

            if (note.contains(USER_NOTES)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append("N");
                    noOfNotes++;

                    changed = true;
                    toolTipString += noteSample.getValueAsString();

                } catch (Exception e) {
                }
            }

            if (note.contains(USER_VALUE)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append("U");
                    noOfNotes++;

                    changed = true;

                } catch (Exception e) {
                }
            }

            if (note.contains(AlarmType.DYNAMIC.toString())) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.dynamic"));
                    noOfNotes++;

                    changed = true;

                    toolTipString += I18n.getInstance().getString("plugin.alarm.table.translation.dynamic");
                } catch (Exception e) {
                }
            }

            if (note.contains(AlarmType.STATIC.toString())) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.static"));
                    noOfNotes++;

                    changed = true;

                    toolTipString += I18n.getInstance().getString("plugin.alarm.table.translation.static");
                } catch (Exception e) {
                }
            }


            if (changed) {
                this.noteString = sb.toString();
            }
        }
    }

    public String getNoteAsString() {
        return noteString;
    }

}
