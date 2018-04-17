/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.patch;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.patch.Patch;

/**
 *
 * @author broder
 */
public enum ReportPatch implements Patch {

    FIRST_VERSION("1.0") {

                @Override
                public void apply() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void undo() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

            },
    REPORT_PERIODIC("1.1") {

                @Override
                public void apply() {
                    ReportPatches.insertPeriodicReportUnderReport(datasource);
                }

                @Override
                public void undo() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

            },
    EVENT_SCHEDULE("1.2") {
                @Override
                public void apply() {
                    ReportPatches.insertScheduledAndEventReportUnderReport(datasource);
                }

                @Override
                public void undo() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
    private final String version;
    private static JEVisDataSource datasource;

    ReportPatch(String version) {
        this.version = version;
    }

    @Override
    public void setJEVisDataSource(JEVisDataSource ds) {
        ReportPatch.datasource = ds;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
