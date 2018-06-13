/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author broder
 */
public class ReportLinkFactory {

    public List<ReportData> getReportLinks(JEVisObject reportObject) {
        List<JEVisObject> reportLinkObjects = initializeReportLinkObjects(reportObject);
        List<JEVisObject> alarmLinkObjects = initializeAlarmLinkObjects(reportObject);
        reportLinkObjects.addAll(alarmLinkObjects);

        List<ReportData> reportLinks = new ArrayList<>();
        //if this is a generic report -> collect all children and iterate over this children
        for (JEVisObject reportLinkObject : reportLinkObjects) {
            String reportName = null;
            try {
                reportName = reportLinkObject.getJEVisClassName();
            } catch (JEVisException ex) {
                Logger.getLogger(ReportLinkFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (reportName == null) {
                continue;
            }
//            System.out.println(reportName);
            if (reportName.equals("Report Link")) {
                ReportData linkProperty = ReportLinkProperty.buildFromJEVisObject(reportLinkObject);
                reportLinks.add(linkProperty);
            } else if (reportName.equals("Alarm Link")) {
                //hier anmelden
                reportLinks.add(new AlarmFunction(reportLinkObject));
            }

        }
        return reportLinks;
    }

    List<JEVisObject> initializeReportLinkObjects(JEVisObject reportObject) {
        try {
            return getAllSubLinkDirs(reportObject, reportObject.getDataSource(), ReportLinkDir.NAME, ReportLink.NAME);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportProperty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    private List<JEVisObject> getAllSubLinkDirs(JEVisObject reportObject, JEVisDataSource ds, String dirName, String objName) {
        JEVisClass reportLinkClass = null;
        JEVisClass reportLinkDirClass = null;

        try {
            reportLinkClass = ds.getJEVisClass(objName);
            reportLinkDirClass = ds.getJEVisClass(dirName);
        } catch (JEVisException ex) {
            ex.printStackTrace();
            Logger.getLogger(ReportProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<JEVisObject> currentObjects = new ArrayList<>();
        if (reportObject == null || reportLinkClass == null || reportLinkDirClass == null) {
            return currentObjects;
        }

        List<JEVisObject> allObjects = getChildirenFromDir(reportObject, currentObjects, ds, reportLinkDirClass, reportLinkClass);
        return allObjects;
    }

//    public List<ReportLinkProperty> getReportLinks(JEVisObject obj) {
//        List<JEVisObject> reportLinkObjects = new ArrayList<>();
//        try {
//            reportLinkObjects = getAllSubDirs(obj, obj.getDataSource(), ReportLinkDir.NAME, ReportLink.NAME);
//        } catch (JEVisException ex) {
//            java.util.logging.Logger.getLogger(ReportProperty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//
//        List<ReportLinkProperty> reportLinks = new ArrayList<>();
//        //if this is a generic report -> collect all children and iterate over this children
//        for (JEVisObject reportLinkObject : reportLinkObjects) {
//            ReportLinkProperty linkProperty = ReportLinkProperty.buildFromJEVisObject(reportLinkObject);
//            reportLinks.add(linkProperty);
//        }
//        return reportLinks;
//    }
//    //Todo more generic form and into the commons
//    private List<JEVisObject> getAllSubDirs(JEVisObject reportObject, JEVisDataSource ds, String dirName, String objName) {
//        JEVisClass reportLinkClass = null;
//        JEVisClass reportLinkDirClass = null;
//        try {
//            reportLinkClass = ds.getJEVisClass(objName);
//            reportLinkDirClass = ds.getJEVisClass(dirName);
//        } catch (JEVisException ex) {
//            ex.printStackTrace();
//            Logger.getLogger(ReportProperty.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        List<JEVisObject> currentObjects = new ArrayList<>();
//        if (reportObject == null || reportLinkClass == null || reportLinkDirClass == null) {
//            return currentObjects;
//        }
//
//        List<JEVisObject> allObjects = getChildirenFromDir(reportObject, currentObjects, ds, reportLinkDirClass, reportLinkClass);
//        return allObjects;
//    }
    private List<JEVisObject> getChildirenFromDir(JEVisObject reportObject, List<JEVisObject> currentObjects, JEVisDataSource ds, JEVisClass reportLinkDirClass, JEVisClass reportLinkClass) {
        if (reportObject == null) {
            return currentObjects;
        }

        try {
            List<JEVisObject> children = reportObject.getChildren(reportLinkClass, false);
            currentObjects.addAll(children);
            for (JEVisObject obj : reportObject.getChildren(reportLinkDirClass, false)) {
                currentObjects.addAll(getChildirenFromDir(obj, currentObjects, ds, reportLinkDirClass, reportLinkClass));
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ReportProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentObjects;
    }

    private List<JEVisObject> initializeAlarmLinkObjects(JEVisObject reportObject) {
//        List<JEVisObject> alarmObjects = new ArrayList<>();
//        try {
//            JEVisClass jeVisClass = reportObject.getDataSource().getJEVisClass("Alarm Link");
//            alarmObjects = reportObject.getChildren(jeVisClass, true);
//        } catch (JEVisException ex) {
//            Logger.getLogger(ReportLinkFactory.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return alarmObjects;
        
         try {
            return getAllSubLinkDirs(reportObject, reportObject.getDataSource(), ReportLinkDir.NAME, "Alarm Link");
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportProperty.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        return new ArrayList<>();
    }

    public enum ReportLinkType {

        SINGLE, RECURSIVE
    }
}
