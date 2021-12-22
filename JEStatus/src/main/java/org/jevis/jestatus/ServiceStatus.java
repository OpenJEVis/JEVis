package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;
import org.joda.time.DateTime;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.text.NumberFormat;
import java.util.List;

public class ServiceStatus extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ServiceStatus.class);
    private static final String timestampFormat = "yyyy-MM-dd HH:MM:ss";

    public ServiceStatus(JEVisDataSource ds) {
        super(ds);

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

        sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.diskspace")).append("</h2>");
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.partition")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.totalspace")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.freespace")).append("</th>");
        sb.append("  </tr>");

        FileSystem fs = FileSystems.getDefault();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        for (FileStore store : fs.getFileStores()) {
            try {
                double totalSpace = store.getTotalSpace() / 1024d / 1024d / 1024d;
                double usableSpace = store.getUsableSpace() / 1024d / 1024d / 1024d;
                double percent = usableSpace / totalSpace * 100d;

                /** ignore 0 size stores **/
                if (Math.round(totalSpace) <= 0) continue;

                sb.append("<tr>");
                sb.append("<td style=\"");
                sb.append(rowCss);
                sb.append("\">");
                sb.append(store.name());
                sb.append("</td>");

                sb.append("<td style=\"");
                sb.append(rowCss);
                sb.append("\">");

                sb.append(nf.format(totalSpace)).append(" GB");
                sb.append("</td>");

                sb.append("<td style=\"");
                if (percent > 15) {
                    sb.append(rowCss);
                } else {
                    sb.append(redRowCss);
                }
                sb.append("\">");
                sb.append(nf.format(usableSpace)).append(" GB");
                sb.append(" (").append(nf.format(percent)).append(" %)");
                sb.append("</td>");

                sb.append("</tr>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sb.append("</table>");
        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.services")).append("</h2>");

        JEVisClass alarmClass = ds.getJEVisClass("JEAlarm");
        JEVisClass dataCollectorClass = ds.getJEVisClass("JEDataCollector");
        JEVisClass dataprocessorClass = ds.getJEVisClass("JEDataProcessor");
        JEVisClass calcClass = ds.getJEVisClass("JECalc");
        JEVisClass reportClass = ds.getJEVisClass("JEReport");
        JEVisClass notifierClass = ds.getJEVisClass("JENotifier");

        List<JEVisObject> alarms = ds.getObjects(alarmClass, true);
        List<JEVisObject> dataCollectors = ds.getObjects(dataCollectorClass, true);
        List<JEVisObject> dataProcessors = ds.getObjects(dataprocessorClass, true);
        List<JEVisObject> calcs = ds.getObjects(calcClass, true);
        List<JEVisObject> reports = ds.getObjects(reportClass, true);
        List<JEVisObject> notifiers = ds.getObjects(notifierClass, true);

        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.service")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.lastcontact")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.status")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.runtime")).append("</th>");
        sb.append("  </tr>");

        DateTime contactAlarm = null;
        Long statusAlarm = null;
        JEVisAttribute alarmStatusAttribute = null;
        JEVisAttribute dataCollectorStatusAttribute = null;
        JEVisAttribute dataProcessorStatusAttribute = null;
        JEVisAttribute calcStatusAttribute = null;
        JEVisAttribute reportStatusAttribute = null;
        JEVisAttribute notifierStatusAttribute = null;

        if (!alarms.isEmpty()) {
            alarmStatusAttribute = alarms.get(0).getAttribute("Status");
            if (alarmStatusAttribute != null) {
                JEVisSample latestSample = alarmStatusAttribute.getLatestSample();
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
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (alarmStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(alarmStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactDataCollector = null;
        Long statusDataCollector = null;

        if (!dataCollectors.isEmpty()) {
            dataCollectorStatusAttribute = dataCollectors.get(0).getAttribute("Status");
            if (dataCollectorStatusAttribute != null) {
                JEVisSample latestSample = dataCollectorStatusAttribute.getLatestSample();
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
        sb.append(rowCss + highlight);
        sb.append("\">");
        sb.append(dataCollectorClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (contactDataCollector != null) {
            sb.append(contactDataCollector.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (statusDataCollector != null) {
            sb.append(getStatus(statusDataCollector));
        }
        sb.append("</td>");
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (dataCollectorStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(dataCollectorStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactDataProcessor = null;
        Long statusDataProcessor = null;

        if (!dataProcessors.isEmpty()) {
            dataProcessorStatusAttribute = dataProcessors.get(0).getAttribute("Status");
            if (dataProcessorStatusAttribute != null) {
                JEVisSample latestSample = dataProcessorStatusAttribute.getLatestSample();
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
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (dataProcessorStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(dataProcessorStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactCalc = null;
        Long statusCalc = null;

        if (!calcs.isEmpty()) {
            calcStatusAttribute = calcs.get(0).getAttribute("Status");
            if (calcStatusAttribute != null) {
                JEVisSample latestSample = calcStatusAttribute.getLatestSample();
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
        sb.append(rowCss + highlight);
        sb.append("\">");
        sb.append(calcClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (contactCalc != null) {
            sb.append(contactCalc.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (statusCalc != null) {
            sb.append(getStatus(statusCalc));
        }
        sb.append("</td>");
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (calcStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(calcStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactReport = null;
        Long statusReport = null;

        if (!reports.isEmpty()) {
            reportStatusAttribute = reports.get(0).getAttribute("Status");
            if (reportStatusAttribute != null) {
                JEVisSample latestSample = reportStatusAttribute.getLatestSample();
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
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss);
        sb.append("\">");
        if (reportStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(reportStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        DateTime contactNotifier = null;
        Long statusNotifier = null;

        if (!notifiers.isEmpty()) {
            notifierStatusAttribute = notifiers.get(0).getAttribute("Status");
            if (notifierStatusAttribute != null) {
                JEVisSample latestSample = notifierStatusAttribute.getLatestSample();
                if (latestSample != null) {
                    contactNotifier = latestSample.getTimestamp();
                    statusNotifier = latestSample.getValueAsLong();
                }
            }
        }

        sb.append("<tr>");
        /**
         * Service Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        sb.append(notifierClass.getName());
        sb.append("</td>");
        /**
         * Last Contact Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (contactNotifier != null) {
            sb.append(contactNotifier.toString(timestampFormat));
        }
        sb.append("</td>");
        /**
         * Status Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (statusNotifier != null) {
            sb.append(getStatus(statusNotifier));
        }
        sb.append("</td>");
        /**
         * average Runtime Column
         */
        sb.append("<td style=\"");
        sb.append(rowCss + highlight);
        sb.append("\">");
        if (notifierStatusAttribute != null) {
            ServiceRuntime serviceRuntime = new ServiceRuntime(notifierStatusAttribute);
            sb.append(serviceRuntime.getResult());
        }
        sb.append("</td>");
        sb.append("</tr>");

        sb.append("</table>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    public String getStatus(Long status) {
        if (status == 0) {
            return I18n.getInstance().getString("status.table.status.offline");
        } else if (status == 1) {
            return I18n.getInstance().getString("status.table.status.online.waiting");
        } else if (status == 2) {
            return I18n.getInstance().getString("status.table.status.online.running");
        } else return null;
    }
}
