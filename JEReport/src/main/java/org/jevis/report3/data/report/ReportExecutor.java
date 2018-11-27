/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import com.google.inject.assistedinject.Assisted;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author broder
 */
public class ReportExecutor {

    private final JEVisObject reportObject;
    private static final Logger logger = LogManager.getLogger(ReportExecutor.class);
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

        if (!precondition.isPreconditionReached(reportObject)) {
            logger.info("Report date not reached");
            return;
        }

        /**
         * Need to reload if report template changed (even if not very probable)
         */
        try {
            reportObject.getDataSource().reloadAttributes();
        } catch (Exception e) {
        }

        List<ReportData> reportLinks = reportLinkFactory.getReportLinks(reportObject);
        intervalCalculator.buildIntervals(reportObject);

        DateTime end = intervalCalculator.getInterval(IntervalCalculator.PeriodMode.CURRENT).getEnd();

        AtomicBoolean isDataAvailable = new AtomicBoolean(true);
        logger.info("Creating report link stati.");
        reportLinks.parallelStream().forEach(curData -> {
            ReportData.LinkStatus reportLinkStatus = curData.getReportLinkStatus(end);
            if (!reportLinkStatus.isSanityCheck()) {
                logger.info(reportLinkStatus.getMessage());
                isDataAvailable.set(false);
            }
        });
        logger.info("Created report link stati.");

        if (!isDataAvailable.get()) {
            return;
        }

        contextBuilder.setIntervalCalculator(intervalCalculator);
        logger.info("Initializing report properties.");
        ReportProperty property = new ReportProperty(reportObject);
        logger.info("Initialized report properties. Building Context.");
        Map<String, Object> contextMap = contextBuilder.buildContext(reportLinks, property, intervalCalculator);

        Report report = new Report(property, contextMap);

        if (isPeriodicReport(reportObject) && !isPeriodicConditionReached(reportObject, new SampleHandler())) {
            logger.info("condition not reached");
            finisher.finishReport(report, property);
            return;
        }

        try {
            byte[] outputBytes = report.getReportFile();

            DateTime start = new DateTime(reportObject.getAttribute(ReportAttributes.START_RECORD).getLatestSample().getValueAsString());
            String startDate = start.toString(DateTimeFormat.forPattern("yyyyMMdd"));

            String reportName = reportObject.getName().replaceAll("\\s", "") + "_" + startDate;
            JEVisFile jeVisFileImp = new JEVisFileImp(reportName + ".xlsx", outputBytes);
            JEVisAttribute lastReportAttribute = reportObject.getAttribute(ReportAttributes.LAST_REPORT);
            lastReportAttribute.buildSample(new DateTime(), jeVisFileImp).commit();

            JEVisFile fileForNotification = jeVisFileImp;
            if (property.getToPdf()) {

                try {
                    File wholePdfFile = new PdfConverter(reportName, outputBytes).runPdfConverter();
                    PdfFileSplitter pdfFileSplitter = new PdfFileSplitter(property.getNrOfPdfPages(), wholePdfFile);
                    pdfFileSplitter.splitPDF();
                    File outFile = pdfFileSplitter.getOutputFile();
                    fileForNotification = new JEVisFileImp(reportName + ".pdf", outFile);
                } catch (Exception e) {
                    logger.error("Could not initialize pdf converter. " + e);
                }
            }

            JEVisObject notificationObject = property.getNotificationObject();
            JEVisAttribute attachmentAttribute = notificationObject.getAttribute(ReportNotification.ATTACHMENTS);
            attachmentAttribute.buildSample(new DateTime(), fileForNotification).commit();

            sendNotification(notificationObject, fileForNotification);

            finisher.finishReport(report, property);
        } catch (JEVisException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
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
                boolean isFullfilled = eventOperator.isFulfilled(value, limit);
                if (isFullfilled) {
                    return true;
                }
            }

        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    private void sendNotification(JEVisObject notificationObject, JEVisFile jeVisFileImp) {
        try {

            ReportServiceProperty service = getReportService();

            JEVisObject notiObj = reportObject.getDataSource().getObject(notificationObject.getID());
            Notification nofi = new EmailNotification();
            nofi.setNotificationObject(notiObj, jeVisFileImp);

            NotificationDriver emailNofi = new EmailNotificationDriver();

            JEVisObject notiDriObj = reportObject.getDataSource().getObject(service.getMailID());
            emailNofi.setNotificationDriverObject(notiDriObj);

            SendNotification sn = new SendNotification(nofi, emailNofi);
            sn.run();

        } catch (JEVisException ex) {
            logger.error(ex);
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
            logger.error(ex);
        }
        return false;
    }
}
