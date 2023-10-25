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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author bf
 */
public class JEVisHTTPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(JEVisHTTPDataSource.class);
    private DateTimeZone timezone;

    private final List<JEVisObject> _channels = new ArrayList<>();


    @Override
    public void run() {

        for (JEVisObject channel : _channels) {

            runParser(channel);
        }
    }

    private void runParser(JEVisObject channel) {
        try {
            _result = new ArrayList<Result>();
            JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
            JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);

            _parser = ParserFactory.getParser(parser);
            _parser.initialize(parser);

            try {
                List<InputStream> input = this.sendSampleRequest(channel);
                if (this._httpdatasource.getStatusLine().getStatusCode()>= 400) {
                    channel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(new DateTime(), _httpdatasource.getEndDateTime().toString()).commit();
                    if (_httpdatasource.getEndDateTime().isBefore(DateTime.now().minusHours(1))) {
                        runParser(channel);
                    }
                }

                if (!input.isEmpty()) {
                    this.parse(input);
                }


                if (!_result.isEmpty()) {
//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
                    JEVisImporterAdapter.importResultsWithOffset(_result, _importer, channel, 1);
                    for (InputStream inputStream : input) {
                        try {
                            inputStream.close();
                        } catch (Exception ex) {
                            logger.warn("could not close input stream: {}", ex.getMessage());
                        }
                    }
                    Optional<Result> lastAnswerDate = _result.stream().max(Comparator.comparing(Result::getDate));
                    if (lastAnswerDate.isPresent()) {
                        if (_httpdatasource.getEndDateTime().isBefore(DateTime.now().minusHours(1))) {
                            runParser(channel);
                        }

                    }
                }else {
                    if ((_httpdatasource.getEndDateTime().isBefore(DateTime.now()))) {
                        channel.getAttribute(DataCollectorTypes.Channel.LAST_READOUT).buildSample(new DateTime(), _httpdatasource.getEndDateTime().toString()).commit();
                        if (_httpdatasource.getEndDateTime().isBefore(DateTime.now().minusHours(1))) {
                            runParser(channel);
                        }
                    }

                }
            } catch (
                    MalformedURLException ex) {
                logger.error("MalformedURLException. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("MalformedURLException. For channel {}:{}", channel.getID(), channel.getName(), ex);
            } catch (
                    ClientProtocolException ex) {
                logger.error("Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
            } catch (
                    IOException ex) {
                logger.error("IO Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                logger.debug("IO Exception. For channel {}:{}.", channel.getID(), channel.getName(), ex);
            } catch (
                    ParseException ex) {
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
                timezone = DateTimeZone.forID(timezoneString);
            } else {
                timezone = DateTimeZone.UTC;
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
            _httpdatasource.setDateTimeZone(timezone);


            String authString = DatabaseHelper.getObjectAsString(httpObject, authType);
            if (authString != null && !authString.isEmpty()) {
                try {
                    HTTPDataSource.AUTH_SCHEME aut = HTTPDataSource.AUTH_SCHEME.valueOf(authString.toUpperCase());
                    _httpdatasource.setAuthScheme(aut);
                } catch (Exception ex) {
                    logger.error("Cannot parse Authentication config, using NONE", ex, ex);
                    _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.NONE);
                }
            } else {
                _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.NONE);
                if (userName != null && !userName.isEmpty()) {
                    /* Default fallback for old configuration **/
                    _httpdatasource.setAuthScheme(HTTPDataSource.AUTH_SCHEME.BASIC);
                }
            }


        } catch (Exception ex) {
            logger.fatal(ex, ex);
        }
    }

    private Parser _parser;
    private Importer _importer;

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws Exception {
        try {
            Channel httpChannel = new Channel();


            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(HTTPChannelTypes.PATH);
            String path = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(HTTPChannelTypes.LAST_READOUT);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

            httpChannel.setLastReadout(lastReadout);
            httpChannel.setPath(path);
            httpChannel.setChannelObject(channel);
            return _httpdatasource.sendSampleRequest(httpChannel);
        } catch (Exception ex) {
            logger.error(ex, ex);
        }

        return new ArrayList<>();
    }

    private List<Result> _result;

    private HTTPDataSource _httpdatasource;

    @Override
    public void parse(List<InputStream> input) {
        _parser.parse(input, _httpdatasource.getDateTimeZone());
        _result = _parser.getResult();


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
        //workaround until server is threadsave
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
