package org.jevis.jecc.tool.dwdbrowser;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datasource.Station;
import org.jevis.commons.datasource.StationData;
import org.jevis.commons.driver.dwd.Aggregation;
import org.jevis.commons.driver.dwd.Attribute;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.methods.DataMethods;
import org.jevis.jecc.application.tools.DisabledItemsComboBox;
import org.jevis.jecc.dialog.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DWDBrowser extends Dialog {
    private static final Logger logger = LogManager.getLogger(DWDBrowser.class);

    private final String initialPath = "climate_environment/CDC/observations_germany/climate/";
    private final Label aggregationLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.aggregation"));
    private final DisabledItemsComboBox<Aggregation> aggregationBox = new DisabledItemsComboBox<>();
    private final Label attributeLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.attribute"));
    private final DisabledItemsComboBox<Attribute> attributeBox = new DisabledItemsComboBox<>();
    private final Label dataLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.data"));
    private final DisabledItemsComboBox<String> dataBox = new DisabledItemsComboBox<>();
    private final Label targetLabel = new Label(I18n.getInstance().getString("dialog.target.data.title"));
    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter minuteFormatter = DateTimeFormat.forPattern("yyyyMMddHHmm").withZoneUTC();
    private final DateTimeFormatter hourFormatter = DateTimeFormat.forPattern("yyyyMMddHH").withZoneUTC();
    private final DateTimeFormatter dayFormatter = DateTimeFormat.forPattern("yyyyMMdd").withZoneUTC();
    private final DateTimeFormatter monthFormatter = DateTimeFormat.forPattern("yyyyMM").withZoneUTC();
    private final DateTimeFormatter yearFormatter = DateTimeFormat.forPattern("yyyy").withZoneUTC();
    private final JEVisObject targetObject;
    private final MFXDatePicker startDatePicker = new MFXDatePicker();
    private final MFXDatePicker endDatePicker = new MFXDatePicker();
    private FilteredList<Station> filteredStations;
    private JEVisAttribute target;
    private DateTime firstDate = DateTime.now();
    private DateTime lastDate = new DateTime("1970-01-01T00:00:00Z");

    public DWDBrowser(JEVisDataSource ds, JEVisObject targetObject) {
        this.targetObject = targetObject;
        setTitle(I18n.getInstance().getString("DWD Browser"));
        setHeaderText(I18n.getInstance().getString("Import data"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        startDatePicker.setPrefWidth(120d);
        endDatePicker.setPrefWidth(120d);
        setCellFactory();

        ButtonType okType = new ButtonType(I18n.getInstance().getString("jevistree.menu.import"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
//        ButtonType deleteType = new ButtonType(I18n.getInstance().getString("jevistree.menu.delete"), ButtonBar.ButtonData.OTHER);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

//        Button deleteButton = (Button) this.getDialogPane().lookupButton(deleteType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);
        okButton.setDisable(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(15));
        gridPane.setHgap(6);
        gridPane.setVgap(9);

        aggregationBox.getItems().addAll(Aggregation.values());
        aggregationBox.getSelectionModel().selectFirst();
        attributeBox.getItems().addAll(Attribute.values());
        attributeBox.getSelectionModel().selectFirst();

        int row = 0;
        gridPane.add(aggregationLabel, 0, row);
        gridPane.add(aggregationBox, 1, row);
        row++;

        gridPane.add(attributeLabel, 0, row);
        gridPane.add(attributeBox, 1, row);
        row++;

        MFXButton loadStationsButton = new MFXButton("Load Stations");

        gridPane.add(loadStationsButton, 0, row);
        row++;

        MFXTextField stationFilter = new MFXTextField();
        stationFilter.setFloatMode(FloatMode.DISABLED);
        MFXComboBox<Station> stationBox = new MFXComboBox<>();
        stationBox.setFloatMode(FloatMode.DISABLED);

        stationBox.setConverter(new StringConverter<Station>() {
            @Override
            public String toString(Station object) {
                if (object != null) {
                    return object.getName();
                } else return "";
            }

            @Override
            public Station fromString(String string) {
                return stationBox.getItems().stream().filter(station -> station.getName().equals(string)).findFirst().orElse(null);
            }
        });

        gridPane.add(stationFilter, 0, row);
        gridPane.add(stationBox, 1, row);
        row++;

        MFXButton loadDataButton = new MFXButton("Load Data for station");
        loadDataButton.setDisable(true);

        gridPane.add(loadDataButton, 1, row);
        row++;

        gridPane.add(dataLabel, 0, row);
        gridPane.add(dataBox, 1, row);
        row++;

        MFXButton targetButton = new MFXButton(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                ControlCenter.getImage("folders_explorer.png", 18, 18));
        targetButton.wrapTextProperty().setValue(true);
        targetButton.setDisable(true);

        gridPane.add(targetLabel, 0, row);
        gridPane.add(targetButton, 1, row);
        row++;

        if (targetObject != null) {
            try {
                target = targetObject.getAttribute("Value");
                targetButton.setText(DataMethods.getObjectName(targetObject));
            } catch (JEVisException e) {
                logger.error(e);
            }
        }

        MFXTextField messageField = new MFXTextField();
        messageField.setFloatMode(FloatMode.DISABLED);
        gridPane.add(messageField, 0, row, 2, 1);
        row++;

        gridPane.add(startDatePicker, 0, row, 1, 1);
        gridPane.add(endDatePicker, 1, row, 1, 1);

        List<String> stationFiles = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();
        loadStationsButton.setOnAction(actionEvent -> loadStations(stationBox, loadDataButton, stationFiles, ftpClient));

        StationData stationData = new StationData();
        List<String> allDataNames = new ArrayList<>();

        loadDataButton.setOnAction(actionEvent -> loadData(stationBox, targetButton, okButton, messageField, ftpClient, stationData, allDataNames));

        targetButton.setOnAction(actionEvent -> {
            try {
                List<JEVisClass> classes = new ArrayList<>();

                List<UserSelection> openList = new ArrayList<>();

                TreeSelectionDialog treeSelectionDialog = new TreeSelectionDialog(ds, classes, SelectionMode.SINGLE, openList, true);

                treeSelectionDialog.setOnCloseRequest(event -> {
                    try {
                        if (treeSelectionDialog.getResponse() == Response.OK) {
                            List<UserSelection> selections = treeSelectionDialog.getUserSelection();
                            for (UserSelection us : selections) {
                                if (us.getSelectedAttribute() != null) {
                                    target = us.getSelectedAttribute();
                                } else {
                                    target = us.getSelectedObject().getAttribute("Value");
                                }
                                Platform.runLater(() -> {
                                    targetButton.setText(DataMethods.getObjectName(target.getObject()));
                                    okButton.setDisable(false);
                                });
                            }
                        }
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                });
                treeSelectionDialog.show();
            } catch (Exception ex) {
                logger.catching(ex);
            }
        });

        okButton.setOnAction(actionEvent -> {
            List<JEVisSample> samples = new ArrayList<>();
            String selectedData = dataBox.getSelectionModel().getSelectedItem();

            Task<Void> importData = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        stationData.getData().forEach((dateTime, stringStringMap) -> {

                            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                                firstDate = toDateTime(startDatePicker.getValue());
                                lastDate = toDateTime(endDatePicker.getValue()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999);
                            }

                            if (dateTime.equals(firstDate) || (dateTime.isAfter(firstDate) && dateTime.isBefore(lastDate)) || dateTime.equals(lastDate)) {
                                String s = stringStringMap.get(selectedData);
                                try {
                                    JEVisSample sample = target.buildSample(dateTime, s);
                                    samples.add(sample);
                                } catch (Exception e) {
                                    logger.error("Could not create JEVisSample", e);
                                }
                            }
                        });

                        try {
                            target.addSamples(samples);
                        } catch (Exception e) {
                            logger.error("Could not commit samples", e);
                        }

                        succeeded();
                    } catch (Exception ex) {
                        failed();
                    } finally {
                        done();
                    }
                    return null;
                }
            };

            ControlCenter.getStatusBar().addTask("DWD Import", importData, ControlCenter.getImage("save.gif"), true);
        });

        cancelButton.setOnAction(actionEvent -> close());

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

        getDialogPane().setContent(gridPane);


    }

    private void loadData(MFXComboBox<Station> stationBox, MFXButton targetButton, Button okButton, MFXTextField messageField, FTPClient ftpClient, StationData stationData, List<String> allDataNames) {
        try {
            Station selectedStation = stationBox.getSelectionModel().getSelectedItem();
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
                                            } else
                                                Platform.runLater(() -> messageField.setText("Could not determine date format"));
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
            Platform.runLater(() -> {
                dataBox.setItems(FXCollections.observableArrayList(allDataNames));
                dataBox.getSelectionModel().selectFirst();
                messageField.setText(firstDate.toString() + " - " + lastDate.toString());
                targetButton.setDisable(false);
                if (targetObject != null) {
                    okButton.setDisable(false);
                }
            });
            setCellFactory();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void loadStations(MFXComboBox<Station> stationBox, MFXButton loadDataButton, List<String> stationFiles, FTPClient ftpClient) {
        try {
            ftpClient.connect("opendata.dwd.de");

            if (!ftpClient.login("Anonymous", "anonymous")) {
                logger.error("No Login possible");
            }

            ftpClient.setBufferSize(1024000);

            ftpClient.setUseEPSVwithIPv4(false);
            ftpClient.enterLocalPassiveMode();

            ftpClient.changeWorkingDirectory(initialPath);

            findAllStationFiles(ftpClient, stationFiles);

            ftpClient.changeWorkingDirectory(initialPath);

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
                            station.getIntervalPath().put(attributeBox.getSelectionModel().getSelectedItem(), stationPathList);

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

                                    if (oldStation.getIntervalPath().get(attributeBox.getSelectionModel().getSelectedItem()) != null) {
                                        oldStation.getIntervalPath().get(attributeBox.getSelectionModel().getSelectedItem()).add(stationPath);
                                    } else {
                                        List<String> stationPathList = new ArrayList<>();
                                        stationPathList.add(stationPath);
                                        oldStation.getIntervalPath().put(attributeBox.getSelectionModel().getSelectedItem(), stationPathList);
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

                AlphanumComparator alphanumComparator = new AlphanumComparator();
                stations.sort((o1, o2) -> alphanumComparator.compare(o1.getName(), o2.getName()));
                filteredStations = new FilteredList<>(FXCollections.observableArrayList(stations), station -> true);

                Platform.runLater(() -> {
                    stationBox.setItems(filteredStations);
                    stationBox.getSelectionModel().selectFirst();
                    loadDataButton.setDisable(false);
                });
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void findAllStationFiles(FTPClient ftpClient, List<String> stationFiles) throws IOException {

        String workingDirectory = ftpClient.printWorkingDirectory();
        FTPFileFilter filter = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(".txt"));
        for (FTPFile ftpFile : ftpClient.listFiles(workingDirectory, filter)) {
            if (ftpFile.isFile() && ftpFile.getName().contains(".txt")) {
                stationFiles.add(ftpClient.printWorkingDirectory() + "/" + ftpFile.getName());
            }
        }

        String aggregationFilter = aggregationBox.getSelectionModel().getSelectedItem().toString();
        Attribute attributeValue = attributeBox.getSelectionModel().getSelectedItem();
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
                    && (ftpFile.getName().contains(aggregationFilter)
                    || attributeFilter.contains(ftpFile.getName()))
                    || ftpFile.getName().contains("historical") || ftpFile.getName().contains("now") || ftpFile.getName().contains("recent")) {
                ftpClient.changeWorkingDirectory(workingDirectory + "/" + ftpFile.getName());
                findAllStationFiles(ftpClient, stationFiles);
            }
        }
    }

    public void setCellFactory() {
        Callback<DatePicker, DateCell> dateCellCallback = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

                        if (firstDate != null && item.isBefore(toLocalDate(firstDate))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }

                        if (lastDate != null && item.isAfter(toLocalDate(lastDate))) {
                            setDisable(true);
                            setStyle("-fx-background-color: #ffc0cb;");
                        }
                    }
                };
            }
        };
//TODO JFX17
        // startDatePicker.setDayCellFactory(dateCellCallback);
        // endDatePicker.setDayCellFactory(dateCellCallback);
    }

    private DateTime toDateTime(LocalDate localDate) {
        return new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }
}
