package org.jevis.dwddatasource;

import javafx.collections.FXCollections;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.http.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.datasource.Station;
import org.jevis.commons.datasource.StationData;
import org.jevis.commons.driver.*;
import org.jevis.commons.driver.dwd.Aggregation;
import org.jevis.commons.driver.dwd.Attribute;
import org.jevis.commons.utils.CommonMethods;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
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
    private List<DWDChannel> channels = new ArrayList<>();
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

                List<String> stationFiles = new ArrayList<>();
                findAllStationFiles(ftpClient, stationFiles, channel.getAggregation(), channel.getAttribute());

                Station station = loadStation(channel, stationFiles, channel.getAttribute());

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
//                    this.importResult();
//
//                    DataSourceHelper.setLastReadout(channel, _importer.getLatestDatapoint());
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
                ex.printStackTrace();
            }
        }
    }

    private StationData loadData(Station selectedStation, DateTime firstDate, DateTime lastDate) {
        StationData stationData = new StationData();
        try {
            List<String> allDataNames = new ArrayList<>();
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
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        logger.info("FTPQuery " + ftpFile.getName());
                        boolean retrieveFile = ftpClient.retrieveFile(stationPath + ftpFile.getName(), out);
                        logger.info("retrieved file " + ftpFile.getName());

                        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
                        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

                        ZipEntry entry;
                        while ((entry = zipInputStream.getNextEntry()) != null) {
                            if (entry.getName().contains("produkt")) {
                                Scanner sc = new Scanner(zipInputStream);
                                int lineNo = 0;
                                List<String> dataNames = new ArrayList<>();

                                while (sc.hasNextLine()) {
                                    String[] split = sc.nextLine().split(";");

                                    Map<String, String> columnMap = new HashMap<>();

                                    for (int i = 2; i < split.length; i++) {
                                        if (lineNo == 0) {
                                            String dataName = split[i].trim();
                                            dataNames.add(dataName);
                                            if (!allDataNames.contains(dataName)) {
                                                allDataNames.add(dataName);
                                            }
                                        } else {
                                            columnMap.put(dataNames.get(i - 2), split[i]);
                                        }
                                    }

                                    if (lineNo > 0) {
                                        try {
                                            DateTimeFormatter dtf = null;
                                            if (split[1].length() == 12)
                                                dtf = minuteFormatter;
                                            else if (split[1].length() == 10)
                                                dtf = hourFormatter;
                                            else if (split[1].length() == 8)
                                                dtf = dayFormatter;
                                            else if (split[1].length() == 6)
                                                dtf = monthFormatter;
                                            else if (split[1].length() == 4)
                                                dtf = yearFormatter;

                                            if (dtf != null) {
                                                DateTime dateTime = dtf.parseDateTime(split[1]);
                                                dataMap.put(dateTime, columnMap);
                                                if (dateTime.isBefore(firstDate)) firstDate = dateTime;
                                                if (dateTime.isAfter(lastDate)) lastDate = dateTime;
                                            }
                                        } catch (Exception e) {
                                            logger.error("Could not create map for {}", split[1], e);
                                        }
                                    }
                                    lineNo++;
                                }
                                break;
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

    private Station loadStation(DWDChannel channel, List<String> stationFiles, Attribute attribute) throws IOException {
        List<Station> stations = new ArrayList<>();
        for (String s : stationFiles) {
            String stationPath = s.substring(0, s.lastIndexOf("/") + 1);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            logger.info("FTPQuery " + s);
            boolean retrieveFile = ftpClient.retrieveFile(s, out);
            logger.info("Request status: " + retrieveFile);

            InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            List<String> lines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                while (reader.ready()) {
                    Station station = new Station();

                    String line = reader.readLine();
                    lines.add(line);

                    if (line.startsWith("S") || line.startsWith("-")) continue;

                    try {
                        int indexOfFirstSpace = line.indexOf(" ");
                        Long id = Long.parseLong(line.substring(0, indexOfFirstSpace));
                        station.setId(id);
                        line = line.substring(indexOfFirstSpace).trim();

                        indexOfFirstSpace = line.indexOf(" ");

                        DateTime from = dateFormatter.parseDateTime(line.substring(0, indexOfFirstSpace));
                        station.setFrom(from);
                        line = line.substring(indexOfFirstSpace).trim();

                        indexOfFirstSpace = line.indexOf(" ");
                        DateTime to = dateFormatter.parseDateTime(line.substring(0, indexOfFirstSpace));
                        station.setTo(to);
                        line = line.substring(indexOfFirstSpace).trim();

                        List<String> stationPathList = new ArrayList<>();
                        stationPathList.add(stationPath);
                        station.getIntervalPath().put(attribute, stationPathList);

                        indexOfFirstSpace = line.indexOf(" ");
                        Long height = Long.parseLong(line.substring(0, indexOfFirstSpace));
                        station.setHeight(height);
                        line = line.substring(indexOfFirstSpace).trim();

                        indexOfFirstSpace = line.indexOf(" ");
                        Double geoWidth = Double.parseDouble(line.substring(0, indexOfFirstSpace));
                        station.setGeoWidth(geoWidth);
                        line = line.substring(indexOfFirstSpace).trim();

                        indexOfFirstSpace = line.indexOf(" ");
                        Double geoHeight = Double.parseDouble(line.substring(0, indexOfFirstSpace));
                        station.setGeoHeight(geoHeight);
                        line = line.substring(indexOfFirstSpace).trim();

                        indexOfFirstSpace = line.indexOf(" ");
                        String name = line.substring(0, indexOfFirstSpace);
                        station.setName(name);
                        line = line.substring(indexOfFirstSpace).trim();

                        String state = line;
                        station.setState(state);

                    } catch (Exception e) {
                        logger.error("Could not parse line {}", line, e);
                    }

                    if (!stations.contains(station)) {
                        stations.add(station);
                    } else {
                        for (Station oldStation : stations) {
                            if (oldStation.equals(station)) {
                                if (station.getFrom().isBefore(oldStation.getFrom())) {
                                    oldStation.setFrom(station.getFrom());
                                }

                                if (station.getTo().isAfter(oldStation.getTo())) {
                                    oldStation.setTo(station.getTo());
                                }

                                if (oldStation.getIntervalPath().get(attribute) != null) {
                                    oldStation.getIntervalPath().get(attribute).add(stationPath);
                                } else {
                                    List<String> stationPathList = new ArrayList<>();
                                    stationPathList.add(stationPath);
                                    oldStation.getIntervalPath().put(attribute, stationPathList);
                                }

                                break;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                logger.error("File not found", e);
            } catch (IOException e) {
                logger.error("IOException", e);
            }
        }

        for (Station station : stations) {
            if (channel.getId().equals(station.getId())) {
                return station;
            }
        }

        return null;
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

    private void findAllStationFiles(FTPClient ftpClient, List<String> stationFiles, Aggregation aggregationFilter, Attribute attributeValue) throws IOException {

        String workingDirectory = ftpClient.printWorkingDirectory();
        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(".txt"));
        for (FTPFile ftpFile : ftpClient.listFiles(workingDirectory, filter)) {
            if (ftpFile.isFile() && ftpFile.getName().contains(".txt")) {
                stationFiles.add(ftpClient.printWorkingDirectory() + "/" + ftpFile.getName());
            }
        }

        List<String> attributeFilter = new ArrayList<>();
        if (attributeValue == Attribute.ALL) {
            for (Attribute attribute : Attribute.values()) {
                if (attribute != Attribute.ALL) {
                    attributeFilter.add(attribute.toString().toLowerCase());
                }
            }
        } else {
            attributeFilter.add(attributeValue.toString().toLowerCase());
        }

        for (FTPFile ftpFile : ftpClient.listDirectories()) {
            if (ftpFile.isDirectory()
                    && (ftpFile.getName().contains(aggregationFilter.toString().toLowerCase())
                    || attributeFilter.contains(ftpFile.getName()))
                    || ftpFile.getName().contains("historical") || ftpFile.getName().contains("now") || ftpFile.getName().contains("recent")) {
                ftpClient.changeWorkingDirectory(workingDirectory + "/" + ftpFile.getName());
                findAllStationFiles(ftpClient, stationFiles, aggregationFilter, attributeValue);
            }
        }
    }

    private void initFTPClient() throws IOException {

        ftpClient = new FTPClient();

        ftpClient.connect("opendata.dwd.de");

        if (!ftpClient.login("Anonymous", "anonymous")) {
            logger.error("No Login possible");
        }

        ftpClient.setBufferSize(1024000);

        if (connectionTimeout != null) {
            ftpClient.setConnectTimeout(connectionTimeout * 1000);
        }
        if (readTimeout != null) {
            ftpClient.setDataTimeout(readTimeout * 1000);
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
            logger.info("Found " + channels.size() + " channel objects");

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

//            logger.info(channelDir.getName() + ":" + channelDir.getID() + " has " + this.channels.size() + " channels.");
        } catch (Exception ex) {
            logger.error(ex);
        }
    }
}
