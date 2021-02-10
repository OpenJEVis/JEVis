package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
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
import org.joda.time.Period;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnterDataDialog extends Dialog implements EventTarget {
    private static final Logger logger = LogManager.getLogger(EnterDataDialog.class);
    public static String ICON = "Startup Wizard_18228.png";
    private final JEVisDataSource ds;
    private final ObjectRelations objectRelations;

    private JEVisObject selectedObject;
    private final TextField doubleField = new TextField();
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
    private final SimpleBooleanProperty isConversionToDifferential = new SimpleBooleanProperty(false);
    private JEVisAttribute target = null;
    private final Label messageLabel = new Label(I18n.getInstance().getString("plugin.object.dialog.data.message.notdifferential"));
    private final DataTypeBox dataTypeBox = new DataTypeBox();
    private final Label idLabel = new Label(I18n.getInstance().getString("plugin.graph.export.text.id"));
    private final Label valueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value"));
    private final Label dateTypeLabel = new Label(I18n.getInstance().getString("plugin.object.dialog.data.datetype.label"));
    private final TextField searchIdField = new TextField();
    private final Button treeButton = new Button(I18n
            .getInstance().getString("plugin.object.attribute.target.button"),
            JEConfig.getImage("folders_explorer.png", 18, 18));
    private final YearBox yearBox = new YearBox();
    private final DayBox dayBox = new DayBox();
    private final MonthBox monthBox = new MonthBox();
    private final Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
    private final JFXDatePicker datePicker = new JFXDatePicker(LocalDate.now());
    private final JFXTimePicker timePicker = new JFXTimePicker(LocalTime.of(0, 0, 0));
    private Object window;
    private boolean selectable = true;
    private DateTime lastTS;
    private List<JEVisSample> conversionDifferential;

    public EnterDataDialog(JEVisDataSource dataSource) {
        super();

        this.ds = dataSource;
        this.objectRelations = new ObjectRelations(ds);
        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);
        init();
    }

    public void init() {
        GridPane gridPane = buildForm();
        this.getDialogPane().setContent(gridPane);
        this.window = this;

        this.initStyle(StageStyle.UTILITY);
        this.initModality(Modality.APPLICATION_MODAL);
        this.initOwner(JEConfig.getStage());
        this.setTitle(I18n.getInstance().getString("plugin.object.dialog.data.title"));
        this.initOwner(JEConfig.getStage());
        this.setResizable(true);
        this.getDialogPane().setPrefWidth(350);
        this.getDialogPane().setPrefHeight(300);

        this.getDialogPane().getScene().getRoot().setEffect(new DropShadow());
        this.getDialogPane().getScene().setFill(Color.TRANSPARENT);

        this.timePicker.set24HourView(true);
        this.timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        this.isConversionToDifferential.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    messageLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.message.differential"));
                    dataTypeBox.getSelectionModel().select(EnterDataTypes.DAY);
                });
            } else {
                Platform.runLater(() -> {
                    messageLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.message.notdifferential"));
                    dataTypeBox.selectFromPeriod(selectedObject);
                });
            }
        });

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


        getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.APPLY);

        getDialogPane().lookupButton(ButtonType.APPLY).addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
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
                                        warning.showAndWait().ifPresent(response -> {
                                            if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                buildSample(finalValueAttribute, finalTs, newVal);
                                            } else {

                                            }
                                        });

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
                                                warning.showAndWait().ifPresent(response -> {
                                                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                        buildSample(finalValueAttribute, finalTs1, newVal);
                                                    } else {

                                                    }
                                                });
                                            } else if (newDiff > Double.parseDouble(c.getKey().getMax())) {
                                                hasError = true;

                                                Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.diff.bigger") + " " +
                                                        newDiff + " > " + Double.parseDouble(c.getKey().getMax()));
                                                warning.setResizable(true);
                                                JEVisAttribute finalValueAttribute = valueAttribute;
                                                DateTime finalTs2 = ts;
                                                warning.showAndWait().ifPresent(response -> {
                                                    if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                        buildSample(finalValueAttribute, finalTs2, newVal);
                                                    } else {

                                                    }
                                                });
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
                                            warning.showAndWait().ifPresent(response -> {
                                                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                    buildSample(finalValueAttribute, finalTs3, newVal);
                                                } else {

                                                }
                                            });
                                        } else if (newVal > Double.parseDouble(c.getKey().getMax())) {
                                            hasError = true;

                                            Alert warning = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.nodiff.bigger") + " " +
                                                    newVal + " > " + Double.parseDouble(c.getKey().getMax()));
                                            warning.setResizable(true);
                                            JEVisAttribute finalValueAttribute = valueAttribute;
                                            DateTime finalTs4 = ts;
                                            warning.showAndWait().ifPresent(response -> {
                                                if (response.getButtonData().getTypeCode().equals(ButtonType.OK.getButtonData().getTypeCode())) {
                                                    buildSample(finalValueAttribute, finalTs4, newVal);
                                                } else {

                                                }
                                            });
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
    }

    public void showPopup(Node parent, String headerText) {
        this.setHeaderText(headerText);
        this.getDialogPane().setContent(buildForm());

//        Button pButton = (Button) parent;
//        Bounds layoutBounds = pButton.localToScene(pButton.getLayoutBounds());

//        System.out.println("X: " + layoutBounds.getMinX() + " Y:" + layoutBounds.getMinY());

//        this.getDialogPane().getScene().getWindow().setX(layoutBounds.getMinX() + (layoutBounds.getWidth() / 2));
//        this.getDialogPane().getScene().getWindow().setX(layoutBounds.getMinY() + (layoutBounds.getHeight() / 2));

        this.showAndWait();
    }

    public ObjectProperty<JEVisSample> getNewSampleProperty() {
        return newSampleProperty;
    }

    public void setSample(JEVisSample sample) {
        initSample = sample;
    }

    public void setTarget(boolean selectable, JEVisAttribute target) {
        this.selectable = selectable;
        this.target = target;
        this.selectedObject = target.getObject();

        Platform.runLater(() -> {

            treeButton.setText(selectedObject.getName());
            searchIdField.setText(selectedObject.getID().toString());
            loadLastValue();
        });
    }

    public void setShowValuePrompt(boolean showValuePrompt) {
        this.showValuePrompt = showValuePrompt;
    }


    private GridPane buildForm() {
        GridPane gridPane = new GridPane();

        try {
            if (initSample != null && showValuePrompt) {

                doubleField.setPromptText(initSample.getValueAsString());
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


        treeButton.setDisable(!selectable);
        searchIdField.setDisable(!selectable);
        if (selectedObject != null) {
            searchIdField.setText("[" + selectedObject.getID().toString() + "] " + selectedObject.getName());
        }

        yearBox.setTS(getNextTS());
        monthBox.setRelations(yearBox, dayBox, getNextTS());
        yearBox.setRelations(monthBox, dayBox);

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

        gridPane.setPadding(new Insets(12));
        gridPane.setVgap(8);
        gridPane.setHgap(8);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        int row = 0;

        gridPane.add(idLabel, 0, row, 1, 1);
        gridPane.add(treeButton, 1, row, 2, 1);
        row++;

        gridPane.add(searchIdField, 0, row, 3, 1);
        row++;

        gridPane.add(messageLabel, 0, row, 3, 1);
        row++;

        gridPane.add(dateTypeLabel, 0, row, 1, 1);
        gridPane.add(dataTypeBox, 1, row, 2, 1);
        row++;
        row++;

        gridPane.addRow(row, valueLabel, doubleField, unitField);
        row++;

        gridPane.add(new Label(I18n.getInstance().getString("status.table.captions.lastrawvalue")), 0, row, 1, 1);
        gridPane.add(lastValueLabel, 1, row, 2, 1);
        row++;
        gridPane.add(sep, 0, row, 3, 1);
        row++;

        filterGridPane(gridPane, yearBox, dayBox, monthBox, dateLabel, datePicker, timePicker, dataTypeBox.getSelectionModel().getSelectedItem());

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints(120);
        ColumnConstraints col3 = new ColumnConstraints(80);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);


        GridPane.setHgrow(treeButton, Priority.ALWAYS);
        GridPane.setFillWidth(treeButton, true);

        Platform.runLater(doubleField::requestFocus);

        dataTypeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                filterGridPane(gridPane, yearBox, dayBox, monthBox, dateLabel, datePicker, timePicker, newValue);
            }
        });

        return gridPane;
    }

    private DateTime getNextTS() {
        DateTime nextTS = lastTS;

        JEVisAttribute periodAttribute = null;
        if (selectedObject != null) {
            try {
                periodAttribute = selectedObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            } catch (JEVisException e) {
                logger.error("Could not get value attribute", e);
            }
        }

        if (periodAttribute != null) {

            Period p = Period.ZERO;
            try {
                p = new Period(periodAttribute.getLatestSample().getValueAsString());
            } catch (JEVisException e) {
                e.printStackTrace();
            }

            if (p.equals(Period.minutes(15))) {
                nextTS = lastTS.plusMinutes(15).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.days(1))) {
                nextTS = lastTS.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.weeks(1))) {
                nextTS = lastTS.plusWeeks(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.months(1))) {
                nextTS = lastTS.plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.years(1))) {
                nextTS = lastTS.plusYears(1).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                try {
                    nextTS = lastTS.plus(p.toStandardDuration().getMillis());
                } catch (Exception e) {
                    logger.error("Could not determine period", e);
                }
            }
        }

        return nextTS;
    }

    private void filterGridPane(GridPane gridPane, YearBox yearBox, DayBox dayBox, MonthBox monthBox, Label dateLabel, JFXDatePicker datePicker, JFXTimePicker timePicker, EnterDataTypes newValue) {
        switch (newValue) {
            case YEAR:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 4, 3, 1));
                break;
            case MONTH:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 4, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 1, 4, 1, 1));
                break;
            case DAY:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.add(yearBox, 0, 4, 1, 1));
                Platform.runLater(() -> gridPane.add(monthBox, 1, 4, 1, 1));
                Platform.runLater(() -> gridPane.add(dayBox, 2, 4, 1, 1));
                break;
            case SPECIFIC_DATETIME:
                Platform.runLater(() -> gridPane.getChildren().removeAll(dateLabel, datePicker, timePicker, yearBox, monthBox, dayBox));
                Platform.runLater(() -> gridPane.addRow(4, dateLabel, datePicker, timePicker));
                break;
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
                Platform.runLater(ok::showAndWait);

                Platform.runLater(() -> doubleField.setText(""));
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

            try {
                JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
                if (selectedObject.getJEVisClassName().equals("Data")) {
                    JEVisObject cleanDataObject = selectedObject.getChildren(cleanDataClass, true).get(0);
                    JEVisAttribute conversionToDiffAttribute = cleanDataObject.getAttribute(CleanDataObject.AttributeName.CONVERSION_DIFFERENTIAL.getAttributeName());

                    if (conversionToDiffAttribute != null) {
                        conversionDifferential = conversionToDiffAttribute.getAllSamples();

                        if (!conversionDifferential.isEmpty()) {
                            JEVisSample sample = conversionDifferential.get(conversionDifferential.size() - 1);
                            if (sample != null) {
                                isConversionToDifferential.set(sample.getValueAsBoolean());
                            }
                        }
                    }
                }
            } catch (JEVisException e) {
                logger.error("Could not determine diff or not", e);
            }

            JEVisSample sample = null;
            lastTS = null;
            lastValue = null;
            if (valueAttribute != null && valueAttribute.hasSample())
                try {
                    ds.reloadAttribute(valueAttribute);
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
