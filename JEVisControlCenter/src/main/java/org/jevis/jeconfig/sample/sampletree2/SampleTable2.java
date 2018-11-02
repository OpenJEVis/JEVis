/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.sample.sampletree2;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * The SampleTable can edit and delete all given sample for an attribute.
 * <p>
 * TODO:
 * - Filter for Value > x
 * - Column for selecting and then delete
 * - imp value column, different editors for different type(Double, String, json, file)
 * - imp commit samples
 * - imp an good way to show imput errors of new samples
 * - add new sample functions, maybe an plus in the first empty selection column
 * - json value editor show "test test...." and when double clicked open popup with long text (what about json support?)
 * -
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTable2 extends TableView<SampleTable2.TableSample> {
    static final DateTimeFormatter dateViewFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LogManager.getLogger(SampleTable2.class);
    private static final Color COLOR_ERROR = Color.INDIANRED;
    private final JEVisAttribute attribute;
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private final Set<TableSample> changedSamples = new HashSet<>();
    private final BooleanProperty deleteInBetween = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty needSave = new SimpleBooleanProperty(false);
    private final ObservableList<SampleTable2.TableSample> data = FXCollections.observableArrayList();
    private DateTime minDate = null;
    private DateTime maxDate = null;

    /**
     * Create an SampleEditor table for the given JEVisSamples.
     *
     * @param attribute
     * @param samples
     */
    public SampleTable2(JEVisAttribute attribute, List<JEVisSample> samples) {
        super();
        this.attribute = attribute;

        setPlaceholder(new Label("No Data"));


        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        TableColumn selectionColumn = createSelectionColumn("Select");
        TableColumn timeStampColumn = createTimeStampColumn("Timestamp");
        TableColumn valueCol = createValueColumn("Value");
        TableColumn noteColumn = createNoteColumn("Note");

        /**
         * TODO: implement an nice resize policy where all take the minimum amount of space and the rest goes to value
         */
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
//        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        selectionColumn.setPrefWidth(60);
        timeStampColumn.setPrefWidth(155);
        noteColumn.setPrefWidth(200);
        valueCol.setPrefWidth(310);


        getColumns().addAll(selectionColumn, timeStampColumn, valueCol, noteColumn);

        setEditable(true);

        loadSamples(samples);
        setItems(data);

    }

    /**
     * Load the sample into the data model and find the min and max value
     *
     * @param samples
     */
    private void loadSamples(List<JEVisSample> samples) {
        /** TODO: prompt user waring if changedSamples is not empty **/
        changedSamples.clear();
        data.clear();
        samples.forEach(jeVisSample -> {
            try {
                data.add(new SampleTable2.TableSample(jeVisSample));
            } catch (Exception ex) {
                logger.error("Error while loading samples in table", ex);
            }
        });

        findMinMaxDate();
    }

    /**
     * Find the min and max timestamp. Used for reloading.
     */
    private void findMinMaxDate() {
        minDate = null;
        maxDate = null;
        getItems().forEach(tableSample -> {
            if (minDate == null || minDate.isAfter(tableSample.getTimeStamp())) {
                minDate = tableSample.getTimeStamp();
            }
            if (maxDate == null || maxDate.isBefore(tableSample.getTimeStamp())) {
                maxDate = tableSample.getTimeStamp();
            }
        });
    }

    /**
     * will be true if the selected data allow an delete in between action
     *
     * @return
     */
    public BooleanProperty deleteInBetweenProperty() {
        return deleteInBetween;
    }

    /**
     * Will be true if the selected data allow an deleting of selected data.
     *
     * @return
     */
    public BooleanProperty deleteSelectedProperty() {
        return deleteSelected;
    }

    public BooleanProperty needSaveProperty() {
        return needSave;
    }

    /**
     * Delete all selected samples
     */
    public void deleteSelectedData() {
        if (deleteSelected.getValue()) {
            getItems().forEach(tableSample -> {
                if (tableSample.isSelected()) {
                    try {
                        attribute.deleteSamplesBetween(tableSample.getJevisSample().getTimestamp(), tableSample.getJevisSample().getTimestamp());
                    } catch (Exception ex) {
                        logger.error("Error while deleteing sample", ex);
                    }
                }
            });
            reloadSamples();
        }
    }

    /**
     * Delete all samples in between the two selected including the two selected
     */
    public void deleteInBetween() {
        if (deleteInBetween.getValue()) {
            DateTime firstDate = null;
            DateTime secoundDate = null;
            for (TableSample tableSample : changedSamples) {
                if (firstDate == null) {
                    firstDate = tableSample.getTimeStamp();
                } else if (firstDate.isBefore(tableSample.getTimeStamp())) {
                    secoundDate = tableSample.getTimeStamp();
                } else {
                    secoundDate = firstDate;
                    firstDate = tableSample.getTimeStamp();
                }
            }
            try {
                if (firstDate != null && secoundDate != null) {
                    attribute.deleteSamplesBetween(firstDate, secoundDate);
                    reloadSamples();
                } else {
                    logger.warn("second timestamp is before first");
                }

            } catch (Exception ex) {
                logger.error("Error while deleting sample", ex);
            }

        }
    }

    public void reloadSamples() {
        if (minDate != null && maxDate != null) {
            List<JEVisSample> samples = attribute.getSamples(minDate, maxDate);
            loadSamples(samples);
        }
    }

    private void selectionChanged() {
        deleteInBetween.setValue(changedSamples.size() == 2);
        deleteSelected.setValue(!changedSamples.isEmpty());

    }


    /**
     * Try to commit all changed to the JEVisDataSource
     */
    public void commitChanges() {
        changedSamples.forEach(tableSample -> {
            try {
                tableSample.commit();
            } catch (Exception ex) {
                logger.error("Error while committing sample", ex);
            }
        });
    }


    public void debugStuff() {
        getItems().forEach(tableSample -> {
            System.out.println("Sample: " + tableSample);
        });
    }


    /**
     * Add an new sample to the table
     *
     * @param timestamp
     * @param value
     * @param note
     * @return
     * @throws Exception
     */
    public TableSample addNewSample(DateTime timestamp, Object value, String note) throws Exception {


//        System.out.println("addNewSample: " + timestamp);
        TableSample tSample = new TableSample(attribute.buildSample(timestamp, value, note));
        tSample.setIsNew(true);
        data.add(tSample);
        this.getSelectionModel().getTableView().scrollTo(tSample);
        this.getSelectionModel().select(tSample);

        findMinMaxDate();
        return tSample;
    }

    /**
     * Create an column for the timestamps
     *
     * @param columnName
     * @return
     */
    private TableColumn<TableSample, DateTime> createTimeStampColumn(String columnName) {

        /**
         * We want the used timezone and im not sure if the default is the same as the API is using so we ask one sample
         * for it to be sure
         */
        try {
            JEVisSample lastSample = attribute.getLatestSample();
            if (lastSample != null) {
                columnName += " (" + DateTimeFormat.forPattern("z").print(lastSample.getTimestamp()) + ")";
            }
        } catch (Exception ex) {
        }


        TableColumn<TableSample, DateTime> column = new TableColumn<>(columnName);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getTimeStamp()));

        column.setCellFactory(new Callback<TableColumn<TableSample, DateTime>, TableCell<TableSample, DateTime>>() {
            @Override
            public TableCell<TableSample, DateTime> call(TableColumn<TableSample, DateTime> param) {
                return new TableCell<TableSample, DateTime>() {


                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            TextField textField = new TextField(dateViewFormat.print(item));
                            setDefaultFieldStyle(this, textField);

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    DateTime newDate = dateViewFormat.parseDateTime(textField.getText());

                                    List<JEVisSample> exitingSample = attribute.getSamples(newDate, newDate);
                                    if (!exitingSample.isEmpty()) {
                                        setErrorCellStyle(this, new Exception("Timestamp already exists"));
                                    }

                                    /**
                                     *       - ?add also an date and time selector gui?
                                     *       - if error set background color red and show error while save event
                                     **/

                                    TableSample tableSample = (TableSample) getTableRow().getItem();
                                    tableSample.setTimeStamp(newDate);
                                    setDefaultCellStyle(this);
                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in timestamp value", ex);
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        });

        column.setEditable(true);
        return column;
    }

    /**
     * Create a selection column. This column is used the make changes like deleting to multiple rows. We are not using the
     * original table.getselectedrows.
     *
     * @param columnName
     * @return
     */
    private TableColumn<TableSample, Boolean> createSelectionColumn(String columnName) {


        TableColumn<TableSample, Boolean> column = new TableColumn<>(columnName);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().isSelected()));

        column.setCellFactory(new Callback<TableColumn<TableSample, Boolean>, TableCell<TableSample, Boolean>>() {
            @Override
            public TableCell<TableSample, Boolean> call(TableColumn<TableSample, Boolean> param) {
                return new TableCell<TableSample, Boolean>() {


                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            try {
                                TableSample tableSample = (TableSample) getTableRow().getItem();
                                CheckBox checkBox = new CheckBox();
                                setDefaultCellStyle(this);
                                checkBox.selectedProperty().bind(tableSample.isSelectedProperty());
                                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                    tableSample.setIsSelected(newValue);
                                });

                                HBox box = new HBox(checkBox);
                                box.setAlignment(Pos.BASELINE_CENTER);
                                setGraphic(box);
                            } catch (Exception ex) {
                                logger.error(ex);
                            }
                        }

                    }
                };
            }

        });

        ContextMenu menu = new ContextMenu();
        MenuItem selectAll = new MenuItem("Select all");
        MenuItem deselectAll = new MenuItem("deselect all");

        selectAll.setOnAction(event -> {
            getItems().forEach(tableSample -> {
                tableSample.setIsSelected(true);
            });
        });
        deselectAll.setOnAction(event -> {
            getItems().forEach(tableSample -> {
                tableSample.setIsSelected(false);
            });
        });

        menu.getItems().addAll(selectAll, deselectAll);
        column.setContextMenu(menu);

