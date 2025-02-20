/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.report.PeriodMode;
import org.jevis.commons.report.ReportName;
import org.jevis.commons.utils.NameFormatter;
import org.jevis.jenotifier.mode.SendNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotification;
import org.jevis.jenotifier.notifier.Email.EmailNotificationDriver;
import org.jevis.jenotifier.notifier.Email.EmailServiceProperty;
import org.jevis.jenotifier.notifier.Notification;
import org.jevis.jenotifier.notifier.NotificationDriver;
import org.jevis.report3.PdfConverter;
import org.jevis.report3.PdfFileSplitter;
import org.jevis.report3.context.ContextBuilder;
import org.jevis.report3.data.notification.ReportNotification;
import org.jevis.report3.data.report.intervals.Finisher;
import org.jevis.report3.data.report.intervals.IntervalCalculator;
import org.jevis.report3.data.report.intervals.Precondition;
import org.jevis.report3.data.reportlink.ReportData;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author broder
 */
public class ReportExecutor {

    private static final Logger logger = LogManager.getLogger(ReportExecutor.class);
    private final JEVisObject reportObject;
    private final Precondition precondition;
    private final ContextBuilder contextBuilder;
    private final ReportLinkFactory reportLinkFactory;
    private final IntervalCalculator intervalCalculator;
    private final Finisher finisher;

    public ReportExecutor(Precondition precondition, IntervalCalculator intervalCalc, ContextBuilder contextBuilder, Finisher finisher, ReportLinkFactory reportLinkFactory, JEVisObject reportObject) {
        this.reportObject = reportObject;
        this.intervalCalculator = intervalCalc;
        this.precondition = precondition;
        this.contextBuilder = contextBuilder;
        this.finisher = finisher;
        this.reportLinkFactory = reportLinkFactory;
    }

