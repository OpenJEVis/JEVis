package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXTimePicker;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.converter.LocalTimeStringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.control.DataTypeBox;
import org.jevis.jeconfig.application.control.DayBox;
import org.jevis.jeconfig.application.control.MonthBox;
import org.jevis.jeconfig.application.control.YearBox;
import org.jevis.jeconfig.application.jevistree.UserSelection;
import org.jevis.jeconfig.application.jevistree.filter.JEVisTreeFilter;
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

    private JEVisObject selectedObject;
    private Stage stage;
    private Response response;


    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private Double lastValue;
    private JEVisClass dataClass;
    private JEVisClass cleanDataClass;
    private final ObjectProperty<JEVisSample> newSampleProperty = new SimpleObjectProperty<>();
    private final Label unitField = new Label();
    private final Label lastTSLabel = new Label();
    private final Label lastValueLabel = new Label();
    private boolean showValuePrompt = false;
    private JEVisSample initSample = null;
    private boolean traceable = true;
    private JEVisAttribute target = null;


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

        GridPane gridPane = buildForm(stage);

        Scene scene = new Scene(gridPane);

        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(JEConfig.getStage());
        stage.setTitle(I18n.getInstance().getString("plugin.object.dialog.data.title"));
//        stage.setHeader(DialogHeader.getDialogHeader(ICON, I18n.getInstance().getString("plugin.object.dialog.data.header")));
        stage.initOwner(JEConfig.getStage());
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(450);
        stage.setScene(scene);
        stage.centerOnScreen();

        stage.getScene().getRoot().setEffect(new DropShadow());
        stage.getScene().setFill(Color.TRANSPARENT);


        stage.showAndWait();
        return response;


    }

    public void showPopup(Node parent) {
        JFXPopup popup = new JFXPopup();
        GridPane gridPane = buildForm(popup);
        popup.setAutoHide(false);
        popup.setAutoFix(true);
        popup.setPopupContent(gridPane);
        popup.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT);

        popup.show(parent);
    }

    public ObjectProperty<JEVisSample> getNewSampleProperty() {
        return newSampleProperty;
    }

    public void setSample(JEVisSample sample) {
        initSample = sample;
    }

    public void setTarget(boolean traceable, JEVisAttribute target) {
        this.traceable = traceable;
        this.target = target;
        this.selectedObject = target.getObject();
    }

    public void setShowValuePrompt(boolean showValuePrompt) {
        this.showValuePrompt = showValuePrompt;
    }


    private GridPane buildForm(Object windows) {
        GridPane gridPane = new GridPane();


        Label idLabel = new Label(I18n.getInstance().getString("plugin.graph.export.text.id"));
        Label valueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value"));
        Label unitLabel = new Label(I18n.getInstance().getString("graph.table.unit"));

        Label unitFieldLastV = new Label();
        TextField doubleField = new TextField();
        TextField searchIdField = new TextField();

        unitFieldLastV.textProperty().bind(unitField.textProperty());

        ScrollPane extendableSearchPane = new ScrollPane();
        extendableSearchPane.setContent(new TableView<>());
        extendableSearchPane.setMaxHeight(1);
        extendableSearchPane.getTransforms().addAll(new Scale(0, 0));

        Rectangle clipRect = new Rectangle();
        clipRect.setHeight(0);
        Button toggleSamples = new Button("+");

        DoubleProperty height = new SimpleDoubleProperty();
        height.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            final Rectangle outputClip = new Rectangle();
            outputClip.setHeight(newValue.doubleValue());
            extendableSearchPane.setClip(outputClip);
            extendableSearchPane.setMinHeight(newValue.doubleValue());
            extendableSearchPane.setMaxHeight(newValue.doubleValue());
        }));
        final KeyValue keyValue1a = new KeyValue(height, 0);
        final KeyValue keyValue2a = new KeyValue(height, 100);
        KeyFrame keyFramea = new KeyFrame(Duration.millis(200), keyValue1a, keyValue2a);
        Timeline timelineUpa = new Timeline();
        timelineUpa.getKeyFrames().add(keyFramea);

        Timeline timelineUp = new Timeline();
        final KeyValue keyValue1 = new KeyValue(clipRect.heightProperty(), 0);
        final KeyValue keyValue2 = new KeyValue(clipRect.heightProperty(), 50);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue1, keyValue2);
        timelineUp.getKeyFrames().add(keyFrame);

        toggleSamples.setOnAction(event -> Platform.runLater(timelineUpa::play));

        try {
            if (initSample != null && showValuePrompt) {

                doubleField.setPromptText(initSample.getValue().toString());
                loadLastValue();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


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

        Button treeButton = new Button(I18n
                .getInstance().getString("plugin.object.attribute.target.button"),
                JEConfig.getImage("folders_explorer.png", 18, 18));

        treeButton.setOnAction(event -> {
            TargetHelper th = null;
            if (selectedObject != null) {
                th = new TargetHelper(ds, selectedObject.getID().toString());
            }

            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allCurrentClassFilter = SelectTargetDialog.buildAllDataFilter();
            allFilter.add(allCurrentClassFilter);

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allCurrentClassFilter, null, SelectionMode.SINGLE);

            List<UserSelection> openList = new ArrayList<>();

            if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject())
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
            }

            if (selectTargetDialog.show(
                    ds,
                    I18n.getInstance().getString("dialog.target.data.title"),
                    openList
            ) == SelectTargetDialog.Response.OK) {
                logger.trace("Selection Done");

                List<UserSelection> selections = selectTargetDialog.getUserSelection();
                for (UserSelection us : selections) {
                    selectedObject = us.getSelectedObject();
                    break;
                }

                treeButton.setText(selectedObject.getName());
                searchIdField.setText(selectedObject.getID().toString());

                loadLastValue();
            }

        });

        treeButton.setDisable(!traceable);
        searchIdField.setDisable(!traceable);
        if (selectedObject != null) {
            searchIdField.setText("[" + selectedObject.getID().toString() + "] " + selectedObject.getName());
        }


        DataTypeBox dataTypeBox = new DataTypeBox();

        YearBox yearBox = new YearBox();
        DayBox dayBox = new DayBox();
        MonthBox monthBox = new MonthBox();

        monthBox.setRelations(yearBox, dayBox);
        yearBox.setRelations(monthBox, dayBox);

        Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
        JFXDatePicker datePicker = new JFXDatePicker(LocalDate.now());
        JFXTimePicker timePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));

        timePicker.set24HourView(true);
        timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));


        HashMap<Long, JEVisObject> finalMap = map;
        searchIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                try {
                    long l = Long.parseLong(newValue);
                    JEVisObject selection = finalMap.get(l);
                    if (selection != null) {
                        selectedObject = selection;
                        treeButton.setText(selection.getName());


                    }
                } catch (Exception ignored) {
                }
            }
        });

        Separator sep = new Separator(Orientation.HORIZONTAL);

        Button confirm = new Button(I18n.getInstance().getString("sampleeditor.confirmationdialog.save"));
        confirm.setDefaultButton(true);
        Button cancel = new Button(I18n.getInstance().getString("attribute.editor.cancel"));
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> closeWindows(windows));
        confirm.setOnAction(event -> {
            if (selectedObject != null) {
                try {
                    if (ds.getCurrentUser().canWrite(selectedObject.getID())) {
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
                                    if (cleanDataObject.getLimitsConfig().size() > 0) {
                                        limitsConfigs.put(cleanDataObject.getLimitsConfig().get(0), jeVisObject);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Could not get value attribute of object {}:{}", selectedObject.getName(), selectedObject.getID(), e);
                            }
                            DateTime ts = null;

                            Integer year = yearBox.getSelectionModel().getSelectedItem();
                            Integer month = monthBox.getSelectionModel().getSelectedIndex() + 1;
                            Integer day = dayBox.getSelectionModel().getSelectedItem();
                            switch (dataTypeBox.getSelectionModel().getSelectedItem()) {
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
                                        DateTime finalTs = ts;
                                        Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                buildSample(finalValueAttribute, finalTs, newVal);
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
                                                DateTime finalTs1 = ts;
                                                Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                        buildSample(finalValueAttribute, finalTs1, newVal);
                                                    } else {

                                                    }
                                                }));
                                            } else if (newDiff > Double.parseDouble(c.getKey().getMax())) {
                                                hasError = true;

                                                Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.diff.bigger") + " " +
                                                        newDiff + " > " + Double.parseDouble(c.getKey().getMax()));
                                                warning.setResizable(true);
                                                JEVisAttribute finalValueAttribute = valueAttribute;
                                                DateTime finalTs2 = ts;
                                                Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                        buildSample(finalValueAttribute, finalTs2, newVal);
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
                                            DateTime finalTs3 = ts;
                                            Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                    buildSample(finalValueAttribute, finalTs3, newVal);
                                                } else {

                                                }
                                            }));
                                        } else if (newVal > Double.parseDouble(c.getKey().getMax())) {
                                            hasError = true;

                                            Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.nodiff.bigger") + " " +
                                                    newVal + " > " + Double.parseDouble(c.getKey().getMax()));
                                            warning.setResizable(true);
                                            JEVisAttribute finalValueAttribute = valueAttribute;
                                            DateTime finalTs4 = ts;
                                            Platform.runLater(() -> warning.showAndWait().ifPresent(response -> {
                                                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                    buildSample(finalValueAttribute, finalTs4, newVal);
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
                    } else {
                        Alert warning = new Alert(Alert.AlertType.WARNING, I18n.getInstance().getString("dialog.warning.notallowed"));
                        warning.setResizable(true);
                        warning.showAndWait();
                    }
                } catch (Exception e) {
                    logger.error("Could not get current User", e);
                }
            }


        });

        HBox buttonBox = new HBox(cancel, confirm);
        buttonBox.setAlignment(Pos.BASELINE_RIGHT);
        buttonBox.setSpacing(8);

        gridPane.setPadding(new Insets(12));
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        int row = 0;
        gridPane.addRow(row, idLabel, searchIdField, treeButton);
        row++;

        gridPane.add(dataTypeBox, 0, row, 3, 1);
        row++;
        row++;

        gridPane.addRow(row, valueLabel, doubleField, unitField);
        row++;

        gridPane.add(new Label("Last Value"), 0, row, 1, 1);
        gridPane.add(lastValueLabel, 1, row, 2, 1);
        row++;

        dataTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                filterGridPane(gridPane, yearBox, dayBox, monthBox, dateLabel, datePicker, timePicker, newValue);
            }
        });

        // gridPane.addRow(3,,lastValueLabel,unitFieldLastV);

        gridPane.add(sep, 0, row, 3, 1);
        row++;

        gridPane.add(buttonBox, 0, row, 3, 1);

        filterGridPane(gridPane, yearBox, dayBox, monthBox, dateLabel, datePicker, timePicker, dataTypeBox.getSelectionModel().getSelectedItem());

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints(120);
        ColumnConstraints col3 = new ColumnConstraints(80);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);


        GridPane.setHgrow(treeButton, Priority.ALWAYS);
        GridPane.setFillWidth(treeButton, true);

        Platform.runLater(doubleField::requestFocus);

        return gridPane;
    }

    private void filterGridPane(GridPane gridPane, YearBox yearBox, DayBox dayBox, MonthBox monthBox, Label dateLabel, JFXDatePicker datePicker, JFXTimePicker timePicker, EnterDataTypes newValue) {
        switch (newValue) {
            case YEAR:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 2, 3, 1));
                break;
            case MONTH:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 2, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 1, 2, 1, 1));
                break;
            case DAY:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 2, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 1, 2, 1, 1));
                Platform.runLater(() -> gridPane.add(dayBox, 2, 2, 1, 1));
                break;
            case SPECIFIC_DATETIME:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.addRow(2, dateLabel, datePicker, timePicker));
                break;
        }
    }

    private void closeWindows(Object windows) {
        if (windows instanceof Stage) {
            ((Stage) windows).close();
            stage.close();
            stage = null;
        } else if (windows instanceof JFXPopup) {
            ((JFXPopup) windows).hide();
        }
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

                newSampleProperty.setValue(sample);
                loadLastValue();

            } catch (JEVisException e) {
                logger.error("Could not commit sample {}", sample, e);
            }
        }
    }

    private void loadLastValue() {
        if (selectedObject != null) {
            JEVisAttribute valueAttribute = null;
            String unitString = "";
            try {
                valueAttribute = selectedObject.getAttribute("Value");

                JEVisUnit displayUnit = valueAttribute.getDisplayUnit();
                unitString = UnitManager.getInstance().format(displayUnit);
                if (!unitString.equals("")) {
                    String finalUnitString = unitString;
                    Platform.runLater(() -> unitField.setText(finalUnitString));
                }
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
                            String finalUnitString = unitString;
                            Platform.runLater(() -> {
                                lastTSLabel.setText(finalLastTS.toString("yyyy-MM-dd HH:mm") + " : ");


                                /**
                                 if (!finalUnitString.equals("")) {
                                 valueString+=" "+finalUnitString;
                                 }
                                 lastValueLabel.setText(valueString);
                                 **/
                                String valueString = numberFormat.format(finalLastValue) + finalUnitString + " @ " + finalLastTS.toString("yyyy-MM-dd HH:mm");
                                lastValueLabel.setText(valueString);
                            });
                        }
                    }
                } catch (Exception e) {
                    logger.error("Could not get last sample.", e);
                }
        }
    }

}
