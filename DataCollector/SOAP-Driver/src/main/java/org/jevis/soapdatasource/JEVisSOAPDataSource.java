/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.soapdatasource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class JEVisSOAPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(JEVisSOAPDataSource.class);

    private Parser _parser;
    private Importer _importer;
    private List<JEVisObject> _channels = new ArrayList<>();
    private List<Result> _result;

    private JEVisObject _dataSource;
    private SOAPDataSource _soapdatasource;
    private DateTimeZone _timeZone;

    @Override
    public void parse(List<InputStream> input) {
        _parser.parse(input, _timeZone);
        _result = _parser.getResult();
    }

    @Override
    public void run() {
        logger.info("Nr channels: " + _channels.size() + " for datasource: " + _dataSource.getID());

        for (JEVisObject channel : _channels) {

            try {
                _result = new ArrayList<Result>();

                List<InputStream> input = this.sendSampleRequest(channel);

                if (!input.isEmpty()) {
                    JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                    JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);

                    _parser = ParserFactory.getParser(parser);
                    _parser.initialize(parser);
                    this.parse(input);
                } else {
                    logger.error("no connection results for channel: " + channel.getID() + " and datasource: " + _dataSource.getID());
                    continue;
                }

                if (!_result.isEmpty()) {

//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
                    JEVisImporterAdapter.importResults(_result, _importer, channel);

                    for (InputStream inputStream : input) {
                        try {
                            inputStream.close();
                        } catch (Exception ex) {
                            logger.warn("could not close input stream: {}", ex.getMessage());
                        }
                    }
                    logger.info("import results: " + _result.size() + " for channel: " + channel.getID() + " and datasource: " + _dataSource.getID());

                } else {
                    logger.error("no parsing results for channel: " + channel.getID() + " and datasource: " + _dataSource.getID());
                }
            } catch (Exception ex) {
                logger.error("error for channel: " + channel.getID() + " and datasource: " + _dataSource.getID());
                logger.error(ex);

            }
        }
        logger.info("--------- finish datasource: " + _dataSource.getID() + " ----------");

    }

    @Override
    public void importResult() {
        //        _importer.importResult(_result);
        //workaround until server is threadsave
//        JEVisImporterAdapter.importResults(_result, _importer);
    }

    @Override
    public void initialize(JEVisObject soapObject) {
        _dataSource = soapObject;
        initializeAttributes(soapObject);
        initializeChannelObjects(soapObject);

        _importer = ImporterFactory.getImporter(_dataSource);
        _importer.initialize(_dataSource);

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) {
        Channel soapChannel = new Channel();

        try {
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.SOAPChannel.PATH);
            String path = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.SOAPChannel.LAST_READOUT);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

            JEVisType templateType = channelClass.getType(DataCollectorTypes.Channel.SOAPChannel.TEMPLATE);
            String template = DatabaseHelper.getObjectAsString(channel, templateType);

            soapChannel.setLastReadout(lastReadout);
            soapChannel.setPath(path);
            soapChannel.setTemplate(template);
        } catch (JEVisException ex) {
            logger.error("Error while send sample request for channel: " + channel.getID() + " and datasource: " + _dataSource.getID());
            logger.error(ex);
        }

        return _soapdatasource.sendSampleRequest(soapChannel);
    }

    private void initializeAttributes(JEVisObject soapObject) {
        try {
            JEVisClass soapType = soapObject.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.SOAP.NAME);
            JEVisType server = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.HOST);
            JEVisType port = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.PORT);
            JEVisType sslType = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.SSL);
            JEVisType connectionTimeout = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.CONNECTION_TIMEOUT);
            JEVisType readTimeout = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.READ_TIMEOUT);
            JEVisType user = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.USER);
            JEVisType password = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.PASSWORD);
            JEVisType timezoneType = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.TIMEZONE);
            JEVisType enableType = soapType.getType(DataCollectorTypes.DataSource.DataServer.SOAP.ENABLE);

            Long _id = soapObject.getID();
            String _name = soapObject.getName();
//            _dateFormat = DatabaseHelper.getObjectAsString(soapObject, dateFormat);
            String _host = DatabaseHelper.getObjectAsString(soapObject, server);
            Integer _port = DatabaseHelper.getObjectAsInteger(soapObject, port);
            Integer _connectionTimeout = DatabaseHelper.getObjectAsInteger(soapObject, connectionTimeout);
            Integer _readTimeout = DatabaseHelper.getObjectAsInteger(soapObject, readTimeout);
            Boolean _ssl = DatabaseHelper.getObjectAsBoolean(soapObject, sslType);
            JEVisAttribute userAttr = soapObject.getAttribute(user);
            String _userName = "";
            if (!userAttr.hasSample()) {
                _userName = "";
            } else {
                _userName = (String) userAttr.getLatestSample().getValue();
            }
            JEVisAttribute passAttr = soapObject.getAttribute(password);
            String _password = "";
            if (!passAttr.hasSample()) {
                _password = "";
            } else {
                _password = (String) passAttr.getLatestSample().getValue();
            }

            String timezoneString = DatabaseHelper.getObjectAsString(soapObject, timezoneType);
            if (timezoneString != null) {
                _timeZone = DateTimeZone.forID(timezoneString);
            } else {
                _timeZone = DateTimeZone.UTC;
            }
            Boolean _enabled = DatabaseHelper.getObjectAsBoolean(soapObject, enableType);

            _soapdatasource = new SOAPDataSource();
            _soapdatasource.setConnectionTimeout(_connectionTimeout);
            _soapdatasource.setHost(_host);
            _soapdatasource.setPassword(_password);
            _soapdatasource.setPort(_port);
            _soapdatasource.setReadTimeout(_readTimeout);
            _soapdatasource.setSsl(_ssl);
            _soapdatasource.setUserName(_userName);
        } catch (JEVisException ex) {
            logger.error("error while init datasource: " + _dataSource.getID());
            logger.error(ex);
        }
    }

    private void initializeChannelObjects(JEVisObject soapObject) {
        try {
            JEVisClass channelDirClass = soapObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.SOAPChannelDirectory.NAME);
            JEVisObject channelDir = soapObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = soapObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.SOAPChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = channelDir.getChildren(channelClass, false);
            logger.info("Found " + channels.size() + " channel objects in " + channelDir.getName() + ":" + channelDir.getID());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    _channels.add(channelObject);
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + _channels.size() + " channels.");
        } catch (JEVisException ex) {
            logger.error("error while init channels for datasource: " + _dataSource.getID());
            logger.error(ex);
        }
    }

}
