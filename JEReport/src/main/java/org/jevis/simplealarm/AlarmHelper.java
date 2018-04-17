/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.simplealarm;

import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;

/**
 *
 * @author fs
 */
public class AlarmHelper {

    public static String formateValue(JEVisSample sample) {
        String strg = "";

        try {
            strg = JEAlarm.deci.format(sample.getValueAsDouble());

            try {
                if (sample.getUnit() != null && sample.getUnit().getLabel() != null && !sample.getUnit().getLabel().equals("null")) {
                    strg = strg + " " + sample.getUnit().getLabel();
                }

            } catch (Exception ex) {

            }

        } catch (Exception ex) {

        }
        return strg;
    }

    public static String formateValue(Double sample, JEVisUnit unit) {
        String strg = "";

        try {
            strg = JEAlarm.deci.format(sample);

            try {
                if (unit != null && unit.getLabel() != null && !unit.getLabel().equals("null")) {
                    strg = strg + " " + unit.getLabel();
                }

            } catch (Exception ex) {

            }

        } catch (Exception ex) {

        }
        return strg;
    }

}
