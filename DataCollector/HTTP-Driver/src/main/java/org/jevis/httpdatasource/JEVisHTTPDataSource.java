package org.jevis.httpdatasource;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class JEVisHTTPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(JEVisHTTPDataSource.class);
    private final List<JEVisObject> _channels = new ArrayList<>();
    private final List<Channel> httpChannels = new ArrayList<>();
    private DateTimeZone timeZone;
    private Importer _importer;
    private HTTPDataSource _httpdatasource;

    @Override
    public void run() {

        for (JEVisObject channel : _channels) {

            runChannel(channel);
        }
    }

    private void runChannel(JEVisObject channel) {
        try {
            JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
            JEVisObject parserObject = channel.getChildren(parserJevisClass, true).get(0);

            Parser parser = ParserFactory.getParser(parserObject);
            parser.initialize(parserObject);

            httpChannels.clear();
            List<Result> results = new ArrayList<>();

            try {
                List<InputStream> input = this.sendSampleRequest(channel);

                if (_httpdatasource.getEndDateTime().isAfterNow() || _httpdatasource.getLastReadout().isAfterNow()) {
                    logger.error("Start or End Date in Future Stop from trying to fetching Data from API");
                    return;
                }
                if (_httpdatasource.getLastReadout().plusMinutes(20).isAfter(_httpdatasource.getEndDateTime())) {
                    logger.error("Start Date is Less than 20 Minutes Before End Date Stop from trying to fetch Data from API");
                    return;
                }


                if (this._httpdatasource.getStatusLine().getStatusCode() < 200 && this._httpdatasource.getStatusLine().getStatusCode() >= 300) {
                    logger.error("API Returned Error Code :{}", this._httpdatasource.getStatusLine().getStatusCode());
                    return;
                }

                if (!input.isEmpty()) {
                    parser.parse(input, _httpdatasource.getDateTimeZone());
                    results.addAll(parser.getResult());
                }

                if (!results.isEmpty()) {
                    JEVisImporterAdapter.importResultsWithLastReadoutOffset(results, _importer, channel, 0);
                    for (InputStream inputStream : input) {
                        try {
                            inputStream.close();
                        } catch (Exception ex) {
                            logger.warn("could not close input stream: {}", ex.getMessage());
                        }
                    }
                } else {
                    Channel httpChannel = httpChannels.stream().filter(c -> c.getChannelObject().equals(channel)).findFirst().orElse(null);
                    if (httpChannel != null && httpChannel.getNextReadout() != null) {
                        JEVisImporterAdapter.setLastReadout(httpChannel.getChannelObject(), httpChannel.getNextReadout());
                    }
                }
            } catch (MalformedURLException ex) {
                logger.error("MalformedURLException. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("MalformedURLException. For channel {}:{}", channel.getID(), channel.getName(), ex);
            } catch (ClientProtocolException ex) {
                logger.error("Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
            } catch (IOException ex) {
                logger.error("IO Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("IO Exception. For channel {}:{}.", channel.getID(), channel.getName(), ex);
            } catch (ParseException ex) {
                logger.error("Parse Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("Parse Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
            } catch (Exception ex) {
                logger.error("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private void initializeAttributes(JEVisObject httpObject) {
        try {
            JEVisClass httpType = httpObject.getDataSource().getJEVisClass(HTTPTypes.NAME);
            JEVisType server = httpType.getType(HTTPTypes.HOST);
            JEVisType portType = httpType.getType(HTTPTypes.PORT);
            JEVisType sslType = httpType.getType(HTTPTypes.SSL);
            JEVisType connectionTimeoutType = httpType.getType(HTTPTypes.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = httpType.getType(HTTPTypes.READ_TIMEOUT);
            JEVisType userType = httpType.getType(HTTPTypes.USER);
            JEVisType passwordType = httpType.getType(HTTPTypes.PASSWORD);
            JEVisType timezoneType = httpType.getType(HTTPTypes.TIMEZONE);
            //JEVisType enableType = httpType.getType(HTTPTypes.ENABLE);
            JEVisType authType = httpType.getType(HTTPTypes.HTTP.AUTHENTICATION);

            String serverURL = DatabaseHelper.getObjectAsString(httpObject, server);
            Integer port = DatabaseHelper.getObjectAsInteger(httpObject, portType);
            Integer connectionTimeout = DatabaseHelper.getObjectAsInteger(httpObject, connectionTimeoutType);
            Integer readTimeout = DatabaseHelper.getObjectAsInteger(httpObject, readTimeoutType);
            Boolean ssl = DatabaseHelper.getObjectAsBoolean(httpObject, sslType);
            JEVisAttribute userAttr = httpObject.getAttribute(userType);
            //JEVisAttribute timeZoneAttr = httpObject.getAttribute(timezoneType);
            //JEVisAttribute authTypeAttr = httpObject.getAttribute(authType);


            String timezoneString = DatabaseHelper.getObjectAsString(httpObject, timezoneType);
            if (timezoneString != null) {
                timeZone = DateTimeZone.forID(timezoneString);
            } else {
                timeZone = DateTimeZone.UTC;
            }


            String userName = null;
            if (!userAttr.hasSample()) {
                userName = "";
            } else {
                userName = (String) userAttr.getLatestSample().getValue();
            }
            JEVisAttribute passAttr = httpObject.getAttribute(passwordType);
            String password = null;
            if (!passAttr.hasSample()) {
                password = "";
            } else {
                password = (String) passAttr.getLatestSample().getValue();
            }
//            _lastReadout = DatabaseHelper.getObjectAsDate(httpObject, lastReadout, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));

            _httpdatasource = new HTTPDataSource();
            _httpdatasource.setId(httpObject.getID());
            _httpdatasource.setName(httpObject.getName());
            _httpdatasource.setConnectionTimeout(connectionTimeout);
            _httpdatasource.setPassword(password);
            _httpdatasource.setPort(port);
            _httpdatasource.setReadTimeout(readTimeout);
            _httpdatasource.setServerURL(serverURL);
            _httpdatasource.setSsl(ssl);
            _httpdatasource.setUserName(userName);
            _httpdatasource.setDateTimeZone(timeZone);


            String authString = DatabaseHelper.getObjectAsString(httpObject, authType);
            if (authString != null && !authString.isEmpty()) {
                try {
                    HTTPDataSource.AUTH_SCHEME aut = HTTPDataSource.AUTH_SCHEME.valueOf(authString.toUpperCase());
                    _httpdatasource.setAuthScheme(aut);
                } catch (Exception ex) {
                    logger.error("Cannot parse Authentication config, using NONE", ex);
                    _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.NONE);
                }
            } else {
                _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.NONE);
                if (userName != null && !userName.isEmpty()) {
                    /* Default fallback for old configuration **/
                    _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.BASIC);
                    logger.debug("Using old auth scheme as fallback");
                }
            }


        } catch (Exception ex) {
            logger.fatal(ex, ex);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        try {
            Channel httpChannel = new Channel();

            httpChannels.add(httpChannel);
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(HTTPChannelTypes.PATH);
            String path = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(HTTPChannelTypes.LAST_READOUT);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

            JEVisType readoutOffsetType = channelClass.getType(HTTPChannelTypes.READOUT_OFFSET);
            Long readoutOffset = DatabaseHelper.getObjectAsLong(channel, readoutOffsetType);

            if (readoutOffset != null && lastReadout != null) {
                lastReadout = lastReadout.minus(readoutOffset);
            }

            httpChannel.setLastReadout(lastReadout);
            httpChannel.setPath(path);
            httpChannel.setChannelObject(channel);
            return _httpdatasource.sendSampleRequest(httpChannel);
        } catch (Exception ex) {
            logger.error(ex, ex);
        }

        return new ArrayList<>();
    }

    @Override
    public void parse(List<InputStream> input) {


    }

    private void initializeChannelObjects(JEVisObject httpObject) {
        try {
            JEVisClass channelDirClass = httpObject.getDataSource().getJEVisClass(HTTPChannelDirectoryTypes.NAME);
            JEVisObject channelDir = httpObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = httpObject.getDataSource().getJEVisClass(HTTPChannelTypes.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = channelDir.getChildren(channelClass, false);
            logger.info("Found {} channel objects in {}:{}", channels.size(), channelDir.getName(), channelDir.getID());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    _channels.add(channelObject);
                    try {
                        httpObject.getDataSource().reloadAttribute(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            logger.info("{}:{} has {} channels.", channelDir.getName(), channelDir.getID(), _channels.size());
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public void importResult() {
//        _importer.importResult(_result);
        //workaround until server is threadsafe
//        JEVisImporterAdapter.importResults(_result, _importer);
    }

    @Override
    public void initialize(JEVisObject httpObject) {
        initializeAttributes(httpObject);
        initializeChannelObjects(httpObject);

        _importer = ImporterFactory.getImporter(httpObject);
        _importer.initialize(httpObject);

    }

    interface HTTPTypes extends DataCollectorTypes.DataSource.DataServer {

        String NAME = "HTTP Server";
        String PASSWORD = "Password";
        String SSL = "SSL";
        String USER = "User";
    }

    interface HTTPChannelDirectoryTypes extends DataCollectorTypes.ChannelDirectory {

        String NAME = "HTTP Channel Directory";
    }

    interface HTTPChannelTypes extends DataCollectorTypes.Channel {

        String NAME = "HTTP Channel";
        String PATH = "Path";
    }

}
