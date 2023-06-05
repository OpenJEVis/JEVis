package org.jevis.jeconfig.plugin.object.extension.revpi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeapi.ws.HTTPConnection;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.dialog.DialogHeader;
import org.jevis.jeconfig.dialog.Response;
import org.jevis.jeconfig.tool.ImageConverter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RevPiAssistant {

    private static final Logger logger = LogManager.getLogger(RevPiAssistant.class);

    private final List<JEVisObject> channels = new ArrayList<>();
    private final HTTPConnection.Trust sslTrustMode = HTTPConnection.Trust.SYSTEM;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String serverURL;
    private Integer port;
    private Integer connectionTimeout;
    private Integer readTimeout;
    private String userName;
    private String password;
    private Boolean ssl = false;
    private DateTimeZone timezone;
    private ObservableList<RevPiTrend> revPiTrends = FXCollections.observableArrayList();
    private HTTPConnection con;

    private JEVisClass dataDirClass;
    private JEVisClass dataClass;
    private JEVisClass cleanDataClass;
    private JEVisClass revPiDataSourceDir;
    private JEVisClass revPiDataSourceChannel;
    public static final Integer OK = 0;

    public static String API_STRING = "api/trends";

    public final ScrollPane scrollPane = new ScrollPane();

    final Stage stage;

    TableView tableView;

    private JEVisObject targetDataObject;

    private JEVisDataSource ds;

    private JEVisObject rootDataFolder;

    private JEVisObject rootDataSourceFolder;

    private JEVisObject revPiServer;
    private JEVisType sourceIdType;
    private JEVisType jevisTargetType;


    public RevPiAssistant(JEVisObject revPiServer) {
        stage = new Stage();

        this.revPiServer = revPiServer;


        stage.initOwner(JEConfig.getStage());
        try {
            ds = revPiServer.getDataSource();
            tableView = RevPiTableFactory.getTable(revPiTrends);


            dataDirClass = ds.getJEVisClass("Data Directory");
            dataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");
            revPiDataSourceDir = ds.getJEVisClass("Revolution PI Channel Directory");
            revPiDataSourceChannel = ds.getJEVisClass(DataCollectorTypes.Channel.RevolutionPiChannel.NAME);
            sourceIdType = revPiDataSourceChannel.getType(DataCollectorTypes.Channel.RevolutionPiChannel.SOURCEID);
            jevisTargetType = revPiDataSourceChannel.getType(DataCollectorTypes.Channel.RevolutionPiChannel.TARGETID);
            Node header = DialogHeader.getDialogHeader(ImageConverter.convertToImageView(revPiServer.getJEVisClass().getIcon(), 64, 64), I18n.getInstance().getString("jevistree.menu.revpi.assistant"));

            GridPane gridPane = new GridPane();
            gridPane.setPadding(new Insets(10, 10, 10, 10));
            gridPane.add(header, 0, 0, 2, 1);
            gridPane.add(tableView, 0, 1, 2, 1);

            gridPane.add(buildTargetButton(), 0, 2, 1, 1);
            gridPane.add(buildImportButton(), 1, 2, 1, 1);

            initializeAttributes(revPiServer);
            Scene scene = new Scene(gridPane);
            stage.setScene(scene);

            TopMenu.applyActiveTheme(scene);
            stage.setScene(scene);


        } catch (Exception e) {
            logger.error(e);
        }
        try (InputStream inputStream = this.con.getInputStreamRequest(API_STRING)) {
            if (inputStream != null) {
                String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                revPiTrends.addAll((objectMapper.readValue(result, RevPiTrend[].class)));

            }

        } catch (Exception e) {
            logger.error(e);
        }
        stage.showAndWait();

    }


    private void initializeAttributes(JEVisObject revolutionPiServer) {
        try {
            JEVisClass serverClass = revolutionPiServer.getDataSource().getJEVisClass(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.NAME);
            JEVisType sslType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.SSL);
            JEVisType serverType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.HOST);
            JEVisType portType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.PORT);
            JEVisType connectionTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.CONNECTION_TIMEOUT);
            JEVisType readTimeoutType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.READ_TIMEOUT);
            JEVisType userType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.USER);
            JEVisType passwordType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.PASSWORD);
            JEVisType timezoneType = serverClass.getType(DataCollectorTypes.DataSource.DataServer.RevolutionPiServer.TIMEZONE);

            ssl = DatabaseHelper.getObjectAsBoolean(revolutionPiServer, sslType);
            serverURL = DatabaseHelper.getObjectAsString(revolutionPiServer, serverType);
            port = DatabaseHelper.getObjectAsInteger(revolutionPiServer, portType);
            if (port == null) {
                port = 8000;
            }
            connectionTimeout = DatabaseHelper.getObjectAsInteger(revolutionPiServer, connectionTimeoutType);
            readTimeout = DatabaseHelper.getObjectAsInteger(revolutionPiServer, readTimeoutType);

            JEVisAttribute userAttr = revolutionPiServer.getAttribute(userType);
            if (userAttr == null || !userAttr.hasSample()) {
                userName = "";
            } else {
                userName = DatabaseHelper.getObjectAsString(revolutionPiServer, userType);
            }

            JEVisAttribute passAttr = revolutionPiServer.getAttribute(passwordType);
            if (passAttr == null || !passAttr.hasSample()) {
                password = "";
            } else {
                password = DatabaseHelper.getObjectAsString(revolutionPiServer, passwordType);
            }
            String timezoneString = DatabaseHelper.getObjectAsString(revolutionPiServer, timezoneType);
            if (timezoneString != null) {
                timezone = DateTimeZone.forID(timezoneString);
            } else {
                timezone = DateTimeZone.UTC;
            }

            String host = serverURL + ":" + port;

            this.con = new HTTPConnection(host, userName, password, sslTrustMode);

        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    private JFXButton buildTargetButton() {
        final JFXButton button = new JFXButton(I18n.getInstance().getString("plugin.object.attribute.target.button"), JEConfig.getImage("folders_explorer.png", 18, 18));
        button.wrapTextProperty().setValue(true);
        button.setOnAction(actionEvent -> {


            List<JEVisClass> filterClasses = new ArrayList<>();
            filterClasses.add(dataDirClass);
            TreeSelectionDialog selectTargetDialog = new TreeSelectionDialog(ds, filterClasses, SelectionMode.SINGLE);


            selectTargetDialog.setOnCloseRequest(event -> {
                try {
                    if (selectTargetDialog.getResponse() == Response.OK) {

                        targetDataObject = selectTargetDialog.getTreeView().getSelectedObjects().get(0);
                        button.setText(targetDataObject.getName());

                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            });
            selectTargetDialog.show();
        });

        return button;
    }

    private JFXButton buildImportButton() {
        JFXButton importTrends = new JFXButton("import");
        importTrends.setOnAction(actionEvent -> {
            if (targetDataObject != null) {
                List<RevPiTrend> selectedTrends = revPiTrends.stream().filter(revPiTrend -> revPiTrend.isSelected()).collect(Collectors.toList());
                if (selectedTrends.size() > 0) {

                    Task<Void> task = new Task<Void>() {

                        @Override
                        protected Void call() throws Exception {
                            try {
                                rootDataFolder = targetDataObject.buildObject("import Rev Pi", dataDirClass);
                                rootDataFolder.commit();

                                rootDataSourceFolder = revPiServer.buildObject("import Rev Pi", revPiDataSourceDir);
                                rootDataSourceFolder.commit();
                                DateTime now = DateTime.now();
                                selectedTrends.forEach(revPiTrend -> {
                                    addDataObject(revPiTrend, now);
                                });

                            } catch (Exception e) {
                                logger.error(e);
                            }
                            return null;
                        }
                    };
                    JEConfig.getStatusBar().addTask(RevPiAssistant.class.getName(), task, JEConfig.getImage("if_dashboard_46791.png"), true);


                }


            }
        });
        return importTrends;
    }

    private void addDataObject(RevPiTrend revPiTrend, DateTime dateTime) {
        try {

            if(!dataClass.isAllowedUnder(rootDataFolder.getJEVisClass())) return;

            JEVisObject dataObject = rootDataFolder.buildObject(revPiTrend.getName(), dataClass);
            dataObject.commit();
            JEVisAttribute dataPeriodAttribute = dataObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            if (dataPeriodAttribute != null) {
                JEVisSample sample = dataPeriodAttribute.buildSample(new DateTime(1990, 1, 1, 0, 0, 0, 0), convertInterval(revPiTrend.getConfig()));
                sample.commit();
            }

            if(!cleanDataClass.isAllowedUnder(dataObject.getJEVisClass())) return;


            JEVisObject cleanDataObject = dataObject.buildObject(I18n.getInstance().getString("tree.treehelper.cleandata.name"), cleanDataClass);
            cleanDataObject.setLocalNames(I18n.getInstance().getTranslationMap("tree.treehelper.cleandata.name"));

            cleanDataObject.commit();

            JEVisAttribute cleanDataPeriodAttribute = cleanDataObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            if (cleanDataPeriodAttribute != null) {
                JEVisSample sample = cleanDataPeriodAttribute.buildSample(new DateTime(1990, 1, 1, 0, 0, 0, 0), convertInterval(revPiTrend.getConfig()));
                sample.commit();
            }
            if(!revPiDataSourceChannel.isAllowedUnder(rootDataSourceFolder.getJEVisClass())) return;


            JEVisObject revPiChannel = rootDataSourceFolder.buildObject(revPiTrend.getName(), revPiDataSourceChannel);
            revPiChannel.commit();
            JEVisAttribute sourceIdAttribute = revPiChannel.getAttribute(sourceIdType);
            JEVisAttribute jevisTargetIdAttribute = revPiChannel.getAttribute(jevisTargetType);


            sourceIdAttribute.buildSample(dateTime, revPiTrend.getTrendId()).commit();

            jevisTargetIdAttribute.buildSample(dateTime, dataObject.getID() + ":Value").commit();


        } catch (Exception e) {
            logger.error(e);
        }


    }

    private long parseCron(String cronexpression) {
        if (cronexpression.equals( "*") || cronexpression.equals("0")) {
            return 0;
        }else{
            try {
                return Long.parseLong(cronexpression.replaceAll("[^0-9]", ""));
            } catch (Exception e) {
                logger.error(e);
            }

        }
        return 0;
    }

    private Period convertInterval(String interval) {
        try {
            List<String> crons = Arrays.asList(interval.split(" "));
            if (interval.equals("cov") || interval.equals("undefined")) {
                return Period.ZERO;
            } else {
                Long second = parseCron(crons.get(0));
                Long minute = parseCron(crons.get(1));
                Long hours = parseCron(crons.get(2));
                Long day = parseCron(crons.get(3));
                Long month = parseCron(crons.get(4));

                if (day == 0 && hours == 0 && minute == 15 && second == 0) {
                    return Period.minutes(15);
                } else if (day == 0 && hours == 0 && minute == 1 && second == 0) {
                    return Period.minutes(1);
                } else if (day == 1 && hours == 0 && minute == 0 && second == 0) {
                    return Period.days(1);
                } else if (day == 0 && hours == 1 && minute == 0 && second == 0) {
                    return Period.hours(1);
                } else if (day == 7 && hours == 0 && minute == 0 && second == 0) {
                    return Period.weeks(1);
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("PT");
                    stringBuilder.append(hours + day * 24).append("H");
                    stringBuilder.append(minute).append("M");
                    stringBuilder.append(second).append("S");
                    return Period.parse(stringBuilder.toString());


                }

            }
        } catch (Exception e) {
            return Period.ZERO;
        }









    }

}
