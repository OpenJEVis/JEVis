package org.jevis.jeconfig.application.jevistree.wizard;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.CommonUnits;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.JEVisDates;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.Chart.ChartPluginElements.TreeSelectionDialog;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.dialog.Response;
import org.joda.time.DateTime;
import org.joda.time.Period;
import tech.units.indriya.AbstractUnit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.jevis.jeconfig.application.jevistree.JEVisTreeContextMenu.THREAD_WAIT;

public class MargeWizard extends MenuItem {

    private static final Logger logger = LogManager.getLogger(MargeWizard.class);

    public MargeWizard(JEVisTree tree) {
        this.setText("MARGE Wizard");
        this.setGraphic(JEConfig.getSVGImage(Icon.WIZARD_HAT, 20, 20));

        //List<Integer> IDs = parseIDS("etc/MARGE.csv");
        //System.out.println("found " + IDs.size() + " Thermostats");


        setOnAction(actionEvent -> {
            JEVisDataSource ds = tree.getJEVisDataSource();
            JEVisObject httpDataSource = ((JEVisTreeItem) tree.getSelectionModel().getSelectedItem()).getValue().getJEVisObject();


            Dialog<ButtonType> buttonTypeDialog = new Dialog<>();
            buttonTypeDialog.setResizable(true);
            buttonTypeDialog.initOwner(JEConfig.getStage());
            buttonTypeDialog.initModality(Modality.APPLICATION_MODAL);
            Stage stage = (Stage) buttonTypeDialog.getDialogPane().getScene().getWindow();
            TopMenu.applyActiveTheme(stage.getScene());

            Button selectTemplateButton = new Button();
            selectTemplateButton.setText("Select Target (Thermostat) Data Directory");
            AtomicReference<JEVisObject> targetDataDirectory = new AtomicReference<>();

            selectTemplateButton.setOnAction(getSelectTemplateEvent(tree, targetDataDirectory));

            ButtonType okType = new ButtonType(I18n.getInstance().getString("newobject.ok"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType(I18n.getInstance().getString("newobject.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

            buttonTypeDialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

            Button okButton = (Button) buttonTypeDialog.getDialogPane().lookupButton(okType);
            okButton.setDefaultButton(true);

            Button cancelButton = (Button) buttonTypeDialog.getDialogPane().lookupButton(cancelType);
            cancelButton.setCancelButton(true);

            Separator separator = new Separator(Orientation.HORIZONTAL);
            separator.setPadding(new Insets(8, 0, 8, 0));

            VBox vBox = new VBox(6, selectTemplateButton, separator);
            buttonTypeDialog.getDialogPane().setContent(vBox);

            Task<Void> upload = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        JEVisObject targetFolder = targetDataDirectory.get();
                        System.out.println("Create new Data Structure under " + targetFolder.getName());

                        //HashMap<Long, String> thermostats = fetchConfig(httpDataSource);
                        HashMap<Long, String> thermostats = new HashMap<>();
                        thermostats.put(102278285386471L, "102278285386471 (active)");


                        System.out.println("Found " + thermostats.size() + " thermostats");
                        HashMap<Long, JEVisObject> newThermostats = new HashMap<>();
                        thermostats.forEach((integer, s) -> {
                            try {
                                JEVisObject newDir = createDataStructure(targetFolder, s);
                                if (newDir != null) {
                                    newThermostats.put(integer, newDir);
                                } else {
                                    System.out.println("Thermostate exists - skip");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

                        JEVisObject httpChannelDir = httpDataSource.getChildren(ds.getJEVisClass(JC.Directory.HTTPChannelDirectory.name), true).get(0);

                        newThermostats.forEach((integer, obj) -> {
                            try {
                                System.out.println("Create channel for: " + integer);
                                createFetchStructure(httpChannelDir, obj, integer);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    succeeded();
                    return null;
                }
            };
            okButton.setOnAction(actionEvent1 -> new Thread(upload).start());
            buttonTypeDialog.show();
        });

    }

    private static String sendGet(String urlStr, String user, String pass) throws Exception {
        System.out.println("Call: " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // Basic Auth Header
        String auth = user + ":" + pass;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP GET Request Failed with Error code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private HashMap<Long, String> fetchConfig(JEVisObject httpDataSourceObj) throws Exception {
        String username = httpDataSourceObj.getAttribute("User").getLatestSample().getValueAsString();
        String password = httpDataSourceObj.getAttribute("Password").getLatestSample().getValueAsString();
        String endpoint = httpDataSourceObj.getAttribute("Host").getLatestSample().getValueAsString();
        Long port = httpDataSourceObj.getAttribute("Port").getLatestSample().getValueAsLong();


        HashMap<Long, String> termostates = new HashMap<>();
        String response1 = sendGet(endpoint + ":" + port + "/thermostat-list", username, password);
        response1 = response1.replaceAll("[\\[\\]\\s]", "");
        String[] values = response1.split(",");
        for (String value : values) {
            try {
                if (termostates.size() > 2) break;//debug

                Long thermostatID = Long.parseLong(value);
                String thermostatName = sendGet(endpoint + ":" + port + "/thermostat/" + thermostatID + "/description", username, password);
                termostates.put(thermostatID, thermostatName.replaceAll("\"", ""));

                System.out.println("thermostatID: " + thermostatID + " name: " + thermostatName.replaceAll("\"", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return termostates;
    }

    private JEVisObject createDataStructure(JEVisObject parent, String name) throws JEVisException, InterruptedException {
        JEVisDataSource ds = parent.getDataSource();

        JEVisUnit tempUnit = CommonUnits.celsius.jevisUnit;
        JEVisUnit noUnit = new JEVisUnitImp();
        JEVisUnit pro100Unit = new JEVisUnitImp(AbstractUnit.ONE, "%", "");//Units.CELSIUS

        Period period = Period.minutes(1);

        //Check if thermostate already exists
        JEVisObject existingObj = null;
        for (JEVisObject object : parent.getChildren()) {
            if (object.getName().equals(name)) existingObj = object;
        }
        if (existingObj != null) return null;

        JEVisObject thermostateDir = parent.buildObject(name, ds.getJEVisClass(JC.Directory.DataDirectory.name));
        thermostateDir.commit();
        Thread.sleep(THREAD_WAIT);

        //Numbers
        JEVisObject userSetTemp = createDataObject(thermostateDir, "userSetTemp", tempUnit, period);
        JEVisObject temperature = createDataObject(thermostateDir, "temperature", tempUnit, period);
        JEVisObject piSetTempInput = createDataObject(thermostateDir, "piSetTempInput", tempUnit, period);
        JEVisObject humidity = createDataObject(thermostateDir, "humidity", pro100Unit, period);
        JEVisObject occupancy = createDataObject(thermostateDir, "occupancy", noUnit, period);
        JEVisObject piControlOutput = createDataObject(thermostateDir, "piControlOutput", noUnit, period);
        JEVisObject reportedValvePosition = createDataObject(thermostateDir, "reportedValvePosition", noUnit, period);

        //Text
        JEVisObject newMeasurement = createTextDataObject(thermostateDir, "newMeasurement", noUnit, period);
        JEVisObject sysMode = createTextDataObject(thermostateDir, "sysMode", noUnit, period);
        JEVisObject windowOpenDetection = createTextDataObject(thermostateDir, "windowOpenDetection", noUnit, period);

        return thermostateDir;
    }

    private void createFetchStructure(JEVisObject mainHttpChannelDir, JEVisObject targetDataObject, long id) throws JEVisException, InterruptedException, IOException {
        System.out.println("-create Fetch structure: " + targetDataObject.getName() + " apiID: " + id);
        JEVisDataSource ds = mainHttpChannelDir.getDataSource();
        JEVisClass httpChannelClass = ds.getJEVisClass(JC.Channel.HTTPChannel.name);
        JEVisClass jsonParserClass = ds.getJEVisClass(JC.Parser.JSONParser.name);
        JEVisClass jsonDataPointDirectoryClass = ds.getJEVisClass(JC.Directory.DataPointDirectory.JSONDataPointDirectory.name);
        JEVisClass jsonDataPointClass = ds.getJEVisClass(JC.DataPoint.JSONDataPoint.name);

        JEVisObject httpChannel = mainHttpChannelDir.buildObject(targetDataObject.getName(), httpChannelClass);
        httpChannel.commit();
        Thread.sleep(THREAD_WAIT);

        DateTime configDate = new DateTime(1990, 1, 1, 0, 0, 0);

        String newMainPathAttributeString = "/thermostat/" + id + "/data?startdate={LAST_TS}&enddate={CURRENT_TS}";
        httpChannel.getAttribute(JC.Channel.HTTPChannel.a_Path).buildSample(configDate, newMainPathAttributeString).commit();
        Thread.sleep(THREAD_WAIT);

        httpChannel.getAttribute(JC.Channel.HTTPChannel.a_LastReadout).buildSample(
                new DateTime(2025, 1, 1, 0, 0, 0)
                , "2025-01-01T00:00:00.000+01:00").commit();
        Thread.sleep(THREAD_WAIT);

        String configString = "[ {\n" +
                "  \"format\" : \"yyyy-MM-dd'T'HH:mm:ss\",\n" +
                "  \"Parameter\" : \"LAST_TS\",\n" +
                "  \"Timezone\" : \"Europe/Berlin\"\n" +
                "}, {\n" +
                "  \"format\" : \"yyyy-MM-dd'T'HH:mm:ss\",\n" +
                "  \"Parameter\" : \"CURRENT_TS\",\n" +
                "  \"Timezone\" : \"Europe/Berlin\"\n" +
                "} ]";


        JEVisFile parameterConfig = new JEVisFileImp("ParameterConfig" + JEVisDates.printDefaultDate(configDate), configString.getBytes(StandardCharsets.UTF_8));
        httpChannel.getAttribute(JC.Channel.HTTPChannel.a_ParameterConfig).buildSample(configDate, parameterConfig).commit();
        Thread.sleep(THREAD_WAIT);

        //not needed anymore
        //httpChannel.getAttribute(JC.Channel.HTTPChannel.a_ChunkSize).buildSample(configDate, 86400).commit();
        //Thread.sleep(THREAD_WAIT);

        JEVisObject jsonParser = httpChannel.buildObject("JsonParser", jsonParserClass);
        jsonParser.commit();
        Thread.sleep(THREAD_WAIT);

        jsonParser.getAttribute(JC.Parser.JSONParser.a_dateTimePath).buildSample(configDate, "time").commit();
        Thread.sleep(THREAD_WAIT);
        jsonParser.getAttribute(JC.Parser.JSONParser.a_dateTimeFormat).buildSample(configDate, "ISO8601").commit();//"yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ").commit();
        Thread.sleep(THREAD_WAIT);

        JEVisObject jsonDataPointsDirectory = jsonParser.buildObject(jsonDataPointDirectoryClass.getName(), jsonDataPointDirectoryClass);
        jsonDataPointsDirectory.commit();
        Thread.sleep(THREAD_WAIT);

        targetDataObject.getChildren().forEach(dataObject -> {
            try {
                System.out.println("jsonDataPointsDirectory: " + jsonDataPointsDirectory);
                System.out.println("dataPointClass: " + jsonDataPointClass);
                System.out.println("dataObject.getName(): " + dataObject.getName());
                JEVisObject dataPoint = jsonDataPointsDirectory.buildObject(dataObject.getName(), jsonDataPointClass);
                dataPoint.setLocalNames(dataObject.getLocalNameList());
                dataPoint.commit();
                Thread.sleep(THREAD_WAIT);

                dataPoint.getAttribute("Target").buildSample(configDate, dataObject.getID() + ":" + "Value").commit();
                Thread.sleep(THREAD_WAIT);
                dataPoint.getAttribute("Data Point Path").buildSample(configDate, dataObject.getLocalName("en")).commit();
                System.out.println("dataObject.getLocalName(\"en\"): " + dataObject.getLocalName("en"));
                switch (dataObject.getLocalName("en")) {
                    case "occupancy":
                    case "temperature":
                    case "humidity":
                    case "userSetTemp":
                    case "piControlOutput":
                    case "piSetTempInput":
                    case "reportedValvePosition":
                        dataPoint.getAttribute("Value Format").buildSample(configDate, "Double").commit();
                        break;
                    case "sysMode":
                        dataPoint.getAttribute("Value Format").buildSample(configDate, "String").commit();
                        break;
                    case "newMeasurement":
                    case "windowOpenDetection":
                        dataPoint.getAttribute("Value Format").buildSample(configDate, "Boolean").commit();
                        break;
                }
                Thread.sleep(THREAD_WAIT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.out.println("done");
    }

    private JEVisObject createTextDataObject(JEVisObject parent, String name, JEVisUnit unit, Period period) throws JEVisException, InterruptedException {
        JEVisDataSource ds = parent.getDataSource();

        JEVisObject newRawData = parent.buildObject(name, ds.getJEVisClass("String Data"));
        Map<String, String> names = new HashMap<>();
        names.put("de", camelCaseToNormalText(name));
        names.put("en", name);
        newRawData.setLocalNames(names);
        newRawData.commit();
        Thread.sleep(THREAD_WAIT);


        JEVisAttribute valueAttributeRaw = newRawData.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
        valueAttributeRaw.setInputSampleRate(period);
        valueAttributeRaw.setDisplaySampleRate(period);
        valueAttributeRaw.setInputUnit(unit);
        valueAttributeRaw.setDisplayUnit(unit);
        valueAttributeRaw.commit();
        Thread.sleep(THREAD_WAIT);

        DateTime startDate = new DateTime(1990, 1, 1, 0, 0, 0);


        setAttribute(newRawData, "Period", startDate, period.toString());
        Thread.sleep(THREAD_WAIT);

        return newRawData;
    }

    private JEVisObject createDataObject(JEVisObject parent, String name, JEVisUnit unit, Period period) throws JEVisException, InterruptedException {
        JEVisDataSource ds = parent.getDataSource();

        JEVisObject newRawData = parent.buildObject(name, ds.getJEVisClass("Data"));
        Map<String, String> names = new HashMap<>();
        names.put("de", camelCaseToNormalText(name));
        names.put("en", name);
        newRawData.setLocalNames(names);
        newRawData.commit();
        Thread.sleep(THREAD_WAIT);
        JEVisObject newCleanData = newRawData.buildObject("Clean Data", ds.getJEVisClass("Clean Data"));
        newCleanData.commit();
        Thread.sleep(THREAD_WAIT);

        JEVisAttribute valueAttributeClean = newCleanData.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
        valueAttributeClean.setInputSampleRate(period);
        valueAttributeClean.setDisplaySampleRate(period);
        valueAttributeClean.setInputUnit(unit);
        valueAttributeClean.setDisplayUnit(unit);
        valueAttributeClean.commit();
        Thread.sleep(THREAD_WAIT);

        JEVisAttribute valueAttributeRaw = newRawData.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
        valueAttributeRaw.setInputSampleRate(period);
        valueAttributeRaw.setDisplaySampleRate(period);
        valueAttributeRaw.setInputUnit(unit);
        valueAttributeRaw.setDisplayUnit(unit);
        valueAttributeRaw.commit();
        Thread.sleep(THREAD_WAIT);

        DateTime startDate = new DateTime(1990, 1, 1, 0, 0, 0);


        setAttribute(newRawData, "Period", startDate, period.toString());
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Period", startDate, period.toString());
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Conversion to Differential", startDate, false);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Enabled", startDate, true);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "GapFilling Enabled", startDate, true);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Period Alignment", startDate, true);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Value is a Quantity", startDate, false);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Enabled", startDate, true);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Value Multiplier", startDate, 1);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Value Offset", startDate, 0);
        Thread.sleep(THREAD_WAIT);
        setAttribute(newCleanData, "Gap Filling Config", startDate, getGapFillingConfig());
        Thread.sleep(THREAD_WAIT);

        return newRawData;
    }

    public void setAttribute(JEVisObject object, String attribute, DateTime ts, Object value) throws JEVisException {
        try {
            JEVisAttribute periodAttribute = object.getAttribute(attribute);
            periodAttribute.buildSample(ts, value).commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getGapFillingConfig() {
        return "[{\n" +
                "  \"name\" : \"Stufe 1\",\n" +
                "  \"type\" : \"INTERPOLATION\",\n" +
                "  \"boundary\" : \"3600000\",\n" +
                "  \"defaultvalue\" : null,\n" +
                "  \"referenceperiod\" : null,\n" +
                "  \"bindtospecific\" : null,\n" +
                "  \"referenceperiodcount\" : null\n" +
                "}, {\n" +
                "  \"name\" : \"Stufe 2\",\n" +
                "  \"type\" : \"AVERAGE\",\n" +
                "  \"boundary\" : \"2592000000\",\n" +
                "  \"defaultvalue\" : null,\n" +
                "  \"referenceperiod\" : \"MONTH\",\n" +
                "  \"bindtospecific\" : \"WEEKDAY\",\n" +
                "  \"referenceperiodcount\" : \"1\"\n" +
                "}]";
    }

    private List<Integer> parseIDS(String filePath) {
        List<Integer> ids = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // Jede Zeile enthält genau einen Integer
                try {
                    int value = Integer.parseInt(line.trim());
                    System.out.println("Read integer: " + value);
                    ids.add(value);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid integer: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ids;
    }

    private EventHandler<ActionEvent> getSelectTemplateEvent(JEVisTree tree, AtomicReference<JEVisObject> templateObject) {
        return t -> {
            try {
                List<JEVisClass> classes = new ArrayList<>();
                List<UserSelection> openList = new ArrayList<>();
                boolean showAttributes = false;

                TreeSelectionDialog treeSelectionDialog = new TreeSelectionDialog(tree.getJEVisDataSource(), classes, SelectionMode.SINGLE, openList, showAttributes);

                treeSelectionDialog.setOnCloseRequest(event -> {
                    try {
                        if (treeSelectionDialog.getResponse() == Response.OK) {
                            logger.trace("Selection Done");

                            List<UserSelection> selections = treeSelectionDialog.getUserSelection();

                            for (UserSelection us : selections) {
                                templateObject.set(us.getSelectedObject());
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
        };
    }


    private String camelCaseToNormalText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String spaced = input.replaceAll("([a-z])([A-Z])", "$1 $2");

        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
