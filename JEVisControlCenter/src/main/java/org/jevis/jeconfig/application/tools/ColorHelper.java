package org.jevis.jeconfig.application.tools;

import javafx.scene.paint.Color;

public class ColorHelper {
    public static Color toColor(String color) {
        return Color.web(color);
    }

    private static String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public static String toRGBCode(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity()))
                .toUpperCase();
    }

    public static String colorToBrighter(String colorString) {
        Color color = ColorHelper.toColor(colorString);

        int red = (int) (color.getRed() * 255);
        red += 0.9 * red;
        if (red > 255) red = 255;
        int green = (int) (color.getGreen() * 255);
        green += 0.9 * green;
        if (green > 255) green = 255;
        int blue = (int) (color.getBlue() * 255);
        blue += 0.9 * blue;
        if (blue > 255) blue = 255;

        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
