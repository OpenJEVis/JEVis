package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
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
    private final JEVisObject entryPoint;
    private final DateTime latestReported;
    private final List<DataServerLine> dataServerLines = new ArrayList<>();

    public DataServerTable(JEVisDataSource ds, JEVisObject entryPoint, DateTime latestReported) {
        super(ds);
        this.ds = ds;
        this.entryPoint = entryPoint;
        this.latestReported = latestReported;

        try {
            createTableString();
        } catch (Exception e) {
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

            JEVisObject dataSource = CommonMethods.getFirstParentalObjectOfClassWithInheritance(channel, "Data Source");
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
                } else continue;
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

            if (channel.getJEVisClass().equals(getVida350ChannelClass())) {
                targetAtt = channel.getAttribute("Target");
            } else {
                targetAtt = channel.getAttribute("Target ID");
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
            } else if (targetAtt == null) {
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
                    if (!attribute.hasSample() || Period.ZERO.equals(new Period(attribute.getLatestSample().getValueAsString()))) {
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

    private void getOtherChannelsTarget(JEVisObject channel, Map<JEVisObject, JEVisObject> channelAndTarget,
                                        List<JEVisObject> outOfBounds, DateTime latestReported) {
        List<JEVisObject> dps = new ArrayList<>();

        try {
            dps.addAll(getChildrenRecursive(channel, getCsvDataPointClass()));
        } catch (Exception e) {
            logger.error("Error while getting CSV Data Points for channel {}", channel, e);
        }
        try {
            dps.addAll(getChildrenRecursive(channel, getXlsxDataPointClass()));
        } catch (Exception e) {
            logger.error("Error while getting XLSX Data Points for channel {}", channel, e);
        }
        try {
            dps.addAll(getChildrenRecursive(channel, getXmlDataPointClass()));
        } catch (Exception e) {
            logger.error("Error while getting XML Data Points for channel {}", channel, e);
        }
        try {
            dps.addAll(getChildrenRecursive(channel, getJsonDataPointClass()));
        } catch (Exception e) {
            logger.error("Error while getting JSON Data Points for channel {}", channel, e);
        }
        try {
            dps.addAll(getChildrenRecursive(channel, getDataPointClass()));
        } catch (Exception e) {
            logger.error("Error while getting Data Points for channel {}", channel, e);
        }

        for (JEVisObject dp : dps) {

            try {
                if (dp.getJEVisClass().equals(getCsvDataPointClass()) || dp.getJEVisClass().equals(getXlsxDataPointClass())
                        || dp.getJEVisClass().equals(getXmlDataPointClass()) || dp.getJEVisClass().equals(getJsonDataPointClass())) {
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
                }
            } catch (Exception e) {
                logger.error("Error while processing Data Point {} for channel {}", dp, channel, e);
            }
        }
    }


    private List<JEVisObject> getChannelObjects() throws JEVisException {
        List<JEVisObject> channelObjects = new ArrayList<>();
        try {
            channelObjects.addAll(CommonMethods.getInheritedChildrenRecursive(entryPoint, getChannelClass()));
        } catch (Exception e) {
            logger.error(e);
        }
        return channelObjects;
    }

    public List<DataServerLine> getDataServerLines() {
        return dataServerLines;
    }
}
