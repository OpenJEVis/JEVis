package org.jevis.jeconfig.plugin.metersv2.ui;

import com.jfoenix.controls.*;
import com.jfoenix.validation.DoubleValidator;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.classes.JC;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.Icon;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.application.control.DataTypeBox;
import org.jevis.jeconfig.application.control.DayBox;
import org.jevis.jeconfig.application.control.MonthBox;
import org.jevis.jeconfig.application.control.YearBox;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
import org.jevis.jeconfig.application.type.GUIConstants;
import org.jevis.jeconfig.dialog.SelectTargetDialog;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.jevis.jeconfig.plugin.metersv2.data.SampleData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.time.*;
import java.util.*;

public class MeterForm extends Dialog {

    private MeterData meterData;

    ScrollPane scrollPane = new ScrollPane();

    GridPane gridPane = new GridPane();

    private JEVisDataSource ds;

    private JEVisTypeWrapper targetType;

    private final JFXCheckBox enterCounterValues = new JFXCheckBox(I18n.getInstance().getString("plugin.meters.meterdialog.entercountervalues"));
    private final DataTypeBox dataTypeBox = new DataTypeBox();

    private final YearBox yearBox = new YearBox(null);
    private final DayBox dayBox = new DayBox();
    private final MonthBox monthBox = new MonthBox();

    private final GridPane innerGridPane = new GridPane();

    private final Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
    private final Label oldCounterValueLabel = new Label(I18n.getInstance().getString("plugin.meters.meterdialog.oldcountervalue"));
    private final Label newCounterValueLabel = new Label(I18n.getInstance().getString("plugin.meters.meterdialog.newcountervalue"));

