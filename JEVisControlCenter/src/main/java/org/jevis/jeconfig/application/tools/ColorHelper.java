package org.jevis.jeconfig.application.tools;

import javafx.scene.paint.Color;

public class ColorHelper {
    public static Color toColor(String color) {
        return Color.web(color);
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
