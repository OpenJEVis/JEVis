package org.jevis.jeconfig.tool;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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
}
