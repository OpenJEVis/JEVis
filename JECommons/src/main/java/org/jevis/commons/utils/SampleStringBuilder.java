/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.utils;

import java.util.List;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;

/**
 *
 * @author Florian Simon
 */
public class SampleStringBuilder {

    public static String toString(List<JEVisSample> samples) throws JEVisException {
        if (samples == null || samples.isEmpty()) {
            return " -No Samples- ";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Sample count: ");
        sb.append(samples.size());
        sb.append("\n");
        sb.append("Sample between: ");
        sb.append(samples.get(0).getTimestamp());
        sb.append(" - ");
        sb.append(samples.get(samples.size() - 1).getTimestamp());
        sb.append("\n");

        for (JEVisSample sample : samples) {
            sb.append("[");
            sb.append(sample.getTimestamp());
            sb.append("] ");
            sb.append(sample.getValueAsString());
            sb.append("\n");
        }
        sb.append("-----------");

        return sb.toString();
    }
}
