/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.ftpdatasource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.jevis.commons.driver.DataSourceHelper;
import org.jevis.commons.driver.Importer;
import org.jevis.commons.driver.ImporterFactory;
import org.jevis.commons.driver.JEVisImporterAdapter;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.ParserFactory;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

/**
 *
 * @author bf
 */
public class FTPDataSource implements DataSource {

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

    @Override
    public void parse(List<InputStream> input) {
        _parser.parse(input,_timezone);
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
                java.util.logging.Logger.getLogger(FTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void importResult() {
//        _importer.importResult(_result);

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
            if (_ssl) {
                System.out.println("ftps connection");
                _fc = new FTPSClient();
            } else {
                _fc = new FTPClient();
            }
//            _fc.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

            if (_connectionTimeout != 0) {
                _fc.setConnectTimeout(_connectionTimeout.intValue() * 1000);
            }
            if (_readTimeout != 0) {
                _fc.setDataTimeout(_readTimeout.intValue() * 1000);
            }

            _fc.connect(_serverURL, _port);
//            _fc.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);

            if (_fc.login(_userName, _password) == false) {
                org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, "No Login possible");
//                throw new FetchingException(_id, FetchingExceptionType.CONNECTION_ERROR);
            }

//            _fc.setFileType(FTP.BINARY_FILE_TYPE);
//            _fc.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
            _fc.setBufferSize(1024000);

            _fc.setUseEPSVwithIPv4(false);
            _fc.enterLocalPassiveMode();

            InputStream answer = null;
            JEVisClass channelClass = channel.getJEVisClass();
            JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.PATH);
            String filePath = DatabaseHelper.getObjectAsString(channel, pathType);
            JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);
            DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
//            String filePath = dp.getFilePath();
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "SendSampleRequest2");
            List<String> fileNames = DataSourceHelper.getFTPMatchedFileNames(_fc, lastReadout, filePath);
//        String currentFilePath = Paths.get(filePath).getParent().toString();
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "Nr of Matched Files " + fileNames.size());
            for (String fileName : fileNames) {
                org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "FileInputName: " + fileName);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                String query = Paths.get(fileName);
                org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "FTPQuery " + fileName);
                boolean retrieveFile = _fc.retrieveFile(fileName, out);
                org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "Request status: " + retrieveFile);
                InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
                answer = new BufferedInputStream(inputStream);
//                InputHandler inputConverter = InputHandlerFactory.getInputConverter(answer);
//                inputConverter.setFilePath(fileName);
                answerList.add(answer);

            }
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(FTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FTPDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if (answerList.isEmpty()) {
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, "Cant get any data from the device");
        }

        return answerList;
    }

    private void initializeAttributes(JEVisObject ftpObject) {
        try {
            JEVisClass ftpType = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.FTP.NAME);
            JEVisType sslType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.SSL);
            JEVisType serverType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.HOST);
            JEVisType portType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.PORT);
            JEVisType connectionTimeoutType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.READ_TIMEOUT);
            JEVisType userType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.USER);
            JEVisType passwordType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.PASSWORD);
            JEVisType timezoneType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.TIMEZONE);
            JEVisType enableType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.ENABLE);

            _name = ftpObject.getName();
            _id = ftpObject.getID();
            _ssl = DatabaseHelper.getObjectAsBoolean(ftpObject, sslType);
            _serverURL = DatabaseHelper.getObjectAsString(ftpObject, serverType);
            _port = DatabaseHelper.getObjectAsInteger(ftpObject, portType);
            if (_port == null) {
                _port = 21;
            }
            _connectionTimeout = DatabaseHelper.getObjectAsInteger(ftpObject, connectionTimeoutType);
            _readTimeout = DatabaseHelper.getObjectAsInteger(ftpObject, readTimeoutType);

            JEVisAttribute userAttr = ftpObject.getAttribute(userType);
            if (!userAttr.hasSample()) {
                _userName = "";
            } else {
                _userName = DatabaseHelper.getObjectAsString(ftpObject, userType);
            }

            JEVisAttribute passAttr = ftpObject.getAttribute(passwordType);
            if (!passAttr.hasSample()) {
                _password = "";
            } else {
                _password = DatabaseHelper.getObjectAsString(ftpObject, passwordType);
            }
            String timezoneString = DatabaseHelper.getObjectAsString(ftpObject, timezoneType);
            _timezone = DateTimeZone.forID(timezoneString);
            _enabled = DatabaseHelper.getObjectAsBoolean(ftpObject, enableType);

        } catch (JEVisException ex) {
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, ex.getMessage());
        }
    }

    private void initializeChannelObjects(JEVisObject ftpObject) {
        try {
            JEVisClass channelDirClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.FTPChannelDirectory.NAME);
            JEVisObject channelDir = ftpObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.FTPChannel.NAME);
            _channels = channelDir.getChildren(channelClass, false);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FTPDataSource.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
