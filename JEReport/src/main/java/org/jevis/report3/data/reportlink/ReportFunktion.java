///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.jevis.report3.data.reportlink;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.jevis.api.JEVisException;
//import org.jevis.api.JEVisObject;
//import org.jevis.report3.DateHelper;
//import org.jevis.report3.data.report.IntervalCalculator;
//import org.jevis.report3.data.report.ReportProperty;
//import org.joda.time.DateTime;
//import org.joda.time.Interval;
//
///**
// *
// * @author broder
// */
//public class ReportFunktion implements ReportData {
//
//    private String templateName = "";
//    private String modusName;
//
//    public ReportFunktion(JEVisObject funktionObject) {
//        try {
//            templateName = funktionObject.getAttribute("blla").getLatestSample().getValueAsString();        
//            modusName = funktionObject.getAttribute("blla").getLatestSample().getValueAsString();        
//        } catch (JEVisException ex) {
//            Logger.getLogger(ReportFunktion.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    @Override
//    public Map<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc) {
//        IntervalCalculator.PeriodModus modus = IntervalCalculator.PeriodModus.valueOf(modusName.toUpperCase());
//        Interval interval = intervalCalc.getInterval(modus);
//        
//        
//        Map<String, Object> funktionMap = new HashMap<>();
//        funktionMap.put(templateName, getForEachList());
//        return funktionMap;
//    }
//
//    private List getForEachList() {
//        List<Map<String, Object>> functionList = new ArrayList<>();
//        functionList.add(getElement());
//        return functionList;
//    }
//
//    private Map<String, Object> getElement() {
//        //hier is das mit .value/.timestamp usw
//        Map<String, Object> tmpMap = new HashMap<>();
//        tmpMap.put("alarmsoll", 5);
//        Double timestamp = DateHelper.transformTimestampsToExcelTime(new DateTime());
//        tmpMap.put("timestamp", timestamp);
//        return tmpMap;
//    }
//
////    @Override
////    public JEVisObject getDataObject() {
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
////
////    @Override
////    public JEVisObject getLinkObject() {
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }
//
//    @Override
//    public LinkStatus getReportLinkStatus() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//}
