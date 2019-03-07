package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.tool.I18n;

import static org.jevis.commons.constants.NoteConstants.Calc.CALC_INFINITE;
import static org.jevis.commons.constants.NoteConstants.Gap.GAP;
import static org.jevis.commons.constants.NoteConstants.Limits.*;
import static org.jevis.commons.constants.NoteConstants.User.USER_NOTES;

public class Note {

    private static final Logger logger = LogManager.getLogger(Note.class);
    private Node node = null;
    private String noteString = null;
//    private static final Image warning = ResourceLoader.getImage("Warning-icon.png");
//    private static final Image limit = ResourceLoader.getImage("rodentia-icons_dialog-warning.png");
//    private static final Image exception = ResourceLoader.getImage("rodentia-icons_process-stop.png");
//    private static final Image infinity = ResourceLoader.getImage("32423523543543_error_div0.png");

    public Note(JEVisSample sample) throws JEVisException {
//        DateTime timeStamp = sample.getTimestamp();
        String note = sample.getNote();
        ObjectHandler objectHandler = new ObjectHandler(sample.getDataSource());

        if (note != null) {
            HBox hbox = new HBox();
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
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject(), objectHandler);
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
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject(), objectHandler);
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

//                    try {
//                        JEVisObject obj = sample.getAttribute().getObject();
//                        JEVisObject correspondingNoteObject = null;
//
//                        final JEVisClass dataNoteClass = obj.getDataSource().getJEVisClass("Data Notes");
//                        List<JEVisObject> listParents = obj.getParents();
//                        for (JEVisObject parent : listParents) {
//                            for (JEVisObject child : parent.getChildren()) {
//                                if (child.getJEVisClass().equals(dataNoteClass) && child.getName().contains(obj.getName())) {
//                                    correspondingNoteObject = child;
//                                    break;
//                                }
//                            }
//                        }
//                        if (correspondingNoteObject != null) {
//                            try {
//                                JEVisAttribute userNoteAttribute = correspondingNoteObject.getAttribute("User Notes");
//                                List<JEVisSample> listSamples = userNoteAttribute.getSamples(timeStamp.minusMillis(1), timeStamp.plusMillis(1));
//                                if (listSamples.size() == 1) {
//                                    for (JEVisSample smp : listSamples) {
//                                        toolTipString += smp.getValueAsString();
//                                    }
//                                }
//                            } catch (JEVisException e) {
//
//                            }
//                        }
//
//                    } catch (Exception e) {
//                        logger.error("Error while getting user notes" + e);
//                    }
                } catch (Exception e) {
                }
            }

//            try {
//                JEVisClass cleanDataClass = sample.getDataSource().getJEVisClass("Clean Data");
//                JEVisObject object = sample.getAttribute().getObject();
//                if (object.getJEVisClass().equals(cleanDataClass)) {
//                    JEVisAttribute log = object.getAttribute("Alarm Log");
//                    if (log != null) {
//                        List<JEVisSample> logSamples = log.getSamples(sample.getTimestamp(), sample.getTimestamp());
//                        if (logSamples != null && !logSamples.isEmpty()) {
//                            if (noOfNotes > 0) sb.append(", ");
//                            sb.append(I18n.getInstance().getString("plugin.graph.chart.note.alarm"));
//                            noOfNotes++;
//
//                            changed = true;
//
//                            toolTipString += "Alarm " + logSamples.get(0).getValueAsDouble();
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                logger.error("Error while getting alarm log" + e);
//            }


//            if (hbox.getChildren().size() > ) {
//                hbox.getChildren().clear();
//                try {
//                    Label labelMultiple = new Label(I18n.getInstance().getString("plugin.graph.chart.note.multiple"));
//                    hbox.getChildren().add(labelMultiple);
//
//                    changed = true;
//                } catch (Exception e) {
//                }
//            }

            //hbox.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderWidths.EMPTY)));

            if (changed) {
                hbox.setPadding(new Insets(2, 2, 2, 2));
                this.node = hbox;
                this.noteString = sb.toString();
                Label label = new Label(sb.toString());
                label.setStyle("-fx-background-color: #ffffff;");
                hbox.getChildren().add(label);
                Tooltip tooltip = new Tooltip(toolTipString);
                if (!toolTipString.equals("")) {
                    label.setTooltip(tooltip);
                }
            }
        }
    }

    public Node getNote() {
        return node;
    }

    public String getNoteAsString() {
        return noteString;
    }
}