    public void executeReport() {

        intervalCalculator.buildIntervals(reportObject);

        DateTime end = intervalCalculator.getInterval(PeriodMode.CURRENT.toString().toUpperCase()).getEnd();

        if (!precondition.isPreconditionReached(reportObject)) {

            logger.info("Precondition not reached");
            finisher.continueWithNextReport();
            return;
        }

        List<ReportData> reportLinks = reportLinkFactory.getReportLinks(reportObject);

        AtomicBoolean isDataAvailable = new AtomicBoolean(true);
        logger.info("Creating report link stati.");
        for (ReportData curData : reportLinks) {
            ReportData.LinkStatus reportLinkStatus = curData.getReportLinkStatus(end);
            if (!reportLinkStatus.isSanityCheck()) {
                logger.error("Report {}:{} - {}", this.reportObject.getName(), this.reportObject.getID(), reportLinkStatus.getMessage());
                isDataAvailable.set(false);
            }
        }
        logger.info("Created report link stati.");

        if (!isDataAvailable.get()) {
            logger.error("One or more Data Objects are missing new Data");
            return;
        }

        contextBuilder.setIntervalCalculator(intervalCalculator);
        logger.info("Initializing report properties.");
        ReportProperty property = new ReportProperty(reportObject);
        logger.info("Initialized report properties. Building Context.");
        Map<String, Object> contextMap = contextBuilder.buildContext(reportLinks, property, intervalCalculator);

        Report report = new Report(property, contextMap);

        logger.info("Built context. {}", contextMap.size());

        if (isPeriodicReport(reportObject) && !isPeriodicConditionReached(reportObject, new SampleHandler())) {
            logger.info("condition not reached");
            finisher.finishReport();
            return;
        }

        try {
            logger.info("Creating reportFile.");
            byte[] outputBytes = report.getReportFile();

            DateTime start = new DateTime(reportObject.getAttribute(ReportAttributes.START_RECORD).getLatestSample().getValueAsString());
            WorkDays wd = new WorkDays(reportObject);

            String prefix = ReportName.getPrefix(reportObject, start);
            String creationDate = new DateTime().toString(DateTimeFormat.forPattern("yyyyMMdd").withZone(wd.getDateTimeZone()));

            String reportName = reportObject.getName().replaceAll("\\s", "_") + "_" + creationDate;
            reportName = NameFormatter.formatNames(reportName);
            reportName = prefix + reportName;

            if (outputBytes != null && outputBytes.length > 0) {
                JEVisFile jeVisFileImp = new JEVisFileImp(reportName + ".xlsx", outputBytes);
                JEVisAttribute lastReportAttribute = reportObject.getAttribute(ReportAttributes.LAST_REPORT);
                JEVisAttribute lastReportPDFAttribute = reportObject.getAttribute(ReportAttributes.LAST_REPORT_PDF);
                lastReportAttribute.buildSample(new DateTime(), jeVisFileImp).commit();
                logger.info("Uploaded report file to JEVis System");

                JEVisFile fileForNotification = jeVisFileImp;
                if (property.getToPdf()) {
                    File pdfFile = null;
                    try {
                        logger.info("Creating pdf file.");
                        File wholePdfFile = new PdfConverter(reportName, outputBytes).runPdfConverter();
                        wholePdfFile.deleteOnExit();
                        PdfFileSplitter pdfFileSplitter = new PdfFileSplitter(property.getNrOfPdfPages(), wholePdfFile);
                        pdfFileSplitter.splitPDF();
                        pdfFile = pdfFileSplitter.getOutputFile();
                        pdfFile.deleteOnExit();
                        JEVisFile jeVisFilePDFImp = new JEVisFileImp(reportName + ".pdf", pdfFile);
                        lastReportPDFAttribute.buildSample(new DateTime(), jeVisFilePDFImp).commit();

                        if (pdfFile != null) {
                            fileForNotification = new JEVisFileImp(reportName + ".pdf", pdfFile);
                        }

                    } catch (Exception e) {
                        logger.error("Could not initialize pdf converter. ", e);
                    }
                }

                JEVisObject notificationObject = property.getNotificationObject();
                if (notificationObject != null && isEnabled(notificationObject)) {
                    JEVisAttribute attachmentAttribute = notificationObject.getAttribute(ReportNotification.ATTACHMENTS);
                    attachmentAttribute.buildSample(new DateTime(), fileForNotification).commit();
                    logger.info("Uploaded pdf file to notification in JEVis System");

                    sendNotification(notificationObject, fileForNotification);
                }

                finisher.finishReport();
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    private boolean isEnabled(JEVisObject jeVisObject) {
        JEVisAttribute enabledAtt = null;
        try {
            enabledAtt = jeVisObject.getAttribute("Enabled");
            if (enabledAtt != null && enabledAtt.hasSample()) {
                return enabledAtt.getLatestSample().getValueAsBoolean();
            }
        } catch (Exception e) {
            logger.error("Could not get enabled status of {} with id {}", jeVisObject.getName(), jeVisObject.getID(), e);
        }
        return false;
    }


    public boolean isPeriodicConditionReached(JEVisObject reportObject, SampleHandler samplesHandler) {

        try {
            String startRecordString = samplesHandler.getLastSample(reportObject, "Start Record", "");
            DateTime startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);

            Boolean conditionEnabled = samplesHandler.getLastSample(reportObject, "Condition Enabled", false);
            if (!conditionEnabled) {
                return true;
            }

            String operator = samplesHandler.getLastSample(reportObject, "Operator", "");
            String limit = samplesHandler.getLastSample(reportObject, "Limit", "");
            Long jevisId = samplesHandler.getLastSample(reportObject, "JEVis ID", -1L);
            String attributeName = samplesHandler.getLastSample(reportObject, "Attribute Name", "");

            String scheduleString = samplesHandler.getLastSample(reportObject, "Schedule", Period.DAILY.toString());
            Period schedule = Period.valueOf(scheduleString.toUpperCase());
            org.jevis.commons.datetime.DateHelper dateHelper = null;
            dateHelper = PeriodHelper.getDateHelper(reportObject, schedule, dateHelper, startRecord);
            DateTimeZone dateTimeZone = samplesHandler.getLastSample(reportObject, JC.Report.a_TimeZone, DateTimeZone.UTC);
            DateTime endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateTimeZone, dateHelper);
            List<JEVisSample> samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, endRecord);

            if (!operator.isEmpty()) {
                Precondition.EventOperator eventOperator = Precondition.EventOperator.getEventOperator(operator);
                for (JEVisSample sample : samplesInPeriod) {
                    String value = sample.getValueAsString();
                    boolean isFulfilled = Objects.requireNonNull(eventOperator).isFulfilled(value, limit);
                    if (isFulfilled) {
                        return true;
                    }
                }
            }

        } catch (JEVisException ex) {
            logger.error(ex);
        }
        return false;
    }

    private void sendNotification(JEVisObject notificationObject, JEVisFile jeVisFileImp) {
        try {

            EmailServiceProperty service = getReportService();

            if (service != null) {
                Notification notification = new EmailNotification();
                notification.setNotificationObject(notificationObject, jeVisFileImp);

                JEVisObject notificationDriverObject = notificationObject.getDataSource().getObject(service.getMailID());

                NotificationDriver notificationDriver = new EmailNotificationDriver();
                notificationDriver.setNotificationDriverObject(notificationDriverObject);

                SendNotification sn = new SendNotification(notification, notificationDriver);
                sn.run();
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private EmailServiceProperty getReportService() {
        JEVisDataSource dataSource = null;
        try {
            dataSource = reportObject.getDataSource();

            EmailServiceProperty service = new EmailServiceProperty();

            JEVisClass jeVisClass = dataSource.getJEVisClass("JEReport");
            List<JEVisObject> reportServices = dataSource.getObjects(jeVisClass, true);
            if (reportServices.size() == 1) {
                service.initialize(reportServices.get(0));
            }

            return service;
        } catch (JEVisException e) {
            logger.error(e);
        }

        return null;
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
