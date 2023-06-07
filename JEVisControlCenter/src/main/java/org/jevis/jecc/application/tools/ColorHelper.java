package org.jevis.jecc.application.tools;

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

    public static String toShortRGBCode(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()))
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

    public static Color colorToBrighter(Color color) {

        int red = (int) (color.getRed() * 255);
        red += 0.9 * red;
        if (red > 255) red = 255;
        int green = (int) (color.getGreen() * 255);
        green += 0.9 * green;
        if (green > 255) green = 255;
        int blue = (int) (color.getBlue() * 255);
        blue += 0.9 * blue;
        if (blue > 255) blue = 255;

        return ColorHelper.toColor(String.format("#%02x%02x%02x", red, green, blue));
    }

    public static Color getHighlightColor(Color color) {
        double[] c = new double[3];
        c[0] = color.getRed();
        c[1] = color.getGreen();
        c[2] = color.getBlue();

        for (int i = 0; i < c.length; i++) {
            if (c[i] <= 0.03928) c[i] = c[i] / 12.92;
            else c[i] = Math.pow(((c[i] + 0.055) / 1.055), 2.4);
        }

        double l = 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2];

        if (l > 0.179) return Color.web("#000000");
        else return Color.web("#ffffff");
    }
}
