package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.AlphanumComparator;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jevis.commons.utils.CommonMethods.getChildrenRecursive;

public class DataServerTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataServerTable.class);
    private final JEVisDataSource ds;
    private final DateTime latestReported;
    private final List<DataServerLine> dataServerLines = new ArrayList<>();

    public DataServerTable(JEVisDataSource ds, DateTime latestReported) {
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

        sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.dataserver")).append("</h2>");

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
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.channel")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.targetdatapoint")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.lasttimestamp")).append("</th>");
        sb.append("  </tr>");//border=\"0\"


        List<JEVisObject> channelObjects = getChannelObjects();
//        channelObjects.sort(getObjectComparator());

        Map<JEVisObject, JEVisObject> channelAndTarget = new HashMap<>();
        List<JEVisObject> outOfBounds = new ArrayList<>();

        for (JEVisObject channel : channelObjects) {
            JEVisAttribute targetAtt = null;
            JEVisSample lastSampleTarget = null;
            JEVisAttribute lastReadoutAtt = null;
            DateTime lr = latestReported;

            JEVisObject dataSource = getCorrespondingDataSource(channel);
            if (dataSource != null) {
                JEVisAttribute enabledAtt = dataSource.getAttribute(ENABLED);
                if (enabledAtt != null) {
                    JEVisSample latestSample = enabledAtt.getLatestSample();
                    if (latestSample != null) {
                        if (!latestSample.getValueAsBoolean()) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                logger.error("Could not find Data Source for channel {}:{}", channel.getName(), channel.getID());
                continue;
            }

            try {
                JEVisAttribute channelLRAtt = dataSource.getAttribute(LATEST_REPORTED);

                if (channelLRAtt.hasSample()) {
                    JEVisSample latestSample = channelLRAtt.getLatestSample();
                    if (latestSample != null) {
                        Long valueAsLong = latestSample.getValueAsLong();
                        lr = DateTime.now().minus(Period.hours(valueAsLong.intValue()));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }


            lastReadoutAtt = channel.getAttribute(LAST_READOUT_ATTRIBUTE_NAME);
            if (lastReadoutAtt != null) {
                if (lastReadoutAtt.hasSample()) {
                    JEVisSample lastSample = lastReadoutAtt.getLatestSample();
                    if (lastSample != null) {

                        DateTime ts = null;
                        try {
                            ts = new DateTime(lastSample.getValueAsString());
                        } catch (Exception e) {
                            try {
                                ts = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(lastSample.getValueAsString());
                            } catch (Exception e1) {

                            }
                        }
                        if (ts != null && ts.isBefore(lr)) {
                            if (!outOfBounds.contains(channel)) outOfBounds.add(channel);
                        }
                    }
                }
            }

            if (channel.getJEVisClass().equals(getLoytecXMLDLChannelClass()) || channel.getJEVisClass().equals(getLoytecOPCUAChannelClass()) || channel.getJEVisClass().equals(getVida350ChannelClass())) {
                if (channel.getJEVisClass().equals(getLoytecXMLDLChannelClass()) || channel.getJEVisClass().equals(getLoytecOPCUAChannelClass()))
                    targetAtt = channel.getAttribute("Target ID");
                else if (channel.getJEVisClass().equals(getVida350ChannelClass())) {
                    targetAtt = channel.getAttribute("Target");
                }

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    if (!th.getObject().isEmpty()) {
                        JEVisObject target = th.getObject().get(0);
                        if (target != null) {

                            channelAndTarget.put(channel, target);
                            getListCheckedData().add(target);

                            JEVisAttribute resultAtt = null;
                            if (!th.getAttribute().isEmpty()) resultAtt = th.getAttribute().get(0);
                            if (resultAtt == null) resultAtt = target.getAttribute(VALUE_ATTRIBUTE_NAME);

                            if (resultAtt != null) {
                                if (resultAtt.hasSample()) {
                                    JEVisSample lastSample = resultAtt.getLatestSample();
                                    if (lastSample != null) {
                                        if (lastSample.getTimestamp().isBefore(lr)) {
                                            if (!outOfBounds.contains(channel)) outOfBounds.add(channel);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                getOtherChannelsTarget(channel, channelAndTarget, outOfBounds, lr);
            }
        }

//        outOfBounds.sort(getObjectComparator());

        List<JEVisObject> asyncTargets = new ArrayList<>();
        for (JEVisObject currentChannel : outOfBounds) {
            JEVisObject target = channelAndTarget.get(currentChannel);
            if (target != null) {
                JEVisAttribute attribute = target.getAttribute(PERIOD_ATTRIBUTE_NAME);
                try {
                    if (new Period(attribute.getLatestSample().getValueAsString()).equals(Period.ZERO)) {
                        asyncTargets.add(currentChannel);
                    }
                } catch (Exception e) {
                    logger.error("Could not get period for object {}:{}", target.getName(), target.getID(), e);
                }
            }
        }

        outOfBounds.removeAll(asyncTargets);

        boolean odd = false;
        for (JEVisObject currentChannel : outOfBounds) {
            DataServerLine dataServerLine = new DataServerLine();
            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            String name = currentChannel.getName() + ":" + currentChannel.getID().toString();
            dataServerLine.setName(name);
            dataServerLine.setId(currentChannel.getID());
            String nameTarget = "";
            JEVisObject targetObject = null;
            try {
                targetObject = channelAndTarget.get(currentChannel);
                dataServerLine.setTargetObject(targetObject);
            } catch (Exception e) {
            }
            if (targetObject != null) {
                nameTarget = targetObject.getName() + ":" + targetObject.getID().toString();
                dataServerLine.setNameTarget(nameTarget);
            }

            dataServerLine.getLineString().append("<tr>");
            /**
             * Organisation Column
             */
            dataServerLine.getLineString().append("<td style=\"");
            dataServerLine.getLineString().append(css);
            dataServerLine.getLineString().append("\">");
            String organisationName = getParentName(currentChannel, getOrganizationClass());
            dataServerLine.getLineString().append(organisationName);
            dataServerLine.setOrganizationName(organisationName);
            dataServerLine.getLineString().append("</td>");
            /**
             * Building Column
             */
            dataServerLine.getLineString().append("<td style=\"");
            dataServerLine.getLineString().append(css);
            dataServerLine.getLineString().append("\">");
            String buildingName = getParentName(currentChannel, getBuildingClass());
            dataServerLine.getLineString().append(buildingName);
            dataServerLine.setBuildingName(buildingName);
            dataServerLine.getLineString().append("</td>");
            /**
             * Channel
             */
            dataServerLine.getLineString().append("<td style=\"");
            dataServerLine.getLineString().append(css);
            dataServerLine.getLineString().append("\">");
            dataServerLine.getLineString().append(name);
            dataServerLine.getLineString().append("</td>");
            /**
             * Target Data Point
             */
            dataServerLine.getLineString().append("<td style=\"");
            dataServerLine.getLineString().append(css);
            dataServerLine.getLineString().append("\">");
            dataServerLine.getLineString().append(nameTarget);
            dataServerLine.getLineString().append("</td>");
            /**
             * Last Time Stamp
             */
            dataServerLine.getLineString().append("<td style=\"");
            dataServerLine.getLineString().append(css);
            dataServerLine.getLineString().append("\">");
            if (targetObject != null) {
                JEVisAttribute targetAtt = targetObject.getAttribute(VALUE_ATTRIBUTE_NAME);
                if (targetAtt != null) {
                    if (targetAtt.hasSample()) {
                        DateTime timestamp = targetAtt.getLatestSample().getTimestamp();
                        dataServerLine.getLineString().append(dtf.print(timestamp));
                        dataServerLine.setLastTimeStamp(timestamp);
                    }
                }
            } else {
                JEVisAttribute lastReadoutAtt = currentChannel.getAttribute(LAST_READOUT_ATTRIBUTE_NAME);
                if (lastReadoutAtt != null) {
                    if (lastReadoutAtt.hasSample()) {
                        DateTime dateTime = new DateTime(lastReadoutAtt.getLatestSample().getValueAsString());
                        dataServerLine.getLineString().append(dtf.print(dateTime));
                        dataServerLine.setLastTimeStamp(dateTime);
                    }
                }
            }
            dataServerLine.getLineString().append("</td>");

            dataServerLine.getLineString().append("</tr>");

            dataServerLines.add(dataServerLine);
        }

        dataServerLines.sort((o1, o2) -> {
            if (o1.getLastTimeStamp().isBefore(o2.getLastTimeStamp())) return -1;
            else if (o1.getLastTimeStamp().isAfter(o2.getLastTimeStamp())) return 1;
            else {
                AlphanumComparator ac = new AlphanumComparator();
                return ac.compare(o1.getOrganizationName() +
                        o1.getBuildingName() +
                        o1.getName() + ":" + o1.getId().toString(), o1.getOrganizationName() +
                        o1.getBuildingName() +
                        o1.getName() + ":" + o1.getId().toString());
            }
        });

        for (DataServerLine dataServerLine : dataServerLines) {
            sb.append(dataServerLine.getLineString().toString());
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    private JEVisObject getCorrespondingDataSource(JEVisObject channel) throws JEVisException {
        JEVisClass dataSourceClass = channel.getDataSource().getJEVisClass("Data Source");

        return getDSParentRec(channel, dataSourceClass);
    }

    private JEVisObject getDSParentRec(JEVisObject channel, JEVisClass dataSourceClass) throws JEVisException {
        for (JEVisObject parent : channel.getParents()) {
            for (JEVisClass heir : dataSourceClass.getHeirs()) {
                if (parent.getJEVisClass().equals(heir)) {
                    return parent;
                }
            }
            return getDSParentRec(parent, dataSourceClass);
        }
        return null;
    }

    private void getOtherChannelsTarget(JEVisObject channel, Map<JEVisObject, JEVisObject> channelAndTarget,
                                        List<JEVisObject> outOfBounds, DateTime latestReported) throws JEVisException {
        List<JEVisObject> dps = new ArrayList<>();

        final JEVisClass channelClass = channel.getJEVisClass();

        final String dwd1 = "Atmospheric Pressure Target";
        final String dwd2 = "Humidity Target";
        final String dwd3 = "Precipitation Target";
        final String dwd4 = "Temperature Target";
        final String dwd5 = "Wind Speed Target";

        if (channelClass.equals(getFtpChannelClass())) {
            dps.addAll(getChildrenRecursive(channel, getCsvDataPointClass()));
            dps.addAll(getChildrenRecursive(channel, getDwdDataPointClass()));
            dps.addAll(getChildrenRecursive(channel, getXmlDataPointClass()));
            dps.addAll(getChildrenRecursive(channel, getDataPointClass()));
        } else {
            dps.addAll(getChildrenRecursive(channel, getCsvDataPointClass()));
            dps.addAll(getChildrenRecursive(channel, getXmlDataPointClass()));
            dps.addAll(getChildrenRecursive(channel, getDataPointClass()));
        }

        for (JEVisObject dp : dps) {

            if (dp.getJEVisClass().equals(getCsvDataPointClass()) || dp.getJEVisClass().equals(getXmlDataPointClass())) {
                JEVisAttribute targetAtt = null;
                JEVisSample lastSampleTarget = null;

                targetAtt = dp.getAttribute(STANDARD_TARGET_ATTRIBUTE_NAME);

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    JEVisObject target = null;
                    if (th.getObject() != null && !th.getObject().isEmpty()) target = th.getObject().get(0);
                    if (target != null) {
                        channelAndTarget.put(target, target);
                        getListCheckedData().add(target);

                        JEVisAttribute resultAtt = null;
                        if (th.getAttribute() != null && !th.getAttribute().isEmpty()) {
                            resultAtt = th.getAttribute().get(0);
                        } else resultAtt = target.getAttribute(VALUE_ATTRIBUTE_NAME);

                        if (resultAtt != null) {
                            if (resultAtt.hasSample()) {
                                JEVisSample lastSample = resultAtt.getLatestSample();
                                if (lastSample != null) {
                                    if (lastSample.getTimestamp().isBefore(latestReported)) {
                                        if (!outOfBounds.contains(target)) outOfBounds.add(target);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (dp.getJEVisClass().equals(getDwdDataPointClass())) {
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
                    try {
                        th = new TargetHelper(ds, lastSampleTarget.getValueAsString());

                        if (!th.isObject()) {
                            logger.error("DP has no valid target: {}:{} in '{}'", lastSampleTarget.getAttribute().getName(), lastSampleTarget);
                            continue;
                        }
                        JEVisObject target = th.getObject().get(0);
                        if (target != null) {

                            channelAndTarget.put(target, target);
                            getListCheckedData().add(target);

                            JEVisAttribute resultAtt = null;
                            if (!th.getAttribute().isEmpty()) resultAtt = th.getAttribute().get(0);
                            if (resultAtt == null) resultAtt = target.getAttribute(VALUE_ATTRIBUTE_NAME);

                            if (resultAtt != null) {
                                if (resultAtt.hasSample()) {
                                    JEVisSample lastSample = resultAtt.getLatestSample();
                                    if (lastSample != null) {
                                        if (lastSample.getTimestamp().isBefore(latestReported)) {
                                            if (!outOfBounds.contains(target)) outOfBounds.add(target);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        logger.error("error in target: {}-{}", dp, lastSampleTarget, ex);
                    }
                }
            }
        }
    }


    private List<JEVisObject> getChannelObjects() throws JEVisException {

        return new ArrayList<>(ds.getObjects(getChannelClass(), true));
    }

    public List<DataServerLine> getDataServerLines() {
        return dataServerLines;
    }
}
