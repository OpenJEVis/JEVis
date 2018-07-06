/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.sftpdatasource;

import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.cli.JEVisCommandLine;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class sFTPDataSource implements DataSource {

    private Long _id;
    private String _name;
    private String _serverURL;
    private Integer _port;
    private Integer _connectionTimeout;
    private Integer _readTimeout;
    private String _userName;
    private String _password;
    private Boolean _ssl = false;
    private DateTimeZone _timezone;
    private Boolean _enabled;
    protected FTPClient _fc;
    private Parser _parser;
    private Importer _importer;
    private List<JEVisObject> _channels;
    private List<Result> _result;

    private JEVisObject _dataSource;

    private ChannelSftp _channel;
    private Session _session;

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
                java.util.logging.Logger.getLogger(sFTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
        _dataSource = ftpObject;
        initializeAttributes(ftpObject);
        initializeChannelObjects(ftpObject);

        _importer = ImporterFactory.getImporter(_dataSource);
        if (_importer != null) {
            _importer.initialize(_dataSource);
        }

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) {

        List<InputStream> answerList = new ArrayList<InputStream>();
        try {
            String hostname = _serverURL;
            String login = _userName;
            String password = _password;

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            JSch ssh = new JSch();
            _session = ssh.getSession(login, hostname, _port);
            _session.setConfig(config);
            _session.setPassword(password);
            _session.connect();
            _channel = (ChannelSftp) _session.openChannel("sftp");
            _channel.connect();
        } catch (JSchException ex) {
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, "No connection possible");
            org.apache.log4j.Logger.getLogger(sFTPDataSource.class).setLevel(org.apache.log4j.Level.ALL);
            org.apache.log4j.Logger.getLogger(sFTPDataSource.class).setLevel(JEVisCommandLine.getInstance().getDebugLevel());
//            throw new FetchingException(_id, FetchingExceptionType.CONNECTION_ERROR);
            _channel.disconnect();
            _session.disconnect();
        }

        try {
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.sFTPChannel.PATH);
            String filePath = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);
            DateTime lastReadout = DateTime.now().minusHours(1);
            try {
                lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"));
            } catch (Exception e) {
                try {
                    lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"));
                } catch (Exception ex) {

                }
            }

//        ChannelSftp sftp = (ChannelSftp) _channel;
            List<String> fileNames = DataSourceHelper.getSFTPMatchedFileNames(_channel, lastReadout, filePath);
//        String currentFilePath = Paths.get(filePath).getParent().toString();
            for (String fileName : fileNames) {
                org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "FileInputName: " + fileName);

//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                String query = Paths.get(fileName);
                InputStream get = _channel.get(fileName);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                try {
                    while ((len = get.read(buffer)) > 1) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(sFTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }

                InputStream answer = new ByteArrayInputStream(baos.toByteArray());
//                InputHandler inputConverter = InputHandlerFactory.getInputConverter(answer);
//                inputConverter.setFilePath(fileName);
                answerList.add(answer);

            }

            _channel.disconnect();
            _session.disconnect();
        } catch (JEVisException ex) {

        } catch (SftpException ex) {
            java.util.logging.Logger.getLogger(sFTPDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (answerList.isEmpty()) {
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, "Cant get any data from the device");
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

            _id = sftpObject.getID();
            _name = sftpObject.getName();
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
            _timezone = DateTimeZone.forID(timezoneString);
            _enabled = DatabaseHelper.getObjectAsBoolean(sftpObject, enableType);
        } catch (JEVisException ex) {
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        }
    }

    private void initializeChannelObjects(JEVisObject ftpObject) {
        try {
            JEVisClass channelDirClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.sFTPChannelDirectory.NAME);
            JEVisObject channelDir = ftpObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.sFTPChannel.NAME);
            _channels = channelDir.getChildren(channelClass, false);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(sFTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
