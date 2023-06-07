package org.jevis.jecc.tool;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class ScreenSize {


    /**
     * Returns pref size if it fits screensize
     *
     * @param prefSize
     * @return
     */
    public static double fitScreenWidth(double prefSize) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        if (primaryScreenBounds.getWidth() < prefSize) {
            return primaryScreenBounds.getWidth();
        } else {
            return prefSize;
        }
    }

    /**
     * Returns pref size if it fits screensize
     *
     * @param prefSize
     * @return
     */
    public static double fitScreenHeight(double prefSize) {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        if (primaryScreenBounds.getHeight() < prefSize) {
            return primaryScreenBounds.getHeight();
        } else {
            return prefSize;
        }
    }

    /**
     * Return the absolute postion on the Screens of an control node.
     *
     * @param control [x,y]
     * @return
     */
    public static double[] getAbsoluteScreenPosition(Control control) {
        try {
            final Scene scene = control.getScene();
            final Point2D windowCoordinate = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
            final Point2D sceneCoordinate = new Point2D(scene.getX(), scene.getY());
            final Point2D nodeCoordinate = control.localToScene(0.0, 0.0);
            final double clickX = Math.round(windowCoordinate.getX() + sceneCoordinate.getX() + nodeCoordinate.getX());
            final double clickY = Math.round(windowCoordinate.getY() + sceneCoordinate.getY() + nodeCoordinate.getY());
            return new double[]{clickX, clickY};
        } catch (Exception ex) {
            return new double[]{0, 0};
        }
    }

    /**
     * After taken an screenshot the layout can be bugged. This function fixes this.
     *
     * @param stage
     */
    public static void fixScreenshotLayout(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        boolean wasMax = stage.isMaximized();
        stage.setMaximized(true);
        stage.setMaximized(false);

        if (wasMax) {
            stage.setMaximized(true);
        } else {
            stage.setHeight(width);
            stage.setHeight(height);
        }

    }

}
