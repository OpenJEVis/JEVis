package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.jeconfig.tool.Layouts;

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
    private Node node = null;
    private String noteString = null;
//    private static final Image warning = ResourceLoader.getImage("Warning-icon.png");
//    private static final Image limit = ResourceLoader.getImage("rodentia-icons_dialog-warning.png");
//    private static final Image exception = ResourceLoader.getImage("rodentia-icons_process-stop.png");
//    private static final Image infinity = ResourceLoader.getImage("32423523543543_error_div0.png");

    public Note(JEVisSample sample, JEVisSample noteSample) throws JEVisException {
//        DateTime timeStamp = sample.getTimestamp();
        String note = sample.getNote();
        ObjectHandler objectHandler = new ObjectHandler(sample.getDataSource());

        if(noteSample!=null && !note.contains(USER_NOTES)){
            note+=","+USER_NOTES;
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
                    toolTipString+=noteSample.getValueAsString();

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

            if (changed) {
//                Pane hbox = new Pane() {
//                    @Override
//                    protected void setWidth(double value) {
//                        //
//                    }
//
//                    @Override
//                    protected void setHeight(double value) {
//                        //
//                    }
//                };
                this.noteString = sb.toString();
                Label label = new Label(sb.toString());

                //label.setBorder(new Border(new BorderStroke(Color.LIGHTBLUE,
                //        BorderStrokeStyle.DASHED, CornerRadii.EMPTY, new BorderWidths(1))));

                label.setStyle("-fx-background-color: #ffffff;");
                //label.setStyle("-fx-background-color: transparent;");
                //Pane hbox = new Pane();
                AnchorPane hbox = new AnchorPane(){

                };
                hbox.setStyle("-fx-background-color: transparent;");

                hbox.getChildren().add(label);
                Layouts.setAnchor(label,1);
                this.node = hbox;
                if (!toolTipString.equals("")) {
                    Tooltip tooltip = new Tooltip(toolTipString);
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
