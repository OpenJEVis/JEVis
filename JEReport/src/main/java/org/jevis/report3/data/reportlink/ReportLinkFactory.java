/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;


/**
 * @author broder
 */
public class ReportLinkFactory {
    private static final Logger logger = LogManager.getLogger(ReportLinkFactory.class);
    private List<JEVisObject> listLinks = new ArrayList<>();

    public List<ReportData> getReportLinks(JEVisObject reportObject) {
        List<JEVisObject> reportLinkObjects = initializeReportLinkObjects(reportObject);
        logger.info("Found " + reportLinkObjects.size() + " Report Links.");

        List<ReportData> reportLinks = new ArrayList<>();
        //if this is a generic report -> collect all children and iterate over this children
        for (JEVisObject reportLinkObject : reportLinkObjects) {

            ReportData linkProperty = ReportLinkProperty.buildFromJEVisObject(reportLinkObject);
            reportLinks.add(linkProperty);

        }
        return reportLinks;
    }

    List<JEVisObject> initializeReportLinkObjects(JEVisObject reportObject) {
        listLinks = new ArrayList<>();
        getChildrenFromDir(reportObject);
        return listLinks;
    }

    private void getChildrenFromDir(JEVisObject currentObject) {
        try {
            currentObject.getChildren().forEach(child -> {
                try {
                    if (child.getJEVisClass().getName().equals(ReportLink.NAME)) listLinks.add(child);
                    else if (child.getJEVisClass().getName().equals(ReportLinkDir.NAME)) {
                        getChildrenFromDir(child);
                    }
                } catch (JEVisException e) {
                    logger.error(e);
                }
            });
        } catch (JEVisException e) {
            logger.error(e);
        }
    }
}
