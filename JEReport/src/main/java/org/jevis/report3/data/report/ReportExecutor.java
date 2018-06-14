/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import com.google.inject.assistedinject.Assisted;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jenotifier.mode.SendNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.jevis.report3.DateHelper;
import org.jevis.report3.PdfConverter;
import org.jevis.report3.PdfFileSplitter;
import org.jevis.report3.ReportLauncher;
import org.jevis.report3.context.ContextBuilder;
import org.jevis.report3.data.notification.ReportNotification;
import org.jevis.report3.data.report.event.EventPrecondition;
import org.jevis.report3.data.reportlink.ReportData;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.jevis.report3.data.service.JEReportService;
import org.jevis.report3.data.service.ReportServiceProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author broder
 */
public class ReportExecutor {

    private final JEVisObject reportObject;
    private static final Logger logger = LoggerFactory.getLogger(ReportExecutor.class);
    private final Precondition precondition;
    private final ContextBuilder contextBuilder;
    private final ReportLinkFactory reportLinkFactory;
    private final IntervalCalculator intervalCalculator;
    private final Finisher finisher;

    @Inject
    public ReportExecutor(Precondition precondition, IntervalCalculator intervalCalc, ContextBuilder contextBuilder, Finisher finisher, ReportLinkFactory reportLinkFactory, @Assisted JEVisObject reportObject) {
        this.reportObject = reportObject;
        this.intervalCalculator = intervalCalc;
        this.precondition = precondition;
        this.contextBuilder = contextBuilder;
        this.finisher = finisher;
        this.reportLinkFactory = reportLinkFactory;
    }

