package org.jevis.commons.datasource;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;

import java.util.*;

import static org.jevis.commons.utils.CommonMethods.getChildrenRecursive;

public class ChannelTools {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ChannelTools.class);

    protected final String VALUE_ATTRIBUTE_NAME = "Value";
    protected final String STANDARD_TARGET_ATTRIBUTE_NAME = "Target";
    private final Map<Long, Long> targetAndChannel = new HashMap<>();
    private final List<JEVisClass> targetAttChannels = new ArrayList<>();
    private JEVisClass vida350ChannelClass;
    private JEVisClass loytecXMLDLChannelClass;
    private JEVisClass loytecOPCUAChannelClass;
    private JEVisClass ftpChannelClass;
    private JEVisClass csvDataPointClass;
    private JEVisClass dwdDataPointClass;
    private JEVisClass xmlDataPointClass;
    private JEVisClass dataPointClass;
    private JEVisClass channelClass;
    private JEVisClass sqlRequestClass;

    public void createChannelMaps(JEVisDataSource ds) {

        Date start = new Date();
        try {
            for (JEVisObject channel : getChannelObjects(ds)) {
                JEVisAttribute targetAtt = null;
                JEVisSample lastSampleTarget = null;

                if (targetAttChannels.contains(channel.getJEVisClass())) {
                    if (channel.getJEVisClass().equals(loytecXMLDLChannelClass) || channel.getJEVisClass().equals(loytecOPCUAChannelClass))
                        targetAtt = channel.getAttribute("Target ID");
                    else if (channel.getJEVisClass().equals(vida350ChannelClass) || channel.getJEVisClass().equals(sqlRequestClass)) {
                        targetAtt = channel.getAttribute("Target");
                    }

                    if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                    TargetHelper th = null;
                    if (lastSampleTarget != null) {
                        th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                        if (!th.getObject().isEmpty()) {
                            JEVisObject target = th.getObject().get(0);
                            if (target != null) {
                                targetAndChannel.put(target.getID(), channel.getID());
                            }
                        }
                    }
                } else {
                    getOtherChannelsTarget(ds, channel, targetAndChannel);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        Date now = new Date();
        System.out.println("Time to fill target: " + (now.getTime() - start.getTime()));
    }

    private void getOtherChannelsTarget(JEVisDataSource ds, JEVisObject channel, Map<Long, Long> targetAndChannel) throws JEVisException {
        List<JEVisObject> dps = new ArrayList<>();

        final JEVisClass channelClass = channel.getJEVisClass();

        final String dwd1 = "Atmospheric Pressure Target";
        final String dwd2 = "Humidity Target";
        final String dwd3 = "Precipitation Target";
        final String dwd4 = "Temperature Target";
        final String dwd5 = "Wind Speed Target";

        if (channelClass.equals(ftpChannelClass)) {
            dps.addAll(getChildrenRecursive(channel, csvDataPointClass));
            dps.addAll(getChildrenRecursive(channel, dwdDataPointClass));
            dps.addAll(getChildrenRecursive(channel, xmlDataPointClass));
            dps.addAll(getChildrenRecursive(channel, dataPointClass));
        } else {
            dps.addAll(getChildrenRecursive(channel, csvDataPointClass));
            dps.addAll(getChildrenRecursive(channel, xmlDataPointClass));
            dps.addAll(getChildrenRecursive(channel, dataPointClass));
        }

        for (JEVisObject dp : dps) {

            if (dp.getJEVisClass().equals(csvDataPointClass) || dp.getJEVisClass().equals(xmlDataPointClass)) {
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
                        targetAndChannel.put(target.getID(), channel.getID());
                    }
                }
            } else if (dp.getJEVisClass().equals(dwdDataPointClass)) {
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

                            targetAndChannel.put(target.getID(), channel.getID());
                        }
                    } catch (Exception ex) {
                        logger.error("error in target: {}-{}", dp, lastSampleTarget, ex);
                    }
                }
            }
        }
    }

    private List<JEVisObject> getChannelObjects(JEVisDataSource ds) throws JEVisException {
        if (channelClass == null) {
            createChannelClasses(ds);
        }
        return new ArrayList<>(ds.getObjects(channelClass, true));
    }

    private void createChannelClasses(JEVisDataSource ds) {
        try {
            channelClass = ds.getJEVisClass("Channel");
            loytecXMLDLChannelClass = ds.getJEVisClass("Loytec XML-DL Channel");
            loytecOPCUAChannelClass = ds.getJEVisClass("OPC UA Channel");
            vida350ChannelClass = ds.getJEVisClass("VIDA350 Channel");
            ftpChannelClass = ds.getJEVisClass("FTP Channel");
            csvDataPointClass = ds.getJEVisClass("CSV Data Point");
            dwdDataPointClass = ds.getJEVisClass("DWD Data Point");
            xmlDataPointClass = ds.getJEVisClass("XML Data Point");
            dataPointClass = ds.getJEVisClass("Data Point");
            sqlRequestClass = ds.getJEVisClass("SQL Request");

            targetAttChannels.add(loytecXMLDLChannelClass);
            targetAttChannels.add(loytecOPCUAChannelClass);
            targetAttChannels.add(vida350ChannelClass);
            targetAttChannels.add(sqlRequestClass);


        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, Long> getTargetAndChannel() {
        return targetAndChannel;
    }
}
