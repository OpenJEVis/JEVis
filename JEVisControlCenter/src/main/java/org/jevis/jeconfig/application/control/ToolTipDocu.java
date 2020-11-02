package org.jevis.jeconfig.application.control;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.transform.Rotate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ToolTipDocu {

    private static final Logger logger = LogManager.getLogger(ToolTipDocu.class);
    private ObservableList<Control> controls = FXCollections.observableArrayList();
    private BooleanProperty isShowing = new SimpleBooleanProperty(false);


    public void showTooltips(boolean show) {
        logger.debug("Show tooltips: {}", show);

        for (Object obj : controls) {
            try {
                if (obj instanceof Control) {
                    Control control = (Control) obj;
                    Tooltip tooltip = control.getTooltip();
                    if (tooltip != null && !tooltip.getText().isEmpty()) {
                        if (tooltip.getGraphic() == null) tooltip.setGraphic(new Region());
                        if (tooltip.isShowing() != show) {
                            if (tooltip.isShowing()) Platform.runLater(() -> {
                                tooltip.hide();
                                Label parent = (Label) tooltip.getGraphic().getParent();
                                parent.getTransforms().clear();
                            });
                            else {
                                Bounds sceneBounds = control.localToScene(control.getBoundsInLocal());
                                double x = sceneBounds.getMinX() + 2;
                                double y = sceneBounds.getMinY();// + 25;//4;

                                Platform.runLater(() -> {
                                    try {


                                        tooltip.show(control, x + 27, y + 60);
                                        Label parent = (Label) tooltip.getGraphic().getParent();
                                        parent.getTransforms().add(new Rotate(90));
                                    } catch (Exception ex) {
                                        logger.warn(ex, ex);
                                    }
                                });
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                logger.error(ex, ex);
            }
        }
        isShowing.setValue(show);
    }

    public void toggle() {
        showTooltips(!isShowing.get());
    }

    public ObservableBooleanValue isShowingProperty() {
        return isShowing;
    }

    public void addItems(List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof Control) {
                addControl((Control) node);
            }
        }
    }


    public void addItems(Node... nodes) {
        for (Node node : nodes) {
            if (node instanceof Control) {
                addControl((Control) node);
            }
        }
    }

    public void addControl(Control... elements) {
        for (Control element : elements) {
            if (!controls.contains(element)) controls.add(element);
        }
    }

}