    public void executeReport() {

        //init ReportProperty first?
        //check if report date is reached
        if (!precondition.isPreconditionReached(reportObject)) {
            logger.info("Report date not reached");
            return;
        }

        //build Report Links
        List<ReportData> reportLinks = reportLinkFactory.getReportLinks(reportObject);
        intervalCalculator.buildIntervals(reportObject);

        //check if links contain data
        boolean isDataAvailable = true;
        DateTime end = intervalCalculator.getInterval(IntervalCalculator.PeriodModus.CURRENT).getEnd();

        for (ReportData curData : reportLinks) {
            ReportData.LinkStatus reportLinkStatus = curData.getReportLinkStatus(end);
            if (!reportLinkStatus.isSanityCheck()) {
                logger.info(reportLinkStatus.getMessage());
                isDataAvailable = false;
            }
        }

        if (!isDataAvailable) {
            return;
        }

        contextBuilder.setIntervalCalculator(intervalCalculator);
        ReportProperty property = new ReportProperty(reportObject);
        Map<String, Object> contextMap = contextBuilder.buildContext(reportLinks, property, intervalCalculator);

        //initialize data
        Report report = new Report(property, contextMap);

        if (isPeriodicReport(reportObject) && !isPeriodicConditionReached(reportObject, new SampleHandler())) {
            logger.info("condition not reached");
            //write file into report
            finisher.finishReport(report, property);
            return;
        }

        //write file into report
        finisher.finishReport(report, property);

        try {
            //set report
            byte[] outputBytes = report.getReportFile();
            String sendReportTimeString = new DateTime().toString(DateTimeFormat.forPattern("dd_MM_yyyy"));
            Interval interval = intervalCalculator.getInterval(IntervalCalculator.PeriodModus.CURRENT);
            String startDate = interval.getStart().toString(DateTimeFormat.forPattern("yyyyMMdd"));
            String endDate = interval.getEnd().toString(DateTimeFormat.forPattern("dd_MM_yyyy"));
            String reportName = reportObject.getName().replaceAll("\\s", "") + "_" + startDate;
            JEVisFile jeVisFileImp = new JEVisFileImp(reportName + ".xls", outputBytes);
            JEVisAttribute lastReportAttribute = reportObject.getAttribute(ReportAttributes.LAST_REPORT);
            lastReportAttribute.buildSample(new DateTime(), jeVisFileImp).commit();

            JEVisFile fileForNotification = jeVisFileImp;
            if (property.getToPdf()) {
                File wholePdfFile = new PdfConverter(reportName, outputBytes).runPdfConverter();
//                JEVisFile wholePdfFile = new JEVisFileImp(reportName + ".pdf", pdfFile);
                PdfFileSplitter pdfFileSplitter = new PdfFileSplitter(property.getNrOfPdfPages(), wholePdfFile);
                pdfFileSplitter.splitPDF();
                File outFile = pdfFileSplitter.getOutputFile();
                fileForNotification = new JEVisFileImp(reportName + ".pdf", outFile);
            }

            //send notification
            JEVisObject notificationObject = property.getNotificationObject();
            sendNotification(notificationObject, fileForNotification);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportExecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(ReportExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean isPeriodicConditionReached(JEVisObject reportObject, SampleHandler samplesHandler) {

        try {
            String startRecordString = samplesHandler.getLastSampleAsString(reportObject, "Start Record");
            DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

            Boolean conditonEnabeld = samplesHandler.getLastSampleAsBoolean(reportObject, "Condition Enabled", false);
            if (!conditonEnabeld) {
                return true;
            }

            String operator = samplesHandler.getLastSampleAsString(reportObject, "Operator");
            EventPrecondition.EventOperator eventOperator = EventPrecondition.EventOperator.getEventOperator(operator);
            String limit = samplesHandler.getLastSampleAsString(reportObject, "Limit");
            Long jevisId = samplesHandler.getLastSampleAsLong(reportObject, "JEVis ID");
            String attributeName = samplesHandler.getLastSampleAsString(reportObject, "Attribute Name");

            String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", ReportProperty.ReportSchedule.DAILY.toString());
            ReportProperty.ReportSchedule schedule = ReportProperty.ReportSchedule.valueOf(scheduleString.toUpperCase());
            DateTime endRecord = DateHelper.calcEndRecord(startRecord, schedule);
            List<JEVisSample> samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, endRecord);
            for (JEVisSample sample : samplesInPeriod) {
                String value = sample.getValueAsString();
                boolean isFullfilled = eventOperator.isFullfilled(value, limit);
                if (isFullfilled) {
                    return true;
                }
            }

//            DateTime lastDate = samplesHandler.getTimeStampFromLastSample(reportObject.getDataSource().getObject(jevisId), attributeName);
//            String newStartTimeString = lastDate.toString(DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT));
//            reportObject.getAttribute(ReportAttributes.START_RECORD).buildSample(new DateTime(), newStartTimeString).commit();
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(EventPrecondition.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private void sendNotification(JEVisObject notificationObject, JEVisFile jeVisFileImp) {
        try {
            //        write data into the notifier
            ReportServiceProperty service = getReportService();
            File notifierFile = service.getNotificationFile();//TODO get the report service and the notifier file
            JEVisAttribute attachmentAttribute = notificationObject.getAttribute(ReportNotification.ATTACHMENTS);
            attachmentAttribute.deleteAllSample();
            attachmentAttribute.buildSample(new DateTime(), jeVisFileImp).commit();

            /* This is from Thread stuff */
            //Single single = new Single(notificationObject.getID(), service.getMailID(), reportObject.getDataSource(), notifierFile, ReportConfiguration.NOTIFICATION, ReportConfiguration.NOTIFICATION_DRIVER);
            //single.start();

            JEVisObject notiObj = reportObject.getDataSource().getObject(notificationObject.getID());
            Notification nofi = new EmailNotification();
            nofi.setNotificationObject(notiObj, jeVisFileImp);
            NotificationDriver emailNofi = new EmailNotificationDriver();

            JEVisObject notiDriObj = reportObject.getDataSource().getObject(service.getMailID());
            emailNofi.setNotificationDriverObject(notiDriObj);

            SendNotification sn = new SendNotification(nofi, emailNofi);
            sn.run();

        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ReportServiceProperty getReportService() {
        JEVisDataSource dataSource = ReportLauncher.getDataSource();
        ReportServiceProperty service = new ReportServiceProperty();
        try {
            JEVisClass jeVisClass = dataSource.getJEVisClass(JEReportService.NAME);
            List<JEVisObject> reportServies = dataSource.getObjects(jeVisClass, true);
            if (reportServies.size() == 1) {
                service.initialize(reportServies.get(0));
            }
        } catch (JEVisException ex) {
            logger.error("error while getting report service", ex);
        }
        return service;
    }

    private boolean isPeriodicReport(JEVisObject reportObject) {
        try {
            String jevisClassName = reportObject.getJEVisClass().getName();
            if (jevisClassName.equals(ReportTypes.PERIODIC_REPORT)) {
                return true;
            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(ReportExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

//    private boolean checkDataAvailability(List<ReportData> reportLinks) {
//        boolean dataAvailable = true;
//        for (ReportData curData : reportLinks) {
//            JEVisObject linkObject = curData.getLinkObject();
//            try {
//                if(linkObject.getAttribute("Optional") != null){
//                    Boolean dataOptional = linkObject.getAttribute("Optional").getLatestSample().getValueAsBoolean();                   if (!dataOptional){
//                        JEVisObject dataObject = curData.getDataObject();
//                        dataObject.get
//                    }
//                }
//            } catch (JEVisException ex) {
//                java.util.logging.Logger.getLogger(ReportExecutor.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            JEVisObject dataObject = curData.getDataObject();
//            
//        }
//
//        return dataAvailable;
//    }
}
