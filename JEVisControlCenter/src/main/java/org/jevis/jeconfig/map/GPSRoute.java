/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.map;

import java.util.List;
import javafx.scene.paint.Color;

/**
 *
 * @author broder
 */
public class GPSRoute {

    private List<GPSSample> gpsSample;
    private Color color;
    private String name;

    public List<GPSSample> getGpsSample() {
        return gpsSample;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setGpsSample(List<GPSSample> gpsSample) {
        this.gpsSample = gpsSample;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
