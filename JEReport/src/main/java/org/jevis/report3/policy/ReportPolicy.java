/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.policy;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;

/**
 *
 * @author broder
 */
public class ReportPolicy {

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
            Logger.getLogger(ReportPolicy.class.getName()).log(Level.ERROR, ex);
        }
        return false;
    }

}
