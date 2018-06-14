/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.report;

import org.jevis.api.*;
import org.jevis.report3.data.DataHelper;
import org.jevis.report3.data.notification.ReportNotification;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author broder
 */
public class ReportProperty {

    private Boolean enabled;
    private JEVisFile template;
    private JEVisObject notificationObject;
    private List<ReportLinkProperty> linkProperties;
    private JEVisObject reportObject;
    private Boolean toPdf = false;
    private DateTimeZone timeZone;
    private Long nrOfPdfPages;

    public ReportProperty(JEVisObject obj) {
        this.reportObject = obj;
        if (obj != null) {
            this.initialize();
        }
    }

    public ReportProperty() {
    }

    boolean initialize() {
        linkProperties = new ArrayList<>();

        //init Attributes
        initializeAttributes(reportObject);

        //init noti object
        initializeNotification(reportObject);

        //init report links
//        initializeReportLinkObjects(reportObject);
        return true;
    }

    public JEVisObject getReportObject() {
        return reportObject;
    }

    void initializeAttributes(JEVisObject reportObject) {
        try {
            JEVisSample enabledSample = reportObject.getAttribute(ReportAttributes.ENABLED).getLatestSample();
//            JEVisSample scheduleSample = reportObject.getAttribute(ReportAttributes.SCHEDULE).getLatestSample();
            JEVisSample templateSample = reportObject.getAttribute(ReportAttributes.TEMPLATE).getLatestSample();
//            JEVisSample startRecordSample = reportObject.getAttribute(ReportAttributes.START_RECORD).getLatestSample();

            if (!DataHelper.checkAllObjectsNotNull(enabledSample, templateSample)) {
                String missing = "";
                if (Objects.isNull(enabledSample)) missing = ReportAttributes.ENABLED;
                if (Objects.isNull(templateSample)) missing += ReportAttributes.TEMPLATE;
                throw new RuntimeException("One Sample missing for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName() + " sample for: " + missing);
            }

            enabled = enabledSample.getValueAsBoolean();
//            String scheduleString = scheduleSample.getValueAsString();
            template = templateSample.getValueAsFile();

            if (reportObject.getAttribute(ReportAttributes.PDF) != null && reportObject.getAttribute(ReportAttributes.PDF).getLatestSample() != null) {
                JEVisSample pdfSample = reportObject.getAttribute(ReportAttributes.PDF).getLatestSample();
                toPdf = pdfSample.getValueAsBoolean();
            }

            if (reportObject.getAttribute(ReportAttributes.PAGESPDF) != null && reportObject.getAttribute(ReportAttributes.PAGESPDF).getLatestSample() != null) {
                JEVisSample pdfSample = reportObject.getAttribute(ReportAttributes.PAGESPDF).getLatestSample();
                nrOfPdfPages = pdfSample.getValueAsLong();
            }

            if (reportObject.getAttribute(ReportAttributes.TIMEZONE) != null && reportObject.getAttribute(ReportAttributes.TIMEZONE).getLatestSample() != null) {
                JEVisSample timezoneSample = reportObject.getAttribute(ReportAttributes.TIMEZONE).getLatestSample();
                String timeZoneString = timezoneSample.getValueAsString();
                timeZone = DateTimeZone.forID(timeZoneString);
            } else {
                timeZone = DateTimeZone.forID("CET");
            }

//            String startRecordString = startRecordSample.getValueAsString();
            if (!DataHelper.checkAllObjectsNotNull(enabled, template)) {
                throw new RuntimeException("One Sample missing for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName());
            }

//            if (!DataHelper.checkValidSchedule(scheduleString)) {
//                throw new IllegalArgumentException("Invalid Schedule for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName());
//            } else {
//                schedule = ReportSchedule.valueOf(scheduleString.toUpperCase());
//            }
//
//            if (!DataHelper.checkValidDateFormat(startRecordString)) {
//                throw new IllegalArgumentException("Invalid Dateformat for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName());
//            } else {
//                startRecord = DateTimeFormat.forPattern(ReportConfiguration.DATE_FORMAT).parseDateTime(startRecordString);
//                endRecord = DateHelper.calcEndRecord(startRecord, schedule);
//            }
        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing attributes for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName(), ex);
        }
    }

    void initializeNotification(JEVisObject reportObject) {
        try {
            JEVisClass notificationType = reportObject.getDataSource().getJEVisClass(ReportNotification.NAME);
            List<JEVisObject> notificationObjects = reportObject.getChildren(notificationType, false);
            if (notificationObjects.size() == 1) {
                notificationObject = notificationObjects.get(0);
            } else {
                throw new IllegalStateException("Too many or no Notification Object for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName());
            }
        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing Notification Object for report Object: id: " + reportObject.getID() + " and name: " + reportObject.getName(), ex);
        }
    }

//    public List<JEVisObject> getReportLinkObjects() {
//        return reportLinkObjects;
//    }
    public List<ReportLinkProperty> getLinkProperties() {
        return linkProperties;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Boolean getToPdf() {
        return toPdf;
    }

    public DateTimeZone getTimeZone() {
        return timeZone;
    }

//    public ReportSchedule getSchedule() {
//        return schedule;
//    }
    public JEVisFile getTemplate() {
        return template;
    }

    public Long getNrOfPdfPages() {
        return nrOfPdfPages;
    }

//    public DateTime getStartRecord() {
//        return startRecord;
//    }
//
//    public DateTime getEndRecord() {
//        return endRecord;
//    }
    public JEVisObject getNotificationObject() {
        return notificationObject;

    }

    public void setReportLinks(List<ReportLinkProperty> reportLinks) {
        linkProperties = reportLinks;
    }

    private List<JEVisObject> getChildirenFromDir(JEVisObject reportObject, List<JEVisObject> currentObjects, JEVisDataSource ds, JEVisClass reportLinkDirClass, JEVisClass reportLinkClass) {
        if (reportObject == null) {
            return currentObjects;
        }

        try {
            List<JEVisObject> children = reportObject.getChildren(reportLinkClass, true);
            currentObjects.addAll(children);
            for (JEVisObject obj : reportObject.getChildren(reportLinkDirClass, true)) {
                currentObjects.addAll(getChildirenFromDir(obj, currentObjects, ds, reportLinkDirClass, reportLinkClass));
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ReportProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        return currentObjects;
    }

    public enum ReportSchedule {

        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    }
}
