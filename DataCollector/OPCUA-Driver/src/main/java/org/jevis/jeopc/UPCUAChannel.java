package org.jevis.jeopc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

/**
 * This implements the channel and related functions
 */
public class UPCUAChannel {

    public final static String JEVIS_CLASS = "OPC UA Channel";
    public final static String DIR_JEVIS_CLASS = "OPC UA Channel Directory";
    private final static Logger log = LogManager.getLogger(UPCUAChannel.class.getName());
    private final static String NODE_ID = "Node ID";
    private final static String TARGET_ID = "Target ID";
    private final static String LAST_READOUT = "Last Readout";
    private final static String STATUS_LOG = "Status Log";


    private final JEVisObject channelObject;
    private String name;
    private String nodeId;
    private DateTime lastReadout;
    private JEVisAttribute targetAtt;
    private JEVisAttribute statusLog;
    private JEVisObject targetObj;
    private final Helper helper = new Helper();
    private String targetString;

    //private DateTime lastSampleTimeStamp = DateTime.now();

    public UPCUAChannel(JEVisObject channelObject) throws JEVisException {
        log.error("UPCUAChannel init: {}", channelObject);

        this.channelObject = channelObject;

        this.update();

        log.error("UPC Channel: {}", toString());

    }

    public NodeId getOPCNodeId() {
        //NodeId nodeIdNumeric = new NodeId(1, 20100);

        NodeId nodeIdName = NodeId.parse(nodeId); //20100
        return nodeIdName;
    }

    public JEVisAttribute getTarget() {
        return targetAtt;
    }

    private void update() throws JEVisException {
        log.debug("Getting channel name");
        name = channelObject.getName();
        log.debug("Getting channel trend id");
        nodeId = helper.getValue(channelObject, NODE_ID);
        log.debug("Getting channel target id");


        JEVisAttribute channelTargetAtt = channelObject.getAttribute(TARGET_ID);
        JEVisSample latestSample = channelTargetAtt.getLatestSample();
        if (latestSample != null) {
            targetString = latestSample.getValueAsString();
        }

        TargetHelper th = new TargetHelper(channelObject.getDataSource(), channelTargetAtt);
        if (!th.getAttribute().isEmpty()) {
            targetAtt = th.getAttribute().get(0);
        }
        targetObj = targetAtt.getObject();
        targetObj.getID();

        log.debug("Getting channel last read out");

        JEVisClass channelClass = channelObject.getJEVisClass();
        JEVisType readoutType = channelClass.getType(LAST_READOUT);
        lastReadout = DatabaseHelper.getObjectAsDate(channelObject, readoutType);
        JEVisType statusLogType = channelClass.getType(STATUS_LOG);
        statusLog = channelObject.getAttribute(statusLogType);
    }

    public String getTargetAttribute() {
        return targetAtt.getName();
    }

    public String getTargetString() {
        return targetString;
    }

    public String getName() {
        return name;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Long getTargetId() {
        return targetAtt.getObjectID();
    }

    public DateTime getLastReadout() {
        return lastReadout;
    }

    public JEVisObject getJeVisObject() {
        return channelObject;
    }

    public JEVisAttribute getStatusLog() {
        return statusLog;
    }

    @Override
    public String toString() {
        return "UPCUAChannel{" +
                "channelObject=" + channelObject +
                ", name='" + name + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", lastReadout=" + lastReadout +
                ", targetStr=" + targetString +
                ", statusLog=" + statusLog +
                '}';
    }
}