    private final JFXDatePicker datePicker = new JFXDatePicker(LocalDate.now());
    private final JFXTimePicker timePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));



    private  Map<Integer,List<Node>> fields = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2== null) {
                return 1;
            }
            return o1.compareTo(o2);
        }
    });

    Map<JEVisType, JEVisSample> newSamples = new HashMap<>();

    private JEVisSample oldMeterSample;
    private JEVisSample newMeterSample;

    private JFXTextField newName = new JFXTextField();

    private static final Logger logger = LogManager.getLogger(MeterForm.class);

    public MeterForm(MeterData meterData, JEVisDataSource ds) {
        long startTime = System.nanoTime();


        gridPane.setVgap(10);
        gridPane.setHgap(10);



        innerGridPane.setVgap(10);
        innerGridPane.setHgap(10);

        gridPane.addRow(0,new Label(I18n.getInstance().getString("plugin.meters.jevisname")), newName);



        this.ds = ds;
        this.meterData = meterData;
        targetType = new JEVisTypeWrapper(getJEVisType(JC.MeasurementInstrument.a_OnlineID));

        initializeMap();

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(false);


        for (Map.Entry<JEVisTypeWrapper, SampleData> entry : meterData.getJeVisAttributeJEVisSampleMap().entrySet()) {
            JEVisType jeVisType =entry.getKey().getJeVisType();

            try {
                if (jeVisType.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
                    buildFileChooser(meterData, jeVisType,entry.getValue().getOptionalJEVisSample());
                } else if (jeVisType.getGUIDisplayType().equals(GUIConstants.DATE_TIME.getId()) || jeVisType.getGUIDisplayType().equals(GUIConstants.BASIC_TEXT_DATE_FULL.getId())) {
                    buildCal(meterData, jeVisType,entry.getValue().getOptionalJEVisSample());
                } else if (jeVisType.getName().equals("Online ID")) {
                    buildTargetSelect(meterData,jeVisType,entry.getValue().getOptionalJEVisSample());
                } else {
                    buildTextField(meterData, jeVisType,entry.getValue().getOptionalJEVisSample());
                }
            } catch (JEVisException e) {
                logger.error(e);
                Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                alert.showAndWait();
            }

        }



        boolean firstRow =true;
        for(Map.Entry<Integer, List<Node>> entryMap : fields.entrySet()){
            int rowcount = getMaxRow();
            if(entryMap.getKey() % 10 == 0 && entryMap.getKey() != 0){
             gridPane.add(new Separator(),0, rowcount+1,4,1);
             rowcount++;
             firstRow = true;
         }
                if(firstRow){
                    gridPane.addRow(rowcount + 1, entryMap.getValue().get(0), entryMap.getValue().get(1));
                    firstRow = false;
                }else {
                    gridPane.addRow(rowcount, entryMap.getValue().get(0),entryMap.getValue().get(1));
                    firstRow = true;
                }
//            }
        }



        scrollPane.setContent(gridPane);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(scrollPane);

        getDialogPane().setContent(borderPane);

       buildReplaceMeterDialog();




    }

    private int getMaxRow() {
        int rowcount = gridPane.getChildren().stream().mapToInt(value -> {
            Integer row = GridPane.getRowIndex(value);
            Integer rowSpan = GridPane.getRowSpan(value);
            return (row == null ? 0 : row) + (rowSpan == null ? 0 : rowSpan - 1);
        }).max().orElse(-1);
        return rowcount;
    }

    private void buildReplaceMeterDialog() {

        gridPane.setPrefHeight(900);
        gridPane.setPrefWidth(800);
        int rowcount = getMaxRow();
        rowcount++;

        JFXCheckBox checkEnterCounter = new JFXCheckBox();
        gridPane.add(new Separator(),0, rowcount++,4,1);
        gridPane.addRow(rowcount++,new Label("Zählerstand eingeben"), checkEnterCounter);

        Label oldMeter_Label = new Label("Alter Zählerstand");

        Label newMeter_Label = new Label("Neuer Zählerstand");

        DatePicker oldMeterDatePicker = new DatePicker(LocalDate.now());
        DatePicker newMeterDatePicker = new DatePicker(LocalDate.now());

        TextField oldMeter_Value = new JFXTextField();

        TextField newMeter_Value = new JFXTextField();


        TargetHelper targetHelper = createTargetHelper();

        gridPane.add(innerGridPane,0,rowcount,4,1);
        innerGridPane.addRow(0 ,dataTypeBox);
        innerGridPane.addRow(4,oldMeter_Label,oldMeter_Value);
        innerGridPane.addRow(5,newMeter_Label,newMeter_Value);

        setMeterValueTimeIntervall(dataTypeBox.getValue());
        replacementGridPane(false);

        dataTypeBox.valueProperty().addListener((observableValue, enterDataTypes, t1) -> {
            setMeterValueTimeIntervall(t1);

        });
        checkEnterCounter.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
         replacementGridPane(t1);

        });

        newMeter_Value.textProperty().addListener((observableValue, s, t1) -> {

            try {
                Double doubleValue = Double.valueOf(t1);
                newMeterSample = buildSample(doubleValue, 0, targetHelper.getObject().get(0).getAttribute("Value"), "Meter Tausch");

            } catch (NumberFormatException numberFormatException) {
                logger.error(numberFormatException);
            } catch (JEVisException jeVisException) {
                logger.error(jeVisException);
            }


        });

        oldMeter_Value.textProperty().addListener((observableValue, s, t1) -> {
            try {
                Double doubleValue = Double.valueOf(t1);
                oldMeterSample = buildSample(doubleValue, 1, targetHelper.getObject().get(0).getAttribute("Value"), "Meter Tausch");

            } catch (NumberFormatException numberFormatException) {
                logger.error(numberFormatException);
            } catch (JEVisException jeVisException) {
                logger.error(jeVisException);
            }
        });

    }

    @NotNull
    private TargetHelper createTargetHelper() {
        try {
            JEVisAttribute onlineIdAttribute = meterData.getJeVisObject().getAttribute(targetType.getJeVisType());
            TargetHelper targetHelper = new TargetHelper(ds, onlineIdAttribute);
            return targetHelper;

        } catch (Exception e) {
            return null;
        }
    }

    public void commit(){

//        if (newName.getText() != null && !newName.getText().isEmpty()) {
//            meterData.getJeVisObject().
//        }

        try {
            for (Map.Entry<JEVisType, JEVisSample> entry : newSamples.entrySet()) {
                entry.getValue().commit();
            }
            if (newMeterSample != null && oldMeterSample != null) {
                newMeterSample.commit();
                oldMeterSample.commit();
            }

        } catch (JEVisException e) {
            logger.error(e);
        }

    }

    private void setVisibility(List<Node> nodes, boolean visible) {
        nodes.forEach(node -> {
            node.setVisible(visible);
        });
    }

    private void initializeMap() {

    }

    private void buildTextField(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        JFXTextField textField = null;
        try {
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
            textField = optionalJEVisSample.isPresent() ? new JFXTextField(optionalJEVisSample.get().getValueAsString()) : new JFXTextField();

            textField.textProperty().addListener((observableValue, s, t1) -> {
                int primitiveType = 0;
                try {
                    primitiveType = jeVisType.getPrimitiveType();
                } catch (JEVisException e) {
                    logger.error(e);
                    return;
                }


                try {
                    switch (primitiveType) {
                        case JEVisConstants.PrimitiveType.STRING:
                            String str = s;
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), str));
                            break;
                        case JEVisConstants.PrimitiveType.LONG:
                            Long l = Long.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), l));
                            break;
                        case JEVisConstants.PrimitiveType.BOOLEAN:
                            Boolean b = Boolean.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), b));
                            break;
                        case JEVisConstants.PrimitiveType.DOUBLE:
                            Double d = Double.valueOf(s);
                            newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), d));
                            break;
                    }
                } catch (NumberFormatException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Number Format Mismatch", ButtonType.OK);
                    alert.showAndWait();
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
                    alert.showAndWait();
                }

            });


        } catch (JEVisException e) {
            logger.error(e);
            return;
        }
        fields.put(getGuiPosition(jeVisType), Arrays.asList(label, textField));
    }

    private void buildFileChooser(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {


        Label label = null;
        HBox hBox = null;
        JFXButton uploadButton = null;

        try {
            Label fileName = new Label();
            fileName.setText(optionalJEVisSample.isPresent() ? optionalJEVisSample.get().getValueAsString() : "");
            uploadButton = new JFXButton("", JEConfig.getSVGImage(Icon.CLOUD_UPLOAD, 20, 20));
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
            hBox = new HBox(uploadButton, fileName);
            hBox.setSpacing(5);


            uploadButton.setOnAction(actionEvent -> {
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(JEConfig.getStage());
                if (selectedFile != null) {
                    try {
                        JEConfig.setLastPath(selectedFile);
                        JEVisFile jfile = new JEVisFileImp(selectedFile.getName(), selectedFile);
                        fileName.setText(selectedFile.getName());
                        newSamples.put(jeVisType, meterData.getJeVisObject().getAttribute(jeVisType).buildSample(DateTime.now(), jfile));
                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                }
            });
        } catch (JEVisException e) {
            logger.error(e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis error", ButtonType.OK);
            alert.showAndWait();
        }
        fields.put(getGuiPosition(jeVisType),Arrays.asList(label,hBox));

    }

    private void buildCal(MeterData meterData,JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {
        Label label = null;
        JFXDatePicker jfxDatePicker = new JFXDatePicker();
        JEVisObject jeVisObject = meterData.getJeVisObject();
        try {
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));
            DateTime dateTime = DatabaseHelper.getObjectAsDate(jeVisObject, jeVisType);
//            System.out.println(DatabaseHelper.getObjectAsLocaleDate(jeVisObject, entry.getKey()).toLocalDate());
            jfxDatePicker.setValue(toLocalDate(dateTime));
            jfxDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {

                try {
                    newSamples.put(jeVisType, jeVisObject.getAttribute(
                            jeVisType).buildSample(DateTime.now(), toDateTime(localDate)));
                    System.out.println(newSamples.get(jeVisType));
                } catch (JEVisException e) {
                    logger.error(e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Could not create Sample", ButtonType.OK);
                    alert.showAndWait();

                }
            });

        } catch (Exception e) {
            logger.error(e);
        }

        fields.put(getGuiPosition(jeVisType),Arrays.asList(label,jfxDatePicker));


    }

    public Map<JEVisType, JEVisSample> getNewSamples() {
        return newSamples;
    }

    public void setNewSamples(Map<JEVisType, JEVisSample> newSamples) {
        this.newSamples = newSamples;
    }

    private LocalDate toLocalDate(DateTime dateTime) {
        DateTime dateTimeUtc = dateTime.withZone(DateTimeZone.UTC);
        return LocalDate.of(dateTimeUtc.getYear(), dateTimeUtc.getMonthOfYear(), dateTimeUtc.getDayOfMonth());
    }

    public DateTime toDateTime(LocalDate localDate) {
        return new DateTime(DateTimeZone.UTC).withDate(
                localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth()
        ).withTime(0, 0, 0, 0);
    }

    public void buildTargetSelect(MeterData meterData, JEVisType jeVisType, Optional<JEVisSample> optionalJEVisSample) {

        Label label = null;
        JFXButton jfxButton = null;
        try {
            jfxButton = new JFXButton("", JEConfig.getSVGImage(Icon.TREE, 20, 20));
            label = new Label(I18nWS.getInstance().getTypeName(jeVisType));

            JEVisSample latestSample = meterData.getJeVisObject().getAttribute(jeVisType).getLatestSample();

            jfxButton.setOnAction(actionEvent -> {
                TargetHelper th = null;
                try {
                    th = new TargetHelper(ds,meterData.getJeVisObject().getAttribute(targetType.getJeVisType()));
                } catch (JEVisException e) {
                    logger.error(e);
                }

                if (th.isValid() && th.targetObjectAccessible()) {
                    logger.info("Target Is valid");
                }
                List<JEVisTreeFilter> allFilter = new ArrayList<>();
                JEVisTreeFilter allDataFilter = SelectTargetDialog.buildAllDataFilter();
                allFilter.add(allDataFilter);
                List<UserSelection> openList = new ArrayList<>();
                if (th != null && !th.getObject().isEmpty()) {
                    for (JEVisObject obj : th.getObject()) {
                        openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
                    }


                }
                SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allDataFilter, null, SelectionMode.SINGLE, ds, openList);
                selectTargetDialog.setOnCloseRequest(event1 -> {
                    try {
                        if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
                            logger.trace("Selection Done");

                            String newTarget = "";
                            List<UserSelection> selections = selectTargetDialog.getUserSelection();
                            for (UserSelection us : selections) {
                                int index = selections.indexOf(us);
                                if (index > 0) newTarget += ";";

                                newTarget += us.getSelectedObject().getID();
                                if (us.getSelectedAttribute() != null) {
                                    newTarget += ":" + us.getSelectedAttribute().getName();

                                } else {
                                    newTarget += ":Value";
                                }
                            }
                            JEVisSample newTargetSample = meterData.getJeVisObject().getAttribute(jeVisType).buildSample(new DateTime(), newTarget);
                            newSamples.put(jeVisType, newTargetSample);
                        }

                    } catch (JEVisException e) {
                        logger.error(e);
                        Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis Exception", ButtonType.OK);
                        alert.showAndWait();
                    }
                });
                selectTargetDialog.showAndWait();
            });

        } catch (Exception e) {
            logger.error(e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "JEVis Exception", ButtonType.OK);
            alert.showAndWait();
        }
        fields.put(getGuiPosition(jeVisType),Arrays.asList(label,jfxButton));
    }

    private int getGuiPosition(JEVisType jeVisType) {
        try {
            return jeVisType.getGUIPosition();
        } catch (JEVisException jeVisException) {
            logger.error(jeVisException);
        }
        return 0;
    }
    private JEVisType getJEVisType(String string){
        try{
            JEVisClass jeVisClass = ds.getJEVisClass(JC.MeasurementInstrument.name);
            JEVisType jeVisType = jeVisClass.getType(string);
            return jeVisType;

        }catch (Exception e){
            logger.error(e);
        }
        return null;


    }

    private void replacementGridPane(boolean visible) {
        if(visible){
            innerGridPane.setVisible(true);
        }else {
            innerGridPane.setVisible(false);
            oldMeterSample = null;
            newMeterSample = null;
        }
    }
    private void setMeterValueTimeIntervall(EnterDataTypes enterDataTypes){
        switch (enterDataTypes) {
            case YEAR:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 1, 1, 3, 1));
                break;
            case MONTH:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 1, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(monthBox, 2, 1, 1, 1));
                break;
            case DAY:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.add(yearBox, 1, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(monthBox, 2, 1, 1, 1));
                Platform.runLater(() -> innerGridPane.add(dayBox, 3, 1, 1, 1));
                break;
            case SPECIFIC_DATETIME:
                Platform.runLater(() -> innerGridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> innerGridPane.addRow(1, dateLabel, datePicker, timePicker));
                break;
        }
    }

    private DateTime getDate(EnterDataTypes enterDataTypes){
        DateTime ts = null;
        Integer year = yearBox.getSelectionModel().getSelectedItem();
        Integer month = monthBox.getSelectionModel().getSelectedIndex() + 1;
        Integer day = dayBox.getSelectionModel().getSelectedItem();

        switch (enterDataTypes) {
            case YEAR:
                ts = new DateTime(year,
                        1,
                        1,
                        0, 0, 0);
                break;
            case MONTH:
                ts = new DateTime(year,
                        month,
                        1,
                        0, 0, 0);
                break;
            case DAY:
                ts = new DateTime(year,
                        month,
                        day,
                        0, 0, 0);
                break;
            case SPECIFIC_DATETIME:
                ts = new DateTime(
                        datePicker.valueProperty().get().getYear(),
                        datePicker.valueProperty().get().getMonthValue(),
                        datePicker.valueProperty().get().getDayOfMonth(),
                        timePicker.valueProperty().get().getHour(),
                        timePicker.valueProperty().get().getMinute(),
                        timePicker.valueProperty().get().getSecond());
                break;
        }
        return ts;
    }

    private JEVisSample buildSample(double value, int offsetSecond, JEVisAttribute jeVisAttribute, String note) throws JEVisException {
            return jeVisAttribute.buildSample(getDate(dataTypeBox.getValue()).minusSeconds(offsetSecond),value,note);
    }


    private void createMeterCounterSamples(double value){

    }
}
