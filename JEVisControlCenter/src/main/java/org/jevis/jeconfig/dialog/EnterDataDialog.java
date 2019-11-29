package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnterDataDialog {
    private static final Logger logger = LogManager.getLogger(EnterDataDialog.class);
    public static String ICON = "Startup Wizard_18228.png";
    private final JEVisDataSource ds;
    private final ObjectRelations objectRelations;
    private JFXDatePicker datePicker;
    private JFXTimePicker timePicker;
    private TextField doubleField;
    private JEVisObject selectedObject;
    private Stage stage;
    private Response response;
    private Label lastValueLabel = new Label();
    private Label lastTSLabel = new Label();
    private NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private Double lastValue;
    private JEVisClass dataClass;
    private JEVisClass cleanDataClass;

    public EnterDataDialog(JEVisDataSource dataSource) {
        this.ds = dataSource;
        this.objectRelations = new ObjectRelations(ds);
        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);
    }

    public Response show() {
        response = Response.CANCEL;

        if (stage != null) {
            stage.close();
            stage = null;
        }

        stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(JEConfig.getStage());

        stage.setTitle(I18n.getInstance().getString("plugin.object.dialog.data.title"));
//        stage.setHeader(DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("plugin.object.dialog.data.header")));
        stage.initOwner(JEConfig.getStage());
        stage.setResizable(true);
//        stage.setMinHeight(450);
        stage.setMinWidth(1000);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(12));
        gridPane.setVgap(8);
        gridPane.setHgap(8);

        Label idLabel = new Label(I18n.getInstance().getString("plugin.graph.export.text.id"));

        List<JEVisObject> allData = new ArrayList<>();
        HashMap<Long, JEVisObject> map = new HashMap<>();
        try {
            dataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");
            allData = ds.getObjects(dataClass, false);
            map = allData.stream().collect(Collectors.toMap(JEVisObject::getID, object -> object, (a, b) -> b, HashMap::new));
        } catch (JEVisException e) {
            e.printStackTrace();
        }
        ComboBox<JEVisObject> idBox = new ComboBox<>(FXCollections.observableList(allData));

        Callback<ListView<JEVisObject>, ListCell<JEVisObject>> cellFactory = new Callback<ListView<JEVisObject>, ListCell<JEVisObject>>() {
            @Override
            public ListCell<JEVisObject> call(ListView<JEVisObject> param) {
                return new ListCell<JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject obj, boolean empty) {
                        super.updateItem(obj, empty);
                        if (obj == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String prefix = objectRelations.getObjectPath(obj);

                            setText(prefix + obj.getName() + ":" + obj.getID());
                        }

                    }
                };
            }
        };

        idBox.setCellFactory(cellFactory);
        idBox.setButtonCell(cellFactory.call(null));
        idBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                selectedObject = newValue;
                loadLastValue();
            }
        });

        Label diffSwitchLabel = new Label(I18n.getInstance().getString("graph.dialog.note.text.diff"));
        ToggleSwitchPlus diffSwitch = new ToggleSwitchPlus();

        Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
        datePicker = new JFXDatePicker(LocalDate.now());
        timePicker = new JFXTimePicker(LocalTime.now());
        datePicker.setPrefWidth(120d);
        timePicker.setPrefWidth(100d);
        timePicker.setMaxWidth(100d);
        timePicker.set24HourView(true);
        timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        Label valueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value"));
        doubleField = new TextField();

        TextField searchIdField = new TextField();
        HashMap<Long, JEVisObject> finalMap = map;
        searchIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                try {
                    long l = Long.parseLong(newValue);
                    JEVisObject selection = finalMap.get(l);
                    if (selection != null) {
                        idBox.getSelectionModel().select(selection);
                    }
                } catch (Exception ignored) {
                }
            }
        });

        Separator sep = new Separator(Orientation.HORIZONTAL);

        Button confirm = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.save"));
        Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
        cancel.setOnAction(event -> {
                    stage.close();
                    stage = null;
                }
        );
        confirm.setOnAction(event -> {
            if (selectedObject != null) {
                DoubleValidator validator = new DoubleValidator();
                Double newVal = validator.validate(doubleField.getText(), I18n.getInstance().getLocale());

                if (newVal != null) {

                    JEVisAttribute valueAttribute = null;
                    JEVisAttribute diffAttribute = null;
                    Map<JsonLimitsConfig, JEVisObject> limitsConfigs = new HashMap<>();
                    try {
                        valueAttribute = selectedObject.getAttribute("Value");
                        for (JEVisObject jeVisObject : selectedObject.getChildren(cleanDataClass, false)) {
                            diffAttribute = jeVisObject.getAttribute("Conversion to Differential");
                            CleanDataObject cleanDataObject = new CleanDataObject(jeVisObject, new ObjectHandler(ds));
                            limitsConfigs.put(cleanDataObject.getLimitsConfig().get(0), jeVisObject);
                        }
                    } catch (JEVisException e) {
                        logger.error("Could not get value attribute of object {}:{}", selectedObject.getName(), selectedObject.getID(), e);
                    }
                    DateTime ts = new DateTime(
                            datePicker.valueProperty().get().getYear(), datePicker.valueProperty().get().getMonthValue(), datePicker.valueProperty().get().getDayOfMonth(),
                            timePicker.valueProperty().get().getHour(), timePicker.valueProperty().get().getMinute(), timePicker.valueProperty().get().getSecond());

                    if (valueAttribute != null) {

                        JEVisSample diffSample = null;
                        Boolean isDiff = false;
                        if (diffAttribute != null && diffAttribute.hasSample()) {
                            diffSample = diffAttribute.getLatestSample();
                            try {
                                isDiff = diffSample.getValueAsBoolean();
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                        }

                        if (isDiff) {
                            if (lastValue != null && lastValue > newVal) {
                                Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.differror"));
                                warning.setResizable(true);
                                JEVisAttribute finalValueAttribute = valueAttribute;
                                Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                        buildSample(finalValueAttribute, ts, newVal);
                                    } else {

                                    }
                                }));

                            } else {
                                boolean hasError = false;
                                DateTime prevTs = ts.minus(valueAttribute.getInputSampleRate());
                                List<JEVisSample> previousSample = valueAttribute.getSamples(prevTs, prevTs);
                                Double prevValue = newVal;
                                try {
                                    prevValue = previousSample.get(previousSample.size() - 1).getValueAsDouble();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                for (Map.Entry<JsonLimitsConfig, JEVisObject> c : limitsConfigs.entrySet()) {
                                    double newDiff = newVal - prevValue;
                                    if (newDiff < Double.parseDouble(c.getKey().getMin())) {
                                        hasError = true;

                                        Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.diff.smaller") + " " +
                                                newDiff + " < " + Double.parseDouble(c.getKey().getMin()));
                                        warning.setResizable(true);
                                        JEVisAttribute finalValueAttribute = valueAttribute;
                                        Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                buildSample(finalValueAttribute, ts, newVal);
                                            } else {

                                            }
                                        }));
                                    } else if (newDiff > Double.parseDouble(c.getKey().getMax())) {
                                        hasError = true;

                                        Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.diff.bigger") + " " +
                                                newDiff + " > " + Double.parseDouble(c.getKey().getMax()));
                                        warning.setResizable(true);
                                        JEVisAttribute finalValueAttribute = valueAttribute;
                                        Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                buildSample(finalValueAttribute, ts, newVal);
                                            } else {

                                            }
                                        }));
                                    }
                                }

                                if (!hasError) {
                                    buildSample(valueAttribute, ts, newVal);
                                }
                            }
                        } else {
                            boolean hasError = false;
                            for (Map.Entry<JsonLimitsConfig, JEVisObject> c : limitsConfigs.entrySet()) {
                                if (newVal < Double.parseDouble(c.getKey().getMin())) {
                                    hasError = true;

                                    Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.nodiff.smaller") + " " +
                                            newVal + " < " + Double.parseDouble(c.getKey().getMin()));
                                    warning.setResizable(true);
                                    JEVisAttribute finalValueAttribute = valueAttribute;
                                    Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                        if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                            buildSample(finalValueAttribute, ts, newVal);
                                        } else {

                                        }
                                    }));
                                } else if (newVal > Double.parseDouble(c.getKey().getMax())) {
                                    hasError = true;

                                    Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.nodiff.bigger") + " " +
                                            newVal + " > " + Double.parseDouble(c.getKey().getMax()));
                                    warning.setResizable(true);
                                    JEVisAttribute finalValueAttribute = valueAttribute;
                                    Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                        if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                            buildSample(finalValueAttribute, ts, newVal);
                                        } else {

                                        }
                                    }));
                                }
                            }

                            if (!hasError) {
                                buildSample(valueAttribute, ts, newVal);
                            }
                        }

                    }

                } else {
                    Alert warning = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("plugin.object.dialog.data.error.number"));
                    warning.setResizable(true);
                    warning.showAndWait();
                }
            }
        });

        int row = 0;
        gridPane.add(idLabel, 0, row);
