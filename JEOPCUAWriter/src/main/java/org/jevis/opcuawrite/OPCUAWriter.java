package org.jevis.opcuawrite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;
import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class OPCUAWriter {
    private static final Logger logger = LogManager.getLogger(OPCUAWriterLauncher.class);
    private static final String USER = "User";
    private static final String PASSWORD = "Password";
    private static final String TARGET_ID = "Target ID";
    private static final String VALUE = "Value";
    private static final String OPC_ID = "OPC ID";
    private OPCClient opcClient;






    public Boolean sendOPCUANotification(JEVisObject outputChannel, JEVisObject dataObject) {



            try {



                if (!outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).hasSample() || dataObject.getAttribute(VALUE).getTimestampFromLastSample().getMillis() > outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).getTimestampFromLastSample().getMillis()) {

                    logger.info("new Data to write for JEVis ID", outputChannel.getID());

                    NodeId nodeId = NodeId.parse(outputChannel.getAttribute(OPC_ID).getLatestSample().getValue().toString());
                    String datatype = opcClient.getDataType(nodeId);
                    logger.info("OPC-UA Node datatype:", datatype);

                    if (datatype.equals(Double.class.getName())) {
                        opcClient.writeValue(dataObject.getAttribute(VALUE).getLatestSample().getValueAsDouble(), nodeId);

                    } else if (datatype.equals(Boolean.class.getName())) {
                        opcClient.writeValue(convertToBool(dataObject.getAttribute(VALUE).getLatestSample().getValueAsDouble()), nodeId);

                    } else if (datatype.equals(String.class.getName())) {
                        opcClient.writeValue(dataObject.getAttribute(VALUE).getLatestSample().getValueAsString(), nodeId);

                    } else {
                        throw new Exception("Datatype not found");

                    }

                    OPCUAStatus opcUAStatus = new OPCUAStatus(OPCUAStatus.SUCCESS);
                    opcUAStatus.writeStatus(outputChannel, DateTime.now());

                    return true;

                }


            } catch (Exception e) {
                e.printStackTrace();
                OPCUAStatus opcUAStatus = new OPCUAStatus(OPCUAStatus.NOT_WRITTEN);
                opcUAStatus.writeStatus(outputChannel, DateTime.now());

            }
        return false;
        }


    public void connectToOPCUAServer(JEVisObject opcServerObj) throws ExecutionException, InterruptedException, UaException {

        try {
            logger.info("set up OPC-UA Connection");


            OPCUAServer opcuaServer = new OPCUAServer(opcServerObj);

            opcClient = new OPCClient(opcuaServer.getURL());//"opc.tcp://10.1.2.128:4840");
            EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
            opcClient.setEndpoints(endpointDescription);

            if (!opcServerObj.getAttribute(USER).getLatestSample().getValue().toString().isEmpty() && !opcServerObj.getAttribute(PASSWORD).getLatestSample().getValue().toString().isEmpty()) {
                UsernameProvider usernameProvider = new UsernameProvider(opcServerObj.getAttribute(USER).getLatestSample().getValue().toString(), opcServerObj.getAttribute(PASSWORD).getLatestSample().getValue().toString());
                opcClient.setIdentification(usernameProvider);
                logger.info("Connect to OPC-UA Server to: ", opcuaServer.getURL());
                opcClient.connect();
            }
        } catch (JEVisException jeVisException) {
            jeVisException.printStackTrace();
        }


    }

    public void disconnect() {
        logger.info("Disconnected from OPC-UA Server");
        opcClient.close();
    }

    public Boolean convertToBool(Double value) {
        if (value == 1.0) {
            return true;
        } else if (value == 0.0) {
            return false;
        } else {
            return null;
        }
    }


}
