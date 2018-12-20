package org.jevis.jeconfig.application.Chart.ChartElements;

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
            double iconSize = 12;
            Boolean changed = false;

            if (note.contains("limit(Step1)")) {
                try {
                    Label labelLimit1 = new Label(I18n.getInstance().getString("plugin.graph.chart.note.limit1"));
                    hbox.getChildren().add(labelLimit1);

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("limit(Default)") || note.contains("limit(Static)") || note.contains("limit(Average)")
                    || note.contains("limit(Median)") || note.contains("limit(Interpolation)") || note.contains("limit(Min)") || note.contains("limit(Max)")) {
                try {
                    Label labelLimit2 = new Label(I18n.getInstance().getString("plugin.graph.chart.note.limit2"));
                    hbox.getChildren().add(labelLimit2);

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("gap")) {
                try {

                    Label labelGap = new Label(I18n.getInstance().getString("plugin.graph.chart.note.gap"));
                    hbox.getChildren().add(labelGap);
                    changed = true;

                } catch (Exception e) {
                }
            }


            if (note.contains("calc(infinite)")) {
                try {
                    Label labelDiv0 = new Label(I18n.getInstance().getString("plugin.graph.chart.note.div0"));
                    hbox.getChildren().add(labelDiv0);

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (note.contains("userNotes")) {
                try {
                    Label labNote = new Label("N");
                    hbox.getChildren().add(labNote);

                    changed = true;
                } catch (Exception e) {
                }
            }

            if (hbox.getChildren().size() >= 2) {
                hbox.getChildren().clear();
                try {
                    Label labelMultiple = new Label(I18n.getInstance().getString("plugin.graph.chart.note.multiple"));
                    hbox.getChildren().add(labelMultiple);

                    changed = true;
                } catch (Exception e) {
                }
            }

            //hbox.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderWidths.EMPTY)));

            if (changed) this.node = hbox;
        }
    }

    public Node getNote() {
        return node;
    }
}
