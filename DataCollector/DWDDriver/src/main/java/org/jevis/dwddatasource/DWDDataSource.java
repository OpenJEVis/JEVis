package org.jevis.dwddatasource;

import javafx.collections.FXCollections;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.datasource.Station;
import org.jevis.commons.datasource.StationData;
import org.jevis.commons.driver.*;
import org.jevis.commons.driver.dwd.Attribute;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DWDDataSource implements DataSource {
    private static final Logger logger = LogManager.getLogger(DWDDataSource.class);
    private static final String CHARSET = "UTF-8";
    private static final Logger log = LogManager.getLogger(DWDDataSource.class);

    private final String PATH_TO_WEATHER = "/climate_environment/CDC/observations_germany/climate";

    private final List<String> AGGREGATION_TYPES = Arrays.asList("10_minutes", "1_minute", "5_minutes", "annual", "daily", "hourly", "monthly", "multi_annual", "subdaily");
    private final String initialPath = "climate_environment/CDC/observations_germany/climate/";
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();
    private final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC();
    private final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("yyyyMM").withZoneUTC();
    private final DateTimeFormatter yearFormatter = DateTimeFormat.forPattern("yyyy").withZoneUTC();
    private final List<DWDChannel> channels = new ArrayList<>();
    private Importer importer;
    private ArrayList<Result> result;
    private boolean successful;
    private FTPClient ftpClient;
    private Integer connectionTimeout;
    private Integer readTimeout;

    @Override
    public void parse(List<InputStream> input) {
//        parser.parse(input, timezone);
//        result = parser.getResult();
    }

    @Override
    public void run() {
        logger.debug("Run DWD DataSource");

        for (DWDChannel channel : channels) {
            logger.debug("Start Channel: [{}] {}", channel.getId(), channel.getName());
            try {
                result = new ArrayList<Result>();

                Station station = new Station();
                station.setId(channel.getId());
                List<String> pathList = new ArrayList<>();
                pathList.add("/" + initialPath + channel.getAggregation().getValue().toLowerCase() + "/" + channel.getAttribute().toString().toLowerCase() + "/historical/");
                pathList.add("/" + initialPath + channel.getAggregation().getValue().toLowerCase() + "/" + channel.getAttribute().toString().toLowerCase() + "/recent/");
                pathList.add("/" + initialPath + channel.getAggregation().getValue().toLowerCase() + "/" + channel.getAttribute().toString().toLowerCase() + "/now/");
                station.getIntervalPath().put(channel.getAttribute(), pathList);

                logger.debug("sending request");

                try {
                    DateTime firstDate = channel.getLastReadout();
                    DateTime lastDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                    StationData stationData = loadData(station, firstDate, lastDate);

                    logger.debug("sending request - done");
                    if (!stationData.getData().isEmpty()) {
                        logger.debug("input is not empty: {}, start parsing", stationData.getData().size());

                        stationData.getData().forEach((dateTime, stringStringMap) -> {

                            if (dateTime.equals(firstDate) || (dateTime.isAfter(firstDate) && dateTime.isBefore(lastDate)) || dateTime.equals(lastDate)) {
                                String s = stringStringMap.get(channel.getDataName());
                                try {
                                    Result result = new Result(channel.getTarget(), s, dateTime);
                                    this.result.add(result);
                                } catch (Exception e) {
                                    logger.error("Could not create JEVisSample", e);
                                }
                            }
                        });

                        logger.debug("parsing - done");
                    }

                    if (!result.isEmpty()) {
                        logger.debug("result is not empty {}, start import", result.size());

                        JEVisImporterAdapter.importResults(result, importer, channel.getObject());
                        logger.debug("import done");
                    }
                } catch (ParseException ex) {
                    logger.error("Parse Exception. For channel {}:{}. {}", channel.getId(), channel.getName(), ex.getMessage());
                    logger.debug("Parse Exception. For channel {}:{}", channel.getId(), channel.getName(), ex);
                    successful = false;
                } catch (Exception ex) {
                    logger.error("Exception. For channel {}:{}", channel.getId(), channel.getName(), ex);
                    successful = false;
                }


            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }

    private StationData loadData(Station selectedStation, DateTime firstDate, DateTime lastDate) {
        StationData stationData = new StationData();
        try {
            StringBuilder idString = new StringBuilder(String.valueOf(selectedStation.getId()));
            for (int i = idString.length(); i < 5; i++) {
                idString.insert(0, "0");
            }
            FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(idString.toString()));

            stationData.setName(selectedStation.getName());
            stationData.setId(selectedStation.getId());
            Map<DateTime, Map<String, String>> dataMap = new HashMap<>();

            for (Map.Entry<Attribute, List<String>> stationPathList : selectedStation.getIntervalPath().entrySet()) {
                for (String stationPath : stationPathList.getValue()) {
                    for (FTPFile ftpFile : ftpClient.listFiles(stationPath, filter)) {

                        if (!stationPath.endsWith("recent/") && !stationPath.endsWith("now/")) {
                            String fileName = ftpFile.getName();

                            fileName = fileName.substring(fileName.indexOf(idString.toString()) + idString.length() + 1);

                            DateTime beginningOfFile = new DateTime(Integer.parseInt(fileName.substring(0, 4)), Integer.parseInt(fileName.substring(4, 6)), Integer.parseInt(fileName.substring(6, 8)), 0, 0);
                            DateTime endOfFile = new DateTime(Integer.parseInt(fileName.substring(9, 13)), Integer.parseInt(fileName.substring(13, 15)), Integer.parseInt(fileName.substring(15, 17)), 23, 59, 59, 999);

                            Interval interval = new Interval(beginningOfFile, endOfFile);
                            if (!interval.contains(firstDate) && !firstDate.isBefore(beginningOfFile)) {
                                continue;
                            }
                        }

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        logger.info("FTPQuery {}", ftpFile.getName());
                        boolean retrieveFile = ftpClient.retrieveFile(stationPath + ftpFile.getName(), out);
                        logger.info("retrieved file {}", ftpFile.getName());

                        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
                        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

                        ZipEntry entry;
                        while ((entry = zipInputStream.getNextEntry()) != null) {
                            try {
                                if (entry.getName().contains("produkt")) {
                                    BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream), 1024);
                                    List<String> header = new ArrayList<>();
                                    String l;
                                    long lineNo = 0;
                                    while ((l = br.readLine()) != null) {
                                        String[] values = l.split(";");
                                        Map<String, String> columnMap = new HashMap<>();

                                        if (lineNo == 0) {
                                            header.addAll(Arrays.asList(values).subList(2, values.length - 1));
                                        } else if (lineNo > 0) {
                                            for (int i = 2; i < values.length - 1; i++) {
                                                columnMap.put(header.get(i - 2), values[i].trim());
                                            }

                                            String dt = values[1].trim();
                                            try {
                                                DateTimeFormatter dtf = getDateTimeFormatter(dt);

                                                if (dtf != null) {
                                                    DateTime dateTime = dtf.parseDateTime(dt);
                                                    if (dateTime.equals(firstDate) || (dateTime.isAfter(firstDate) && dateTime.isBefore(lastDate))) {
                                                        dataMap.put(dateTime, columnMap);
                                                        logger.debug("Added Datetime {}, with map {}", dt, columnMap);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                logger.error("{} - Could not create map for line - {}", entry.getName(), l, e);
                                            }
                                        }
                                        lineNo++;
                                    }
                                }
                            } catch (Exception e) {
                                logger.error(e);
                            }
                        }
                    }
                }
            }

            stationData.setData(FXCollections.observableMap(dataMap));
        } catch (Exception e) {
            logger.error(e);
        }

        return stationData;
    }

    private @Nullable DateTimeFormatter getDateTimeFormatter(String dt) {
        DateTimeFormatter dtf = null;
        if (dt.length() == 12)
            dtf = minuteFormatter;
        else if (dt.length() == 10)
            dtf = hourFormatter;
        else if (dt.length() == 8)
            dtf = dayFormatter;
        else if (dt.length() == 6)
            dtf = monthFormatter;
        else if (dt.length() == 4)
            dtf = yearFormatter;
        return dtf;
    }

    @Override
    public void importResult() {
        importer.importResult(result);
    }

    @Override
    public void initialize(JEVisObject dwdDataSource) {
        initializeAttributes(dwdDataSource);
        initializeChannelObjects(dwdDataSource);

        try {
            initFTPClient();
        } catch (Exception e) {
            logger.error(e);
        }

        importer = ImporterFactory.getImporter(dwdDataSource);
        importer.initialize(dwdDataSource);

    }

    private void initializeAttributes(JEVisObject dwdDataSource) {

        try {
            JEVisType readTimeoutAttribute = dwdDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.DWDServer.READ_TIMEOUT);
            JEVisType connectionTimeoutAttribute = dwdDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.DWDServer.CONNECTION_TIMEOUT);

            connectionTimeout = DatabaseHelper.getObjectAsInteger(dwdDataSource, connectionTimeoutAttribute);
            readTimeout = DatabaseHelper.getObjectAsInteger(dwdDataSource, readTimeoutAttribute);

        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JEVisException, IOException {
        List<InputStream> answerList = new ArrayList<InputStream>();

        return answerList;
    }

    private void initFTPClient() throws IOException {

        ftpClient = new FTPClient();

        ftpClient.connect("opendata.dwd.de");

        if (!ftpClient.login("Anonymous", "anonymous")) {
            logger.error("No Login possible");
        }

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setBufferSize(1024000);

        if (connectionTimeout != null) {
            ftpClient.setConnectTimeout(connectionTimeout * 1000);
        }
        if (readTimeout != null) {
            ftpClient.setDataTimeout(Duration.ofMillis(readTimeout * 1000));
        }

        ftpClient.setUseEPSVwithIPv4(false);
        ftpClient.enterLocalPassiveMode();

        ftpClient.changeWorkingDirectory(initialPath);

    }

    private void initializeChannelObjects(JEVisObject dwdServer) {
        try {
            JEVisClass channelClass = dwdServer.getDataSource().getJEVisClass(DataCollectorTypes.Channel.DWDChannel.NAME);

            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channels = CommonMethods.getChildrenRecursive(dwdServer, channelClass);
            logger.info("Found {} channel objects", channels.size());

            channels.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {

                    try {
                        dwdServer.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for object {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }

                    DWDChannel dwdChannel = new DWDChannel(channelObject);
                    this.channels.add(dwdChannel);

                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
