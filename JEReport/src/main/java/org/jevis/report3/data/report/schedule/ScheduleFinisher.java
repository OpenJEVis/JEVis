package org.jevis.report3.data.report.schedule;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.report3.data.report.Finisher;
import org.jevis.report3.data.report.Report;
import org.jevis.report3.data.report.ReportProperty;

/**
 *
 * @author broder
 */
public class ScheduleFinisher implements Finisher {
    private static final Logger logger = LogManager.getLogger(ScheduleFinisher.class);

    @Override
    public void finishReport(Report report, ReportProperty property) {
        try {
            //set the report specific things
            JEVisSample lastNotUpdatedSample = JEVisIntervalParser.getLastNotUpdatedSample();
            lastNotUpdatedSample.setValue(JEVisIntervalParser.getNewValue());
            lastNotUpdatedSample.commit();
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void continueWithNextReport(JEVisObject reportObject) {
        try {
            //set the report specific things
            JEVisSample lastNotUpdatedSample = JEVisIntervalParser.getLastNotUpdatedSample();
            lastNotUpdatedSample.setValue(JEVisIntervalParser.getNewValue());
            lastNotUpdatedSample.commit();
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

}
