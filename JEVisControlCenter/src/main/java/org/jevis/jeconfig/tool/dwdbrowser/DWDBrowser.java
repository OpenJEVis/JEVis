package org.jevis.jeconfig.tool.dwdbrowser;

import com.jfoenix.controls.*;
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
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.datasource.Station;
import org.jevis.commons.datasource.StationData;
import org.jevis.commons.driver.dwd.Aggregation;
import org.jevis.commons.driver.dwd.Attribute;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.application.tools.DisabledItemsComboBox;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.dialog.Response;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DWDBrowser extends Dialog<ButtonType> {
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
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private FilteredList<Station> filteredStations;
    private JEVisAttribute target;
    private DateTime firstDate = DateTime.now();
    private DateTime lastDate = new DateTime("1970-01-01T00:00:00Z");

    public DWDBrowser(JEVisDataSource ds, JEVisObject targetObject) {
        this.targetObject = targetObject;
        setTitle(I18n.getInstance().getString("plugin.object.dwd.title"));
        setHeaderText(I18n.getInstance().getString("plugin.object.dwd.subtitle"));
        setResizable(true);
        initOwner(JEConfig.getStage());
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

        JFXButton loadStationsButton = new JFXButton(I18n.getInstance().getString("plugin.object.dwd.button.loadstations"));

        gridPane.add(loadStationsButton, 0, row);
        row++;

        JFXTextField stationFilter = new JFXTextField();
        JFXComboBox<Station> stationBox = new JFXComboBox<>();
        StringConverter<Station> stationStringConverter = new StringConverter<Station>() {
            @Override
            public String toString(Station station) {
                return station.getName();
            }

            @Override
            public Station fromString(String s) {
                return filteredStations.stream().filter(station -> station.getName().equals(s)).findFirst().orElse(null);
            }
        };
        stationBox.setConverter(stationStringConverter);

        gridPane.add(stationFilter, 0, row);
        gridPane.add(stationBox, 1, row);
        row++;

        JFXButton loadDataButton = new JFXButton(I18n.getInstance().getString("plugin.object.dwd.button.loaddata"));
        loadDataButton.setDisable(true);


        gridPane.add(loadDataButton, 0, row);
        row++;

        JFXButton showDescriptionButton = new JFXButton("", JEConfig.getSVGImage(Icon.INFO, 12, 12));
        showDescriptionButton.setDisable(true);

        gridPane.add(dataLabel, 0, row);
        gridPane.add(dataBox, 1, row);
        gridPane.add(showDescriptionButton, 2, row);
        row++;

        JFXButton targetButton = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));
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

        Label messageLabel = new Label(I18n.getInstance().getString("plugin.object.dwd.label.message"));
        JFXTextField messageField = new JFXTextField();
        gridPane.add(messageLabel, 0, row, 1, 1);
        gridPane.add(messageField, 1, row, 2, 1);
        row++;

        Label importDataSelection = new Label(I18n.getInstance().getString("plugin.object.dwd.label.dataselection"));
        gridPane.add(importDataSelection, 0, row, 1, 1);
        row++;

        gridPane.add(startDatePicker, 0, row, 1, 1);
        gridPane.add(endDatePicker, 1, row, 1, 1);
        row++;

        JFXCheckBox createDataPoint = new JFXCheckBox(I18n.getInstance().getString("plugin.object.dwd.label.createdatapoint"));
        createDataPoint.setSelected(true);
        gridPane.add(createDataPoint, 0, row, 1, 1);

        List<String> stationFiles = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();
        loadStationsButton.setOnAction(actionEvent -> loadStations(stationBox, loadDataButton, stationFiles, ftpClient));

        StationData stationData = new StationData();
        List<String> allDataNames = new ArrayList<>();

        loadDataButton.setOnAction(actionEvent -> loadData(stationBox, targetButton, showDescriptionButton, okButton, messageField, ftpClient, stationData, allDataNames));

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

                        if (createDataPoint.isSelected() && target != null) {
                            try {
                                Station selectedStation = stationBox.getSelectionModel().getSelectedItem();
                                long stationId = selectedStation.getId();
                                Attribute selectedAttribute = attributeBox.getSelectionModel().getSelectedItem();
                                Aggregation selectedAggregation = aggregationBox.getSelectionModel().getSelectedItem();
                                String selectedDataName = dataBox.getSelectionModel().getSelectedItem();
                                SampleHandler sampleHandler = new SampleHandler();

                                JEVisClass dwdDataSourceClass = ds.getJEVisClass(JC.DataSource.DataServer.DwdServer.name);
                                JEVisClass dwdChannelDirectoryClass = ds.getJEVisClass(JC.Directory.ChannelDirectory.DWDChannelDirectory.name);
                                JEVisClass dwdChannelClass = ds.getJEVisClass(JC.Channel.DWDChannel.name);
                                JEVisClass siteClass = ds.getJEVisClass(JC.MonitoredObject.Building.name);
                                JEVisClass dataSourceDirectoryClass = ds.getJEVisClass(JC.Directory.DataSourceDirectory.name);
                                JEVisClass dwdServerClass = ds.getJEVisClass(JC.DataSource.DataServer.DwdServer.name);

                                JEVisObject nextSite = CommonMethods.getNextSiteRecursive(target.getObject(), siteClass);
                                if (nextSite != null) {
                                    JEVisObject dwdServerObject = null;
                                    JEVisObject dataSourceDirectory = null;
                                    for (JEVisObject dataSourceDir : nextSite.getChildren(dataSourceDirectoryClass, true)) {
                                        dataSourceDirectory = dataSourceDir;
                                        for (JEVisObject dataSource : dataSourceDirectory.getChildren(dwdDataSourceClass, true)) {
                                            dwdServerObject = dataSource;
                                            break;
                                        }
                                    }

                                    if (dwdServerObject == null && dataSourceDirectory != null) {
                                        JEVisObject dwdServer = dataSourceDirectory.buildObject("DWD Server", dwdServerClass);
                                        dwdServer.commit();
                                        dwdServerObject = dwdServer;

                                        JEVisObject dwdChannels = dwdServer.buildObject("DWD Channels", dwdChannelDirectoryClass);
                                        dwdChannels.commit();
                                    }

                                    JEVisObject foundExistingChannel = null;
                                    for (JEVisObject dwdServerChild : CommonMethods.getChildrenRecursive(dwdServerObject, dwdChannelClass)) {
                                        if (isSameChannel(dwdServerChild, stationId, selectedAttribute.toString(), selectedAggregation.toString(), selectedDataName)) {
                                            foundExistingChannel = dwdServerChild;
                                            break;
                                        }
                                    }

                                    if (foundExistingChannel != null) {
                                        String targetString = target.getObjectID() + ":" + target.getName();
                                        String foundTargetString = sampleHandler.getLastSample(foundExistingChannel, JC.Channel.DWDChannel.a_Target, "");
                                        if (!foundTargetString.contains(targetString)) {
                                            foundTargetString += ";" + targetString;
                                            JEVisSample updatedTarget = target.buildSample(DateTime.now(), foundTargetString);
                                            updatedTarget.commit();
                                        }

                                    } else {

                                        for (JEVisObject channelDirectory : dwdServerObject.getChildren()) {
                                            JEVisObject foundStationDirectory = channelDirectory.getChildren().stream().filter(stationDirectory -> selectedStation.getName().equals(stationDirectory.getName())).findFirst().orElse(null);

                                            if (foundStationDirectory == null) {
                                                foundStationDirectory = channelDirectory.buildObject(selectedStation.getName(), dwdChannelDirectoryClass);
                                                foundStationDirectory.commit();
                                            }

                                            String channelName = selectedStation.getName() + " " + selectedAggregation + " " + selectedAttribute + " " + selectedDataName;

                                            JEVisObject channel = foundStationDirectory.buildObject(channelName, dwdChannelClass);
                                            channel.commit();

                                            DateTime now = new DateTime();
                                            JEVisSample idSample = channel.getAttribute(JC.Channel.DWDChannel.a_Id).buildSample(now, selectedStation.getId());
                                            idSample.commit();
                                            JEVisSample aggregationSample = channel.getAttribute(JC.Channel.DWDChannel.a_Aggregation).buildSample(now, selectedAggregation);
                                            aggregationSample.commit();
                                            JEVisSample attributeSample = channel.getAttribute(JC.Channel.DWDChannel.a_Attribute).buildSample(now, selectedAttribute);
                                            attributeSample.commit();
                                            JEVisSample dataNameSample = channel.getAttribute(JC.Channel.DWDChannel.a_DataName).buildSample(now, selectedDataName);
                                            dataNameSample.commit();
                                            JEVisSample targetSample = channel.getAttribute(JC.Channel.DWDChannel.a_Target).buildSample(now, target.getObjectID() + ":" + target.getName());
                                            targetSample.commit();
                                            JEVisSample lastReadoutSample = channel.getAttribute(JC.Channel.DWDChannel.a_LastReadout).buildSample(now, samples.get(samples.size() - 1).getTimestamp());
                                            lastReadoutSample.commit();
                                        }

                                    }
                                }


                            } catch (JEVisException e) {
                                logger.error("Could not create datapoints ");
                            }
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

            JEConfig.getStatusBar().addTask("DWD Import", importData, JEConfig.getImage("save.gif"), true);
        });

        cancelButton.setOnAction(actionEvent -> close());

        stationFilter.textProperty().addListener(obs -> {
            String filter = stationFilter.getText();
            if (filter == null || filter.isEmpty()) {
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

        showDescriptionButton.setOnAction(actionEvent -> {
            PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
            pdfViewerDialog.show(null, stationBox.getSelectionModel().getSelectedItem().getDescriptionFile(), this.getDialogPane().getScene().getWindow());
        });

        getDialogPane().setContent(gridPane);


    }

    private boolean isSameChannel(JEVisObject dwdChannel, Long stationId, String selectedAttribute, String selectedAggregation, String selectedDataName) {

        SampleHandler sampleHandler = new SampleHandler();

        Long channelId = sampleHandler.getLastSample(dwdChannel, JC.Channel.DWDChannel.a_Id, -1L);
        String channelAttribute = sampleHandler.getLastSample(dwdChannel, JC.Channel.DWDChannel.a_Attribute, "");
        String channelAggregation = sampleHandler.getLastSample(dwdChannel, JC.Channel.DWDChannel.a_Aggregation, "");
        String channelDataName = sampleHandler.getLastSample(dwdChannel, JC.Channel.DWDChannel.a_DataName, "");

        return stationId.equals(channelId) && selectedAttribute.equals(channelAttribute) && selectedAggregation.equals(channelAggregation) && selectedDataName.equals(channelDataName);
    }

    private void loadData(JFXComboBox<Station> stationBox, JFXButton targetButton, JFXButton showDescriptionButton, Button okButton, JFXTextField messageField, FTPClient ftpClient, StationData stationData, List<String> allDataNames) {
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

            for (Map.Entry<Attribute, List<String>> entry : selectedStation.getIntervalPath().entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    String s = entry.getValue().get(0);
                    s = s.substring(0, s.lastIndexOf("/"));
                    s = s.substring(0, s.lastIndexOf("/") + 1);

                    FTPFileFilter filter2 = ftpFile -> (ftpFile.isFile() && ftpFile.getName().contains(".pdf") && ftpFile.getName().contains("DESCRIPTION"));
                    String fileName = "null";
                    for (FTPFile ftpFile : ftpClient.listFiles(s, filter2)) {
                        if (ftpFile.isFile() && ftpFile.getName().contains("en.pdf")) {
                            s += ftpFile.getName();
                            fileName = ftpFile.getName();
                        }
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    logger.info("FTPQuery {}", s);
                    boolean retrieveFile = ftpClient.retrieveFile(s, out);
                    logger.info("Request status: {}", retrieveFile);

                    JEVisFile jeVisFile = new JEVisFileImp(fileName, out.toByteArray());
                    selectedStation.setDescriptionFile(jeVisFile);
                }
                break;
            }

            for (Map.Entry<Attribute, List<String>> stationPathList : selectedStation.getIntervalPath().entrySet()) {
                for (String stationPath : stationPathList.getValue()) {
                    for (FTPFile ftpFile : ftpClient.listFiles(stationPath, filter)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        logger.info("FTPQuery {}", ftpFile.getName());
                        boolean retrieveFile = ftpClient.retrieveFile(stationPath + ftpFile.getName(), out);
                        logger.info("retrieved file {}", retrieveFile);

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
                showDescriptionButton.setDisable(false);
                if (targetObject != null) {
                    okButton.setDisable(false);
                }
            });
            setCellFactory();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void loadStations(JFXComboBox<Station> stationBox, JFXButton loadDataButton, List<String> stationFiles, FTPClient ftpClient) {
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

        startDatePicker.setDayCellFactory(dateCellCallback);
        endDatePicker.setDayCellFactory(dateCellCallback);
    }

    private DateTime toDateTime(LocalDate localDate) {
        return new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0);
    }

    public LocalDate toLocalDate(DateTime dateTime) {
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }
}
