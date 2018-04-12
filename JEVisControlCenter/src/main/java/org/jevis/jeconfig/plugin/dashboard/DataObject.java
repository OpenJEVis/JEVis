/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.plugin.dashboard;

import java.awt.Point;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;

/**
 *
 * @author br
 */
public class DataObject {

    private JEVisAttribute att;
    private Point cord = new Point();

    public DataObject(JEVisAttribute att) {
        this.att = att;
    }

    JEVisSample getValue() {
        //if type= gvetLastest
        return att.getLatestSample();
    }

    Point getCoordinates() {
        return cord;
    }

    void setCoordinates(double x, double y) {
        cord.setLocation(x, y);
    }

    JEVisAttribute getAttribute() {
        return att;
    }

}
