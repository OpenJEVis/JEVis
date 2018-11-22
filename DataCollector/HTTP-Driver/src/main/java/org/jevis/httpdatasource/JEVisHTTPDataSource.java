package org.jevis.httpdatasource;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class JEVisHTTPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(JEVisHTTPDataSource.class);

    @Override
    public void run() {

        for (JEVisObject channel : _channels) {

            try {
                _result = new ArrayList<Result>();
                JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);

                _parser = ParserFactory.getParser(parser);
                _parser.initialize(parser);

                List<InputStream> input = this.sendSampleRequest(channel);

                if (!input.isEmpty()) {
                    this.parse(input);
                }

                if (!_result.isEmpty()) {
//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
                    JEVisImporterAdapter.importResults(_result, _importer, channel);
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) {
        Channel httpChannel = new Channel();

        try {
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(HTTPChannelTypes.PATH);
            String path = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(HTTPChannelTypes.LAST_READOUT);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
            httpChannel.setLastReadout(lastReadout);
            httpChannel.setPath(path);
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        return _httpdatasource.sendSampleRequest(httpChannel);
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
            JEVisType enableType = httpType.getType(HTTPTypes.ENABLE);

            String serverURL = DatabaseHelper.getObjectAsString(httpObject, server);
            Integer port = DatabaseHelper.getObjectAsInteger(httpObject, portType);
            Integer connectionTimeout = DatabaseHelper.getObjectAsInteger(httpObject, connectionTimeoutType);
            Integer readTimeout = DatabaseHelper.getObjectAsInteger(httpObject, readTimeoutType);
            Boolean ssl = DatabaseHelper.getObjectAsBoolean(httpObject, sslType);
            JEVisAttribute userAttr = httpObject.getAttribute(userType);
            JEVisAttribute timeZoneAttr = httpObject.getAttribute(timezoneType);

            String timeZone = null;
            if (!timeZoneAttr.hasSample()) {
                timeZone = "UTC";
            } else {
                timeZone = timeZoneAttr.getLatestSample().getValueAsString();
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
            _httpdatasource.setConnectionTimeout(connectionTimeout);
            _httpdatasource.setPassword(password);
            _httpdatasource.setPort(port);
            _httpdatasource.setReadTimeout(readTimeout);
            _httpdatasource.setServerURL(serverURL);
            _httpdatasource.setSsl(ssl);
            _httpdatasource.setUserName(userName);
            _httpdatasource.setDateTimeZone(timeZone);

        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    private Parser _parser;
    private Importer _importer;
    private List<JEVisObject> _channels;
    private List<Result> _result;

    private JEVisObject _dataSource;
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
            _channels = channelDir.getChildren(channelClass, false);
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
        _dataSource = httpObject;
        initializeAttributes(httpObject);
        initializeChannelObjects(httpObject);

        _importer = ImporterFactory.getImporter(_dataSource);
        if (_importer != null) {
            _importer.initialize(_dataSource);
        }

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
