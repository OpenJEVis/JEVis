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

    public static String colorToBrighter(String colorString) {
        Color color = ColorHelper.toColor(colorString);

        int red = (int) (color.getRed() * 255);
        red += 0.4 * red;
        if (red > 255) red = 255;
        int green = (int) (color.getGreen() * 255);
        green += 0.4 * green;
        if (green > 255) green = 255;
        int blue = (int) (color.getBlue() * 255);
        blue += 0.4 * blue;
        if (blue > 255) blue = 255;

        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
