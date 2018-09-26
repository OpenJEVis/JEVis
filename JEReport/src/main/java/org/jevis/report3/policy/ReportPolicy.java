/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.policy;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportAttributes;

/**
 *
 * @author broder
 */
public class ReportPolicy {
    private static final Logger logger = LogManager.getLogger(ReportPolicy.class);

//    public Boolean isReportDateReached(ReportProperty dataHandler) {
//        DateTime reportDate = dataHandler.getEndRecord();
//        if (reportDate != null) {
//            return reportDate.isBefore(new DateTime());
//        }
//        return false;
//    }

    public Boolean isReportEnabled(JEVisObject reportObject) {
        try {
            if (reportObject.getAttribute(ReportAttributes.ENABLED).getLatestSample() != null) {
                Boolean enabled = reportObject.getAttribute(ReportAttributes.ENABLED).getLatestSample().getValueAsBoolean();
                if (enabled != null && enabled) {
                    return true;
                }
            } else {
                return false;
            }

        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

}
