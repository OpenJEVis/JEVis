package org.jevis.jeopc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryReadResult;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Importer;
import org.jevis.commons.driver.ImporterFactory;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.commons.driver.DataCollectorTypes.DataSource.TIMEZONE;

public class OPCUAServer {

    private final static Logger logger = LogManager.getLogger(OPCUAServer.class.getName());

    String USER = "User";
    String PASSWORD = "Password";
    String SSL = "SSL";
    String USER_DEFAULT = "operator";
    String DEFAULT_PASSWORD = "loytec4u";
    String DEFAULT_SSL = "false";
    String DEFAULT_PORT = "80";
    String DEFAULT_CONNECTION_TIMEOUT = "30";
    String DEFAULT_READ_TIMEOUT = "60";

    private String host;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private Boolean ssl;
    private String name;
    private String user;
    private String password;
    private String logHandleBasePath;
    private DateTimeZone timezone;
    private List<UPCChannel> channelDirectories = new ArrayList<>();
    private JEVisObject dataSourceObject;

    public OPCUAServer(JEVisObject dataSourceObject) {
        logger.error("Init: {}",dataSourceObject);
        this.dataSourceObject=dataSourceObject;

        Helper helper = new Helper();
        host = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.HOST);
        logger.debug("OPCUAServer - Host: " + host);
        port = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.PORT, DEFAULT_PORT));
        logger.debug("OPCUAServer - Port: " + port);
        connectionTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        logger.debug("OPCUAServer - Connection Timeout: " + connectionTimeout);
        readTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        logger.debug("OPCUAServer - Read Timeout: " + readTimeout);
        user = helper.getValue(dataSourceObject, USER, USER_DEFAULT);
        logger.debug("OPCUAServer - User: " + user);
        password = helper.getValue(dataSourceObject, PASSWORD, DEFAULT_PASSWORD);
        logger.debug("OPCUAServer - Password: "+password );
        ssl = Boolean.valueOf(helper.getValue(dataSourceObject, SSL, DEFAULT_SSL));
        logger.debug("OPCUAServer - Ssl: " + ssl);



        JEVisClass opcServerJClass = null;
        try {
            opcServerJClass = dataSourceObject.getJEVisClass();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        JEVisType timezoneType = null;
        try {
            if (opcServerJClass != null) {
                timezoneType = opcServerJClass.getType(TIMEZONE);
            }
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        String timezoneString = null;
        try {
            timezoneString = DatabaseHelper.getObjectAsString(dataSourceObject, timezoneType);
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        if (timezoneString != null) {
            timezone = DateTimeZone.forID(timezoneString);
        } else {
            timezone = DateTimeZone.UTC;
        }
        logger.debug("OPC UA Server - Timezone: " + timezone);

        // Get all data channel directories
        try {
            logger.debug("Getting the channel directories (type: "  + ")");
            // Get channel directory class
            JEVisClass dirJClass = dataSourceObject.getDataSource().getJEVisClass(UPCChannel.DIR_JEVIS_CLASS);
            JEVisClass channelJClass = dataSourceObject.getDataSource().getJEVisClass(UPCChannel.JEVIS_CLASS);

            getChannelRecursive(dataSourceObject, channelJClass,dirJClass , channelDirectories);
            logger.info("Found " + channelDirectories.size() + " channel directories.");

        } catch (Exception ex) {
            logger.error("Error while getting data channel directories");
            logger.error(ex.getMessage());
        }
    }

    public void run(){
        try {
            logger.error("Start Readout");
            //opc.tcp://10.1.2.128:4840
            /**
             * todo: support different protocol
             * TODO: select endpoint
             * TODO: add loop for sample request
             * TODO: load chucks
             * TODO: setTimeouts
             * TODO: disconnet
             * **/


            logger.error("Endpoint: {}","opc.tcp://"+host+":"+port);
            OPCClient opcClient = new OPCClient("opc.tcp://"+host+":"+port);

            EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
            opcClient.setEndpoints(endpointDescription);
            logger.error("Call connect");
            opcClient.connect();
            logger.error("Connection done start readout");

            DateTime endOfToday = new DateTime().withHourOfDay(23).withMinuteOfHour(59);

            Importer importer = ImporterFactory.getImporter(dataSourceObject);



            for (UPCChannel upcChannel : channelDirectories) {
                logger.error("Start readout for channel: {}", upcChannel);

                HistoryReadResult historyReadResult = opcClient.getHistory(upcChannel.getOPCNodeId(), endOfToday.withHourOfDay(1), endOfToday.withHourOfDay(4));
                // HistoryReadResult historyReadResult = opcClient.getHistory(upcChannel.getOPCNodeId(), upcChannel.getLastReadout(), endOfToday);
                List<DataValue> valueList = opcClient.getDateValues(historyReadResult);
                List<Result> results = new ArrayList<>();
                List<JEVisSample> statusResults = new ArrayList<>();

                logger.error("Samples received: {}",valueList.size());
                valueList.forEach(dataValue -> {
                    try {

                        Class<?> javaType = String.class;
                        try{
                            javaType = BuiltinDataType.getBackingClass(dataValue.getValue().getDataType().get());
                        }catch (Exception ex){
                            logger.error("cannot cast type: {}",ex,ex);
                        }

                        Object value = null;
                        DateTime ts = new DateTime(dataValue.getSourceTime().getJavaDate()).withZone(timezone);
                        if(javaType.equals(org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger.class)){
                            value= ((org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger) dataValue.getValue().getValue()).doubleValue();
                        }else if(javaType.equals(java.lang.Float.class)){
                            Float aFloat = (java.lang.Float)dataValue.getValue().getValue();
                            value=  Double.valueOf(aFloat.toString());
                        } else{
                            try{
                                value = Double.parseDouble(dataValue.getValue().getValue().toString());
                            }catch (Exception ex){
                                value= dataValue.getValue().getValue().toString();
                            }

                        }
                        logger.error(" TS: {} -> v: {} ",ts,value);
                        //logger.error("dataValue.getValue(): {} -> {}",dataValue.getValue(),dataValue.getValue().getDataType());

                        if(dataValue.getStatusCode().isGood()){
                            results.add(new Result(upcChannel.getJeVisObject().getID(),upcChannel.getTargetAttribute(),value,ts));
                        }else{
                            logger.error("Error status for value: {}",dataValue);
                        }
                    } catch (Exception ex) {
                        logger.error("DataValue error; {}", ex);
                    }
                });
               // JEVisImporterAdapter.importResults(results, statusResults, importer, upcChannel.getJeVisObject());


            }
        }catch (Exception ex){
            logger.error(ex);
        }
    }

    private void getChannelRecursive(JEVisObject parent, JEVisClass channelClass, JEVisClass dirClass, List<UPCChannel> children) throws JEVisException {
        parent.getChildren().forEach(jeVisObject -> {
            try {

                if (jeVisObject.getJEVisClassName().equals(channelClass.getName())) {
                    try {
                        children.add(new UPCChannel(jeVisObject));
                    }catch (Exception ex){
                        logger.error("Error in channel: {}",jeVisObject);
                    }
                }

                if (jeVisObject.getJEVisClassName().equals(dirClass.getName())) {
                    getChannelRecursive(jeVisObject, channelClass,dirClass, children);
                }

            }catch (Exception ex){
                logger.error("Error in child: {}",jeVisObject);
            }


        });

    }
}
