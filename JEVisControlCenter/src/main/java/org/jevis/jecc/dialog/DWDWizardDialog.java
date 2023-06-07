package org.jevis.jecc.dialog;

import com.jfoenix.controls.JFXDialog;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.datasource.Station;
import org.jevis.commons.datasource.StationData;
import org.jevis.commons.driver.dwd.Aggregation;
import org.jevis.commons.driver.dwd.Attribute;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.tools.DisabledItemsComboBox;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DWDWizardDialog {

    private static final Logger logger = LogManager.getLogger(DWDWizardDialog.class);
    private final MFXTextField stationFilter = new MFXTextField();
    private final ListView<Station> listViewStations = new ListView<>();
    private final String initialPath = "climate_environment/CDC/observations_germany/climate/";
    private final Label aggregationLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.aggregation"));
    private final DisabledItemsComboBox<Aggregation> aggregationBox = new DisabledItemsComboBox<>();
    private final Label attributeLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.attribute"));
    private final ListView<Attribute> listViewAttribute = new ListView<>();
    private final Label dataLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.data"));
    private final ListView<String> listViewData = new ListView<>();
    private final Label targetLabel = new Label(I18n.getInstance().getString("dialog.target.data.title"));
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();
    private final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC();
    private final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("yyyyMM").withZoneUTC();
    private final DateTimeFormatter yearFormatter = DateTimeFormat.forPattern("yyyy").withZoneUTC();
    private JEVisDataSource ds;
    private FilteredList<Station> filteredStations;
    private JEVisAttribute target;
    private DateTime firstDate = DateTime.now();
    private DateTime lastDate = new DateTime("1970-01-01T00:00:00Z");

    public DWDWizardDialog(JEVisObject newObject) {
        try {
            ds = newObject.getDataSource();
        } catch (JEVisException e) {
            logger.error(e);
        }

        JFXDialog dialog = new JFXDialog();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(15));
        gridPane.setHgap(6);
        gridPane.setVgap(9);

        aggregationBox.getItems().addAll(Aggregation.values());
        aggregationBox.getSelectionModel().selectFirst();
        listViewAttribute.getItems().addAll(Attribute.values());
        listViewAttribute.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        int row = 0;
        gridPane.add(aggregationLabel, 0, row);
        gridPane.add(aggregationBox, 1, row);
        row++;

        gridPane.add(attributeLabel, 0, row);
        gridPane.add(listViewAttribute, 1, row);
        row++;

        MFXButton loadStationsButton = new MFXButton("Load Stations");
        gridPane.add(loadStationsButton, 0, row);
        row++;

        gridPane.add(stationFilter, 0, row, 1, 2);
        row++;

        listViewStations.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewStations.setCellFactory(param -> new ListCell<Station>() {
            @Override
            protected void updateItem(Station obj, boolean empty) {
                super.updateItem(obj, empty);
                if (empty || obj == null || obj.getName() == null) {
                    setText("");
                } else {
                    setText(obj.getName());
                }
            }
        });
        gridPane.add(listViewStations, 0, row, 12, 2);

        MFXButton loadDataButton = new MFXButton("Load Data for station");
        loadDataButton.setDisable(true);

        gridPane.add(loadDataButton, 1, row);
        row++;

        listViewData.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        gridPane.add(dataLabel, 0, row);
        gridPane.add(listViewData, 1, row);
        row++;

        MFXButton targetButton = new MFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                ControlCenter.getImage("folders_explorer.png", 18, 18));
        targetButton.wrapTextProperty().setValue(true);
        targetButton.setDisable(true);

        gridPane.add(targetLabel, 0, row);
        gridPane.add(targetButton, 1, row);
        row++;

        List<Station> stations = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();
        loadStationsButton.setOnAction(actionEvent -> loadStations(loadDataButton, stations, ftpClient));
        loadDataButton.setOnAction(actionEvent -> loadData(listViewStations, targetButton, ftpClient));

        dialog.show();
    }

    private void loadStations(MFXButton loadDataButton, List<Station> stations, FTPClient ftpClient) {
        try {
            ftpClient.connect("opendata.dwd.de");

            if (!ftpClient.login("Anonymous", "anonymous")) {
                logger.error("No Login possible");
            }

            ftpClient.setBufferSize(1024000);

            ftpClient.setUseEPSVwithIPv4(false);
            ftpClient.enterLocalPassiveMode();

            ftpClient.changeWorkingDirectory(initialPath);

            Map<Attribute, List<String>> stationFiles = new HashMap<>();
            findAllStationFiles(ftpClient, stationFiles);

            ftpClient.changeWorkingDirectory(initialPath);

            for (Map.Entry<Attribute, List<String>> stationPathList : stationFiles.entrySet()) {
                for (String stationPath : stationPathList.getValue()) {
                    Attribute attribute = stationPathList.getKey();

                    stationPath = stationPath.substring(0, stationPath.lastIndexOf("/") + 1);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    logger.info("FTPQuery " + stationPath);
                    boolean retrieveFile = ftpClient.retrieveFile(stationPath, out);
                    logger.info("Request status: " + retrieveFile);

                    InputStream inputStream = new ByteArrayInputStream(out.toByteArray());

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        while (reader.ready()) {
                            Station station = new Station();

                            String line = reader.readLine();

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

                                List<String> stationFileList = new ArrayList<>();
                                stationFileList.add(stationPath);
                                station.getIntervalPath().put(attribute, stationFileList);

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

                                        //                                    oldStation.getIntervalPath().add(stationPath);
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

                    AlphanumComparator alphanumComparator = new AlphanumComparator();
                    stations.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
                    filteredStations = new FilteredList<>(FXCollections.observableArrayList(stations), station -> true);

                    Platform.runLater(() -> {
                        listViewStations.setItems(filteredStations);
                        loadDataButton.setDisable(false);
                    });
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }

        stationFilter.textProperty().addListener(obs -> {
            String filter = stationFilter.getText();
            if (filter == null || filter.length() == 0) {
                filteredStations.setPredicate(s -> true);
            } else {
                if (filter.contains(" ")) {
                    String[] result = filter.split(" ");
                    filteredStations.setPredicate(s -> {
                        boolean match = false;
                        String string = s.getName().toLowerCase();
                        for (String value : result) {
                            String subString = value.toLowerCase();
                            if (!string.contains(subString))
                                return false;
                            else match = true;
                        }
                        return match;
                    });
                } else {
                    filteredStations.setPredicate(s -> s.getName().toLowerCase().contains(filter.toLowerCase()));
                }
            }
        });
    }

    private void loadData(ListView<Station> listViewStations, MFXButton targetButton, FTPClient ftpClient) {
        try {
            List<String> allDataNames = new ArrayList<>();
            for (Station selectedStation : listViewStations.getSelectionModel().getSelectedItems()) {
                StringBuilder idString = new StringBuilder(String.valueOf(selectedStation.getId()));
                for (int i = idString.length(); i < 5; i++) {
                    idString.insert(0, "0");
                }
                FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(idString.toString()));

                StationData stationData = new StationData();
                stationData.setName(selectedStation.getName());
                stationData.setId(selectedStation.getId());
                Map<DateTime, Map<String, String>> dataMap = new HashMap<>();

                for (Map.Entry<Attribute, List<String>> stationPath : selectedStation.getIntervalPath().entrySet()) {
                    for (String stationFile : stationPath.getValue()) {
                        for (FTPFile ftpFile : ftpClient.listFiles(stationFile, filter)) {
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
                                                } else
                                                    logger.warn("Could not determine date format");
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
//                selectedStation.getStationData().put(attribute, stationData);
            }

            Platform.runLater(() -> {
                listViewData.setItems(FXCollections.observableArrayList(allDataNames));
                targetButton.setDisable(false);
            });
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void findAllStationFiles(FTPClient ftpClient, Map<Attribute, List<String>> stationFiles) throws IOException {

        String workingDirectory = ftpClient.printWorkingDirectory();
        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(".txt"));
        for (FTPFile ftpFile : ftpClient.listFiles(workingDirectory, filter)) {
            if (ftpFile.isFile() && ftpFile.getName().contains(".txt")) {
                Attribute attribute = Arrays.stream(Attribute.values()).filter(att -> workingDirectory.toLowerCase().contains(att.toString().toLowerCase())).findFirst().orElse(null);
                if (attribute != null) {
                    if (stationFiles.get(attribute) != null) {
                        stationFiles.get(attribute).add(ftpClient.printWorkingDirectory() + "/" + ftpFile.getName());
                    } else {
                        List<String> stationFilesList = new ArrayList<>();
                        stationFilesList.add(ftpClient.printWorkingDirectory() + "/" + ftpFile.getName());
                        stationFiles.put(attribute, stationFilesList);
                    }
                }
            }
        }

        String aggregationFilter = aggregationBox.getSelectionModel().getSelectedItem().toString();
        List<String> attributeFilter = new ArrayList<>();
        for (Attribute attribute : listViewAttribute.getSelectionModel().getSelectedItems()) {
            if (attribute == Attribute.ALL) {
                attributeFilter.clear();
                for (Attribute att : Attribute.values()) {
                    if (att != Attribute.ALL) {
                        attributeFilter.add(att.toString().toLowerCase());
                    }
                }
                break;
            } else {
                attributeFilter.add(attribute.toString().toLowerCase());
            }
        }

        for (FTPFile ftpFile : ftpClient.listDirectories()) {
            if (ftpFile.isDirectory()
                    && (ftpFile.getName().contains(aggregationFilter)
                    || attributeFilter.contains(ftpFile.getName()))
                    || ftpFile.getName().contains("historical") || ftpFile.getName().contains("now") || ftpFile.getName().contains("recent")) {
                ftpClient.changeWorkingDirectory(workingDirectory + "/" + ftpFile.getName());
                findAllStationFiles(ftpClient, stationFiles);
            }
        }
    }
}
