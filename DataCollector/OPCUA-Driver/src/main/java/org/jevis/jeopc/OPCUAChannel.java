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
public class OPCUAChannel {

    public final static String JEVIS_CLASS = "OPC UA Channel";
    public final static String DIR_JEVIS_CLASS = "OPC UA Channel Directory";
    private final static Logger log = LogManager.getLogger(OPCUAChannel.class.getName());
    private final static String NODE_ID = "Node ID";
    private final static String TARGET_ID = "Target ID";
    private final static String LAST_READOUT = "Last Readout";
    private final static String STATUS_LOG = "Status Log";
    private final static String FUNCTION_NODE = "Function Node ID";
    private final static String FUNCTION_INTERVAL = "Function Interval";

    private final JEVisObject channelObject;
    private String name;
    private String nodeId;
    private DateTime lastReadout;
    private JEVisAttribute targetAtt;
    private JEVisAttribute statusLog;
    private JEVisObject targetObj;
    private final Helper helper = new Helper();
    private String targetString;
    private String functionNodeID;
    private double functionInterval;


    //private DateTime lastSampleTimeStamp = DateTime.now();

    /**
     * manual constructor for debug use
     **/
    public OPCUAChannel(String nodeID, String functionNodeID, double functionInterval) {
        channelObject = null;
        this.nodeId = nodeID;
        this.functionNodeID = functionNodeID;
        this.functionInterval = functionInterval;
    }

    public OPCUAChannel(JEVisObject channelObject) throws JEVisException {
        log.error("UPCUAChannel init: {}", channelObject);

        try {
            channelObject.getDataSource().reloadAttribute(channelObject);
        } catch (Exception e) {
            log.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
        }

        this.channelObject = channelObject;
        this.update();

        log.error("UPC Channel: {}", toString());

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

        functionNodeID = helper.getValue(channelObject, FUNCTION_NODE);
        try {
            String intervalStr = helper.getValue(channelObject, FUNCTION_INTERVAL);
            if (intervalStr != null && !intervalStr.isEmpty()) {
                functionInterval = Double.parseDouble(intervalStr);
            }
        } catch (Exception e) {
            log.warn("Error parsing Interval: " + e);
        }
    }

    public NodeId getOPCNodeId() {
        //NodeId nodeIdNumeric = new NodeId(1, 20100);

        NodeId nodeIdName = NodeId.parse(nodeId); //20100
        return nodeIdName;
    }

    public JEVisAttribute getTarget() {
        return targetAtt;
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

    public String getFunctionNode() {
        return functionNodeID;
    }

    public double getFunctionInterval() {
        return functionInterval;
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
