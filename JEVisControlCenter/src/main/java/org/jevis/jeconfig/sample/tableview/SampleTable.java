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
package org.jevis.jeconfig.sample.tableview;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.dialog.ImageViewerDialog;
import org.jevis.jeconfig.dialog.PDFViewerDialog;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.jevis.jeconfig.plugin.unit.SamplingRateUI;
import org.jevis.jeconfig.tool.ToggleSwitchPlus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * The CSVExportTableSampleTable can edit and delete all given sample for an attribute.
 * <p>
 * TODO:
 * - Filter for Value > x
 * - implement an good way to show input errors of new samples
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTable extends TableView<SampleTable.TableSample> {
    private final DateTimeFormatter dateViewFormat;
    private final static NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
    private final static Logger logger = LogManager.getLogger(SampleTable.class);
    private final static Color COLOR_ERROR = Color.INDIANRED;
    private final JEVisAttribute attribute;
    private final Set<TableSample> changedSamples = new HashSet<>();
    private final BooleanProperty deleteInBetween = new SimpleBooleanProperty(false);
    private final BooleanProperty deleteSelected = new SimpleBooleanProperty(false);
    private final BooleanProperty needSave = new SimpleBooleanProperty(false);
    private final ObservableList<SampleTable.TableSample> data = FXCollections.observableArrayList();
    private final DateTimeZone dateTimeZone;
    private DateTime minDate = null;
    private DateTime maxDate = null;
    boolean canDelete = false;

    /**
     * Create an SampleEditor table for the given JEVisSamples.
     *
     * @param attribute
     * @param samples
     */
    public SampleTable(JEVisAttribute attribute, DateTimeZone dateTimeZone, List<JEVisSample> samples) {
        super();
        this.attribute = attribute;
        this.dateTimeZone = dateTimeZone;
        this.dateViewFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ssZ").withZone(dateTimeZone);

        try {
            if (attribute.getObject().getDataSource().getCurrentUser().canWrite(attribute.getObject().getID())) {
                canDelete = true;
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

        setEditable(true);

        setPlaceholder(new Label(I18n.getInstance().getString("sampleeditor.confirmationdialog.nodata")));


        setMinWidth(555d);//TODo: replace Dirty workaround

        setPrefHeight(200d);//TODo: replace Dirty workaround
        //setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);


        TableColumn<TableSample, Boolean> selectionColumn = createSelectionColumn(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.select"));
        TableColumn<TableSample, DateTime> timeStampColumn = createTimeStampColumn(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.time"));
        TableColumn<TableSample, Object> valueCol = createValueColumn(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.value"));
        TableColumn<TableSample, String> noteColumn = createNoteColumn(I18n.getInstance().getString("sampleeditor.confirmationdialog.column.note"));

        try {
            if (attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE || attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                valueCol.comparatorProperty().setValue(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        try {
                            Double d1 = Double.parseDouble((String) o1);
                            Double d2 = Double.parseDouble((String) o2);

                            return d1.compareTo(d2);
                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                        return 0;
                    }
                });
            }
        } catch (Exception ex) {
            logger.error(ex);
        }


        /**
         * TODO: implement an nice resize policy where all take the minimum amount of space and the rest goes to value
         */
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
//        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

        selectionColumn.setPrefWidth(70);
        timeStampColumn.setPrefWidth(155);
        noteColumn.setPrefWidth(200);
        valueCol.setPrefWidth(310);

        getColumns().addAll(selectionColumn, timeStampColumn, valueCol, noteColumn);

//        setEditable(true);

        loadSamples(samples);
        setItems(data);
        editableProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("Table.editable: " + newValue);
        });

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
                data.add(new SampleTable.TableSample(jeVisSample));
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
     * Return the the oldest date loaded after change
     *
     * @return
     */
    public DateTime getMinDate() {
        findMinMaxDate();
        return minDate;
    }


    /**
     * Return the newest date loaded after change
     *
     * @return
     */
    public DateTime getMaxDate() {
        findMinMaxDate();
        return maxDate;
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
                        logger.error("Error while deleting samples", ex);
                    }
                }
            });
            reloadSamples();
        }
    }

    public DateTime[] findSelectedMinMaxDate() {
        DateTime[] minMax = new DateTime[2];
        DateTime firstDate = null;
        DateTime secondDate = null;
        for (TableSample tableSample : changedSamples) {
            if (firstDate == null) {
                firstDate = tableSample.getTimeStamp();
            } else if (firstDate.isBefore(tableSample.getTimeStamp())) {
                secondDate = tableSample.getTimeStamp();
            } else {
                secondDate = firstDate;
                firstDate = tableSample.getTimeStamp();
            }
        }
        minMax[0] = firstDate;
        minMax[1] = secondDate;
        return minMax;
    }

    /**
     * Delete all samples in between the two selected including the two selected
     */
    public void deleteInBetween() {
        if (deleteInBetween.getValue()) {
            /**
             DateTime firstDate = null;
             DateTime secondDate = null;
             for (TableSample tableSample : changedSamples) {
             if (firstDate == null) {
             firstDate = tableSample.getTimeStamp();
             } else if (firstDate.isBefore(tableSample.getTimeStamp())) {
             secondDate = tableSample.getTimeStamp();
             } else {
             secondDate = firstDate;
             firstDate = tableSample.getTimeStamp();
             }
             }
             **/
            DateTime[] minMax = findSelectedMinMaxDate();
            DateTime firstDate = minMax[0];
            DateTime secondDate = minMax[1];

            try {
                if (firstDate != null && secondDate != null) {
                    attribute.deleteSamplesBetween(firstDate, secondDate);
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
        findMinMaxDate();
        if (minDate != null && maxDate != null) {
            List<JEVisSample> samples = attribute.getSamples(minDate, maxDate);
            loadSamples(samples);
        }
    }

    private void selectionChanged() {
        logger.debug("selectionChanged: {}", changedSamples.size());

        if (canDelete) {
            deleteInBetween.setValue(changedSamples.size() == 2);
            deleteSelected.setValue(!changedSamples.isEmpty());
        }


    }


    /**
     * Try to commit all changed to the JEVisDataSource
     */
    public void commitChanges() {

        final ProgressForm pForm = new ProgressForm(I18n.getInstance().getString("plugin.object.waitsave"));

        Task<Void> upload = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                changedSamples.forEach(tableSample -> {
                    try {
                        tableSample.commit();
                    } catch (Exception ex) {
                        logger.error("Error while committing sample", ex);
                    }
                });

                Thread.sleep(60);
                reloadSamples();
                return null;
            }
        };
        upload.setOnSucceeded(event -> pForm.getDialogStage().close());

        upload.setOnCancelled(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.canceled"));
            pForm.getDialogStage().hide();
        });

        upload.setOnFailed(event -> {
            logger.error(I18n.getInstance().getString("plugin.object.waitsave.failed"));
            pForm.getDialogStage().hide();
        });

        pForm.activateProgressBar(upload);
        pForm.getDialogStage().show();

        new Thread(upload).start();

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


        TableSample tSample = new TableSample(attribute.buildSample(timestamp, value, note));
        tSample.setIsNew();
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
                columnName += " (" + DateTimeZone.getNameProvider().getShortName(I18n.getInstance().getLocale(), dateTimeZone.getID(), dateTimeZone.getNameKey(0)) + ")";
            }
        } catch (Exception ex) {
        }


        TableColumn<TableSample, DateTime> column = new TableColumn<>(columnName);
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTimeStamp()));

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
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            JFXTextField textField = new JFXTextField(dateViewFormat.print(tableSample.getTimeStamp()));
                            setDefaultFieldStyle(this, textField);

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    DateTime newDate = dateViewFormat.parseDateTime(textField.getText());


                                    List<JEVisSample> exitingSample = attribute.getSamples(newDate, newDate);
                                    if (!exitingSample.isEmpty()) {
                                        setErrorCellStyle(this, new Exception(I18n.getInstance().getString("sampleeditor.confirmationdialog.error.exists")));
                                    }

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
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().isSelected()));

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
                                JFXCheckBox checkBox = new JFXCheckBox();
                                checkBox.setSelected(tableSample.isSelected());
                                checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                                    tableSample.setIsSelected(newValue);
                                });
                                setDefaultCellStyle(this);
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
        MenuItem selectAll = new MenuItem(I18n.getInstance().getString("sampleeditor.confirmationdialog.selectall"));
        MenuItem deselectAll = new MenuItem(I18n.getInstance().getString("sampleeditor.confirmationdialog.deselect"));

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
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellDouble() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();

                            JFXTextField textField = new JFXTextField(tableSample.getValue().toString());
