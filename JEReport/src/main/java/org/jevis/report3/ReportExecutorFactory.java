/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.report.ReportExecutor;

/**
 *
 * @author broder
 */
public interface ReportExecutorFactory {
    Logger logger = LogManager.getLogger(ReportExecutorFactory.class);

    static ReportExecutor getReportExecutor(JEVisObject reportObject) {
        //switch cases
        ReportType reportType = ReportType.getReportType(reportObject);
        Injector injector = null;
        if (reportType != null) {
            switch (reportType) {
                case PERIODIC:
                    injector = Guice.createInjector(new ReportPeriodicInjector());
                    break;
                case SCHEDULED:
                    injector = Guice.createInjector(new ReportScheduledInjector());
                    break;
                case EVENT:
                    injector = Guice.createInjector(new ReportEventInjector());
                    break;
                default:
                    logger.info("report object not supported");
            }
        }
        if (injector == null) {
            return null;
        }
        ReportExecutorFactory fac = injector.getInstance(ReportExecutorFactory.class);
        return fac.create(reportObject);
    }

    ReportExecutor create(JEVisObject obj);

    enum ReportType {

        PERIODIC("Periodic Report"), SCHEDULED("Scheduled Report"), EVENT("Event Report");
        private final String className;

        ReportType(String className) {
            this.className = className;
        }

        public static ReportType getReportType(JEVisObject reportObject) {
            try {
                String name = reportObject.getJEVisClass().getName();
                if (name.equals(PERIODIC.className)) {
                    return PERIODIC;
                } else if (name.equals(SCHEDULED.className)) {
                    return SCHEDULED;
                } else if (name.equals(EVENT.className)) {
                    return EVENT;
                }
            } catch (JEVisException ex) {
                logger.error(ex);
            }
            return null;
        }
    }
}
