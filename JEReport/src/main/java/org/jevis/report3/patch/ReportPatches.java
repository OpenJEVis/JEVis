/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.patch;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.database.ClassHandler;

/**
 *
 * @author broder
 */
public class ReportPatches {

    //14.10.2016: Report Patch
    //add a new Reportclass Periodic Report and add it under Report
    //CARE: valid parents for report link not validated
    //CARE: choose right folder for the pic
    public static void insertPeriodicReportUnderReport(JEVisDataSource ds) {
        ClassHandler classHandler = new ClassHandler(ds);
        classHandler.renameClass("Report", "Periodic Report");
        classHandler.createClass("Report");
        classHandler.setIcon("Report", "Report.png");
        classHandler.setInheritanceParent("Report", "Periodic Report");
        classHandler.removeType("Report", "Start Record");
        classHandler.removeType("Report", "Schedule");
        classHandler.addType("Periodic Report", "Start Record");
        classHandler.addType("Periodic Report", "Schedule");

        classHandler.createClass("Report Link Directory");
        classHandler.setOkParent("Report", "Report Link Directory");
        classHandler.setOkParent("Report Link Directory", "Report Link");
    }

    //18.11.2016: Report Patch
    //add Scheduled Report
    //add Event Report
    public static void insertScheduledAndEventReportUnderReport(JEVisDataSource ds) {
        ClassHandler classHandler = new ClassHandler(ds);
        classHandler.renameType("Report", "Last Report", "Report");

        classHandler.createClass("Scheduled Report");
        classHandler.setInheritanceParent("Report", "Scheduled Report");
        classHandler.addType("Scheduled Report", "Interval");

        classHandler.createClass("Event Report");
        classHandler.setInheritanceParent("Report", "Event Report");
        classHandler.addType("Event Report", "JEVis ID");
        classHandler.addType("Event Report", "Attribute Name");
        classHandler.addType("Event Report", "Operator");
        classHandler.addType("Event Report", "Limit");
        classHandler.addType("Event Report", "Start Record");
        
        classHandler.setIcon("Event Report", "Report.png");
        classHandler.setIcon("Scheduled Report", "Report.png");
        
        classHandler.setOkParent("Report Directory", "Scheduled Report");
        classHandler.setOkParent("Event Report", "E-Mail Notification");
        classHandler.setOkParent("Scheduled Report", "E-Mail Notification");
        classHandler.setOkParent("Event Report", "Report Link");
        classHandler.setOkParent("Scheduled Report", "Report Link");
    }

}
