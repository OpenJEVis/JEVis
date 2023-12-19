package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.report.PeriodMode;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.Precondition;
import org.jevis.report3.data.report.periodic.PeriodPrecondition;
import org.jevis.report3.data.report.periodic.PeriodicIntervalCalc;
import org.jevis.report3.data.reportlink.ReportData;
import org.jevis.report3.data.reportlink.ReportLinkFactory;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReportTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ReportTable.class);
    private final JEVisDataSource ds;
    private final DateTime latestReported;

    public ReportTable(JEVisDataSource ds, DateTime latestReported) {
        super(ds);
        this.ds = ds;
        this.latestReported = latestReported;

        try {
            createTableString();
        } catch (JEVisException e) {
            logger.error("Could not initialize.");
        }
    }

    private void createTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();
        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.reports")).append("</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.organisation")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.building")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.report")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.lasttimestamp")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.linkstatus")).append("</th>");
        sb.append("  </tr>");//border=\"0\"

        List<JEVisObject> reportObjects = getChannelObjects();
        List<ReportStatusLine> outOfBounds = new ArrayList<>();
        List<JEVisObject> brokenReports = new ArrayList<>();

        for (JEVisObject report : reportObjects) {
            try {
                ReportStatusLine reportStatusLine = new ReportStatusLine();
                SampleHandler sampleHandler = new SampleHandler();

                reportStatusLine.setName(report.getName());
                reportStatusLine.setId(report.getID());
                reportStatusLine.setOrganizationName(getParentName(report, getOrganizationClass()));
                reportStatusLine.setBuildingName(getParentName(report, getBuildingClass()));
                reportStatusLine.setLastTimeStamp(sampleHandler.getLastSample(report, "Start Record", new DateTime()));

                final IntervalCalculator intervalCalculator = new PeriodicIntervalCalc(sampleHandler);
                final Precondition precondition = new PeriodPrecondition(sampleHandler);
                final ReportLinkFactory reportLinkFactory = new ReportLinkFactory();

                intervalCalculator.buildIntervals(report);

                DateTime end = intervalCalculator.getInterval(PeriodMode.CURRENT.toString().toUpperCase()).getEnd();

                boolean dateCheck = DateTime.now().isAfter(end) && end.isBefore(latestReported);

                if (!dateCheck) {
                    logger.info("Date conditions not met, report ok");
                    continue;
                }

                if (!precondition.isPreconditionReached(report)) {
                    logger.info("Precondition not reached, report ok");
                    continue;
                }

                List<ReportData> reportLinks = reportLinkFactory.getReportLinks(report);
                AtomicBoolean isDataAvailable = new AtomicBoolean(true);
                logger.info("Creating report link stati.");

                StringBuilder linkStatus = new StringBuilder();
                for (ReportData curData : reportLinks) {
                    int index = reportLinks.indexOf(curData);
                    ReportData.LinkStatus reportLinkStatus = curData.getReportLinkStatus(end);
                    if (!reportLinkStatus.isSanityCheck()) {
                        if (index > 0) {
                            linkStatus.append("<br>");
                        }
                        linkStatus.append(reportLinkStatus.getMessage());
                        isDataAvailable.set(false);
                    }
                }
                logger.info("Created report link stati.");
                reportStatusLine.setLinkStatus(linkStatus.toString());

                if (!isDataAvailable.get()) {
                    logger.error("One or more Data Objects are missing new Data");
                    outOfBounds.add(reportStatusLine);
                }
            } catch (Exception e) {
                logger.error("Could not create status for report {}:{}", report.getName(), report.getID(), e);
                brokenReports.add(report);
            }
        }

        AlphanumComparator ac = new AlphanumComparator();
        outOfBounds.sort((o1, o2) -> {
            if (o1.getLastTimeStamp().isBefore(o2.getLastTimeStamp())) return -1;
            else if (o1.getLastTimeStamp().isAfter(o2.getLastTimeStamp())) return 1;
            else {
                return ac.compare(o1.getOrganizationName() +
                        o1.getBuildingName() +
                        o1.getName() + ":" + o1.getId().toString(), o1.getOrganizationName() +
                        o1.getBuildingName() +
                        o1.getName() + ":" + o1.getId().toString());
            }
        });

        boolean odd = false;
        for (ReportStatusLine line : outOfBounds) {

            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            line.getLineString().append("<tr>");
            /**
             * Organisation Column
             */
            line.getLineString().append("<td style=\"");
            line.getLineString().append(css);
            line.getLineString().append("\">");
            line.getLineString().append(line.getOrganizationName());
            line.getLineString().append("</td>");
            /**
             * Building Column
             */
            line.getLineString().append("<td style=\"");
            line.getLineString().append(css);
            line.getLineString().append("\">");
            line.getLineString().append(line.getBuildingName());
            line.getLineString().append("</td>");
            /**
             * Report
             */
            line.getLineString().append("<td style=\"");
            line.getLineString().append(css);
            line.getLineString().append("\">");
            line.getLineString().append(line.getName()).append(":").append(line.getId());
            line.getLineString().append("</td>");
            /**
             * Last Time Stamp
             */
            line.getLineString().append("<td style=\"");
            line.getLineString().append(css);
            line.getLineString().append("\">");
            line.getLineString().append(dtf.print(line.getLastTimeStamp()));
            line.getLineString().append("</td>");
            /**
             * Link Status
             */
            line.getLineString().append("<td style=\"");
            line.getLineString().append(css);
            line.getLineString().append("\">");
            line.getLineString().append(line.getLinkStatus());
            line.getLineString().append("</td>");

            line.getLineString().append("</tr>");

            sb.append(line.getLineString().toString());
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        if (!brokenReports.isEmpty()) {
            sb.append("<h4>").append(I18n.getInstance().getString("status.table.title.reports.broken")).append("</h4>");
            for (JEVisObject brokenReport : brokenReports) {
                int index = brokenReports.indexOf(brokenReport);
                if (index > 0) {
                    sb.append(", ");
                }
                sb.append(brokenReport.getName()).append(":").append(brokenReport.getID());
            }
        }
        sb.append("<br>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    private List<JEVisObject> getChannelObjects() throws JEVisException {
        List<JEVisObject> enabledReports = new ArrayList<>();

        for (JEVisObject report : new ArrayList<>(ds.getObjects(getReportClass(), true))) {
            JEVisAttribute enabledAtt = report.getAttribute(ENABLED);
            if (enabledAtt != null) {
                JEVisSample latestSample = enabledAtt.getLatestSample();
                if (latestSample != null) {
                    if (latestSample.getValueAsBoolean()) {
                        enabledReports.add(report);
                    }
                }
            }
        }

        return enabledReports;
    }
}
