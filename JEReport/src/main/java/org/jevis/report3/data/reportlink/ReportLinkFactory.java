/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author broder
 */
public class ReportLinkFactory {
    private static final Logger logger = LogManager.getLogger(ReportLinkFactory.class);

    public List<ReportData> getReportLinks(JEVisObject reportObject) {

        List<JEVisObject> reportLinkObjects = new ArrayList<>(initializeReportLinkObjects(reportObject));
        logger.info("Found {} report links", reportLinkObjects.size());

        List<JEVisObject> alarmLinkObjects = new ArrayList<>(initializeAlarmLinkObjects(reportObject));

        logger.info("Found {} alarm links", alarmLinkObjects.size());

        List<JEVisObject> listLinks = new ArrayList<>();
        listLinks.addAll(reportLinkObjects);
        listLinks.addAll(alarmLinkObjects);

        List<ReportData> reportLinks = new ArrayList<>();
        //if this is a generic report -> collect all children and iterate over this children
        for (JEVisObject reportLinkObject : listLinks) {

            String reportName = null;
            try {
                reportName = reportLinkObject.getJEVisClassName();
            } catch (JEVisException ex) {
                logger.error(ex);
            }
            if (reportName == null) {
                continue;
            }
            if (reportName.equals("Report Link")) {
                ReportData linkProperty = ReportLinkProperty.buildFromJEVisObject(reportLinkObject);
                reportLinks.add(linkProperty);
            } else if (reportName.equals("Alarm Link")) {
                AlarmFunction alarmFunction = new AlarmFunction(reportLinkObject);
                reportLinks.add(alarmFunction);
            }

        }
        return reportLinks;
    }

    List<JEVisObject> initializeReportLinkObjects(JEVisObject reportObject) {
        List<JEVisObject> list = new ArrayList<>();
        return getChildrenFromDir(list, reportObject, ReportLinkDir.NAME, ReportLink.NAME);
    }

    private List<JEVisObject> initializeAlarmLinkObjects(JEVisObject reportObject) {
        List<JEVisObject> list = new ArrayList<>();
        return getChildrenFromDir(list, reportObject, ReportLinkDir.NAME, "Alarm Link");
    }

    private List<JEVisObject> getChildrenFromDir(List<JEVisObject> list, JEVisObject currentObject, String dirName, String className) {
        try {
            for (JEVisObject child : currentObject.getChildren()) {
                try {
                    if (child.getJEVisClass().getName().equals(className)) list.add(child);
                    else if (child.getJEVisClass().getName().equals(dirName)) {
                        getChildrenFromDir(list, child, dirName, className);
                    }
                } catch (JEVisException e) {
                    logger.error(e);
                }
            }
        } catch (JEVisException e) {
            logger.error(e);
        }
        return list;
    }

    private static Map<JEVisObject, JEVisObject> enpiMap;

    public static Map<JEVisObject, JEVisObject> getEnPICalcMap(JEVisDataSource ds) throws JEVisException {
        if (enpiMap == null) {
            Map<JEVisObject, JEVisObject> calcAndResult = new HashMap<>();
            JEVisClass calculation = ds.getJEVisClass("Calculation");
            JEVisClass outputClass = ds.getJEVisClass("Output");

            for (JEVisObject calculationObj : ds.getObjects(calculation, true)) {
                try {
                    List<JEVisObject> outputs = calculationObj.getChildren(outputClass, true);

                    if (outputs != null && !outputs.isEmpty()) {
                        for (JEVisObject output : outputs) {
                            JEVisAttribute targetAttribute = output.getAttribute("Output");
                            if (targetAttribute != null) {
                                try {
                                    TargetHelper th = new TargetHelper(ds, targetAttribute);
                                    if (th.getObject() != null && !th.getObject().isEmpty()) {
                                        calcAndResult.put(th.getObject().get(0), calculationObj);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            enpiMap = calcAndResult;
        }

        return enpiMap;
    }
}
