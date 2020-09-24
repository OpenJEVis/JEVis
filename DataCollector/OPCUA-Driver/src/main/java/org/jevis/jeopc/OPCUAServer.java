package org.jevis.jeopc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.HistoryReadResult;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.driver.*;
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
    String PROTOCOL = "Protocol";

    String DEFAULT_PROTOCOL = "opc.tcp";
    String USER_DEFAULT = "operator";
    String DEFAULT_PASSWORD = "loytec4u";
    String DEFAULT_SSL = "false";
    String DEFAULT_PORT = "80";
    String DEFAULT_CONNECTION_TIMEOUT = "30";
    String DEFAULT_READ_TIMEOUT = "60";
    int DAYS_PER_Request = 30;

    private final String host;
    private final Integer port;
    private final String protocol;
    private final Integer connectionTimeout;
    private final Integer readTimeout;
    private final Boolean ssl;
    private final String user;
    private final String password;
    private final DateTimeZone timezone;
    private final List<UPCChannel> channelDirectories = new ArrayList<>();
    private final JEVisObject dataSourceObject;

    public OPCUAServer(JEVisObject dataSourceObject) {
        logger.error("Init: {}", dataSourceObject);
        this.dataSourceObject = dataSourceObject;

        Helper helper = new Helper();
        host = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.HOST);
        logger.debug("OPCUAServer - Host: {}", host);
        port = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.PORT, DEFAULT_PORT));
        logger.debug("OPCUAServer - Port: {}", port);
        connectionTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        logger.debug("OPCUAServer - Connection Timeout: {}", connectionTimeout);
        readTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        logger.debug("OPCUAServer - Read Timeout: {}", readTimeout);
        user = helper.getValue(dataSourceObject, USER, USER_DEFAULT);
        logger.debug("OPCUAServer - User: {}", user);
        password = helper.getValue(dataSourceObject, PASSWORD, DEFAULT_PASSWORD);
        logger.debug("OPCUAServer - Password: {}", password);
        ssl = Boolean.valueOf(helper.getValue(dataSourceObject, SSL, DEFAULT_SSL));
        logger.debug("OPCUAServer - Ssl: {}", ssl);
        protocol = String.valueOf(helper.getValue(dataSourceObject, PROTOCOL, DEFAULT_PROTOCOL));
        logger.debug("OPCUAServer - protocol: {}", protocol);

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
        logger.debug("OPC UA Server - Timezone: {}", timezone);

        // Get all data channel directories
        try {
            // Get channel directory class
            JEVisClass dirJClass = dataSourceObject.getDataSource().getJEVisClass(UPCChannel.DIR_JEVIS_CLASS);
            JEVisClass channelJClass = dataSourceObject.getDataSource().getJEVisClass(UPCChannel.JEVIS_CLASS);

            getChannelRecursive(dataSourceObject, channelJClass, dirJClass, channelDirectories);
            logger.info("Found {} channel directories.", channelDirectories.size());

        } catch (Exception ex) {
            logger.error("Error while getting data channel directories");
            logger.error(ex.getMessage());
        }
    }

    public void run() {

        OPCClient opcClient = null;
        try {
            logger.error("Start Readout");
            //opc.tcp://10.1.2.128:4840
            /**
             * todo: support different protocol
             * TODO: select endpoint
             * TODO: setTimeouts
             * **/


            String deviceAddress = protocol + "://" + host + ":" + port;
            logger.error("Endpoint: {}", deviceAddress);
            opcClient = new OPCClient(deviceAddress);

            EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
            opcClient.setEndpoints(endpointDescription);
            logger.error("Call connect");
            opcClient.connect();
            logger.error("Connection done start readout");

            DateTime endOfToday = new DateTime().withHourOfDay(23).withMinuteOfHour(59);
            Importer importer = ImporterFactory.getImporter(dataSourceObject);


            for (UPCChannel upcChannel : channelDirectories) {
                try {
                    logger.error("Start readout for channel: {}", upcChannel);

                    boolean reachedEnd = false;
                    DateTime starttime = upcChannel.getLastReadout();

                    while (!reachedEnd) {
                        System.out.println("-- start --");
                        DateTime endtime = calculateEndDate(starttime);
                        List<JEVisSample> statusResults = new ArrayList<>();

                        List<Result> results = readChannel(opcClient, upcChannel, starttime, endtime, statusResults);

                        logger.error("read end: {} {}", results.isEmpty(), endtime.isAfter(new DateTime()));
                        if (results.isEmpty() && endtime.isAfter(new DateTime())) {
                            reachedEnd = true;
                        } else {
                            starttime = endtime;
                        }

                        logger.error("Import samples into: {} sample: {} status: {}", upcChannel.getJeVisObject(), results.size(), statusResults.size());
                        System.out.println("-- end --");
                        JEVisImporterAdapter.importResults(results, statusResults, importer, upcChannel.getJeVisObject());
                    }
                } catch (Exception ex) {
                    logger.error("Error while reading channel {}", upcChannel, ex);
                }

            }
        } catch (Exception ex) {
            logger.error(ex);
        } finally {
            try {
                if (opcClient != null) {
                    opcClient.close();
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    private DateTime calculateEndDate(DateTime date) {
        logger.error("calculateEndDate: date: {} d2: {} bool: {}", date, date.plusDays(DAYS_PER_Request), date.plusDays(DAYS_PER_Request).isBeforeNow());
        if (date.plusDays(DAYS_PER_Request).isBeforeNow()) {
            System.out.println("1");
            return date.plusDays(DAYS_PER_Request).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        } else {
            System.out.println("2");
            return DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        }
    }


    private List<Result> readChannel(OPCClient opcClient, UPCChannel upcChannel, DateTime startDate, DateTime endTime, List<JEVisSample> statusResults) throws Exception {
        logger.error("Start readout for channel: {} from: {} until: {}", upcChannel.getJeVisObject(), startDate, endTime);


        HistoryReadResult historyReadResult = opcClient.getHistory(upcChannel.getOPCNodeId(), startDate, endTime);
        List<DataValue> valueList = opcClient.getDateValues(historyReadResult);
        List<Result> results = new ArrayList<>();

        logger.error("Samples received: {}", valueList.size());
        valueList.forEach(dataValue -> {
            try {

                DateTime ts = new DateTime(dataValue.getSourceTime().getJavaDate()).withZone(timezone);

                if (dataValue.getStatusCode().isBad()) {
                    statusResults.add(new VirtualSample(ts, dataValue.getStatusCode().getValue()));
                } else {
                    Class<?> javaType = String.class;
                    try {
                        javaType = BuiltinDataType.getBackingClass(dataValue.getValue().getDataType().get());
                    } catch (Exception ex) {
                        logger.error("cannot cast type: {}", ex, ex);
                    }

                    Object value = null;

                    if (javaType.equals(org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger.class)) {
                        value = ((org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger) dataValue.getValue().getValue()).doubleValue();
                    } else if (javaType.equals(java.lang.Float.class)) {
                        Float aFloat = (java.lang.Float) dataValue.getValue().getValue();
                        value = Double.valueOf(aFloat.toString());
                    } else {
                        try {
                            value = Double.parseDouble(dataValue.getValue().getValue().toString());
                        } catch (Exception ex) {
                            value = dataValue.getValue().getValue().toString();
                        }

                    }
                    logger.error(" TS: {} -> v: {} ", ts, value);

                    if (dataValue.getStatusCode().isGood()) {
                        results.add(new Result(upcChannel.getTargetString(), value, ts));
                    } else {
                        statusResults.add(new VirtualSample(ts, -1l));
                        logger.error("Error status for value: {}", dataValue);
                    }
                }


            } catch (Exception ex) {
                logger.error("DataValue error; {}", ex);
            }
        });

        return results;

    }


    private void getChannelRecursive(JEVisObject parent, JEVisClass channelClass, JEVisClass dirClass, List<UPCChannel> children) throws JEVisException {
        parent.getChildren().forEach(jeVisObject -> {
            try {

                if (jeVisObject.getJEVisClassName().equals(channelClass.getName())) {
                    try {
                        children.add(new UPCChannel(jeVisObject));
                    } catch (Exception ex) {
                        logger.error("Error in channel: {}", jeVisObject);
                    }
                }

                if (jeVisObject.getJEVisClassName().equals(dirClass.getName())) {
                    getChannelRecursive(jeVisObject, channelClass, dirClass, children);
                }

            } catch (Exception ex) {
                logger.error("Error in child: {}", jeVisObject);
            }


        });

    }
}
