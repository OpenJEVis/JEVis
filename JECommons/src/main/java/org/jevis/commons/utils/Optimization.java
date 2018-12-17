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
        System.out.println("==== Optimization ====");
        System.out.println("Objects.amount: " + objectAmount);
        System.out.println("Attribute.amount: " + attributeAmount);
        System.out.println("Sample.amount: " + sampleAmount);
//        System.out.println("Object.totalSize: ~" + (sampleAmount * 3.5) + "mb");
        System.out.println("Memory.used:  " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / megabyte + "mb");
        System.out.println("Memory.total: " + (Runtime.getRuntime().totalMemory()) / megabyte + "mb");
        System.out.println("Memory.free:  " + (Runtime.getRuntime().freeMemory()) / megabyte + "mb");

//        Stream<Map.Entry<String, Long>> sorted =
//                byClass.entrySet().stream()
//                        .sorted(Map.Entry.comparingByValue());
//
//        sorted.forEach(stringLongEntry -> {
//            System.out.println("Class: '" + stringLongEntry.getKey() + "' amount: " + stringLongEntry.getValue());
//        });
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

}
