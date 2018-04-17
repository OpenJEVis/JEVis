/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import org.jevis.api.JEVisException;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.jevis.report3.patch.ReportPatches;

/**
 *
 * @author broder
 */
public class ReportPatcher {

    public static void main(String[] args) throws JEVisException {
        ReportPatcher patch = new ReportPatcher();
        patch.run();
    }

    public void run() throws JEVisException {
        JEVisDataSourceWS datasource = new JEVisDataSourceWS("http://openjevis.org:18090");
        ReportPatches.insertScheduledAndEventReportUnderReport(datasource);
    }
}
