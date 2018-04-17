/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import java.util.Map;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;

/**
 *
 * @author broder
 */
public interface ReportData {

    public Map<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc);

    public JEVisObject getDataObject();
//
//    public JEVisObject getLinkObject();
    public LinkStatus getReportLinkStatus(DateTime end);

    public class LinkStatus {

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
