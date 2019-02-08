package org.jevis.jeconfig.application.Chart.ChartElements;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.jevis.jeconfig.tool.I18n;

public class Note {
    private Node node = null;
//    private static final Image warning = ResourceLoader.getImage("Warning-icon.png");
//    private static final Image limit = ResourceLoader.getImage("rodentia-icons_dialog-warning.png");
//    private static final Image exception = ResourceLoader.getImage("rodentia-icons_process-stop.png");
//    private static final Image infinity = ResourceLoader.getImage("32423523543543_error_div0.png");

    public Note(String note) {
        if (note != null) {
            HBox hbox = new HBox();
            boolean changed = false;
            StringBuilder sb = new StringBuilder();
            int noOfNotes = 0;

            if (note.contains("limit(Step1)")) {
                try {
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit1"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("limit(Default)") || note.contains("limit(Static)") || note.contains("limit(Average)")
                    || note.contains("limit(Median)") || note.contains("limit(Interpolation)") || note.contains("limit(Min)") || note.contains("limit(Max)")) {
                try {
                    if (noOfNotes > 0) sb = new StringBuilder();
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.limit2"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("gap")) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.gap"));
                    noOfNotes++;

                    changed = true;

                } catch (Exception e) {
                }
            }


            if (note.contains("calc(infinite)")) {
                try {
                    if (noOfNotes > 0) sb.append(", ");
                    sb.append(I18n.getInstance().getString("plugin.graph.chart.note.div0"));
                    noOfNotes++;

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("userNotes")) {
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
            }
        }
    }

    public Node getNote() {
        return node;
    }
}
