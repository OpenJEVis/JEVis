package org.jevis.commons.chart;

public enum BubbleType {
    X, Y, SIZE, NONE;

    public static BubbleType parseBubbleType(String bubbleType) {
        switch (bubbleType) {
            case ("X"):
                return X;
            case ("Y"):
                return Y;
            case ("SIZE"):
                return SIZE;
            default:
                return NONE;
        }
    }
}
