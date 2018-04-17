/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevDataMap;
import envidatec.jevis.capi.data.JevSample;
import java.util.List;

/**
 *
 * @author broder
 */
public class Value extends AbstractValue {

    Value(List<JevSample<Object>> listOfSamples, JevDataMap map) {
        super(listOfSamples,map);
    }
}