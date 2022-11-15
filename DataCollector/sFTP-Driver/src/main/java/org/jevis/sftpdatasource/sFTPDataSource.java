/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.sftpdatasource;

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class sFTPDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(sFTPDataSource.class);
    private final Boolean _ssl = false;
    private final List<JEVisObject> _channels = new ArrayList<>();
    protected FTPClient _fc;
    private String _serverURL;
    private Integer _port;
    private String _userName;
    private String _password;
    private DateTimeZone _timezone;
    private Parser _parser;
    private Importer _importer;
    private List<Result> _result;

    private ChannelSftp _channel;
    private Session _session;
    private Integer _connectionTimeout;
    private Integer _readTimeout;

    @Override
    public void parse(List<InputStream> input) {
        _parser.parse(input, _timezone);
        _result = _parser.getResult();
    }

    @Override
    public void run() {

        for (JEVisObject channel : _channels) {

            try {
                _result = new ArrayList<Result>();
                JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);

                _parser = ParserFactory.getParser(parser);
                _parser.initialize(parser);

                try {
                    List<InputStream> input = this.sendSampleRequest(channel);

                    if (!input.isEmpty()) {
                        this.parse(input);
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
                    }
                } catch (JSchException ex) {
                    logger.error("No connection possible. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    logger.debug("No connection possible. For channel {}:{}", channel.getID(), channel.getName(), ex);
                    _channel.disconnect();
                    _session.disconnect();
                } catch (JEVisException ex) {
                    logger.error("JEVisException. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    logger.debug("JEVisException. For channel {}:{}", channel.getID(), channel.getName(), ex);
                } catch (SftpException ex) {
                    logger.error("Sftp Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
                    logger.debug("Sftp Exception. For channel {}:{}.", channel.getID(), channel.getName(), ex);
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
    }

    @Override
    public void importResult() {
        //        _importer.importResult(_result);
        //workaround until server is threadsave
//        JEVisImporterAdapter.importResults(_result, _importer);
    }

    @Override
    public void initialize(JEVisObject ftpObject) {
        initializeAttributes(ftpObject);
        initializeChannelObjects(ftpObject);

        _importer = ImporterFactory.getImporter(ftpObject);
        _importer.initialize(ftpObject);

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JSchException, JEVisException, SftpException {

        List<InputStream> answerList = new ArrayList<InputStream>();

        String hostname = _serverURL;
        String login = _userName;
        String password = _password;

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        try {
            JSch ssh = new JSch();
            _session = ssh.getSession(login, hostname, _port);
            _session.setConfig(config);
            _session.setPassword(password);
            _session.setTimeout(_readTimeout * 1000);
            _session.connect(_connectionTimeout * 1000);
            _channel = (ChannelSftp) _session.openChannel("sftp");
            _channel.connect();

            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.sFTPChannel.PATH);
            String filePath = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);

            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

            List<String> fileNames = DataSourceHelper.getSFTPMatchedFileNames(_channel, lastReadout, filePath);

            for (String fileName : fileNames) {
                logger.info("FileInputName: {}", fileName);
                InputStream get = _channel.get(fileName);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                try {
                    while ((len = get.read(buffer)) > 1) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                } catch (Exception ex) {
                    logger.error(ex);
                }

                InputStream answer = new ByteArrayInputStream(baos.toByteArray());

                answerList.add(answer);
            }

            _channel.exit();
        } catch (JSchException | SftpException e) {
            logger.error(e);
        } finally {
            if (_session != null) {
                _session.disconnect();
            }
        }


        if (answerList.isEmpty()) {
            logger.error("Cant get any data from the device");
        }

        return answerList;
    }

    private void initializeAttributes(JEVisObject sftpObject) {
        try {
            JEVisClass sftpType = sftpObject.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.sFTP.NAME);
            JEVisType server = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.HOST);
            JEVisType port = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.PORT);
            JEVisType connectionTimeout = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.CONNECTION_TIMEOUT);
            JEVisType readTimeout = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.READ_TIMEOUT);
            //            JEVisType maxRequest = type.getType("Maxrequestdays");
            JEVisType user = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.USER);
            JEVisType password = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.PASSWORD);
            JEVisType timezoneType = sftpType.getType(DataCollectorTypes.DataSource.DataServer.sFTP.TIMEZONE);
            JEVisType enableType = sftpType.getType(DataCollectorTypes.DataSource.DataServer.ENABLE);

            Long _id = sftpObject.getID();
            String _name = sftpObject.getName();
            _serverURL = DatabaseHelper.getObjectAsString(sftpObject, server);
            JEVisAttribute portAttr = sftpObject.getAttribute(port);
            if (!portAttr.hasSample()) {
                _port = 22;
            } else {
                _port = DatabaseHelper.getObjectAsInteger(sftpObject, port);
            }

            _connectionTimeout = DatabaseHelper.getObjectAsInteger(sftpObject, connectionTimeout);
            _readTimeout = DatabaseHelper.getObjectAsInteger(sftpObject, readTimeout);
            //            if (node.getAttribute(maxRequest).hasSample()) {
            //                _maximumDayRequest = Integer.parseInt((String) node.getAttribute(maxRequest).getLatestSample().getValue());
            //            }
            JEVisAttribute userAttr = sftpObject.getAttribute(user);
            if (!userAttr.hasSample()) {
                _userName = "";
            } else {
                _userName = DatabaseHelper.getObjectAsString(sftpObject, user);
            }
            JEVisAttribute passAttr = sftpObject.getAttribute(password);
            if (!passAttr.hasSample()) {
                _password = "";
            } else {
                _password = DatabaseHelper.getObjectAsString(sftpObject, password);
            }

            String timezoneString = DatabaseHelper.getObjectAsString(sftpObject, timezoneType);
            if (timezoneString != null) {
                _timezone = DateTimeZone.forID(timezoneString);
            } else {
                _timezone = DateTimeZone.UTC;
            }
            Boolean _enabled = DatabaseHelper.getObjectAsBoolean(sftpObject, enableType);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private void initializeChannelObjects(JEVisObject ftpObject) {
        try {
            JEVisClass channelDirClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.sFTPChannelDirectory.NAME);
            JEVisObject channelDir = ftpObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.sFTPChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = channelDir.getChildren(channelClass, false);
            logger.info("Found " + channels.size() + " channel objects in " + channelDir.getName() + ":" + channelDir.getID());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    _channels.add(channelObject);
                    try {
                        ftpObject.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + _channels.size() + " channels.");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
