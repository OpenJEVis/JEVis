package org.jevis.jeopc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
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

/**
 * OPC UA Server Driver for the JEDataCollector
 *
 * @author florian.simon@envidatec.com
 */
public class OPCUAServer {

    private final static Logger logger = LogManager.getLogger(OPCUAServer.class.getName());


    /**
     * The default value is {@value}.
     */
    public final String DEFAULT_PROTOCOL = "opc.tcp";
    /**
     * The default value is {@value}.
     */
    public final String USER_DEFAULT = "";
    /**
     * The default value is {@value}.
     */
    public final String DEFAULT_PASSWORD = "";
    /**
     * The default value is {@value}.
     */
    public final String DEFAULT_PORT = "4840";
    /**
     * The default value is {@value}.
     */
    public final String DEFAULT_CONNECTION_TIMEOUT = "30";
    /**
     * The default value is {@value}.
     */
    public final String DEFAULT_READ_TIMEOUT = "60";

    private int DAYS_PER_Request = 30;
    private final String host;
    private final Integer port;
    private final String protocol;
    private final Integer connectionTimeout;
    private final Integer readTimeout;
    private final String user;
    private final String password;
    private final DateTimeZone timezone;
    private final List<OPCUAChannel> channelDirectories = new ArrayList<>();
    private final JEVisObject dataSourceObject;

    /**
     * Default constructor
     * <p>
     * TODO: support different protocol
     * TODO: select endpoint
     * TODO: setTimeouts
     *
     * @param dataSourceObject
     */
    public OPCUAServer(JEVisObject dataSourceObject) {
        logger.info("Init: {}", dataSourceObject);
        this.dataSourceObject = dataSourceObject;

        Helper helper = new Helper();
        host = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.HOST);
        port = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.PORT, DEFAULT_PORT));
        connectionTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT));
        readTimeout = Integer.parseInt(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.READ_TIMEOUT, DEFAULT_READ_TIMEOUT));
        user = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.OPCUA.USER, USER_DEFAULT);
        logger.error("user attribute: '{}' User: {}", DataCollectorTypes.DataSource.DataServer.OPCUA.USER, user);
        password = helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.OPCUA.PASSWORD, DEFAULT_PASSWORD);
        protocol = String.valueOf(helper.getValue(dataSourceObject, DataCollectorTypes.DataSource.DataServer.OPCUA.PROTOCOL, DEFAULT_PROTOCOL));


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
            JEVisClass dirJClass = dataSourceObject.getDataSource().getJEVisClass(OPCUAChannel.DIR_JEVIS_CLASS);
            JEVisClass channelJClass = dataSourceObject.getDataSource().getJEVisClass(OPCUAChannel.JEVIS_CLASS);

            getChannelRecursive(dataSourceObject, channelJClass, dirJClass, channelDirectories);
            logger.info("Found {} channel directories.", channelDirectories.size());

        } catch (Exception ex) {
            logger.error("Error while getting data channel directories");
            logger.error(ex.getMessage());
        }
    }

    /**
     * Return the configured URL
     *
     * @return
     */
    public String getURL() {
        return protocol + "://" + host + ":" + port;
    }

    /**
     * Start the readout process
     */
    public void run() {

        OPCClient opcClient = null;
        try {
            logger.info("Start Readout");
            /** example: opc.tcp://10.1.2.128:4840 **/
            String deviceAddress = getURL();
            logger.info("Endpoint: {}", deviceAddress);
            opcClient = new OPCClient(deviceAddress);

            EndpointDescription endpointDescription = opcClient.autoSelectEndpoint();
            opcClient.setEndpoints(endpointDescription);
            if (!user.isEmpty()) {
                logger.error("Using user: {} pw:{}", user, password);
                opcClient.setIdentification(new UsernameProvider(user, password));
            }


            logger.info("Call connect");
            opcClient.connect();
            logger.info("Connection done start readout");

            Importer importer = new JEVisImporter();
            importer.initialize(dataSourceObject);


            for (OPCUAChannel OPCUAChannel : channelDirectories) {
                try {
                    logger.info("Start readout for channel: {}", OPCUAChannel);

                    boolean reachedEnd = false;
                    DateTime starttime = OPCUAChannel.getLastReadout();

                    while (!reachedEnd) {
                        DateTime endTime = calculateEndDate(starttime);
                        List<JEVisSample> statusResults = new ArrayList<>();

                        List<Result> results = readChannel(opcClient, OPCUAChannel, starttime, endTime, statusResults);

                        logger.debug("read end: {} {}", results.isEmpty(), endTime.isAfter(new DateTime()));
                        if (results.isEmpty() && endTime.isAfter(new DateTime())) {
                            reachedEnd = true;
                        } else {
                            starttime = endTime;
                        }

                        if (!results.isEmpty() || !statusResults.isEmpty()) {
                            logger.info("Import samples into: {} sample: {} status: {}", OPCUAChannel.getJeVisObject(), results.size(), statusResults.size());

                            JEVisImporterAdapter.importResults(results, statusResults, importer, OPCUAChannel.getJeVisObject());
                        } else {
                            logger.info("No Data to import for {}", OPCUAChannel.getJeVisObject());
                        }


                    }
                } catch (Exception ex) {
                    logger.error("Error while reading channel {}", OPCUAChannel, ex);
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

    /**
     * Calculates the enddate for a data call based on the last date plus the DAYS_PER_Request setting.
     *
     * @param date last date to start from
     * @return
     */
    private DateTime calculateEndDate(DateTime date) {
        logger.debug("calculateEndDate: date: {} d2: {} bool: {}", date, date.plusDays(DAYS_PER_Request), date.plusDays(DAYS_PER_Request).isBeforeNow());
        if (date.plusDays(DAYS_PER_Request).isBeforeNow()) {
            return date.plusDays(DAYS_PER_Request).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        } else {
            return DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
        }
    }


    /**
     * Read the data from the given channel.
     *
     * @param opcClient
     * @param OPCUAChannel
     * @param startDate
     * @param endTime
     * @param statusResults
     * @return List of Result samples
     * @throws Exception
     */
    private List<Result> readChannel(OPCClient opcClient, OPCUAChannel OPCUAChannel, DateTime startDate, DateTime endTime, List<JEVisSample> statusResults) throws Exception {
        logger.error("Start readout for channel: {} from: {} until: {}", OPCUAChannel.getJeVisObject(), startDate, endTime);


        HistoryReadResult historyReadResult = opcClient.getHistory(OPCUAChannel, startDate, endTime);
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
                    logger.debug(" Target: {} TS: {} -> v: {}  class: {} raw: {} ", OPCUAChannel.getTargetId(), ts, value, javaType, dataValue.getValue());

                    if (dataValue.getStatusCode().isGood()) {
                        results.add(new Result(OPCUAChannel.getTargetString(), value, ts));
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


    /**
     * Fetch all channel setting in for the given OPC Server in the jevistree
     *
     * @param parent
     * @param channelClass
     * @param dirClass
     * @param children
     * @throws JEVisException
     */
    private void getChannelRecursive(JEVisObject parent, JEVisClass channelClass, JEVisClass dirClass, List<OPCUAChannel> children) throws JEVisException {
        parent.getChildren().forEach(jeVisObject -> {
            try {

                if (jeVisObject.getJEVisClassName().equals(channelClass.getName())) {
                    try {
                        children.add(new OPCUAChannel(jeVisObject));
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
