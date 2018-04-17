/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevSample;
import java.util.List;

/**
 *
 * @author broder
 */
public class TimestampLastYear extends AbstractTimestamp {

    TimestampLastYear(List<JevSample<Object>> listOfTimestamps) {
        super(listOfTimestamps);
    }
}