//        gridPane.add(diffSwitchLabel, 2, row);
        gridPane.add(dateLabel, 3, row, 2, 1);
        gridPane.add(valueLabel, 5, row);
        row++;
        gridPane.add(searchIdField, 0, row);
        gridPane.add(idBox, 1, row, 2, 1);
//        gridPane.add(diffSwitch, 2, row);
        gridPane.add(datePicker, 3, row);
        gridPane.add(timePicker, 4, row);
        gridPane.add(doubleField, 5, row);
        row++;
        gridPane.add(sep, 0, row, 6, 1);
        row++;
        gridPane.add(lastTSLabel, 1, row);
        gridPane.add(lastValueLabel, 2, row);
        gridPane.add(cancel, 4, row);
        gridPane.add(confirm, 5, row);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.centerOnScreen();

        GridPane.setHgrow(idBox, Priority.ALWAYS);
        GridPane.setFillWidth(idBox, true);

        stage.showAndWait();

        return response;
    }

    private void buildSample(JEVisAttribute valueAttribute, DateTime ts, Double newVal) {
        JEVisSample sample = null;
        try {
            sample = valueAttribute.buildSample(ts, newVal, I18n.getInstance().getString("menu.file.import.manual") + " " + DateTime.now());
        } catch (JEVisException e) {
            logger.error("Could not build sample with value {} and ts {}", newVal, ts, e);
        }
        if (sample != null) {
            try {
                sample.commit();
                String message = sample.getTimestamp() + " : " + sample.getValueAsDouble() + " " + I18n.getInstance().getString("plugin.object.dialog.data.import");
                Alert ok = new Alert(Alert.AlertType.INFORMATION, message);
                ok.setResizable(true);
                ok.showAndWait();
            } catch (JEVisException e) {
                logger.error("Could not commit sample {}", sample, e);
            }
        }
    }

    private void loadLastValue() {
        if (selectedObject != null) {
            JEVisAttribute valueAttribute = null;
            try {
                valueAttribute = selectedObject.getAttribute("Value");
            } catch (JEVisException e) {
                logger.error("Could not get value attribute of object {}:{}", selectedObject.getName(), selectedObject.getID(), e);
            }

            JEVisSample sample = null;
            DateTime lastTS = null;
            lastValue = null;
            if (valueAttribute != null && valueAttribute.hasSample())
                try {
                    sample = valueAttribute.getLatestSample();
                    if (sample != null) {
                        lastTS = sample.getTimestamp();
                        lastValue = sample.getValueAsDouble();
                        if (lastTS != null && lastValue != null) {
                            DateTime finalLastTS = lastTS;
                            Double finalLastValue = lastValue;
                            Platform.runLater(() -> {
                                this.lastTSLabel.setText(finalLastTS.toString("yyyy-MM-dd HH:mm") + " : ");

                                this.lastValueLabel.setText(numberFormat.format(finalLastValue));
                            });
                        }
                    }
                } catch (JEVisException e) {
                    logger.error("Could not get last sample.", e);
                }
        }
    }
}
