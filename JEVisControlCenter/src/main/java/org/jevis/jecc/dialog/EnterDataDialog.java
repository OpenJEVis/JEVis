package org.jevis.jecc.dialog;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.enums.FloatMode;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalTimePicker;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.constants.EnterDataTypes;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.relationship.ObjectRelations;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.TopMenu;
import org.jevis.jecc.application.control.DataTypeBox;
import org.jevis.jecc.application.control.DayBox;
import org.jevis.jecc.application.control.MonthBox;
import org.jevis.jecc.application.control.YearBox;
import org.jevis.jecc.application.jevistree.UserSelection;
import org.jevis.jecc.application.jevistree.filter.JEVisTreeFilter;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnterDataDialog extends Dialog implements EventTarget {
    public static final String CONVERSION_TO_DIFFERENTIAL_ATTRIBUTE_NAME = "Conversion to Differential";
    private static final Logger logger = LogManager.getLogger(EnterDataDialog.class);
    public static String ICON = "Startup Wizard_18228.png";
    private final JEVisDataSource ds;
    private final ObjectRelations objectRelations;
    private final MFXTextField doubleField = new MFXTextField();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private final Label messageLabel = new Label(I18n.getInstance().getString("plugin.object.dialog.data.message.notdiffquantity"));
    private final ObjectProperty<JEVisSample> newSampleProperty = new SimpleObjectProperty<>();
    private final Label unitField = new Label();
    private final Label lastTSLabel = new Label();
    private final Label lastValueLabel = new Label();
    private final SimpleBooleanProperty isConversionToDifferential = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty isQuantity = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Period> period = new SimpleObjectProperty<>(Period.ZERO);
    private final DataTypeBox dataTypeBox = new DataTypeBox();
    private final Label idLabel = new Label(I18n.getInstance().getString("plugin.graph.export.text.id"));
    private final Label valueLabel = new Label(I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value"));
    private final Label lastRawValueLabel = new Label(I18n.getInstance().getString("status.table.captions.lastrawvalue"));
    private final Label dateTypeLabel = new Label(I18n.getInstance().getString("plugin.object.dialog.data.datetype.label"));
    private final MFXTextField searchIdField = new MFXTextField();
    private final MFXButton treeButton = new MFXButton(I18n
            .getInstance().getString("plugin.object.attribute.target.button"),
            ControlCenter.getImage("folders_explorer.png", 18, 18));
    private final Label targetLabel = new Label();
    private final YearBox yearBox = new YearBox();
    private final DayBox dayBox = new DayBox();
    private final MonthBox monthBox = new MonthBox();
    private final Label dateLabel = new Label(I18n.getInstance().getString("graph.dialog.column.timestamp"));
    private final MFXDatePicker datePicker = new MFXDatePicker(I18n.getInstance().getLocale(), YearMonth.now());
    private final LocalTimePicker timePicker = new LocalTimePicker(LocalTime.of(0, 0, 0));
    private final SimpleObjectProperty<DateTime> lastTS = new SimpleObjectProperty<>(new DateTime(1990, 1, 1, 0, 0, 0, 0));
    private final GridPane gridPane = new GridPane();
    private JEVisObject selectedObject;
    private Response response;
    private Double lastValue;
    private JEVisClass dataClass;
    private JEVisClass cleanDataClass;
    private boolean showValuePrompt = false;
    private JEVisSample initSample = null;
    private JEVisAttribute target = null;
    private JEVisClass baseDataClass;
    private boolean selectable = true;
    private boolean showDetailedTarget = true;

    public EnterDataDialog(JEVisDataSource dataSource) {
        super();
        setTitle(I18n.getInstance().getString("plugin.object.dialog.data.title"));
        setHeaderText(I18n.getInstance().getString("plugin.object.dialog.data.header"));
        setResizable(true);
        initOwner(ControlCenter.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        this.ds = dataSource;
        this.objectRelations = new ObjectRelations(ds);
        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);
        this.doubleField.setFloatMode(FloatMode.DISABLED);
        this.searchIdField.setFloatMode(FloatMode.DISABLED);

        init();
    }

    public void init() {
        GridPane gridPane = buildForm();

//        this.timePicker.set24HourView(true);
//        this.timePicker.setConverter(new LocalTimeStringConverter(FormatStyle.SHORT));

        treeButton.setOnAction(event -> {
            TargetHelper th = null;
            if (selectedObject != null) {
                th = new TargetHelper(ds, selectedObject.getID().toString());
            }

            List<JEVisTreeFilter> allFilter = new ArrayList<>();
            JEVisTreeFilter allCurrentClassFilter = SelectTargetDialog.buildAllDataFilter();
            allFilter.add(allCurrentClassFilter);

            List<UserSelection> openList = new ArrayList<>();

            if (th != null && !th.getObject().isEmpty()) {
                for (JEVisObject obj : th.getObject())
                    openList.add(new UserSelection(UserSelection.SelectionType.Object, obj));
            }

            SelectTargetDialog selectTargetDialog = new SelectTargetDialog(allFilter, allCurrentClassFilter, null, SelectionMode.SINGLE, ds, openList);

            selectTargetDialog.setOnCloseRequest(event1 -> {
                if (selectTargetDialog.getResponse() == SelectTargetDialog.Response.OK) {
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
            selectTargetDialog.show();
        });

        ButtonType showMoreType = new ButtonType(I18n.getInstance().getString("plugin.object.dialog.data.history"), ButtonBar.ButtonData.HELP);
        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(showMoreType, cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        Button showMoreButton = (Button) this.getDialogPane().lookupButton(showMoreType);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setPadding(new Insets(8, 0, 8, 0));

        VBox vBox = new VBox(6, gridPane, separator);
        getDialogPane().setContent(vBox);
        getDialogPane().setMinWidth(450);

        okButton.setOnAction(event -> {
//            event.consume();
            if (selectedObject != null) {
                try {
                    if (ds.getCurrentUser().canWrite(selectedObject.getID())) {
                        DoubleValidator validator = new DoubleValidator();
                        Double newVal = validator.validate(doubleField.getText(), I18n.getInstance().getLocale());

                        if (newVal != null) {

                            JEVisAttribute valueAttribute = null;
                            JEVisAttribute periodAttribute = null;
                            JEVisAttribute diffAttribute = null;
                            Map<JsonLimitsConfig, JEVisObject> limitsConfigs = new HashMap<>();
                            try {
                                valueAttribute = selectedObject.getAttribute("Value");

                                if (selectedObject != null && selectedObject.getJEVisClass().equals(baseDataClass)) {
                                    diffAttribute = selectedObject.getAttribute(CONVERSION_TO_DIFFERENTIAL_ATTRIBUTE_NAME);
                                }

                                for (JEVisObject jeVisObject : selectedObject.getChildren(cleanDataClass, false)) {
                                    diffAttribute = jeVisObject.getAttribute(CONVERSION_TO_DIFFERENTIAL_ATTRIBUTE_NAME);
                                    CleanDataObject cleanDataObject = new CleanDataObject(jeVisObject);
                                    if (cleanDataObject.getLimitsConfig().size() > 0) {
                                        limitsConfigs.put(cleanDataObject.getLimitsConfig().get(0), jeVisObject);
                                    }
                                }
                                periodAttribute = selectedObject.getAttribute("Period");
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
                                            timePicker.getLocalTime().getHour(),
                                            timePicker.getLocalTime().getMinute(),
                                            timePicker.getLocalTime().getSecond());
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
                                                close();
                                            } else {

                                            }
                                        });

                                    } else {
                                        boolean hasError = false;
                                        Period periodForDate = CleanDataObject.getPeriodForDate(periodAttribute.getObject(), ts);
                                        DateTime prevTs = ts.minus(periodForDate);
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
                                            close();
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
                                        close();
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

        cancelButton.setOnAction(event -> close());

        showMoreButton.setOnAction(actionEvent -> {
            JEVisAttribute value = null;
            try {
                value = selectedObject.getAttribute("Value");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (value != null) {
                DataDialog dataDialog = new DataDialog(value);
                dataDialog.show();
            }
        });
    }

    private void updateView() {
        if (isConversionToDifferential.get()) {
            Platform.runLater(() -> {
                messageLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.message.differential"));
                dataTypeBox.setVisible(false);

                dataTypeBox.selectItem(EnterDataTypes.DAY);
                dateTypeLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.datetype.date"));
                valueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.meterreading"));
                lastRawValueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.lastmeterreading"));

            });
        } else if (!isConversionToDifferential.get()) {
            Period p = dataTypeBox.selectFromPeriod(selectedObject);
            String periodName = p.toString(PeriodFormat.wordBased(I18n.getInstance().getLocale()));


            Platform.runLater(() -> {

                if (isQuantity.get()) {
                    messageLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.message.notdiffquantity"));
                } else {
                    messageLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.message.notdiffmean"));
                }


                dataTypeBox.setVisible(false);

                if (!p.equals(Period.ZERO)) {
                    dateTypeLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.datetype.period") + ", " + periodName);

                    if (isQuantity.get()) {
                        valueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.consumption"));
                        lastRawValueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.lastconsumption"));
                    } else {
                        valueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.meanvalue"));
                        lastRawValueLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.value.lastmeanvalue"));
                    }

                } else {
                    dateTypeLabel.setText(I18n.getInstance().getString("plugin.object.dialog.data.datetype.valid"));
                    valueLabel.setText(I18n.getInstance().getString("plugin.dashboard.tablewidget.column.value"));
                    lastRawValueLabel.setText(I18n.getInstance().getString("status.table.captions.lastrawvalue"));
                }
            });
        }
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
            removeNode(0, 0, gridPane);
            removeNode(1, 0, gridPane);
            removeNode(0, 1, gridPane);

            if (showDetailedTarget) {
                treeButton.setText(selectedObject.getName());
                searchIdField.setText(selectedObject.getID().toString());

                gridPane.add(idLabel, 0, 0, 1, 1);
                gridPane.add(treeButton, 1, 0, 2, 1);
                gridPane.add(searchIdField, 0, 1, 3, 1);
            } else {
                targetLabel.setText(selectedObject.getName());
                gridPane.add(targetLabel, 0, 0, 3, 1);
            }

            loadLastValue();
        });
    }

    private void removeNode(final int row, final int column, GridPane gridPane) {
        ObservableList<Node> children = gridPane.getChildren();
        for (Node node : children) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                gridPane.getChildren().remove(node);
                break;
            }
        }
    }

    public void setShowValuePrompt(boolean showValuePrompt) {
        this.showValuePrompt = showValuePrompt;
    }


    private GridPane buildForm() {
        gridPane.getChildren().clear();

        loadLastValue();

        try {
            if (initSample != null && showValuePrompt) {
                doubleField.setText(numberFormat.format(initSample.getValueAsDouble()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<JEVisObject> allData = new ArrayList<>();
        HashMap<Long, JEVisObject> map = new HashMap<>();
        try {
            dataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");
            baseDataClass = ds.getJEVisClass("Base Data");
            allData = ds.getObjects(dataClass, false);
            map = allData.stream().collect(Collectors.toMap(JEVisObject::getID, object -> object, (a, b) -> b, HashMap::new));
        } catch (Exception e) {
            e.printStackTrace();
        }


        treeButton.setDisable(!selectable);
        searchIdField.setDisable(!selectable);
        if (selectedObject != null) {
            searchIdField.setText("[" + selectedObject.getID().toString() + "] " + selectedObject.getName());
        }

        DateTime nextTS = getNextTS();
        yearBox.setTS(nextTS);
        monthBox.setRelations(yearBox, dayBox, nextTS);
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

        gridPane.add(lastRawValueLabel, 0, row, 1, 1);
        gridPane.add(lastValueLabel, 1, row, 2, 1);
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
        DateTime nextTS = lastTS.get();

        JEVisAttribute periodAttribute = null;
        if (selectedObject != null) {
            try {
                periodAttribute = selectedObject.getAttribute(CleanDataObject.AttributeName.PERIOD.getAttributeName());
            } catch (Exception e) {
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
                nextTS = lastTS.get().plusMinutes(15).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.days(1))) {
                nextTS = lastTS.get().plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.weeks(1))) {
                nextTS = lastTS.get().plusWeeks(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.months(1))) {
                nextTS = lastTS.get().plusMonths(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else if (p.equals(Period.years(1))) {
                nextTS = lastTS.get().plusYears(1).withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
            } else {
                try {
                    nextTS = lastTS.get().plus(p.toStandardDuration().getMillis());
                } catch (Exception e) {
                    logger.error("Could not determine period", e);
                }
            }
        }

        return nextTS;
    }

    private void filterGridPane(GridPane gridPane, YearBox yearBox, DayBox dayBox, MonthBox monthBox, Label dateLabel, MFXDatePicker datePicker, LocalTimePicker timePicker, EnterDataTypes newValue) {
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
        } catch (Exception e) {
            logger.error("Could not build sample with value {} and ts {}", newVal, ts, e);
        }
        if (sample != null) {
            try {
                sample.commit();
                String message = sample.getTimestamp() + " : " + sample.getValueAsDouble() + " " + I18n.getInstance().getString("plugin.object.dialog.data.import");
                Alert ok = new Alert(Alert.AlertType.INFORMATION, message);
                ok.setResizable(true);
                Platform.runLater(ok::showAndWait);
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, I18n.getInstance().getString("plugin.object.dialog.data.resetdependencies"));
                confirm.setOnCloseRequest(dialogEvent -> {
                    if (confirm.getResult() == ButtonType.OK) {
                        List<JEVisObject> parentObject = new ArrayList<>();
                        parentObject.add(valueAttribute.getObject());
                        CommonMethods.cleanDependentObjects(parentObject, ts);
                    }
                });
                ok.setOnCloseRequest(dialogEvent -> {
                    confirm.show();
                });

                Platform.runLater(() -> doubleField.setText(""));
                newSampleProperty.setValue(sample);
                loadLastValue();

            } catch (Exception e) {
                logger.error("Could not commit sample {}", sample, e);
            }
        }
    }

    private void loadLastValue() {
        if (selectedObject != null) {
            JEVisAttribute valueAttribute = null;
            WorkDays workDays = new WorkDays(selectedObject);
            String unitString = "";
            try {
                valueAttribute = selectedObject.getAttribute("Value");

                JEVisUnit displayUnit = valueAttribute.getDisplayUnit();
                unitString = UnitManager.getInstance().format(displayUnit);
                if (!unitString.equals("")) {
                    String finalUnitString = unitString;
                    Platform.runLater(() -> unitField.setText(finalUnitString));
                }
            } catch (Exception e) {
                logger.error("Could not get value attribute of object {}:{}", selectedObject.getName(), selectedObject.getID(), e);
            }

            JEVisSample sample = null;
            lastValue = null;

            try {
                if (valueAttribute != null && valueAttribute.hasSample()) {
                    ds.reloadAttribute(valueAttribute);
                    sample = valueAttribute.getLatestSample();
                    if (sample != null) {
                        lastTS.set(sample.getTimestamp());
                        lastValue = sample.getValueAsDouble();
                        if (lastValue != null) {

                            getCleanDataSettings();

                            if (workDays.isCustomWorkDay() && workDays.getWorkdayStart().isAfter(workDays.getWorkdayEnd()) && PeriodHelper.isGreaterThenDays(period.get())) {
                                lastTS.set(lastTS.get().plusDays(1));
                            }

                            String normalPattern = PeriodHelper.getFormatString(period.get(), isConversionToDifferential.get());
                            String valueString = numberFormat.format(lastValue) + unitString + " @ " + lastTS.get().toString(normalPattern);

                            Platform.runLater(() -> {
                                lastTSLabel.setText(lastTS.get().toString(normalPattern) + " : ");
                                lastValueLabel.setText(valueString);
                            });

                        }
                    }
                } else {
                    getCleanDataSettings();
                }
            } catch (Exception e) {
                logger.error("Could not get last sample.", e);
            }
        }
    }

    private void getCleanDataSettings() throws JEVisException {
        if (!selectedObject.getChildren().isEmpty()) {
            JEVisObject cleanDataObject = selectedObject.getChildren(cleanDataClass, true).get(0);
            CleanDataObject cdo = new CleanDataObject(cleanDataObject);
            isQuantity.set(cdo.getValueIsQuantity());
            isConversionToDifferential.set(CleanDataObject.isDifferentialForDate(cdo.getDifferentialRules(), lastTS.get()));
            period.set(CleanDataObject.getPeriodForDate(cdo.getCleanDataPeriodAlignment(), lastTS.get()));
        } else {
            period.set(CleanDataObject.getPeriodForDate(selectedObject, lastTS.get()));

            if (selectedObject != null && selectedObject.getJEVisClass().equals(baseDataClass)) {
                try {
                    JEVisType isQuantity = selectedObject.getJEVisClass().getType("Value is a Quantity");
                    this.isQuantity.set(DatabaseHelper.getObjectAsBoolean(selectedObject, isQuantity));
                    JEVisType isConversionToDifferential = selectedObject.getJEVisClass().getType(CONVERSION_TO_DIFFERENTIAL_ATTRIBUTE_NAME);
                    this.isConversionToDifferential.set(DatabaseHelper.getObjectAsBoolean(selectedObject, isConversionToDifferential));
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

        try {
            DateTime nextTS = getNextTS();
            yearBox.setTS(nextTS);
            Platform.runLater(() -> {
                monthBox.selectIndex(nextTS.getMonthOfYear() - 1);
                dayBox.selectIndex(nextTS.getDayOfMonth() - 1);
            });
        } catch (Exception e) {
            logger.error(e);
        }

        updateView();
    }

    public boolean isShowDetailedTarget() {
        return showDetailedTarget;
    }

    public void setShowDetailedTarget(boolean showDetailedTarget) {
        this.showDetailedTarget = showDetailedTarget;
    }
}
