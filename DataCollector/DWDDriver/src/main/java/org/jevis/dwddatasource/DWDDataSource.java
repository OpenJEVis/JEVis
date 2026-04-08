package org.jevis.dwddatasource;

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

    private final String initialPath = "climate_environment/CDC/observations_germany/climate/";
    private final DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();
    private final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC();
    private final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("yyyyMM").withZoneUTC();
    private final DateTimeFormatter yearFormatter = DateTimeFormat.forPattern("yyyy").withZoneUTC();
    private final List<DWDChannel> channels = new ArrayList<>();
    private Importer importer;
    private ArrayList<Result> result;
    private FTPClient ftpClient;
    private Integer connectionTimeout;
    private Integer readTimeout;

    @Override
    public void parse(List<InputStream> input) {
    }

    @Override
    public void run() {
        logger.info("Run DWD DataSource with {} channel(s)", channels.size());
        List<String> failedChannels = new ArrayList<>();

        for (DWDChannel channel : channels) {
            logger.info("[{}:{}] Processing channel", channel.getName(), channel.getId());
            try {
                result = new ArrayList<>();

                Station station = new Station();
                station.setId(channel.getId());

                String aggPath = channel.getAggregation().getValue().toLowerCase();
                String attrPath = channel.getAttribute().toString().toLowerCase();
                List<String> pathList = new ArrayList<>();
                pathList.add("/" + initialPath + aggPath + "/" + attrPath + "/historical/");
                pathList.add("/" + initialPath + aggPath + "/" + attrPath + "/recent/");
                pathList.add("/" + initialPath + aggPath + "/" + attrPath + "/now/");
                station.getIntervalPath().put(channel.getAttribute(), pathList);

                DateTime firstDate = channel.getLastReadout();
                DateTime lastDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                logger.info("[{}:{}] Requesting data from {} to {}", channel.getName(), channel.getId(), firstDate, lastDate);

                StationData stationData = loadData(station, firstDate, lastDate);
                logger.info("[{}:{}] Received {} data point(s) from FTP", channel.getName(), channel.getId(), stationData.getData().size());

                if (!stationData.getData().isEmpty()) {
                    stationData.getData().forEach((dateTime, stringStringMap) -> {
                        if (dateTime.equals(firstDate) || (dateTime.isAfter(firstDate) && dateTime.isBefore(lastDate)) || dateTime.equals(lastDate)) {
                            String s = stringStringMap.get(channel.getDataName());
                            try {
                                Result r = new Result(channel.getTarget(), s, dateTime);
                                if (!String.valueOf(r.getValue()).equals(String.valueOf(-999))) {
                                    this.result.add(r);
                                }
                            } catch (Exception e) {
                                logger.error("[{}:{}] Could not create Result for datetime {}", channel.getName(), channel.getId(), dateTime, e);
                            }
                        }
                    });
                    logger.info("[{}:{}] Parsed {} valid result(s) after filtering", channel.getName(), channel.getId(), result.size());
                } else {
                    logger.info("[{}:{}] No data received for the requested time range", channel.getName(), channel.getId());
                }

                if (!result.isEmpty()) {
                    logger.info("[{}:{}] Importing {} result(s)", channel.getName(), channel.getId(), result.size());
                    JEVisImporterAdapter.importResults(result, importer, channel.getObject());
                    logger.info("[{}:{}] Import complete", channel.getName(), channel.getId());
                } else {
                    logger.info("[{}:{}] No results to import", channel.getName(), channel.getId());
                }

            } catch (ParseException ex) {
                logger.error("[{}:{}] Parse error: {}", channel.getName(), channel.getId(), ex.getMessage());
                logger.debug("[{}:{}] Parse error details", channel.getName(), channel.getId(), ex);
                failedChannels.add(channel.getName() + " (ParseException: " + ex.getMessage() + ")");
            } catch (Exception ex) {
                logger.error("[{}:{}] Unexpected error", channel.getName(), channel.getId(), ex);
                failedChannels.add(channel.getName() + " (" + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")");
            }
        }

        if (!failedChannels.isEmpty()) {
            throw new RuntimeException("Failed channel(s): " + String.join("; ", failedChannels));
        }

        logger.info("DWD DataSource run complete");
    }

    private StationData loadData(Station selectedStation, DateTime firstDate, DateTime lastDate) throws IOException {
        StationData stationData = new StationData();

        StringBuilder idString = new StringBuilder(String.valueOf(selectedStation.getId()));
        for (int i = idString.length(); i < 5; i++) {
            idString.insert(0, "0");
        }
        logger.debug("Loading data for station id={}", idString);

        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains("_" + idString + "_"));

        stationData.setName(selectedStation.getName());
        stationData.setId(selectedStation.getId());
        Map<DateTime, Map<String, String>> dataMap = new HashMap<>();

        for (Map.Entry<Attribute, List<String>> stationPathList : selectedStation.getIntervalPath().entrySet()) {
            for (String stationPath : stationPathList.getValue()) {
                FTPFile[] ftpFiles = ftpClient.listFiles(stationPath, filter);
                logger.debug("FTP path {}: {} file(s) found", stationPath, ftpFiles.length);

                for (FTPFile ftpFile : ftpFiles) {
                    if (!stationPath.endsWith("recent/") && !stationPath.endsWith("now/")) {
                        String fileName = ftpFile.getName();
                        fileName = fileName.substring(fileName.indexOf(idString.toString()) + idString.length() + 1);

                        try {
                            DateTime beginningOfFile = new DateTime(Integer.parseInt(fileName.substring(0, 4)), Integer.parseInt(fileName.substring(4, 6)), Integer.parseInt(fileName.substring(6, 8)), 0, 0);
                            DateTime endOfFile = new DateTime(Integer.parseInt(fileName.substring(9, 13)), Integer.parseInt(fileName.substring(13, 15)), Integer.parseInt(fileName.substring(15, 17)), 23, 59, 59, 999);

                            Interval interval = new Interval(beginningOfFile, endOfFile);
                            if (!interval.contains(firstDate) && !firstDate.isBefore(beginningOfFile)) {
                                logger.debug("Skipping historical file {} (range {} - {} does not overlap)", ftpFile.getName(), beginningOfFile, endOfFile);
                                continue;
                            }
                        } catch (Exception e) {
                            logger.warn("Could not parse date range from filename {}, including it anyway", ftpFile.getName(), e);
                        }
                    }

                    logger.info("Downloading {}", ftpFile.getName());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    boolean retrieved = ftpClient.retrieveFile(stationPath + ftpFile.getName(), out);
                    if (!retrieved) {
                        logger.warn("FTP retrieveFile returned false for {}", ftpFile.getName());
                        continue;
                    }
                    logger.debug("Downloaded {} ({} bytes)", ftpFile.getName(), out.size());

                    InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
                    ZipInputStream zipInputStream = new ZipInputStream(inputStream);

                    ZipEntry entry;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        if (!entry.getName().contains("produkt")) {
                            continue;
                        }
                        logger.debug("Parsing ZIP entry {}", entry.getName());
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(zipInputStream), 1024);
                            List<String> header = new ArrayList<>();
                            String l;
                            long lineNo = 0;
                            int parsedCount = 0;
                            while ((l = br.readLine()) != null) {
                                String[] values = l.split(";");
                                if (lineNo == 0) {
                                    header.addAll(Arrays.asList(values).subList(2, values.length - 1));
                                } else {
                                    Map<String, String> columnMap = new HashMap<>();
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
                                                parsedCount++;
                                            }
                                        } else {
                                            logger.warn("Unknown date format for value '{}' in {}", dt, entry.getName());
                                        }
                                    } catch (Exception e) {
                                        logger.error("{} - Could not parse line: {}", entry.getName(), l, e);
                                    }
                                }
                                lineNo++;
                            }
                            logger.debug("ZIP entry {}: {} record(s) in time range", entry.getName(), parsedCount);
                        } catch (Exception e) {
                            logger.error("Error reading ZIP entry {}", entry.getName(), e);
                        }
                    }
                }
            }
        }

        stationData.setData(dataMap);
        return stationData;
    }

    private @Nullable DateTimeFormatter getDateTimeFormatter(String dt) {
        if (dt.length() == 12) return minuteFormatter;
        if (dt.length() == 10) return hourFormatter;
        if (dt.length() == 8)  return dayFormatter;
        if (dt.length() == 6)  return monthFormatter;
        if (dt.length() == 4)  return yearFormatter;
        return null;
    }

    @Override
    public void importResult() {
        importer.importResult(result);
    }

    @Override
    public void initialize(JEVisObject dwdDataSource) {
        logger.info("Initializing DWD DataSource: {}:{}", dwdDataSource.getName(), dwdDataSource.getID());
        initializeAttributes(dwdDataSource);
        initializeChannelObjects(dwdDataSource);

        try {
            initFTPClient();
            logger.info("FTP connection to opendata.dwd.de established");
        } catch (IOException e) {
            throw new RuntimeException("FTP connection to opendata.dwd.de failed: " + e.getMessage(), e);
        }

        importer = ImporterFactory.getImporter(dwdDataSource);
        importer.initialize(dwdDataSource);
        logger.info("DWD DataSource initialized with {} channel(s)", channels.size());
    }

    private void initializeAttributes(JEVisObject dwdDataSource) {
        try {
            JEVisType readTimeoutAttribute = dwdDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.DWDServer.READ_TIMEOUT);
            JEVisType connectionTimeoutAttribute = dwdDataSource.getJEVisClass().getType(DataCollectorTypes.DataSource.DataServer.DWDServer.CONNECTION_TIMEOUT);
            connectionTimeout = DatabaseHelper.getObjectAsInteger(dwdDataSource, connectionTimeoutAttribute);
            readTimeout = DatabaseHelper.getObjectAsInteger(dwdDataSource, readTimeoutAttribute);
            logger.debug("Timeouts: connect={}s read={}s", connectionTimeout, readTimeout);
        } catch (Exception e) {
            logger.error("Could not read DWD server attributes", e);
        }
    }

    @Override
    public List<InputStream> sendSampleRequest(JEVisObject channel) throws JEVisException, IOException {
        return new ArrayList<>();
    }

    private void initFTPClient() throws IOException {
        ftpClient = new FTPClient();
        logger.debug("Connecting to opendata.dwd.de");
        ftpClient.connect("opendata.dwd.de");

        if (!ftpClient.login("Anonymous", "anonymous")) {
            throw new IOException("FTP login failed for opendata.dwd.de (reply: " + ftpClient.getReplyString().trim() + ")");
        }

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setBufferSize(1024000);

        if (connectionTimeout != null) {
            ftpClient.setConnectTimeout(connectionTimeout * 1000);
        }
        if (readTimeout != null) {
            ftpClient.setDataTimeout(Duration.ofMillis(readTimeout * 1000L));
        }

        ftpClient.setUseEPSVwithIPv4(false);
        ftpClient.enterLocalPassiveMode();
        ftpClient.changeWorkingDirectory(initialPath);
    }

    private void initializeChannelObjects(JEVisObject dwdServer) {
        try {
            JEVisClass channelClass = dwdServer.getDataSource().getJEVisClass(DataCollectorTypes.Channel.DWDChannel.NAME);
            List<Long> counterCheckForErrorInAPI = new ArrayList<>();
            List<JEVisObject> channelObjects = CommonMethods.getChildrenRecursive(dwdServer, channelClass);
            logger.info("Found {} channel object(s)", channelObjects.size());

            channelObjects.forEach(channelObject -> {
                if (!counterCheckForErrorInAPI.contains(channelObject.getID())) {
                    try {
                        dwdServer.getDataSource().reloadObject(channelObject);
                    } catch (Exception e) {
                        logger.error("Could not reload attributes for channel {}:{}", channelObject.getName(), channelObject.getID(), e);
                    }
                    DWDChannel dwdChannel = new DWDChannel(channelObject);
                    this.channels.add(dwdChannel);
                    logger.debug("Loaded channel {}:{}", dwdChannel.getName(), dwdChannel.getId());
                    counterCheckForErrorInAPI.add(channelObject.getID());
                }
            });
        } catch (Exception ex) {
            logger.error("Could not load channel objects", ex);
        }
    }
}
