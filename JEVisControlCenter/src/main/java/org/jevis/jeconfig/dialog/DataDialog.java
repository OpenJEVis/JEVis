package org.jevis.jeconfig.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.TopMenu;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataDialog extends Dialog {
    private static final Logger logger = LogManager.getLogger(DataDialog.class);
    private final String ICON = "1404313956_evolution-tasks.png";
    private final AlphanumComparator ac = new AlphanumComparator();
    private final JEVisAttribute attribute;
    private final DateTimeFormatter dateTimeFormat;
    private final Label messageLabel = new Label("");
    private final Color COLOR_ERROR = Color.INDIANRED;
    private final int ICON_SIZE = 12;
    private final TableView<DataSample> tableView = new TableView<>();
    private final Pagination pagination = new Pagination();
    private final List<DataSample> deleteList = new ArrayList<>();
    private final List<DataSample> data = new ArrayList<>();
    private final JFXDatePicker startDatePicker = new JFXDatePicker(LocalDate.now().minusDays(1));
    private final JFXDatePicker endDatePicker = new JFXDatePicker(LocalDate.now());
    private final WorkDays workDays;
    private final DoubleValidator doubleValidator = DoubleValidator.getInstance();
    private final NumberFormat nf = NumberFormat.getInstance(I18n.getInstance().getLocale());
    private final List<DataSample> changedSamples = new ArrayList<>();
    private final SimpleBooleanProperty saved = new SimpleBooleanProperty(false);
    private final List<DataSample> selection = new ArrayList<>();
    private Response response = Response.CANCEL;
    private int ROWS_PER_PAGE = 25;

    public DataDialog(JEVisAttribute attribute) {
        super();
        setTitle(I18n.getInstance().getString("plugin.enterdata.history.title"));
        setHeaderText(I18n.getInstance().getString("plugin.enterdata.history.header"));
        setResizable(true);
        initOwner(JEConfig.getStage());
        initModality(Modality.APPLICATION_MODAL);
        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        this.attribute = attribute;
        DateTime lastTs = attribute.getTimestampFromLastSample();
        logger.debug("Attribute {} of object {}:{} last ts - {}", attribute.getName(), attribute.getObject().getName(), attribute.getObject().getID(), lastTs);

        if (lastTs != null) {
            LocalDate ld = LocalDate.of(lastTs.getYear(), lastTs.getMonthOfYear(), lastTs.getDayOfMonth());
            startDatePicker.setValue(ld);
            endDatePicker.setValue(ld.plusDays(1));
        } else {
            lastTs = DateTime.now();
        }

        tableView.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        tableView.setMinWidth(450);
        double height = 718;
        tableView.setMinHeight(height);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setEditable(true);
        tableView.setTableMenuButtonVisible(true);

        pagination.setMinWidth(500);
        pagination.setMinHeight(height + 50);

        workDays = new WorkDays(attribute.getObject());
        Period p = CleanDataObject.getPeriodForDate(attribute.getObject(), lastTs);
        logger.debug("Attribute {} of object {}:{} last period - {}", attribute.getName(), attribute.getObject().getName(), attribute.getObject().getID(), p);

        DateTime newStart = lastTs;
        if (p.equals(Period.minutes(1))) {
            ROWS_PER_PAGE = 144;

            newStart = newStart.withHourOfDay(0);
        } else if (p.equals(Period.minutes(5))) {
            ROWS_PER_PAGE = 30;

            newStart = newStart.withHourOfDay(0);
        } else if (p.equals(Period.minutes(15))) {
            ROWS_PER_PAGE = 96;

            newStart = newStart.withHourOfDay(0);
        } else if (p.equals(Period.hours(1))) {
            ROWS_PER_PAGE = 24;

            newStart = newStart.withHourOfDay(0);
        } else if (p.equals(Period.days(1))) {
            ROWS_PER_PAGE = 24;

            newStart = newStart.withDayOfMonth(1);
        } else if (p.equals(Period.months(1))) {
            ROWS_PER_PAGE = 12;

            newStart = newStart.minusYears(1);
        } else if (p.equals(Period.years(1))) {
            ROWS_PER_PAGE = 10;

            newStart = newStart.minusYears(10);
        }

        LocalDate ldMinusP = LocalDate.of(newStart.getYear(), newStart.getMonthOfYear(), newStart.getDayOfMonth());
        if (!ldMinusP.equals(startDatePicker.getValue())) {
            startDatePicker.setValue(ldMinusP);
        }

        boolean isCounter = CleanDataObject.isCounter(attribute.getObject(), attribute.getLatestSample());
        String normalPattern = PeriodHelper.getFormatString(p, isCounter);
        dateTimeFormat = DateTimeFormat.forPattern(normalPattern);
        logger.debug("Attribute {} of object {}:{} pattern - {}", attribute.getName(), attribute.getObject().getName(), attribute.getObject().getID(), normalPattern);

        TableColumn<DataSample, DateTime> dateColumn = buildDateColumn();
        TableColumn<DataSample, Double> valueColumn = buildValueColumn();
        TableColumn<DataSample, String> noteColumn = buildNoteColumn();

        tableView.getColumns().setAll(dateColumn, valueColumn, noteColumn);
        tableView.getSortOrder().setAll(dateColumn);
        noteColumn.setVisible(false);

        ButtonType okType = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        Button okButton = (Button) this.getDialogPane().lookupButton(okType);
        okButton.setDefaultButton(true);

        Button cancelButton = (Button) this.getDialogPane().lookupButton(cancelType);
        cancelButton.setCancelButton(true);

        okButton.setOnAction(actionEvent -> save());
        cancelButton.setOnAction(actionEvent -> close());

        saved.addListener((observableValue, aBoolean, t1) -> {
            if (observableValue.getValue()) {
                response = Response.OK;
                this.close();
            }
        });

        JFXComboBox<String> dateConfig = new JFXComboBox<>();
        dateConfig.getItems().addAll("Auto", "Man");

        Label fromLabel = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.from"));
        VBox fromBox = new VBox(fromLabel);
        fromBox.setAlignment(Pos.CENTER);
        Label toLabel = new Label(I18n.getInstance().getString("plugin.graph.dialog.export.to"));
        VBox toBox = new VBox(toLabel);
        toBox.setAlignment(Pos.CENTER);
        HBox toolBar = new HBox(8, fromBox, startDatePicker, toBox, endDatePicker);
        toolBar.setPadding(new Insets(12));

        dateConfig.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
            toolBar.getChildren().clear();
            switch (number.intValue()) {
                case 0:

                    break;
                case 1:
                    toolBar.getChildren().setAll(startDatePicker, endDatePicker);
                    break;
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                if (pageIndex > data.size() / ROWS_PER_PAGE + 1) {
                    return null;
                } else {
                    return createPage(pageIndex);
                }
            }
        });

        JFXButton addButton = new JFXButton("", JEConfig.getImage("list-add.png", ICON_SIZE, ICON_SIZE));
        addButton.setOnAction(actionEvent -> {
            int currentPageIndex = pagination.getCurrentPageIndex();
            List<DataSample> selectedItems = tableView.getSelectionModel().getSelectedItems();
            for (DataSample selectedItem : selectedItems) {
                int i = data.indexOf(selectedItem);
                DateTime currentTS = selectedItem.getTs();
                DateTime nextTS = currentTS.plus(CleanDataObject.getPeriodForDate(attribute.getObject(), currentTS));
                VirtualSample sample = new VirtualSample(nextTS, 0d);
                sample.setNote("");
                DataSample nextSample = new DataSample(sample, nextTS, 0d, "");
                if (data.contains(nextSample)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("sampleeditor.confirmationdialog.error.exists") + ": " + nextTS.toString(dateTimeFormat));
                    alert.showAndWait();
                } else {
                    data.add(i + 1, nextSample);
                    changedSamples.add(nextSample);
                }
            }
            createPage(currentPageIndex);
        });

        JFXButton removeButton = new JFXButton("", JEConfig.getImage("list-remove.png", ICON_SIZE, ICON_SIZE));
        removeButton.prefWidthProperty().bind(addButton.widthProperty());
        removeButton.setOnAction(actionEvent -> {
            int currentPageIndex = pagination.getCurrentPageIndex();
            List<DataSample> selectedItems = tableView.getSelectionModel().getSelectedItems();

            for (DataSample selectedItem : selectedItems) {
                deleteList.add(selectedItem);
                changedSamples.remove(selectedItem);
                data.remove(selectedItem);
            }

            createPage(currentPageIndex);
        });

        Region freeSpace = new Region();
        VBox.setVgrow(freeSpace, Priority.ALWAYS);

        VBox vBoxRight = new VBox(8, freeSpace, addButton, removeButton);
        vBoxRight.setPadding(new Insets(12));

        HBox hBox = new HBox(8, pagination, vBoxRight);
        hBox.setPadding(new Insets(12));

        VBox vBox = new VBox(8, toolBar, hBox);
        vBox.setPadding(new Insets(12));

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getDialogPane().setContent(scrollPane);

        updateTable();

        startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> updateTable());

        endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> updateTable());

    }


    private void save() {

        TableColumn<DataSample, String> oldValueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.data.confirm.table.header.oldvalue"));
        oldValueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().oldToString()));

        TableColumn<DataSample, String> newValueColumn = new TableColumn<>(I18n.getInstance().getString("dialog.data.confirm.table.header.newvalue"));
        newValueColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().toString()));

        TableColumn<DataSample, Boolean> commitChangeColumn = buildCommitColumn(I18n.getInstance().getString("dialog.data.confirm.table.header.commit"));

        TableView<DataSample> changedView = new TableView<>();
        changedView.setEditable(true);
        changedView.setMinHeight(360);
        changedView.setMinWidth(650);
        changedView.getColumns().setAll(oldValueColumn, newValueColumn, commitChangeColumn);
        changedView.getItems().setAll(changedSamples);

        TableColumn<DataSample, String> deleteColumn = new TableColumn<>(I18n.getInstance().getString("dialog.data.confirm.table.header.change"));
        deleteColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().toString()));

        TableColumn<DataSample, Boolean> commitDeleteColumn = buildCommitColumn(I18n.getInstance().getString("dialog.data.confirm.table.header.commit"));

        TableView<DataSample> deleteView = new TableView<>();
        deleteView.setEditable(true);
        deleteView.setMinHeight(360);
        deleteView.setMinWidth(650);
        deleteView.getColumns().setAll(deleteColumn, commitDeleteColumn);
        deleteView.getItems().setAll(deleteList);

        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setTitle(I18n.getInstance().getString("plugin.enterdata.history.confirmation.title"));
        confirmationDialog.setHeaderText(I18n.getInstance().getString("plugin.enterdata.history.confirmation.header"));
        confirmationDialog.setResizable(true);
        Stage stage = (Stage) confirmationDialog.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        Label headerText = new Label(I18n.getInstance().getString("dialog.data.confirm.message"));

        JFXButton ok = new JFXButton(I18n.getInstance().getString("graph.dialog.ok"));
        ok.setOnAction(event -> {
            try {
                List<JEVisSample> newSamples = new ArrayList<>();
                for (DataSample dataSample : changedSamples) {
                    if (dataSample.isCommit()) {
                        JEVisSample sample = dataSample.getSample();
                        DateTime dateTime = dataSample.getTs();
                        if (sample.getTimestamp().equals(dateTime) && !(sample instanceof VirtualSample)) {
                            sample.setValue(dataSample.getValue());
                            sample.setNote(dataSample.getNote());
                            sample.commit();
                        } else if (dateTime != null && !(sample instanceof VirtualSample)) {
                            newSamples.add(attribute.buildSample(dateTime, dataSample.getValue(), dataSample.getNote()));
                            attribute.deleteSamplesBetween(dataSample.getSample().getTimestamp(), dataSample.getSample().getTimestamp());
                        } else if (dateTime != null) {
                            newSamples.add(attribute.buildSample(dateTime, dataSample.getValue(), dataSample.getNote()));
                        }
                    }
                }

                attribute.addSamples(newSamples);

                for (DataSample dataSample : deleteList) {
                    if (dataSample.isCommit()) {
                        attribute.deleteSamplesBetween(dataSample.getSample().getTimestamp(), dataSample.getSample().getTimestamp());
                    }
                }
                confirmationDialog.close();
                saved.set(true);
            } catch (Exception e) {
                logger.error("Error while saving samples", e);
                Alert noSave = new Alert(Alert.AlertType.ERROR, I18n.getInstance().getString("dashboard.save.error") + e.getMessage());
                noSave.showAndWait();
            }
        });

        JFXButton cancel = new JFXButton(I18n.getInstance().getString("graph.dialog.cancel"));
        cancel.setOnAction(event -> confirmationDialog.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBar = new HBox(8, spacer, cancel, ok);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        VBox vBox = new VBox(12, headerText);
        vBox.setPadding(new Insets(15));

        if (!changedSamples.isEmpty()) {
            Label changedLabel = new Label(I18n.getInstance().getString("dialog.data.confirm.table.changed.label"));
            vBox.getChildren().addAll(changedLabel, changedView);
        }

        if (!deleteList.isEmpty()) {
            Label deletedLabel = new Label(I18n.getInstance().getString("dialog.data.confirm.table.deleted.label"));
            vBox.getChildren().addAll(deletedLabel, deleteView);
        }

        vBox.getChildren().add(buttonBar);

        confirmationDialog.getDialogPane().setContent(vBox);

        confirmationDialog.show();
    }

    private TableColumn<DataSample, Boolean> buildCommitColumn(String columnName) {
        TableColumn<DataSample, Boolean> column = new TableColumn<>(columnName);
        column.setStyle("-fx-alignment: CENTER;");
        column.setMinWidth(150);
        column.setCellValueFactory(new PropertyValueFactory<>("commit"));
        column.setCellFactory(CheckBoxTableCell.forTableColumn(column));
        column.setEditable(true);
        column.setOnEditCommit(dataSampleBooleanCellEditEvent ->
                (dataSampleBooleanCellEditEvent.getTableView().getItems().get(dataSampleBooleanCellEditEvent.getTablePosition().getRow()))
                        .setCommit(dataSampleBooleanCellEditEvent.getNewValue()));
        return column;
    }

    private void updateTable() {
        data.clear();

        for (JEVisSample jeVisSample : attribute.getSamples(getDTfromLD(startDatePicker.getValue()), getDTfromLD(endDatePicker.getValue()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999))) {
            try {
                data.add(new DataSample(jeVisSample, jeVisSample.getTimestamp(), jeVisSample.getValueAsDouble(), jeVisSample.getNote()));
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        createPage(0);
    }

    private DateTime getDTfromLD(LocalDate localDate) {
        return new DateTime(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth(), 0, 0, 0);
    }

    private TableColumn<DataSample, Double> buildValueColumn() {
        TableColumn<DataSample, Double> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.value"));
        column.setMinWidth(150);
        column.setStyle("-fx-alignment: CENTER-RIGHT;");
        column.setCellValueFactory(new PropertyValueFactory<>("value"));

        StringConverter<Double> doubleStringConverter = new StringConverter<Double>() {
            @Override
            public String toString(Double aDouble) {
                return aDouble == null ? "" : doubleValidator.format(aDouble);
            }

            @Override
            public Double fromString(String s) {
                if (s == null) {
                    return null;
                } else {
                    s = s.trim();
                    if (s.length() < 1) return null;
                    try {
                        return doubleValidator.validate(s);
                    } catch (Exception e) {
                        message(e.getLocalizedMessage());
                    }
                }
                return null;
            }
        };
        column.setCellFactory(TextFieldTableCell.forTableColumn(doubleStringConverter));
        column.setEditable(true);
        column.setOnEditCommit(dataSampleDoubleCellEditEvent -> {
            if (dataSampleDoubleCellEditEvent.getNewValue() != null) {
                (dataSampleDoubleCellEditEvent.getTableView().getItems().get(dataSampleDoubleCellEditEvent.getTablePosition().getRow()))
                        .setValue(dataSampleDoubleCellEditEvent.getNewValue());

                if (!changedSamples.contains(dataSampleDoubleCellEditEvent.getRowValue())) {
                    changedSamples.add(dataSampleDoubleCellEditEvent.getRowValue());
                }
            } else
                (dataSampleDoubleCellEditEvent.getTableView().getItems().get(dataSampleDoubleCellEditEvent.getTablePosition().getRow()))
                        .setValue(dataSampleDoubleCellEditEvent.getOldValue());

            dataSampleDoubleCellEditEvent.getTableView().refresh();
        });

        return column;
    }

    private TableColumn<DataSample, String> buildNoteColumn() {
        TableColumn<DataSample, String> column = new TableColumn<>(I18n.getInstance().getString("plugin.graph.table.note"));
        column.setMinWidth(250);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("note"));
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setEditable(true);
        column.setOnEditCommit(dataSampleStringCellEditEvent -> {
            (dataSampleStringCellEditEvent.getTableView().getItems().get(dataSampleStringCellEditEvent.getTablePosition().getRow()))
                    .setNote(dataSampleStringCellEditEvent.getNewValue());

            if (!changedSamples.contains(dataSampleStringCellEditEvent.getRowValue())) {
                changedSamples.add(dataSampleStringCellEditEvent.getRowValue());
            }
        });

        return column;
    }

    private Node createPage(int pageIndex) {
        int numOfPages = 1;
        if (data.size() % ROWS_PER_PAGE == 0) {
            numOfPages = data.size() / ROWS_PER_PAGE;
        } else if (data.size() > ROWS_PER_PAGE) {
            numOfPages = data.size() / ROWS_PER_PAGE + 1;
        }
        pagination.setPageCount(numOfPages);
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, data.size());

        tableView.setItems(FXCollections.observableArrayList(data.subList(fromIndex, toIndex)));

        return tableView;
    }

    private TableColumn<DataSample, DateTime> buildDateColumn() {
        TableColumn<DataSample, DateTime> column = new TableColumn<>(I18n.getInstance().getString("plugin.alarm.table.date"));
        column.setMinWidth(150);
        column.setStyle("-fx-alignment: CENTER;");
        column.setCellValueFactory(new PropertyValueFactory<>("ts"));
        StringConverter<DateTime> dateTimeStringConverter = new StringConverter<DateTime>() {
            @Override
            public String toString(DateTime dateTime) {
                return dateTime == null ? "" : dateTime.toString(dateTimeFormat);
            }

            @Override
            public DateTime fromString(String s) {
                if (s == null) {
                    return null;
                } else {
                    s = s.trim();
                    if (s.length() < 1) return null;
                    try {
                        return dateTimeFormat.parseDateTime(s);
                    } catch (Exception e) {
                        message(e.getLocalizedMessage());
                    }
                }
                return null;
            }
        };
        column.setCellFactory(TextFieldTableCell.forTableColumn(dateTimeStringConverter));
        column.setEditable(true);
        column.setSortable(true);
        column.setOnEditCommit(dataSampleStringCellEditEvent -> {
            try {
                if (dataSampleStringCellEditEvent.getNewValue() != null) {
                    (dataSampleStringCellEditEvent.getTableView().getItems().get(dataSampleStringCellEditEvent.getTablePosition().getRow()))
                            .setTs(dataSampleStringCellEditEvent.getNewValue());

                    if (!changedSamples.contains(dataSampleStringCellEditEvent.getRowValue())) {
                        changedSamples.add(dataSampleStringCellEditEvent.getRowValue());
                    }
                } else
                    (dataSampleStringCellEditEvent.getTableView().getItems().get(dataSampleStringCellEditEvent.getTablePosition().getRow()))
                            .setTs(dataSampleStringCellEditEvent.getOldValue());

                dataSampleStringCellEditEvent.getTableView().refresh();
            } catch (Exception ex) {
                logger.error("Error in timestamp value", ex);
            }
        });

        return column;
    }

    public Response getResponse() {
        return response;
    }

    private void message(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    public class DataSample {
        private final JEVisSample sample;
        private final SimpleObjectProperty<DateTime> ts = new SimpleObjectProperty<>(this, "ts");
        private final SimpleDoubleProperty value = new SimpleDoubleProperty(this, "value");
        private final SimpleStringProperty note = new SimpleStringProperty(this, "note");
        private final SimpleBooleanProperty commit = new SimpleBooleanProperty(this, "commit", true);

        public DataSample(JEVisSample sample, DateTime timeStamp, Double value, String note) {
            this.sample = sample;
            this.ts.set(timeStamp);
            this.value.set(value);
            this.note.set(note);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DataSample) {
                DataSample otherObj = (DataSample) obj;
                return this.ts.get().equals(otherObj.getTs());
            }
            return false;
        }

        public JEVisSample getSample() {
            return sample;
        }

        public DateTime getTs() {
            return ts.get();
        }

        public void setTs(DateTime ts) {
            this.ts.set(ts);
        }

        public SimpleObjectProperty<DateTime> tsProperty() {
            return ts;
        }

        public double getValue() {
            return value.get();
        }

        public void setValue(double value) {
            this.value.set(value);
        }

        public SimpleDoubleProperty valueProperty() {
            return value;
        }

        public String getNote() {
            return note.get();
        }

        public void setNote(String note) {
            this.note.set(note);
        }

        public SimpleStringProperty noteProperty() {
            return note;
        }

        public boolean isCommit() {
            return commit.get();
        }

        public void setCommit(boolean commit) {
            this.commit.set(commit);
        }

        public SimpleBooleanProperty commitProperty() {
            return commit;
        }

        public String oldToString() {
            try {
                return sample.getTimestamp().toString(dateTimeFormat) +
                        " : " + sample.getValue() +
                        " (" + sample.getNote() + ')';
            } catch (Exception e) {
                logger.error("Could not get values for original sample");
            }

            return "";
        }

        @Override
        public String toString() {
            try {
                return ts.get().toString(dateTimeFormat) +
                        " : " + value.get() +
                        " (" + note.get() + ')';
            } catch (Exception e) {
                logger.error("Could not get values for new sample");
            }

            return "";
        }
    }
}
