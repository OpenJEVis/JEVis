package org.jevis.commons.utils;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class to help find memory leaks by creating some statistics
 */
public class Optimization {

    private static Optimization instance;
    private long attributeAmount = 0;
    private long objectAmount = 0;
    private long sampleAmount = 0;
    private long megabyte = 1024l * 1024l;
    private long objectSize = 0;
    private Map<String, Long> byClass = new HashMap<>();

    public static Optimization getInstance() {
        if (Optimization.instance == null) {
            Optimization.instance = new Optimization();
        }
        return Optimization.instance;
    }


    public void printStatistics() {
        System.out.println(String.format("Data Object/Attribute/Samples: %s/%s/%s", objectAmount, attributeAmount, sampleAmount));
        System.out.println(String.format("Memory used %s mb / total reserved: %s mb",
                ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / megabyte),
                (Runtime.getRuntime().totalMemory() / megabyte)));

    }

    public void addSample(JEVisSample sample) {
        sampleAmount++;
    }


    public void addAttribute(JEVisAttribute att) {
        attributeAmount++;
    }

    public void addObject(JEVisObject obj) {
//        System.out.print(".");
        objectAmount++;
//        try {
//            long oldValue = byClass.getOrDefault(obj.getJEVisClassName(), 0l) + 1;
//            byClass.put(obj.getJEVisClassName(), oldValue);
//        } catch (Exception ex) {
//
//        }
//        long size = RamUsageEstimator.sizeOf(obj);
//        objectSize += size;
//        if (obj.getID() == 7921) {
//            System.out.println("Objectsize: " + RamUsageEstimator.humanSizeOf(obj));
//        }
    }

    public void clearCache() {
        objectAmount = 0;
        attributeAmount = 0;
        sampleAmount = 0;
    }
}
