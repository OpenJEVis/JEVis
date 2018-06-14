///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.jevis.jecalc.functional.aggregation;
//
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.jevis.api.JEVisAttribute;
//import org.jevis.api.JEVisClass;
//import org.jevis.api.JEVisDataSource;
//import org.jevis.api.JEVisException;
//import org.jevis.api.JEVisObject;
//import org.jevis.api.JEVisSample;
//import org.jevis.commons.database.SampleHandler;
//import org.jevis.jeapi.ws.JEVisDataSourceWS;
//import org.joda.time.DateTime;
//import org.joda.time.Period;
//
///**
// *
// * @author broder
// */
//public class AggregationLauncher {
//
//    public static void main(String[] args) throws JEVisException {
//        AggregationLauncher launcher = new AggregationLauncher();
//        launcher.run();
//    }
//
//    private void run() throws JEVisException {
//        JEVisDataSource ds = null;
//        try {
////            ds = new JEVisDataSourceWS("http://openjevis.org:18090");
//            ds = new JEVisDataSourceWS("http://start.my-jevis.de:8000");
//            ds.connect("Sys Admin", "MyJEV34Env");
//        } catch (JEVisException ex) {
//            System.exit(1);
//        }
//        JEVisClass jeVisClass = ds.getJEVisClass("Aggregated Data");
//        List<JEVisObject> objects = ds.getObjects(jeVisClass, true);
//        JobCreator jobCr = new JobCreator();
//        Aggregator aggr = new Aggregator();
//        for (JEVisObject aggObj : objects) {
//            System.out.println(aggObj.getID());
//            JEVisAttribute attribute = aggObj.getAttribute("Value");
//            attribute.deleteAllSample();
//            AggregationJob createAggregationJob = jobCr.createAggregationJob(aggObj, new DateTime(0));
//            List<JEVisSample> aggregatedData = aggr.getAggregatedData(createAggregationJob);
//            SampleHandler sampleHandler = new SampleHandler();
//            Period inputSampleRate = attribute.getInputSampleRate();
//            System.out.println(inputSampleRate.toString());
//            System.out.println(attribute.getAllSamples().size());
////            aggObj.commit();
////            attribute.commit();
//            System.out.println(attribute.getAllSamples().size());
//            sampleHandler.importData(aggregatedData, attribute);
//        }
//    }
//
//    public void execute(JEVisObject aggObj) {
//        JobCreator jobCr = new JobCreator();
//        Aggregator aggr = new Aggregator();
//        System.out.println(aggObj.getID());
//        AggregationJob createAggregationJob = jobCr.createAggregationJob(aggObj, new DateTime(0));
//        List<JEVisSample> aggregatedData = aggr.getAggregatedData(createAggregationJob);
//        SampleHandler sampleHandler = new SampleHandler();
//        try {
//            JEVisAttribute attribute = aggObj.getAttribute("Value");
//            sampleHandler.importData(aggregatedData, attribute);
//        } catch (JEVisException ex) {
//            Logger.getLogger(AggregationLauncher.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//}
