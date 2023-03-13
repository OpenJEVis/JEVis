package org.jevis.jevisserver;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.jeapi.ws.HTTPConnection;
import org.jevis.jeapi.ws.REQUEST;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JEVisServer implements DataSource {
    private static final Logger logger = LogManager.getLogger(JEVisServer.class);

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
    private List<Result> result;
    private HTTPConnection con;

    @Override
    public void run() {

        for (JEVisObject channel : channels) {
            try {
                JEVisClass channelClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Channel.JEVisChannel.NAME);
                JEVisType lastReadoutType = channelClass.getType(DataCollectorTypes.Channel.JEVisChannel.LAST_READOUT);
                JEVisType sourceIdType = channelClass.getType(DataCollectorTypes.Channel.JEVisChannel.SOURCEID);
                JEVisType sourceAttributeType = channelClass.getType(DataCollectorTypes.Channel.JEVisChannel.SOURCEATTRIBUTE);
                JEVisType targetIdType = channelClass.getType(DataCollectorTypes.Channel.JEVisChannel.TARGETID);

                JEVisAttribute lastReadoutAttribute = channel.getAttribute(lastReadoutType);
                DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, lastReadoutType);
                Long sourceId = DatabaseHelper.getObjectAsLong(channel, sourceIdType);
                String sourceAttribute = DatabaseHelper.getObjectAsString(channel, sourceAttributeType);
                String targetString = DatabaseHelper.getObjectAsString(channel, targetIdType);
                TargetHelper targetHelper = new TargetHelper(channel.getDataSource(), targetString);
                JEVisAttribute targetAttribute = targetHelper.getAttribute().get(0);

                List<JEVisSample> samples = new ArrayList<>();

                String resource = REQUEST.API_PATH_V1
                        + REQUEST.OBJECTS.PATH
                        + sourceId + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.PATH
                        + sourceAttribute + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

                if (lastReadout == null) {
                    lastReadout = new DateTime(1990, 1, 1, 0, 0, 0, 0);
                }
                resource += "?" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM + HTTPConnection.FMT.print(lastReadout);

                resource += "&";
                resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL + HTTPConnection.FMT.print(DateTime.now());

                String prefix = "&";
                if (!resource.contains("?")) {
                    prefix = "?";
                }
                resource += prefix + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.LIMIT + "=" + 10000;

                List<JsonSample> jsons = new ArrayList<>();
                try {
                    InputStream inputStream = this.con.getInputStreamRequest(resource);
                    if (inputStream != null) {
                        jsons = new ArrayList<>(Arrays.asList(this.objectMapper.readValue(inputStream, JsonSample[].class)));
                        inputStream.close();
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

                for (JsonSample sample : jsons) {
                    try {
                        DateTime dateTime = new DateTime(sample.getTs());
                        samples.add(targetAttribute.buildSample(dateTime, sample.getValue(), sample.getNote()));
                        if (lastReadout.isBefore(dateTime)) {
                            lastReadout = dateTime;
                        }
                    } catch (Exception ex) {
                        logger.error("Error parsing sample {} of sourceAttribute {}:{}", sample.toString(), targetAttribute.getObject().getID(), targetAttribute.getName());
                    }
                }

                targetAttribute.addSamples(samples);
                JEVisSample sample = lastReadoutAttribute.buildSample(new DateTime(), lastReadout);
                sample.commit();
            } catch (Exception e) {
                logger.error("Could not complete channel {}:{}", channel.getName(), channel.getID(), e);
            }
        }
    }

    @Override
    public void initialize(JEVisObject jevisServer) {
        initializeAttributes(jevisServer);
        initializeChannelObjects(jevisServer);

        importer = ImporterFactory.getImporter(jevisServer);
        importer.initialize(jevisServer);
    }

    private void initializeChannelObjects(JEVisObject jevisServer) {
        try {
            JEVisClass channelDirClass = jevisServer.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.JEVisChannelDirectory.NAME);
            JEVisObject channelDir = jevisServer.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = jevisServer.getDataSource().getJEVisClass(DataCollectorTypes.Channel.JEVisChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = channelDir.getChildren(channelClass, false);
            logger.info("Found " + channels.size() + " channel objects in " + channelDir.getName() + ":" + channelDir.getID());

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

            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + this.channels.size() + " channels.");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void initializeAttributes(JEVisObject jevisServer) {
        try {
            JEVisClass serverClass = jevisServer.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.JEVisServer.NAME);
            JEVisType sslType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.SSL);
            JEVisType serverType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.HOST);
            JEVisType portType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.PORT);
            JEVisType connectionTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.READ_TIMEOUT);
            JEVisType userType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.USER);
            JEVisType passwordType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.PASSWORD);
            JEVisType timezoneType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.JEVisServer.TIMEZONE);

            ssl = DatabaseHelper.getObjectAsBoolean(jevisServer, sslType);
            serverURL = DatabaseHelper.getObjectAsString(jevisServer, serverType);
            port = DatabaseHelper.getObjectAsInteger(jevisServer, portType);
            if (port == null) {
                port = 8000;
            }
            connectionTimeout = DatabaseHelper.getObjectAsInteger(jevisServer, connectionTimeoutType);
            readTimeout = DatabaseHelper.getObjectAsInteger(jevisServer, readTimeoutType);

            JEVisAttribute userAttr = jevisServer.getAttribute(userType);
            if (userAttr == null || !userAttr.hasSample()) {
                userName = "";
            } else {
                userName = DatabaseHelper.getObjectAsString(jevisServer, userType);
            }

            JEVisAttribute passAttr = jevisServer.getAttribute(passwordType);
            if (passAttr == null || !passAttr.hasSample()) {
                password = "";
            } else {
                password = DatabaseHelper.getObjectAsString(jevisServer, passwordType);
            }
            String timezoneString = DatabaseHelper.getObjectAsString(jevisServer, timezoneType);
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
}
