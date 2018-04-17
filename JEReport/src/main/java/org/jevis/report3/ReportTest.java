///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.jevis.report3;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.jxls.common.Context;
//import org.jxls.util.JxlsHelper;
//
///**
// *
// * @author broder
// */
//public class ReportTest {
//
//    public static void main(String[] args) throws FileNotFoundException {
//
//        InputStream input = new FileInputStream("/Users/broder/tmp/report_proto.xlsx");
//        OutputStream output = new FileOutputStream("/Users/broder/tmp/report_proto_2_test.xls");
//
//        Context context = new Context();
//        DateTime monday = DateTimeFormat.forPattern("yyyy.MM.dd HH:mm:ss").parseDateTime("2016.02.01 00:00:00");
//        DateTime thuesday = monday.plusDays(1);
//        DateTime wednesday = monday.plusDays(2);
//        DateTime thurthday = monday.plusDays(3);
//        DateTime friday = monday.plusDays(4);
//        DateTime saturday = monday.plusDays(5);
//        DateTime sunday = monday.plusDays(6);
//        DateTime mondayOld = monday.minusWeeks(1);
//        DateTime thusdayOld = thuesday.minusWeeks(1);
//        DateTime wednesdayOld = wednesday.minusWeeks(1);
//        DateTime thurthdayOld = thurthday.minusWeeks(1);
//        DateTime fridayOld = friday.minusWeeks(1);
//        DateTime saturdayOld = saturday.minusWeeks(1);
//        DateTime sundayOld = sunday.minusWeeks(1);
//
//        HashMap<Object, Object> valuesMap = new HashMap<>();
//        List<Object> values = new ArrayList<>();
//        Map<Object, Object> valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 150.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(monday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 200.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(thuesday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 250.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(wednesday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 200.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(thurthday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 150.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(friday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 100.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(saturday));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 300.0);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(sunday));
//        values.add(valueEntryMap);
//        valuesMap.put("value", values);
//        context.putVar("mainmeters_current", valuesMap);
//
//        //weekvalues main
//        valuesMap = new HashMap<>();
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 1200);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(monday));
//        valuesMap.put("value", valueEntryMap);
//        context.putVar("mainall", valuesMap);
//
//        //weekvalues last main
//        valuesMap = new HashMap<>();
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 1500);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(mondayOld));
//        valuesMap.put("value", valueEntryMap);
//        context.putVar("main_all_last", valuesMap);
//
//        //weekvalues heating
//        valuesMap = new HashMap<>();
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 2300);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(monday));
//        valuesMap.put("value", valueEntryMap);
//        context.putVar("heatingall", valuesMap);
//
//        //weekvalues last heating
//        valuesMap = new HashMap<>();
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 2500);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(mondayOld));
//        valuesMap.put("value", valueEntryMap);
//        context.putVar("heating_all_last", valuesMap);
//
//        //old week values main
//        valuesMap = new HashMap<>();
//        values = new ArrayList<>();
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 200);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(mondayOld));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 150);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(thuesday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 300);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(wednesday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 150);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(thurthday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 100);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(friday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 50);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(saturday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valueEntryMap = new HashMap<>();
//        valueEntryMap.put("value", 350);
//        valueEntryMap.put("timestamp", DateHelper.transformTimestampsToExcelTime(sunday.minusWeeks(1)));
//        values.add(valueEntryMap);
//        valuesMap.put("value", values);
//        context.putVar("mainmeters_old", valuesMap);
//
//        try {
//            JxlsHelper.getInstance().processTemplate(input, output, context);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            System.out.println(ex.getMessage());
//            Logger.getLogger(ReportTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//}
