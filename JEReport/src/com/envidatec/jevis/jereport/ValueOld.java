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
//Interface oder von Sample erben?
public class ValueOld extends AbstractValue {

    ValueOld(List<JevSample<Object>> listOfSamples, JevDataMap map) {
        super(listOfSamples, map);
    }
}