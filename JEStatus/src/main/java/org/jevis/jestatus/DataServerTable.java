package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.*;

public class DataServerTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataServerTable.class);
    private final JEVisDataSource ds;
    private final Alarm alarm;

    public DataServerTable(JEVisDataSource ds, Alarm alarm) {
        this.ds = ds;
        this.alarm = alarm;

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

        sb.append("<h2>Data Server</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Organisation</th>");
        sb.append("    <th>Building</th>");
        sb.append("    <th>Channel</th>");
        sb.append("    <th>Target Data Point</th>");
        sb.append("    <th>Last Time Stamp</th>");
        sb.append("  </tr>");//border=\"0\"


        List<JEVisObject> channelObjects = getChannelObjects();

        DateTime now = new DateTime();
        DateTime ignoreTS = now.minus(Period.hours(alarm.getIgnoreOld()));
        DateTime limit = now.minus(Period.hours(alarm.getTimeLimit()));
        Map<JEVisObject, JEVisObject> channelAndTarget = new HashMap<>();
        List<JEVisObject> outOfBounds = new ArrayList<>();

        for (JEVisObject channel : channelObjects) {
            JEVisAttribute targetAtt = null;
            JEVisSample lastSampleTarget = null;
            JEVisAttribute lastReadoutAtt = null;

            lastReadoutAtt = channel.getAttribute("Last Readout");
            if (lastReadoutAtt != null) {
                if (lastReadoutAtt.hasSample()) {
                    JEVisSample lastSample = lastReadoutAtt.getLatestSample();
                    if (lastSample != null) {
                        if (lastSample.getTimestamp().isBefore(limit) && lastSample.getTimestamp().isAfter(ignoreTS)) {
                            if (!outOfBounds.contains(channel)) outOfBounds.add(channel);
                        }
                    }
                }
            }

            if (channel.getJEVisClassName().equals("Loytec XML-DL Channel") || channel.getJEVisClassName().equals("VIDA350 Channel")) {
                if (channel.getJEVisClassName().equals("Loytec XML-DL Channel"))
                    targetAtt = channel.getAttribute("Target ID");
                else if (channel.getJEVisClassName().equals("VIDA350 Channel")) {
                    targetAtt = channel.getAttribute("Target");
                }

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    JEVisObject target = th.getObject();
                    if (target != null) {

                        channelAndTarget.put(channel, target);
                        getListCheckedData().add(target);

                        JEVisAttribute resultAtt = th.getAttribute();
                        if (resultAtt == null) resultAtt = target.getAttribute("Value");

                        if (resultAtt != null) {
                            if (resultAtt.hasSample()) {
                                JEVisSample lastSample = resultAtt.getLatestSample();
                                if (lastSample != null) {
                                    if (lastSample.getTimestamp().isBefore(limit) && lastSample.getTimestamp().isAfter(ignoreTS)) {
                                        if (!outOfBounds.contains(channel)) outOfBounds.add(channel);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                getOtherChannelsTarget(channel, channelAndTarget, outOfBounds, limit, ignoreTS);
            }
        }

        outOfBounds.sort(new Comparator<JEVisObject>() {
            @Override
            public int compare(JEVisObject o1, JEVisObject o2) {
                DateTime o1ts = null;
                try {
                    JEVisAttribute o1att = o1.getAttribute("Last Readout");
                    if (o1att != null) {
                        JEVisSample o1smp = o1att.getLatestSample();
                        if (o1smp != null) {
                            o1ts = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(o1smp.getValueAsString());
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                DateTime o2ts = null;
                try {
                    JEVisAttribute o2att = o2.getAttribute("Last Readout");
                    if (o2att != null) {
                        JEVisSample o2smp = o2att.getLatestSample();
                        if (o2smp != null) {
                            o2ts = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(o2smp.getValueAsString());
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if ((o1ts != null && o2ts != null && o1ts.isBefore(o2ts))) return -1;
                else if ((o1ts != null && o2ts != null && o1ts.isAfter(o2ts))) return 1;
                else return 0;
            }
        });

        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        JEVisClass buildingClass = ds.getJEVisClass("Monitored Object");

        boolean odd = false;
        for (JEVisObject currentChannel : outOfBounds) {
            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            String name = currentChannel.getName() + ":" + currentChannel.getID().toString();
            String nameTarget = "";
            JEVisObject targetObject = null;
            try {
                targetObject = channelAndTarget.get(currentChannel);
            } catch (Exception e) {
            }
            if (targetObject != null) nameTarget = targetObject.getName() + ":" + targetObject.getID().toString();

            sb.append("<tr>");
            /**
             * Organisation Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentChannel, organizationClass));
            sb.append("</td>");
            /**
             * Building Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentChannel, buildingClass));
            sb.append("</td>");
            /**
             * Channel
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(name);
            sb.append("</td>");
            /**
             * Target Data Point
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(nameTarget);
            sb.append("</td>");
            /**
             * Last Time Stamp
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (targetObject != null) {
                JEVisAttribute targetAtt = targetObject.getAttribute("Value");
                if (targetAtt != null) {
                    if (targetAtt.hasSample()) {
                        sb.append(dtf.print(targetAtt.getLatestSample().getTimestamp()));
                    }
                }
            } else {
                JEVisAttribute lastReadoutAtt = currentChannel.getAttribute("Last Readout");
                if (lastReadoutAtt != null) {
                    if (lastReadoutAtt.hasSample()) {
                        sb.append(dtf.print(lastReadoutAtt.getLatestSample().getTimestamp()));
                    }
                }
            }
            sb.append("</td>");

            sb.append("</tr>");
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    private void getOtherChannelsTarget(JEVisObject channel, Map<JEVisObject, JEVisObject> channelAndTarget, List<JEVisObject> outOfBounds, DateTime limit, DateTime ignoreTS) throws JEVisException {
        List<JEVisObject> dps = new ArrayList<>();

        final JEVisClass channelClass = channel.getJEVisClass();
        final JEVisClass ftpChannel = ds.getJEVisClass("FTP Channel");
        final JEVisClass httpChannel = ds.getJEVisClass("HTTP Channel");
        final JEVisClass sFtpChannel = ds.getJEVisClass("sFTP Channel");
        final JEVisClass soapChannel = ds.getJEVisClass("SOAP Channel");
        final JEVisClass csvDataPoint = ds.getJEVisClass("CSV Data Point");
        final String standardTargetAttribute = "Target";
        final JEVisClass dwdDataPoint = ds.getJEVisClass("DWD Data Point");
        final JEVisClass xmlDataPoint = ds.getJEVisClass("XML Data Point");
        final JEVisClass dataPoint = ds.getJEVisClass("Data Point");
        final String dwd1 = "Atmospheric Pressure Target";
        final String dwd2 = "Humidity Target";
        final String dwd3 = "Precipitation Target";
        final String dwd4 = "Temperature Target";
        final String dwd5 = "Wind Speed Target";

        if (channelClass.equals(ftpChannel)) {
            dps.addAll(getChildrenRecursive(channel, csvDataPoint));
            dps.addAll(getChildrenRecursive(channel, dwdDataPoint));
            dps.addAll(getChildrenRecursive(channel, xmlDataPoint));
            dps.addAll(getChildrenRecursive(channel, dataPoint));
        } else {
            dps.addAll(getChildrenRecursive(channel, csvDataPoint));
            dps.addAll(getChildrenRecursive(channel, xmlDataPoint));
            dps.addAll(getChildrenRecursive(channel, dataPoint));
        }

        for (JEVisObject dp : dps) {

            if (dp.getJEVisClass().equals(csvDataPoint) || dp.getJEVisClass().equals(xmlDataPoint)) {
                JEVisAttribute targetAtt = null;
                JEVisSample lastSampleTarget = null;

                targetAtt = dp.getAttribute(standardTargetAttribute);

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    JEVisObject target = th.getObject();
                    if (target != null) {
                        channelAndTarget.put(target, target);
                        getListCheckedData().add(target);

                        JEVisAttribute resultAtt = th.getAttribute();
                        if (resultAtt == null) resultAtt = target.getAttribute("Value");

                        if (resultAtt != null) {
                            if (resultAtt.hasSample()) {
                                JEVisSample lastSample = resultAtt.getLatestSample();
                                if (lastSample != null) {
                                    if (lastSample.getTimestamp().isBefore(limit) && lastSample.getTimestamp().isAfter(ignoreTS)) {
                                        if (!outOfBounds.contains(target)) outOfBounds.add(target);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (dp.getJEVisClass().equals(dwdDataPoint)) {
                JEVisAttribute targetAtt1 = null;
                JEVisSample lastSampleTarget1 = null;
                JEVisAttribute targetAtt2 = null;
                JEVisSample lastSampleTarget2 = null;
                JEVisAttribute targetAtt3 = null;
                JEVisSample lastSampleTarget3 = null;
                JEVisAttribute targetAtt4 = null;
                JEVisSample lastSampleTarget4 = null;
                JEVisAttribute targetAtt5 = null;
                JEVisSample lastSampleTarget5 = null;

                targetAtt1 = dp.getAttribute(dwd1);
                targetAtt2 = dp.getAttribute(dwd2);
                targetAtt3 = dp.getAttribute(dwd3);
                targetAtt4 = dp.getAttribute(dwd4);
                targetAtt5 = dp.getAttribute(dwd5);

                if (targetAtt1 != null) lastSampleTarget1 = targetAtt1.getLatestSample();
                if (targetAtt2 != null) lastSampleTarget2 = targetAtt2.getLatestSample();
                if (targetAtt3 != null) lastSampleTarget3 = targetAtt3.getLatestSample();
                if (targetAtt4 != null) lastSampleTarget4 = targetAtt4.getLatestSample();
                if (targetAtt5 != null) lastSampleTarget5 = targetAtt5.getLatestSample();

                List<JEVisSample> samples = new ArrayList<>();
                if (lastSampleTarget1 != null) samples.add(lastSampleTarget1);
                if (lastSampleTarget2 != null) samples.add(lastSampleTarget2);
                if (lastSampleTarget3 != null) samples.add(lastSampleTarget3);
                if (lastSampleTarget4 != null) samples.add(lastSampleTarget4);
                if (lastSampleTarget5 != null) samples.add(lastSampleTarget5);

                TargetHelper th = null;
                for (JEVisSample lastSampleTarget : samples) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    JEVisObject target = th.getObject();
                    if (target != null) {

                        channelAndTarget.put(target, target);
                        getListCheckedData().add(target);

                        JEVisAttribute resultAtt = th.getAttribute();
                        if (resultAtt == null) resultAtt = target.getAttribute("Value");

                        if (resultAtt != null) {
                            if (resultAtt.hasSample()) {
                                JEVisSample lastSample = resultAtt.getLatestSample();
                                if (lastSample != null) {
                                    if (lastSample.getTimestamp().isBefore(limit) && lastSample.getTimestamp().isAfter(ignoreTS)) {
                                        if (!outOfBounds.contains(target)) outOfBounds.add(target);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<JEVisObject> getChildrenRecursive(JEVisObject channel, JEVisClass jeVisClass) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        for (JEVisObject child : channel.getChildren()) {
            if (child.getJEVisClass().equals(jeVisClass)) list.add(child);

            for (JEVisObject child2 : child.getChildren()) {
                list.addAll(getChildrenRecursive(child2, jeVisClass));
            }
        }

        return list;
    }


    private List<JEVisObject> getChannelObjects() throws JEVisException {
        JEVisClass channelClass = ds.getJEVisClass("Channel");
        return new ArrayList<>(ds.getObjects(channelClass, true));
    }

}
