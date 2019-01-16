/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author broder
 */
public interface ReportData {

    ConcurrentHashMap<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc);

    JEVisObject getDataObject();
//
//    public JEVisObject getLinkObject();
LinkStatus getReportLinkStatus(DateTime end);

    class LinkStatus {

        private final boolean sanityCheck;
        private final String message;

        public LinkStatus(boolean sanityCheck, String message) {
            this.sanityCheck = sanityCheck;
            this.message = message;
        }

        public boolean isSanityCheck() {
            return sanityCheck;
        }

        public String getMessage() {
            return message;
        }

    }
}
