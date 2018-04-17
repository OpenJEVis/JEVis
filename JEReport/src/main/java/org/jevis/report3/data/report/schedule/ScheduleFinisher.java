package org.jevis.report3.data.report.schedule;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.report3.DateHelper;
import org.jevis.report3.data.report.Finisher;
import org.jevis.report3.data.report.Report;
import org.jevis.report3.data.report.ReportAttributes;
import org.jevis.report3.data.report.ReportConfiguration;
import org.jevis.report3.data.report.ReportProperty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author broder
 */
public class ScheduleFinisher implements Finisher {

    @Override
    public void finishReport(Report report, ReportProperty property) {
        try {
            //set the report specific things
            JEVisSample lastNotUpdatedSample = JEVisIntervalParser.getLastNotUpdatedSample();
            lastNotUpdatedSample.setValue(JEVisIntervalParser.getNewValue());
            lastNotUpdatedSample.commit();
        } catch (JEVisException ex) {
            Logger.getLogger(ScheduleFinisher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
