/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.ftpdatasource;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bf
 */
public class FTPDataSource implements DataSource {

    private static final Logger logger = LogManager.getLogger(FTPDataSource.class);
    private final List<JEVisObject> channels = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();
    private String serverURL;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String userName;
    private String password;
    private Boolean ssl = false;
    private Boolean deleteOnSuccess = false;
    private DateTimeZone timezone;
    private Parser parser;
    private Importer importer;
    private List<Result> result;

    @Override
    public void parse(List<InputStream> input) {
        parser.parse(input, timezone);
        result = parser.getResult();
    }

    @Override
    public void run() {
        logger.debug("Run FTp DataSource");
        for (JEVisObject channel : channels) {
            logger.debug("Start Channel: [{}] {}", channel.getID(), channel.getName());
            try {
                result = new ArrayList<Result>();

                JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
                logger.debug("parser.class: ", parserJevisClass.getName());
                JEVisObject parser = channel.getChildren(parserJevisClass, true).get(0);
                logger.debug("parser.object: ", parser.getID());

                this.parser = ParserFactory.getParser(parser);
                this.parser.initialize(parser);

                logger.debug("Parser.initialize");

                logger.debug("sending request");

                try {
                    List<InputStream> input = this.sendSampleRequest(channel);

                    logger.debug("sending request - done");
                    if (!input.isEmpty()) {
                        logger.debug("input is not empty: {}, start parsing", input.size());
                        this.parse(input);
                        logger.debug("parsing - done");
                    }

                    if (!result.isEmpty()) {
                        logger.debug("result is not empty {}, start import", result.size());
//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
                        JEVisImporterAdapter.importResults(result, importer, channel);
                        logger.debug("import done");

                        if (result.size() == fileNames.size() && deleteOnSuccess) {
                            FTPClient ftpClient = initFTPClient();
                            try {
                                for (String fileName : fileNames) {
                                    ftpClient.deleteFile(fileName);
                                }
                            } catch (Exception e) {
                                logger.error("Error while deleting files from ftp server", e);
                            } finally {
                                ftpClient.disconnect();
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
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void importResult() {
//        _importer.importResult(_result);

    }

    @Override
    public void initialize(JEVisObject ftpObject) {
        initializeAttributes(ftpObject);
        initializeChannelObjects(ftpObject);

        importer = ImporterFactory.getImporter(ftpObject);
        importer.initialize(ftpObject);

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JEVisException, IOException {
        List<InputStream> answerList = new ArrayList<InputStream>();
        FTPClient _fc = initFTPClient();

        InputStream answer = null;
        JEVisClass channelClass = channel.getJEVisClass();
        JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.PATH);
        String filePath = DatabaseHelper.getObjectAsString(channel, pathType);
        JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);
        DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

//            String filePath = dp.getFilePath();
        logger.info("SendSampleRequest2");
        fileNames.addAll(DataSourceHelper.getFTPMatchedFileNames(_fc, lastReadout, timezone, filePath));
//        String currentFilePath = Paths.get(filePath).getParent().toString();
        logger.info("Nr of Matched Files " + fileNames.size());
        for (String fileName : fileNames) {
            logger.info("FileInputName: " + fileName);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
//                String query = Paths.get(fileName);
            logger.info("FTPQuery " + fileName);
            boolean retrieveFile = _fc.retrieveFile(fileName, out);
            logger.info("Request status: " + retrieveFile);
            InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            answer = new BufferedInputStream(inputStream);
//                InputHandler inputConverter = InputHandlerFactory.getInputConverter(answer);
//                inputConverter.setFilePath(fileName);
            answerList.add(answer);
        }

        return answerList;
    }

    private FTPClient initFTPClient() throws IOException {
        FTPClient ftpClient;
        if (ssl) {
            logger.info("ftps connection");
            ftpClient = new FTPSClient();
        } else {
            ftpClient = new FTPClient();
        }
//            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

        if (connectionTimeout != 0) {
            ftpClient.setConnectTimeout(connectionTimeout * 1000);
        }
        if (readTimeout != 0) {
            ftpClient.setDataTimeout(readTimeout * 1000);
        }

        ftpClient.connect(serverURL, port);
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);

        if (!ftpClient.login(userName, password)) {
            logger.error("No Login possible");
//                throw new FetchingException(_id, FetchingExceptionType.CONNECTION_ERROR);
        }

//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//            ftpClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
        ftpClient.setBufferSize(1024000);

        ftpClient.setUseEPSVwithIPv4(false);
        ftpClient.enterLocalPassiveMode();

        return ftpClient;
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
            JEVisType deleteFileOnSuccessType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.FTP.DELETE_ON_SUCCESS);
//            JEVisType enableType = ftpType.getType(DataCollectorTypes.DataSource.DataServer.ENABLE);

//            String _name = ftpObject.getName();
//            Long _id = ftpObject.getID();
            ssl = DatabaseHelper.getObjectAsBoolean(ftpObject, sslType);
            serverURL = DatabaseHelper.getObjectAsString(ftpObject, serverType);
            port = DatabaseHelper.getObjectAsInteger(ftpObject, portType);
            if (port == null) {
                port = 21;
            }
            connectionTimeout = DatabaseHelper.getObjectAsInteger(ftpObject, connectionTimeoutType);
            readTimeout = DatabaseHelper.getObjectAsInteger(ftpObject, readTimeoutType);

            JEVisAttribute userAttr = ftpObject.getAttribute(userType);
            if (userAttr == null || !userAttr.hasSample()) {
                userName = "";
            } else {
                userName = DatabaseHelper.getObjectAsString(ftpObject, userType);
            }

            JEVisAttribute passAttr = ftpObject.getAttribute(passwordType);
            if (passAttr == null || !passAttr.hasSample()) {
                password = "";
            } else {
                password = DatabaseHelper.getObjectAsString(ftpObject, passwordType);
            }
            String timezoneString = DatabaseHelper.getObjectAsString(ftpObject, timezoneType);
            if (timezoneString != null) {
                timezone = DateTimeZone.forID(timezoneString);
            } else {
                timezone = DateTimeZone.UTC;
            }

            JEVisAttribute deleteOnSuccessAttr = ftpObject.getAttribute(deleteFileOnSuccessType);
            if (deleteOnSuccessAttr == null || !deleteOnSuccessAttr.hasSample()) {
                deleteOnSuccess = false;
            } else {
                deleteOnSuccess = DatabaseHelper.getObjectAsBoolean(ftpObject, deleteFileOnSuccessType);
            }
//            Boolean _enabled = DatabaseHelper.getObjectAsBoolean(ftpObject, enableType);

        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private void initializeChannelObjects(JEVisObject ftpObject) {
        try {
            JEVisClass channelDirClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.ChannelDirectory.FTPChannelDirectory.NAME);
            JEVisObject channelDir = ftpObject.getChildren(channelDirClass, false).get(0);
            JEVisClass channelClass = ftpObject.getDataSource().getJEVisClass(DataCollectorTypes.Channel.FTPChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = channelDir.getChildren(channelClass, false);
            logger.info("Found " + channels.size() + " channel objects in " + channelDir.getName() + ":" + channelDir.getID());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    this.channels.add(channelObject);
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + this.channels.size() + " channels.");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
