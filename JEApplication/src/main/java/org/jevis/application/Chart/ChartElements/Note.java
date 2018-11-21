package org.jevis.application.Chart.ChartElements;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jevis.application.resource.ResourceLoader;

public class Note extends Node {
    private Node node = null;
    private static final Image warning = ResourceLoader.getImage("Warning-icon.png");
    private static final Image limit = ResourceLoader.getImage("rodentia-icons_dialog-warning.png");
    private static final Image exception = ResourceLoader.getImage("rodentia-icons_process-stop.png");

    public Note(String note, Color color) {
        if (note != null) {
            HBox hbox = new HBox();
            double iconSize = 12;
            Boolean changed = false;

            if (note.contains("limit(Step1)")) {
                try {
                    ImageView warning = new ImageView(Note.warning);
                    warning.fitHeightProperty().set(iconSize);
                    warning.fitWidthProperty().set(iconSize);

                    BorderPane warningWrapper = new BorderPane(warning);
                    warningWrapper.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    hbox.getChildren().add(warningWrapper);
                    changed = true;
                } catch (Exception e) {
                }
            }
            if (note.contains("gap(") || note.contains("limit(Default)") || note.contains("limit(Static)") || note.contains("limit(Average)")
                    || note.contains("limit(Median)") || note.contains("limit(Interpolation)") || note.contains("limit(Min)") || note.contains("limit(Max)")) {
                try {
                    ImageView limit = new ImageView(Note.limit);
                    limit.fitHeightProperty().set(iconSize);
                    limit.fitWidthProperty().set(iconSize);

                    BorderPane limitWrapper = new BorderPane(limit);
                    limitWrapper.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    hbox.getChildren().add(limitWrapper);
                    changed = true;
                } catch (Exception e) {
                }
            }

            if (hbox.getChildren().size() == 2) {
                hbox.getChildren().clear();
                try {
                    ImageView exception = new ImageView(Note.exception);
                    exception.fitHeightProperty().set(iconSize);
                    exception.fitWidthProperty().set(iconSize);

                    BorderPane exceptionWrapper = new BorderPane(exception);
                    exceptionWrapper.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    hbox.getChildren().add(exceptionWrapper);
                    changed = true;
                } catch (Exception e) {
                }
            }

            if (changed) this.node = hbox;
        }
    }

    public Node getNote() {
        return node;
    }


    @Override
    protected NGNode impl_createPeer() {
        return null;
    }

    @Override
    public BaseBounds impl_computeGeomBounds(BaseBounds bounds, BaseTransform tx) {
        return null;
    }

    @Override
    protected boolean impl_computeContains(double localX, double localY) {
        return false;
    }

    @Override
    public Object impl_processMXNode(MXNodeAlgorithm alg, MXNodeAlgorithmContext ctx) {
        return null;
    }
}
