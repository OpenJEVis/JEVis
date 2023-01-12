package org.jevis.dwddriver;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.DataSource;
import org.joda.time.DateTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DWDDriver implements DataSource {

    private static final String CHARSET = "UTF-8";
    private static final Logger log = LogManager.getLogger(DWDDriver.class);

    private final String PATH_TO_WEATHER = "/climate_environment/CDC/observations_germany/climate";

    private final List<String> AGGREGATION_TYPES = Arrays.asList("10_minutes", "1_minute", "5_minutes", "annual", "daily", "hourly", "monthly", "multi_annual", "subdaily");

    @Override
    public void parse(List<InputStream> input) {
//        parser.parse(input, timezone);
//        result = parser.getResult();
    }

    @Override
    public void run() {
//        logger.debug("Run DWD DataSource");
//        for (JEVisObject channel : channels) {
//            logger.debug("Start Channel: [{}] {}", channel.getID(), channel.getName());
//            try {
//                result = new ArrayList<Result>();
//
//                JEVisClass parserJevisClass = channel.getDataSource().getJEVisClass(DataCollectorTypes.Parser.NAME);
//                logger.debug("parser.class: {}", parserJevisClass.getName());
//                List<JEVisObject> parserList = channel.getChildren(parserJevisClass, true);
//
//                boolean successful = true;
//                for (JEVisObject parser : parserList) {
//                    logger.debug("parser.object: {}", parser.getID());
//
//                    this.parser = ParserFactory.getParser(parser);
//                    this.parser.initialize(parser);
//
//                    logger.debug("Parser.initialize");
//
//                    logger.debug("sending request");
//
//                    try {
//                        List<InputStream> input = this.sendSampleRequest(channel);
//
//                        logger.debug("sending request - done");
//                        if (!input.isEmpty()) {
//                            logger.debug("input is not empty: {}, start parsing", input.size());
//                            this.parse(input);
//                            logger.debug("parsing - done");
//                        }
//
//                        if (!result.isEmpty()) {
//                            logger.debug("result is not empty {}, start import", result.size());
////                    this.importResult();
////
////                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
//                            JEVisImporterAdapter.importResults(result, importer, channel);
//                            logger.debug("import done");
//                        }
//                    } catch (
//                            MalformedURLException ex) {
//                        logger.error("MalformedURLException. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
//                        logger.debug("MalformedURLException. For channel {}:{}", channel.getID(), channel.getName(), ex);
//                        successful = false;
//                    } catch (
//                            ClientProtocolException ex) {
//                        logger.error("Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
//                        logger.debug("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
//                        successful = false;
//                    } catch (
//                            IOException ex) {
//                        logger.error("IO Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
//                        logger.debug("IO Exception. For channel {}:{}.", channel.getID(), channel.getName(), ex);
//                        successful = false;
//                    } catch (
//                            ParseException ex) {
//                        logger.error("Parse Exception. For channel {}:{}. {}", channel.getID(), channel.getName(), ex.getMessage());
//                        logger.debug("Parse Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
//                        successful = false;
//                    } catch (Exception ex) {
//                        logger.error("Exception. For channel {}:{}", channel.getID(), channel.getName(), ex);
//                        successful = false;
//                    }
//                }
//
//                if (successful && deleteOnSuccess && parser.getReport().errors().isEmpty()) {
//                    FTPClient ftpClient = initFTPClient();
//                    try {
//                        for (String fileName : fileNames) {
//                            logger.debug("Deleting file {} from ftp server", fileName);
//                            ftpClient.deleteFile(fileName);
//                        }
//                    } catch (Exception e) {
//                        logger.error("Error while deleting files from ftp server", e);
//                    } finally {
//                        ftpClient.disconnect();
//                    }
//                }
//            } catch (Exception ex) {
//                logger.error(ex);
//                ex.printStackTrace();
//            }
//        }
    }

    @Override
    public void importResult() {
//        _importer.importResult(_result);

    }

    @Override
    public void initialize(JEVisObject ftpObject) {
//        initializeAttributes(ftpObject);
//        initializeChannelObjects(ftpObject);
//
//        importer = ImporterFactory.getImporter(ftpObject);
//        importer.initialize(ftpObject);

    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JEVisException, IOException {
        List<InputStream> answerList = new ArrayList<InputStream>();
        FTPClient ftpClient = initFTPClient();

        InputStream answer = null;
        JEVisClass channelClass = channel.getJEVisClass();
        JEVisType pathType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.PATH);
        String filePath = DatabaseHelper.getObjectAsString(channel, pathType);
        JEVisType readoutType = channelClass.getType(DataCollectorTypes.Channel.FTPChannel.LAST_READOUT);
        DateTime lastReadout = DatabaseHelper.getObjectAsDate(channel, readoutType);

//            String filePath = dp.getFilePath();
        logger.info("SendSampleRequest2");
        List<String> fileNames = new ArrayList<>();
        fileNames.clear();
//        fileNames.addAll(DataSourceHelper.getFTPMatchedFileNames(ftpClient, lastReadout, timezone, filePath, importer.getOverwrite()));
//        String currentFilePath = Paths.get(filePath).getParent().toString();
        logger.info("Nr of Matched Files " + fileNames.size());
        for (String fileName : fileNames) {
            logger.info("FileInputName: " + fileName);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
//                String query = Paths.get(fileName);
            logger.info("FTPQuery " + fileName);
            boolean retrieveFile = ftpClient.retrieveFile(fileName, out);
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

        FTPClient ftpClient = new FTPClient();

        ftpClient.connect("opendata.dwd.de", 80);

        if (!ftpClient.login("Anonymous", "anonymous")) {
            logger.error("No Login possible");
        }

        ftpClient.setBufferSize(1024000);

        ftpClient.setUseEPSVwithIPv4(false);
        ftpClient.enterLocalPassiveMode();

        return ftpClient;
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
//                    this.channels.add(channelObject);
                    try {
                        ftpObject.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });

//            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + this.channels.size() + " channels.");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
