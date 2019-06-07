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
import org.jevis.commons.datetime.Period;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.report.PeriodMode;
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
import org.jevis.report3.data.report.event.EventPrecondition;
import org.jevis.report3.data.reportlink.ReportData;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.inject.Inject;
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

        intervalCalculator.buildIntervals(reportObject);

        DateTime end = intervalCalculator.getInterval(PeriodMode.CURRENT).getEnd();

        if (!precondition.isPreconditionReached(reportObject)) {

            logger.info("Precondition not reached");
            finisher.continueWithNextReport(reportObject);
            return;
        }

        List<ReportData> reportLinks = reportLinkFactory.getReportLinks(reportObject);

        AtomicBoolean isDataAvailable = new AtomicBoolean(true);
        logger.info("Creating report link stati.");
        reportLinks.forEach(curData -> {
            ReportData.LinkStatus reportLinkStatus = curData.getReportLinkStatus(end);
            if (!reportLinkStatus.isSanityCheck()) {
                logger.info(reportLinkStatus.getMessage());
                isDataAvailable.set(false);
            }
        });
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

        logger.info("Built context. " + contextMap.size());

        if (isPeriodicReport(reportObject) && !isPeriodicConditionReached(reportObject, new SampleHandler())) {
            logger.info("condition not reached");
            finisher.finishReport(report, property);
            return;
        }

        try {
            logger.info("Creating reportFile.");
            byte[] outputBytes = report.getReportFile();

            DateTime start = new DateTime(reportObject.getAttribute(ReportAttributes.START_RECORD).getLatestSample().getValueAsString());
            String startDate = start.toString(DateTimeFormat.forPattern("yyyyMMdd"));

            String reportName = reportObject.getName().replaceAll("\\s", "") + "_" + startDate;
            JEVisFile jeVisFileImp = new JEVisFileImp(reportName + ".xlsx", outputBytes);
            JEVisAttribute lastReportAttribute = reportObject.getAttribute(ReportAttributes.LAST_REPORT);
            lastReportAttribute.buildSample(new DateTime(), jeVisFileImp).commit();
            logger.info("Uploaded report file to JEVis System");

            JEVisFile fileForNotification = jeVisFileImp;
            if (property.getToPdf()) {

                try {
                    logger.info("Creating pdf file.");
                    File wholePdfFile = new PdfConverter(reportName, outputBytes).runPdfConverter();
                    wholePdfFile.deleteOnExit();
                    PdfFileSplitter pdfFileSplitter = new PdfFileSplitter(property.getNrOfPdfPages(), wholePdfFile);
                    pdfFileSplitter.splitPDF();
                    File outFile = pdfFileSplitter.getOutputFile();
                    outFile.deleteOnExit();
                    fileForNotification = new JEVisFileImp(reportName + ".pdf", outFile);
                } catch (Exception e) {
                    logger.error("Could not initialize pdf converter. " + e);
                }
            }

            JEVisObject notificationObject = property.getNotificationObject();
            JEVisAttribute attachmentAttribute = notificationObject.getAttribute(ReportNotification.ATTACHMENTS);
            attachmentAttribute.buildSample(new DateTime(), fileForNotification).commit();
            logger.info("Uploaded pdf file to notification in JEVis System");

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
            DateTime endRecord = PeriodHelper.calcEndRecord(startRecord, schedule, dateHelper);
            List<JEVisSample> samplesInPeriod = samplesHandler.getSamplesInPeriod(reportObject.getDataSource().getObject(jevisId), attributeName, startRecord, endRecord);

            if (!operator.equals("")) {
                EventPrecondition.EventOperator eventOperator = EventPrecondition.EventOperator.getEventOperator(operator);
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

            Notification nofi = new EmailNotification();
            nofi.setNotificationObject(notificationObject, jeVisFileImp);

            JEVisObject notiDriObj = notificationObject.getDataSource().getObject(service.getMailID());

            NotificationDriver emailNofi = new EmailNotificationDriver();
            emailNofi.setNotificationDriverObject(notiDriObj);

            SendNotification sn = new SendNotification(nofi, emailNofi);
            sn.run();

        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private EmailServiceProperty getReportService() {
        JEVisDataSource dataSource = null;
        try {
            dataSource = reportObject.getDataSource();
        } catch (JEVisException e) {
            logger.error(e);
        }
        EmailServiceProperty service = new EmailServiceProperty();
        try {
            JEVisClass jeVisClass = dataSource.getJEVisClass("JEReport");
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
