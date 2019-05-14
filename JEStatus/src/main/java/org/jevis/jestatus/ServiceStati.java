package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.joda.time.DateTime;

import java.util.List;

public class ServiceStati extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ServiceStati.class);
    private static final String timestampFormat = "yyyy-MM-dd HH:MM:ss";
    private final JEVisDataSource ds;

    public ServiceStati(JEVisDataSource ds) {
        this.ds = ds;

        try {
            createTableString();
        } catch (JEVisException e) {
            logger.error("Could not initialize.");
        }
    }

    public void createTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();

        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>Service Stati</h2>");

        JEVisClass alarmClass = ds.getJEVisClass("JEAlarm");
        JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
        JEVisClass dataprocessorClass = ds.getJEVisClass("JEDataProcessor");
        JEVisClass calcClass = ds.getJEVisClass("JECalc");
        JEVisClass reportClass = ds.getJEVisClass("JEReport");

        List<JEVisObject> alarms = ds.getObjects(alarmClass, true);
        List<JEVisObject> dataCollectors = ds.getObjects(dataCollectorClass, true);
        List<JEVisObject> dataProcessors = ds.getObjects(dataprocessorClass, true);
        List<JEVisObject> calcs = ds.getObjects(calcClass, true);
        List<JEVisObject> reports = ds.getObjects(reportClass, true);

        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Service</th>");
        sb.append("    <th>Last Contact</th>");
        sb.append("    <th>status</th>");
        sb.append("  </tr>");

        DateTime contactAlarm = null;
        Long statusAlarm = null;

        if (!dataCollectors.isEmpty()) {
            JEVisAttribute statusAttribute = dataCollectors.get(0).getAttribute("Status");
            if (statusAttribute != null) {
                JEVisSample latestSample = statusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactAlarm = latestSample.getTimestamp();
                    statusAlarm = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        sb.append(alarmClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (contactAlarm != null) {
            sb.append(contactAlarm.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (statusAlarm != null) {
            sb.append(getStatus(statusAlarm));
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactDataCollector = null;
        Long statusDataCollector = null;

        if (!dataCollectors.isEmpty()) {
            JEVisAttribute statusAttribute = dataCollectors.get(0).getAttribute("Status");
            if (statusAttribute != null) {
                JEVisSample latestSample = statusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactDataCollector = latestSample.getTimestamp();
                    statusDataCollector = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        sb.append(dataCollectorClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        if (contactDataCollector != null) {
            sb.append(contactDataCollector.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        if (statusDataCollector != null) {
            sb.append(getStatus(statusDataCollector));
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactDataProcessor = null;
        Long statusDataProcessor = null;

        if (!dataProcessors.isEmpty()) {
            JEVisAttribute statusAttribute = dataProcessors.get(0).getAttribute("Status");
            if (statusAttribute != null) {
                JEVisSample latestSample = statusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactDataProcessor = latestSample.getTimestamp();
                    statusDataProcessor = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        sb.append(dataprocessorClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (contactDataProcessor != null) {
            sb.append(contactDataProcessor.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (statusDataProcessor != null) {
            sb.append(getStatus(statusDataProcessor));
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactCalc = null;
        Long statusCalc = null;

        if (!calcs.isEmpty()) {
            JEVisAttribute statusAttribute = calcs.get(0).getAttribute("Status");
            if (statusAttribute != null) {
                JEVisSample latestSample = statusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactCalc = latestSample.getTimestamp();
                    statusCalc = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        sb.append(calcClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        if (contactCalc != null) {
            sb.append(contactCalc.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(highlight);
        sb.append("\">");
        if (statusCalc != null) {
            sb.append(getStatus(statusCalc));
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactReport = null;
        Long statusReport = null;

        if (!reports.isEmpty()) {
            JEVisAttribute statusAttribute = reports.get(0).getAttribute("Status");
            if (statusAttribute != null) {
                JEVisSample latestSample = statusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactReport = latestSample.getTimestamp();
                    statusReport = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        sb.append(reportClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (contactReport != null) {
            sb.append(contactReport.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (statusReport != null) {
            sb.append(getStatus(statusReport));
        }
        sb.append("</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    public String getStatus(Long status) {
        if (status == 0) {
            return "offline";
        } else if (status == 1) {
            return "online, waiting";
        } else if (status == 2) {
            return "online, running";
        } else return null;
    }
}