//                            textField.setDisable(!SampleTable.this.isEditable());
//                            this.disableProperty().bind(textField.disableProperty());
                            setDefaultFieldStyle(this, textField);


                            UnaryOperator<TextFormatter.Change> filter = t -> {

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
                            };

                            textField.setTextFormatter(new TextFormatter<>(filter));

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);

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
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellFile() {
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
                            String fileName = "";
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            JEVisSample sample = tableSample.getJevisSample();
                            boolean isPDF = false;
                            boolean isImage = false;
                            try {
                                fileName = sample.getValueAsString();
                                String s = fileName.substring(fileName.length() - 3).toLowerCase();
                                switch (s) {
                                    case "pdf":
                                        isPDF = true;
                                        break;
                                    case "png":
                                    case "jpg":
                                    case "jpeg":
                                    case "gif":
                                        isImage = true;
                                        break;
                                }

                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            JFXButton downloadButton = new JFXButton(fileName, JEConfig.getImage("save.gif", 14, 14));

                            downloadButton.setOnAction(event -> {

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

                            HBox hBox = new HBox(downloadButton);
                            if (isPDF) {
                                JFXButton pdfButton = new JFXButton("", JEConfig.getImage("pdf_24_2133056.png", 14, 14));
                                hBox.getChildren().add(pdfButton);
                                pdfButton.setOnAction(event -> {
                                    PDFViewerDialog pdfViewerDialog = new PDFViewerDialog();
                                    try {
                                        pdfViewerDialog.show(sample.getAttribute(), sample.getValueAsFile(), this.getScene().getWindow());
                                    } catch (JEVisException e) {
                                        logger.error("Could not open pdf viewer", e);
                                    }
                                });
                            }

                            if (isImage) {
                                JFXButton imageButton = new JFXButton("", JEConfig.getImage("export-image.png", 18, 18));
                                hBox.getChildren().add(imageButton);
                                imageButton.setOnAction(event -> {
                                    ImageViewerDialog imageViewerDialog = new ImageViewerDialog();
                                    try {
                                        imageViewerDialog.show(sample.getAttribute(), sample.getValueAsFile(), JEConfig.getStage());
                                    } catch (JEVisException e) {
                                        logger.error("Could not open pdf viewer", e);
                                    }
                                });
                            }

                            hBox.setSpacing(4);
                            setGraphic(hBox);

                        }

                    }
                };
            }

        };
    }

    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellBoolean() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            JEVisSample sample = tableSample.getJevisSample();

                            ToggleSwitchPlus toggleSwitchPlus = new ToggleSwitchPlus();
                            try {
                                toggleSwitchPlus.setSelected(sample.getValueAsBoolean());
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            toggleSwitchPlus.selectedProperty().addListener((ov, t, t1) -> {
                                try {
                                    sample.setValue(t1);
                                    sample.commit();
                                } catch (Exception ex) {
                                    logger.fatal(ex);
                                }
                            });

                            setGraphic(toggleSwitchPlus);

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
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellString() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();

                            JFXTextArea textField = new JFXTextArea(tableSample.getValue().toString());

                            JFXButton expand = new JFXButton(null);
                            expand.setBackground(new Background(new BackgroundImage(
                                    JEConfig.getImage("if_ExpandMore.png"),
                                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                                    new BackgroundSize(expand.getWidth(), expand.getHeight(),
                                            true, true, true, false))));

                            expand.setOnAction(event -> {
                                Platform.runLater(() -> {
                                    if (textField.getPrefRowCount() == 20) {
                                        textField.setPrefRowCount(1);
                                        textField.setWrapText(false);
                                    } else {
                                        textField.setPrefRowCount(20);
                                        textField.setWrapText(true);
                                    }

                                });

                            });

                            setDefaultFieldStyle(this, textField);
                            textField.setWrapText(false);
                            textField.setPrefRowCount(1);
                            textField.autosize();

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);


                                    tableSample.setValue(newValue + "");

                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in string text", ex);
                                }
                            });

                            HBox box = new HBox(5, textField, expand);

                            setGraphic(box);

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
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellPeriod() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            Period p = Period.ZERO;
                            try {
                                p = new Period(tableSample.getValue().toString());
                            } catch (Exception e) {
                                logger.error(e);
                            }
                            SamplingRateUI samplingRateUI = new SamplingRateUI(p);

                            setDefaultFieldStyle(this, samplingRateUI);
                            samplingRateUI.autosize();

                            samplingRateUI.samplingRateProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);
                                    tableSample.setValue(newValue + "");
                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in string text", ex);
                                }
                            });

                            setGraphic(samplingRateUI);
                        }

                    }
                };
            }

        };
    }

    /**
     * Create an Callback cell for String based passwords. Text will be shown as ****
     *
     * @return
     */
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellStringPassword() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();

                            JFXPasswordField textField = new JFXPasswordField();
                            textField.setText(tableSample.getValue().toString());
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);
                                    tableSample.setValue(newValue + "");

                                } catch (Exception ex) {
                                    setErrorCellStyle(this, ex);
                                    logger.error("Error in string text", ex);
                                }
                            });

                            HBox box = new HBox(5, textField);
                            HBox.setHgrow(textField, Priority.ALWAYS);
                            setGraphic(box);

                        }

                    }
                };
            }

        };
    }

    /**
     * Create an callback cell for integer values
     *
     * @return
     */
    private Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>> valueCellInteger() {
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
                            TableSample tableSample = (TableSample) getTableRow().getItem();
                            JFXTextField textField = new JFXTextField(tableSample.getValue().toString());
                            setDefaultFieldStyle(this, textField);
//                            textField.setDisable(!SampleTable.this.isEditable());
//                            this.disableProperty().bind(textField.disableProperty());

                            UnaryOperator<TextFormatter.Change> filter = t -> {

                                if (t.getControlNewText().isEmpty()) {
                                    t.setText("0");
                                } else {
                                    try {
                                        Long bewValue = Long.parseLong(t.getControlNewText());
                                    } catch (Exception ex) {
                                        t.setText("");
                                    }
                                }

                                return t;
                            };

                            textField.setTextFormatter(new TextFormatter<>(filter));

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    setDefaultCellStyle(this);

                                    tableSample.setValue(Long.parseLong(newValue));
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
                case JEVisConstants.PrimitiveType.LONG:
                    column.setCellFactory(valueCellInteger());
                    break;
                case JEVisConstants.PrimitiveType.DOUBLE:
                    column.setCellFactory(valueCellDouble());
                    break;
                case JEVisConstants.PrimitiveType.FILE:
                    column.setCellFactory(valueCellFile());
                    break;
                case JEVisConstants.PrimitiveType.BOOLEAN:
                    column.setCellFactory(valueCellBoolean());
                    break;
                default:
                    if (attribute.getName().equalsIgnoreCase("Password") || attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.PASSWORD_PBKDF2) {
                        column.setCellFactory(valueCellStringPassword());
                    } else if (attribute.getType().getGUIDisplayType().equalsIgnoreCase("Period")) {
                        column.setCellFactory(valueCellPeriod());
                    } else {
                        column.setCellFactory(valueCellString());
                    }

                    break;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        column.remainingWidth();
//        column.setEditable(true);
//        this.disableProperty().bind(column.editableProperty().not());
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
        column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getNote()));

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

                            JFXTextField textField = new JFXTextField(tableSample.getNote());
//                            textField.setDisable(!SampleTable.this.isEditable());
//                            this.disableProperty().bind(textField.disableProperty());

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

        return column;
    }


    /**
     * Inner Class to capsule JEVisSample to be manipulated in a table
     * <p>
     * TODO:
     * - Revert changed function
     */
    public class TableSample {

        private SimpleObjectProperty value = new SimpleObjectProperty();
        private SimpleStringProperty note = new SimpleStringProperty();
        private SimpleObjectProperty<DateTime> timeStamp = new SimpleObjectProperty<>();
        private final SimpleBooleanProperty isSelected = new SimpleBooleanProperty(false);

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
                this.timeStamp = new SimpleObjectProperty<>(jevisSample.getTimestamp());
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
         */
        private void setIsNew() {
            this.isNew = true;
            checkChanged();
        }

        @Override
        public String toString() {
            return "CSVExportTableSample{" +
                    "value=" + value +
                    ", note=" + note +
                    ", timeStamp=" + timeStamp +
                    ", jevisSample=" + jevisSample +
                    '}';
        }
    }
}
