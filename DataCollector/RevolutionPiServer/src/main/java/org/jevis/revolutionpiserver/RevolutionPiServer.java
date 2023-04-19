package org.jevis.revolutionpiserver;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeapi.ws.HTTPConnection;
import org.jevis.jeapi.ws.REQUEST;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RevolutionPiServer implements DataSource {
    private static final Logger logger = LogManager.getLogger(RevolutionPiServer.class);

    private final List<JEVisObject> channels = new ArrayList<>();
    private final HTTPConnection.Trust sslTrustMode = HTTPConnection.Trust.SYSTEM;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String serverURL;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String userName;
    private String password;
    private Boolean ssl = false;
    private DateTimeZone timezone;
    private Importer importer;
    private HTTPConnection con;
    public static final DateTimeFormatter FMT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").withZoneUTC();
    public static final DateTimeFormatter FMT2 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
    public static final Integer OK = 0;

    public static String API_STRING = "api/data";


    @Override
    public void run() {
        for (JEVisObject channel : channels) {
            try {
                JEVisClass channelClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Channel.RevolutionPiChannel.NAME);
                JEVisType lastReadoutType = channelClass.getType(DataCollectorTypes.Channel.RevolutionPiChannel.LAST_READOUT);
                JEVisType sourceIdType = channelClass.getType(DataCollectorTypes.Channel.RevolutionPiChannel.SOURCEID);
                JEVisType targetIdType = channelClass.getType(DataCollectorTypes.Channel.RevolutionPiChannel.TARGETID);
                JEVisType statusType = channelClass.getType(DataCollectorTypes.Channel.RevolutionPiChannel.STATUS);

                JEVisAttribute lastReadoutAttribute = channel.getAttribute(lastReadoutType);
                JEVisAttribute statusAttribute = channel.getAttribute(statusType);
                DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, lastReadoutType);
                String sourceId = DatabaseHelper.getObjectAsString(channel, sourceIdType);
                String targetString = DatabaseHelper.getObjectAsString(channel, targetIdType);
                TargetHelper targetHelper = new TargetHelper(channel.getDataSource(), targetString);
                JEVisAttribute targetAttribute = targetHelper.getAttribute().get(0);

                List<JEVisSample> samples = new ArrayList<>();
                List<JEVisSample> stati = new ArrayList<>();

                String resource = API_STRING;
                if (lastReadout == null) {
                    lastReadout = new DateTime(1990, 1, 1, 0, 0, 0, 0);
                }
                resource += "?" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM + FMT.print(lastReadout);

                resource += "&";
                resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL + FMT.print(DateTime.now());

                String prefix = "&";
                if (!resource.contains("?")) {
                    prefix = "?";
                }
                resource += prefix + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.LIMIT + "=" + 10000;


                prefix = "&";
                if (!resource.contains("?")) {
                    prefix = "?";
                }
                resource += prefix + "id" + "=" + sourceId;
                try(InputStream inputStream = this.con.getInputStreamRequest(resource)) {
                    if (inputStream != null) {
                        String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        RevPiResult[] revPiResults = objectMapper.readValue(result, RevPiResult[].class);
                        for (RevPiResult sample : revPiResults) {
                            try {
                                DateTime dateTime = DateTime.parse(sample.getDateTime(), FMT2);
                                JEVisSample statusSample = statusAttribute.buildSample(dateTime, sample.getStatus());
                                stati.add(statusSample);
                                if (sample.getStatus() == OK) {
                                    JEVisSample jeVisSample = targetAttribute.buildSample((dateTime), sample.getValue());
                                    logger.debug("Add Sample {}", jeVisSample);
                                    samples.add(jeVisSample);
                                }
                                if (lastReadout.isBefore(dateTime)) {
                                    lastReadout = dateTime;
                                }
                            } catch (Exception ex) {
                                logger.error("Error parsing sample {} of sourceAttribute {}:{}", sample.toString(), targetAttribute.getObject().getID(), targetAttribute.getName());
                            }
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    logger.error("Illegal argument exception. Error in getting samples.", ex);
                } catch (JsonParseException ex) {
                    logger.error("Json parse exception. Error in getting samples.", ex);
                } catch (JsonMappingException ex) {
                    logger.error("Json mapping exception. Error in getting samples.", ex);
                } catch (IOException ex) {
                    logger.error("IO exception. Error in getting samples.", ex);
                } catch (InterruptedException e) {
                    logger.error("Interrupted exception. Error in getting samples.", e);
                }
                targetAttribute.addSamples(samples);
                statusAttribute.addSamples(stati);
                JEVisSample sample = lastReadoutAttribute.buildSample(new DateTime(), lastReadout);
                sample.commit();
            } catch (Exception e) {
                logger.error("Could not complete channel {}:{}", channel.getName(), channel.getID(), e);
            }
        }

    }

    @Override
    public void initialize(JEVisObject dataSourceJEVis) {
        initializeAttributes(dataSourceJEVis);
        initializeChannelObjects(dataSourceJEVis);

        importer = ImporterFactory.getImporter(dataSourceJEVis);
        importer.initialize(dataSourceJEVis);
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        return null;
    }

    @Override
    public void parse(List<InputStream> input) {

    }

    @Override
    public void importResult() {

    }

    private void initializeAttributes(JEVisObject revolutionPiServer) {
        try {
            JEVisClass serverClass = revolutionPiServer.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.NAME);
            JEVisType sslType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.SSL);
            JEVisType serverType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.HOST);
            JEVisType portType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.PORT);
            JEVisType connectionTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.READ_TIMEOUT);
            JEVisType userType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.USER);
            JEVisType passwordType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.PASSWORD);
            JEVisType timezoneType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.TIMEZONE);

            ssl = DatabaseHelper.getObjectAsBoolean(revolutionPiServer, sslType);
            serverURL = DatabaseHelper.getObjectAsString(revolutionPiServer, serverType);
            port = DatabaseHelper.getObjectAsInteger(revolutionPiServer, portType);
            if (port == null) {
                port = 8000;
            }
            connectionTimeout = DatabaseHelper.getObjectAsInteger(revolutionPiServer, connectionTimeoutType);
            readTimeout = DatabaseHelper.getObjectAsInteger(revolutionPiServer, readTimeoutType);

            JEVisAttribute userAttr = revolutionPiServer.getAttribute(userType);
            if (userAttr == null || !userAttr.hasSample()) {
                userName = "";
            } else {
                userName = DatabaseHelper.getObjectAsString(revolutionPiServer, userType);
            }

            JEVisAttribute passAttr = revolutionPiServer.getAttribute(passwordType);
            if (passAttr == null || !passAttr.hasSample()) {
                password = "";
            } else {
                password = DatabaseHelper.getObjectAsString(revolutionPiServer, passwordType);
            }
            String timezoneString = DatabaseHelper.getObjectAsString(revolutionPiServer, timezoneType);
            if (timezoneString != null) {
                timezone = DateTimeZone.forID(timezoneString);
            } else {
                timezone = DateTimeZone.UTC;
            }

            String host = serverURL + ":" + port;

            this.con = new HTTPConnection(host, userName, password, sslTrustMode);

        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private void initializeChannelObjects(JEVisObject jevisServer) {
        try {
            JEVisClass channelDirClass = jevisServer.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.RevolutionPiChannelDirectory.NAME);
            System.out.println(channelDirClass);
            System.out.println(jevisServer.getChildren(channelDirClass, false));


            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = getChannels(jevisServer);
            logger.info("Found " + channels.size());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    this.channels.add(channelObject);
                    try {
                        jevisServer.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public List<JEVisObject> getChannels(JEVisObject jeVisObject) {
        List<JEVisObject> channels = new ArrayList<>();
        try {
            JEVisClass channelDirClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.RevolutionPiChannelDirectory.NAME);
            JEVisClass channelClass = jeVisObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.RevolutionPiChannel.NAME);
            jeVisObject.getChildren(channelDirClass, false).forEach(dir -> {
                channels.addAll(getChannels(dir));
            });
            List<JEVisObject> channelsToBeAdded = jeVisObject.getChildren(channelClass, false);
            logger.debug("Added Channels to List {}",channelsToBeAdded);
            channels.addAll(channelsToBeAdded);
        } catch (Exception e) {
            logger.error(e);
        }
        return channels;

    }

}
