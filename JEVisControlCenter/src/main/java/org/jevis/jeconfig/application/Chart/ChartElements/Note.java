package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
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

    private Node node = null;
//    private static final Image warning = ResourceLoader.getImage("Warning-icon.png");
//    private static final Image limit = ResourceLoader.getImage("rodentia-icons_dialog-warning.png");
//    private static final Image exception = ResourceLoader.getImage("rodentia-icons_process-stop.png");
//    private static final Image infinity = ResourceLoader.getImage("32423523543543_error_div0.png");

    public Note(JEVisSample sample) throws JEVisException {
        String note = sample.getNote();
        ObjectHandler objectHandler = new ObjectHandler(sample.getDataSource());
        if (note != null) {
            HBox hbox = new HBox();
            boolean changed = false;
            StringBuilder sb = new StringBuilder();
            int noOfNotes = 0;
            String limitsForTooltip = "";

            if (note.contains(LIMIT_STEP1)) {
                try {
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit1"));
                    noOfNotes++;
                    changed = true;

                    try {
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject(), objectHandler);
                        JsonLimitsConfig l1Config = cleanDataObject.getLimitsConfig().get(0);
                        limitsForTooltip += "L1 Min: " + l1Config.getMin() + " L1 Max: " + l1Config.getMax();
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
                        limitsForTooltip += " ";
                    }
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit2"));
                    noOfNotes++;
                    changed = true;

                    try {
                        CleanDataObject cleanDataObject = new CleanDataObject(sample.getAttribute().getObject(), objectHandler);
                        JsonLimitsConfig l2Config = cleanDataObject.getLimitsConfig().get(1);
                        limitsForTooltip += "L2 Min: " + l2Config.getMin() + " L2 Max: " + l2Config.getMax();
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
                } catch (Exception e) {
                }
            }

            if (note.contains(USER_NOTES)) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append("N");
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

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
                Label label = new Label(sb.toString());
                label.setStyle("-fx-background-color: #ffffff;");
                hbox.getChildren().add(label);
                Tooltip tooltip = new Tooltip(limitsForTooltip);
                label.setTooltip(tooltip);
            }
        }
    }

    public Node getNote() {
        return node;
    }
}
