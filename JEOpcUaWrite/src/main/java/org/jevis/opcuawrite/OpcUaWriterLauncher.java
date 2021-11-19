/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.opcuawrite;

import com.google.inject.Injector;
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
import org.jevis.commons.cli.AbstractCliApp;

import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.utils.Samples;
import org.jevis.jeopc.OPCClient;
import org.jevis.jeopc.OPCUAServer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;



/**
 * @author broder
 */
public class OpcUaWriterLauncher extends AbstractCliApp {

    private static final Logger logger = LogManager.getLogger(OpcUaWriterLauncher.class);
    private static Injector injector;
    private static final String APP_INFO = "JEOpcUaWriter";
    private static final String USER = "User";
    private static final String PASSWORD = "Password";
    private static final String TARGET_ID = "Target ID";
    private static final String VALUE = "Value";
    private static final String OPC_ID = "OPC ID";

    private final Command commands = new Command();
    private boolean firstRun = true;
    private OPCClient opcClient;
    private List<JEVisObject> outputChannels = new ArrayList<>();

    public OpcUaWriterLauncher(String[] args, String appname) {
        super(args, appname);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        logger.info("-------Start JEOpcUaWriter-------");
        OpcUaWriterLauncher app = new OpcUaWriterLauncher(args, APP_INFO);
        app.execute();
    }

    public static Injector getInjector() {
        return injector;
    }

    public static void setInjector(Injector inj) {
        injector = inj;
    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {
        APP_SERVICE_CLASS_NAME = "JEOpcUaWriter";
        initializeThreadPool(APP_SERVICE_CLASS_NAME);
    }

    @Override
    protected void runSingle(List<Long> ids) {
        System.out.println("Start Single Mode");

        for (Long id : ids) {
            JEVisObject loytecDataServer = null;

            try {
                logger.info("Try adding Single Mode for ID {}", id);
                loytecDataServer = ds.getObject(id);
                getOutputChannel(loytecDataServer);
                connectToOpc(loytecDataServer);
                sendOpcUaNotification();
                System.out.println(loytecDataServer.getJEVisClass().getName());
            } catch (UaException | ExecutionException | InterruptedException e) {
                e.printStackTrace();
                for (JEVisObject outputChannel:outputChannels) {
                    OpcUAStatus opcUAStatus = new OpcUAStatus(OpcUAStatus.OPC_SERVER_NOT_REACHABLE);
                    opcUAStatus.writeStatus(outputChannel,DateTime.now());
                }
            }  catch (JEVisException jeVisException) {
                jeVisException.printStackTrace();
            }

        }

        logger.info("Start Single Mode");


    }

    @Override
    protected void runServiceHelp() {

    }

    @Override
    protected void runComplete() {

    }

    public void connectToOpc(JEVisObject opcServerObj) throws ExecutionException, InterruptedException, UaException {

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

    private void getOutputChannel(JEVisObject object) {

        logger.info("get all output Channels");

        try {
            if (object.getJEVisClassName().equals("Loytec XML-DL Output Channel")) {
                outputChannels.add(object);
            }
            for (JEVisObject child :object.getChildren()) {
                getOutputChannel(child);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public void sendOpcUaNotification() {


            for (JEVisObject outputChannel: outputChannels) {
                try {


                    JEVisObject dataObject = ds.getObject(Long.valueOf(outputChannel.getAttribute(TARGET_ID).getLatestSample().getValue().toString().split(":")[0]));

                    if (!outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).hasSample() || dataObject.getAttribute(VALUE).getTimestampFromLastSample().getMillis()> outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).getTimestampFromLastSample().getMillis()) {

                        System.out.println("write to op");

                        NodeId nodeId = NodeId.parse(outputChannel.getAttribute(OPC_ID).getLatestSample().getValue().toString());
                        String datatype = opcClient.getDataType(nodeId);
                        logger.info("OPC-UA Node datatype:", datatype);

                        if (datatype.equals(Double.class.getName())) {
                            writeOPC(dataObject.getAttribute(VALUE).getLatestSample().getValueAsDouble(), nodeId);

                        } else if (datatype.equals(Boolean.class.getName())) {
                            writeOPC(dataObject.getAttribute(VALUE).getLatestSample().getValueAsBoolean(), nodeId);

                        } else if (datatype.equals(String.class.getName())) {
                            writeOPC(dataObject.getAttribute(VALUE).getLatestSample().getValueAsString(), nodeId);

                        } else {
                            throw new Exception("Datatype not found");

                        }

                        OpcUAStatus opcUAStatus = new OpcUAStatus(OpcUAStatus.SUCCESS);
                        opcUAStatus.writeStatus(outputChannel,DateTime.now());

                        setLastReadout(outputChannel,dataObject.getAttribute(VALUE).getTimestampFromLastSample());



                    }






            }
         catch (Exception e){
                e.printStackTrace();
             OpcUAStatus opcUAStatus = new OpcUAStatus(OpcUAStatus.NOT_WRITTEN);
             opcUAStatus.writeStatus(outputChannel, DateTime.now());
            }
        }

    }


    public void writeOPC(Double value, NodeId nodeId) throws UaException {
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        opcClient.writeValue(dataValue,nodeId);
    }

    public void writeOPC(Integer value, NodeId nodeId) throws UaException {
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        opcClient.writeValue(dataValue,nodeId);
    }

    public void writeOPC(String value, NodeId nodeId) throws UaException {
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        opcClient.writeValue(dataValue,nodeId);
    }

    public void writeOPC(Boolean value, NodeId nodeId) throws UaException {
        DataValue dataValue = new DataValue(new Variant(value), null, null, null);
        opcClient.writeValue(dataValue,nodeId);
    }


    public void setLastReadout(JEVisObject outputChannel, DateTime dateTime) throws JEVisException {
        outputChannel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(DateTime.now(), dateTime.toString()).commit();
    }








}