//        column.setMaxWidth(70d);
        column.setEditable(true);
        return column;
    }


    /**
     * Set the default style fpr control elements in cells
     *
     * @param field
     */
    private void setDefaultFieldStyle(TableCell cell, Control... field) {
        for (Control control : field) {
            control.setStyle("-fx-background-color: transparent;" +
                    "    -fx-background-insets: 0;" +
                    "    -fx-padding: 0 0 0 0;");
            control.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    getSelectionModel().clearAndSelect(cell.getTableRow().getIndex());
                }
            });
        }

    }

    /**
     * Set the default style for an cell
     *
     * @param cell
     */
    private void setDefaultCellStyle(TableCell cell) {
        cell.setBackground(Background.EMPTY);
        cell.setTooltip(null);
    }

    /**
     * Marks an cell as faulty by changing style and adding an tooltip
     *
     * @param cell
     * @param ex
     */
    private void setErrorCellStyle(TableCell cell, Exception ex) {
        cell.setBackground(new Background(new BackgroundFill(COLOR_ERROR, CornerRadii.EMPTY, Insets.EMPTY)));
        Tooltip tt = new Tooltip(ex.getMessage());
        cell.setTooltip(tt);
    }

    /**
     * Marks cell and control elements as fault.
     *
     * @param cell
     * @param ex
     * @param field
     */
    private void setErrorCellStyle(TableCell cell, Exception ex, Control... field) {
        cell.setBackground(new Background(new BackgroundFill(COLOR_ERROR, CornerRadii.EMPTY, Insets.EMPTY)));
        Tooltip tt = new Tooltip(ex.getMessage());
        cell.setTooltip(tt);
        for (Control control : field) {
            control.setTooltip(tt);
        }
    }

    /**
     * Create an callback for an Double based sample
     *
     * @return
     */
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> doubleValueCell() {
        return new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell<TableSample, Object>() {

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {

                            TextField textField = new TextField(item.toString());
                            setDefaultFieldStyle(this, textField);


                            UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

                                @Override
                                public TextFormatter.Change apply(TextFormatter.Change t) {

                                    if (t.getText().length() > 1) {/** Copy&paste case **/
                                        try {
                                            /**
                                             * TODO: maybe try different locales
                                             */
                                            Number newNumber = numberFormat.parse(t.getText());
                                            t.setText(String.valueOf(newNumber.doubleValue()));
                                        } catch (Exception ex) {
                                            t.setText("");
                                        }
                                    } else if (t.getText().matches(",")) {/** to be use the Double.parse **/
                                        t.setText(".");
                                    }


                                    try {
                                        /** We don't use the NumberFormat to validate, because he is not strict enough **/
                                        Double parse = Double.parseDouble(t.getControlNewText());
                                    } catch (Exception ex) {
                                        t.setText("");
                                    }
                                    return t;
                                }
                            };

                            textField.setTextFormatter(new TextFormatter<>(filter));

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);
                                    TableSample tableSample = (TableSample) getTableRow().getItem();
                                    tableSample.setValue(Double.parseDouble(newValue));
                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in double text", ex);
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        };
    }

    /**
     * Create an Callback cell for file samples
     *
     * @return
     */
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> fileValueCell() {
        return new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell<TableSample, Object>() {

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Button downloadButton = new Button(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                            TableSample tableSample = (TableSample) getTableRow().getItem();

                            downloadButton.setOnAction(event -> {
                                JEVisSample sample = tableSample.getJevisSample();

                                try {
                                    JEVisFile file = sample.getValueAsFile();

                                    FileChooser fileChooser = new FileChooser();
                                    fileChooser.setInitialFileName(file.getFilename());
                                    fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
                                    fileChooser.getExtensionFilters().addAll(
                                            new FileChooser.ExtensionFilter("All Files", "*.*"));
                                    File selectedFile = fileChooser.showSaveDialog(null);
                                    if (selectedFile != null) {
                                        JEConfig.setLastPath(selectedFile);
                                        file.saveToFile(selectedFile);
                                    }
                                    setDefaultCellStyle(this);
                                } catch (Exception ex) {
                                    logger.fatal(ex);
                                    setErrorCellStyle(this, ex);
                                }

                            });
                            setGraphic(downloadButton);

                        }

                    }
                };
            }

        };
    }

    /**
     * Create an Callback cell for String based values
     *
     * @return
     */
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> stringValueCell() {
        return new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell<TableSample, Object>() {

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {

                            TextField textField = new TextField(item.toString());
                            setDefaultFieldStyle(this, textField);

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);


                                    TableSample tableSample = (TableSample) getTableRow().getItem();
                                    tableSample.setValue(newValue + "");

                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in string text", ex);
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        };
    }

    /**
     * Create an value column based on the getPrimitiveType() and getGUI type.
     *
     * @param columnName
     * @return
     */
    private TableColumn<TableSample, Object> createValueColumn(String columnName) {

        TableColumn<TableSample, Object> column = new TableColumn<>(columnName);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getValue()));

        try {

            switch (attribute.getPrimitiveType()) {
                case JEVisConstants.PrimitiveType.DOUBLE:
                    column.setCellFactory(doubleValueCell());
                    break;
                case JEVisConstants.PrimitiveType.FILE:
                    column.setCellFactory(fileValueCell());
                    break;
                default:
                    column.setCellFactory(stringValueCell());
                    break;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        column.remainingWidth();
        column.setEditable(true);
        return column;
    }


    /**
     * Create an note column
     *
     * @param columnName
     * @return
     */
    private TableColumn<TableSample, String> createNoteColumn(String columnName) {


        TableColumn<TableSample, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getNote()));

        column.setCellFactory(new Callback<TableColumn<TableSample, String>, TableCell<TableSample, String>>() {
            @Override
            public TableCell<TableSample, String> call(TableColumn<TableSample, String> param) {
                return new TableCell<TableSample, String>() {


                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setDefaultCellStyle(this);
                            TableSample tableSample = (TableSample) getTableRow().getItem();

                            TextField textField = new TextField(tableSample.getNote());
                            setDefaultFieldStyle(this, textField);
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);
                                    tableSample.setNote(newValue);

                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                }
                            });

                            setGraphic(textField);
                        }

                    }
                };
            }

        });

        column.setEditable(true);
        return column;
    }


    /**
     * Inner Class to capsule JEVisSample to be manipulated in a table
     * <p>
     * TODO:
     * - Revert changed function
     */
    public class TableSample {

        private SimpleObjectProperty value;
        private SimpleStringProperty note;
        private SimpleObjectProperty<DateTime> timeStamp;
        private SimpleBooleanProperty isSelected = new SimpleBooleanProperty(false);

        private JEVisSample jevisSample = null;
        private boolean isNew = false;


        private TableSample() {
        }

        public TableSample(JEVisSample sample) {
            this.jevisSample = sample;
            loadSampleData();
        }

        private void loadSampleData() {
            try {
                this.timeStamp = new SimpleObjectProperty(jevisSample.getTimestamp());
                this.value = new SimpleObjectProperty(jevisSample.getValue());
                this.note = new SimpleStringProperty(jevisSample.getNote());


                timeStamp.addListener((observable, oldValue, newValue) -> {
                    checkChanged();
                });
                value.addListener((observable, oldValue, newValue) -> {
                    checkChanged();
                });
                note.addListener((observable, oldValue, newValue) -> {
                    checkChanged();
                });

                isSelected.addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        changedSamples.add(this);
                    } else {
                        changedSamples.remove(this);
                    }
                    selectionChanged();
                });
            } catch (Exception ex) {
                logger.error("Error while loading sample", ex);
            }
        }

        private void checkChanged() {
            if (isNew) {
                needSave.setValue(true);
                changedSamples.add(this);
            } else if (hasChanged()) {
                needSave.setValue(true);
                changedSamples.add(this);
            } else {
                changedSamples.remove(this);
                if (changedSamples.isEmpty()) {
                    needSave.setValue(false);
                }
            }
        }

        /**
         * Commits this sample to the JEVisDataSource.
         * If the timestamp change it will be deleted an recreated with new with it.
         *
         * @throws Exception
         */
        private void commit() throws Exception {
            jevisSample.setValue(value.getValue());
            jevisSample.setNote(note.getValue());

            logger.info("Commit sample {}", this);
            if (jevisSample.getTimestamp().equals(timeStamp.getValue())) {
                jevisSample.commit();
                isNew = false;
                changedSamples.remove(this);
            } else {
                JEVisSample newSample = attribute.buildSample(timeStamp.getValue(), value.getValue(), note.getValue());
                newSample.commit();
                isNew = false;
                changedSamples.remove(this);
                attribute.deleteSamplesBetween(jevisSample.getTimestamp(), jevisSample.getTimestamp());
                this.jevisSample = newSample;
                loadSampleData();
            }


        }

        private boolean hasChanged() {
            try {
                if (!jevisSample.getTimestamp().equals(timeStamp.getValue())) {
                    return true;
                }

                if (!jevisSample.getNote().equals(note.getValue())) {
                    return true;
                }
                if (!jevisSample.getValue().equals(value.getValue())) {
                    return true;
                }

            } catch (Exception ex) {
                logger.error("Error while checking for changes", ex);
            }

            return false;


        }

        public boolean isSelected() {
            return isSelected.get();
        }

        public void setIsSelected(boolean isSelected) {
            this.isSelected.set(isSelected);
        }

        public SimpleBooleanProperty isSelectedProperty() {
            return isSelected;
        }

        public Object getValue() {
            return value.get();
        }

        public void setValue(Object value) {
            this.value.setValue(value);
        }

        public SimpleObjectProperty valueProperty() {
            return value;
        }

        public String getNote() {
            return note.getValue();
        }

        public void setNote(String note) {
            this.note.set(note);
        }

        public SimpleStringProperty noteProperty() {
            return note;
        }

        public DateTime getTimeStamp() {
            return timeStamp.get();
        }

        public void setTimeStamp(DateTime timeStamp) {
            this.timeStamp.set(timeStamp);
        }

        public SimpleObjectProperty<DateTime> timeStampProperty() {
            return timeStamp;
        }

        public JEVisSample getJevisSample() {
            return jevisSample;
        }

        public void setJevisSample(JEVisSample jevisSample) {
            this.jevisSample = jevisSample;
        }

        /**
         * Set taht this is an new created sample request an commit
         *
         * @param isNew
         */
        private void setIsNew(boolean isNew) {
            this.isNew = isNew;
            checkChanged();
        }

        @Override
        public String toString() {
            return "TableSample{" +
                    "value=" + value +
                    ", note=" + note +
                    ", timeStamp=" + timeStamp +
                    ", jevisSample=" + jevisSample +
                    '}';
        }
    }
}
