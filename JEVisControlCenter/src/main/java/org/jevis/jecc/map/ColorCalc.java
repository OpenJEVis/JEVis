/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecc.map;

/**
 * @author broder
 */
public class ColorCalc {

    public static java.awt.Color getColorFromJavaFX(javafx.scene.paint.Color fxColor) {
        int r = (int) (255 * fxColor.getRed());
        int g = (int) (255 * fxColor.getGreen());
        int b = (int) (255 * fxColor.getBlue());
        java.awt.Color awtColor = new java.awt.Color(r, g, b);
        return awtColor;
    }
}
