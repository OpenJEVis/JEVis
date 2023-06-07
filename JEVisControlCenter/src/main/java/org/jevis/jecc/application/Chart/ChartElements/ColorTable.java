package org.jevis.jecc.application.Chart.ChartElements;

import javafx.scene.paint.Color;

import java.util.List;

public class ColorTable {
    public static final Color[] color_list = {
            Color.web("0x803E75"),    // Strong Purple
            Color.web("0xFF6800"),    // Vivid Orange
            Color.web("0xA6BDD7"),    // Very Light Blue
            Color.web("0xC10020"),    // Vivid Red
            Color.web("0xCEA262"),    // Grayish Yellow
            Color.web("0x817066"),    // Medium Gray

            Color.web("0x007D34"),    // Vivid Green
            Color.web("0xF6768E"),    // Strong Purplish Pink
            Color.web("0x00538A"),    // Strong Blue
            Color.web("0xFF7A5C"),    // Strong Yellowish Pink
            Color.web("0x53377A"),    // Strong Violet
            Color.web("0xFF8E00"),    // Vivid Orange Yellow
            Color.web("0xB32851"),    // Strong Purplish Red
            Color.web("0xF4C800"),    // Vivid Greenish Yellow
            Color.web("0x7F180D"),    // Strong Reddish Brown
            Color.web("0x93AA00"),    // Vivid Yellowish Green
            Color.web("0x593315"),    // Deep Yellowish Brown
            Color.web("0xF13A13"),    // Vivid Reddish Orange
            Color.web("0x232C16"),    // Dark Olive Green
            Color.web("0xFFB300"),    // Vivid Yellow
    };
    public static Color STANDARD_COLOR = Color.LIGHTBLUE;

    public static Color getNextColor(List<Color> usedColors) {
        for (Color color : color_list) {
            if (!usedColors.contains(color)) {
                usedColors.add(color);
                return color;
            }
        }
        return STANDARD_COLOR;
    }
}
