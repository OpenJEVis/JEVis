/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.envidatec.jevis.jereport;

import envidatec.jevis.capi.data.JevDataMap;
import envidatec.jevis.capi.data.JevSample;
import envidatec.jevis.capi.data.JevUnit;
import envidatec.jevis.capi.nodes.RegTreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author broder
 */
public abstract class AbstractValue {

    RegTreeNode regNode;
    List<Double> allValues;
    List<JevSample<Object>> sampleList;
    List<Double> sortedList;
    Object first;
    Object last;
    double maxval, minval, avgval, sum;
    JevDataMap siMap;

    public AbstractValue(List<JevSample<Object>> listOfSamples, JevDataMap si) {
        siMap = si;
        sampleList = listOfSamples;
        if (sampleList.size() > 0) {
            first = sampleList.get(0).getVal();
            last = sampleList.get(sampleList.size() - 1).getVal();
        }

        allValues = new ArrayList<Double>();
        sortedList = new ArrayList<Double>();
        minval = Double.MAX_VALUE;
        maxval = 0;
        avgval = 0;
        sum = 0;

        if (listOfSamples.size() > 0) {
            if (listOfSamples.get(0).getVal() instanceof String) {
                for (JevSample s : listOfSamples) {
//                    double tmpval = s.getVal().toString();
//                    //TODO was macht man hier?
//                    allValues.add(tmpval);
//                    sortedList.add(tmpval);
                }
                Collections.sort(sortedList);
            } else {
                for (JevSample s : listOfSamples) {
                    double tmpval = Double.parseDouble(s.getVal().toString());
                    sum += tmpval;
                    allValues.add(tmpval);
                    sortedList.add(tmpval);
                    if (tmpval > maxval) {
                        maxval = tmpval;
                    }
                    if (tmpval < minval) {
                        minval = tmpval;
                    }
                    avgval += tmpval / listOfSamples.size();
                }
                Collections.sort(sortedList);
            }
        }
    }

    public Object getFirst() {
        return first;
    }

    public Object getLast() {
        return last;
    }

    public List<Double> getall() {
        return allValues;
    }

    public double getMax() {
        return maxval;
    }

    public double getAvg() {
        return avgval;
    }

    public double getMin() {
        return minval;
    }

    public double getSum() {
        return sum;
    }

    public double getkwmax() {
        return maxval * 4;    //TODO SO RICHTIG SCHLECHT
    }

    public List<Double> getValuesInkW() {
        List<Double> kwList = new ArrayList<Double>();
        if (!siMap.isEmpty()) {

            JevUnit unit = JevUnit.createUnit("kW");
            List<JevSample<Double>> tmpList = unit.convertEnergyPowerValue(siMap).getListOfSamples();

            for (JevSample s : tmpList) {
                kwList.add((Double) s.getVal());
            }
        }
        return kwList;
    }

    public List<Double> getSortedList() {
        return sortedList;
    }
}
