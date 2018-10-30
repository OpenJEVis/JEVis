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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class SampleTable2 extends TableView<SampleTable2.TableSample> {
    static final DateTimeFormatter fmtDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger logger = LogManager.getLogger(SampleTable2.class);
    private static final Color COLOR_ERROR = Color.INDIANRED;
    private final JEVisAttribute attribute;

    public SampleTable2(JEVisAttribute attribute, List<JEVisSample> samples) {
        super();
        this.attribute = attribute;

        setPlaceholder(new Label("No Data"));

        buildAllColomns();


        setMinWidth(555d);//TODo: replace Dirty workaround
        setPrefHeight(200d);//TODo: replace Dirty workaround
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        final ObservableList<SampleTable2.TableSample> data = FXCollections.observableArrayList();
        samples.forEach(jeVisSample -> data.add(new SampleTable2.TableSample(jeVisSample)));

        TableColumn timeSpampColoumn = createTimeStampColumn("Timestamp");
        TableColumn valueCol = createValueColumn("Value");
        TableColumn noteColoumn = createNoteColumn("Note");

        valueCol.setMinWidth(100);


        getColumns().addAll(timeSpampColoumn, valueCol, noteColoumn);

        setEditable(true);

        setItems(data);


    }

    public void debugStuff() {
        System.out.println("DebugStuff");
        getItems().forEach(tableSample -> {
            System.out.println("Sample: " + tableSample);
        });
    }

    private void buildAllColomns() {

//        try {
//            if (attribute.getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
//                valueCol.setOnEditStart(new EventHandler<TableColumn.CellEditEvent>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent event) {
//                        try {
//                            event.consume();
//                            JEVisSample sample = ((org.jevis.jeconfig.sampletable.TableSample) event.getTableView().getItems().get(event.getTablePosition().getRow())).getSample();
//                            logger.info("Sample: " + sample.getTimestamp());
//
//                            try {
////                                loadWithAnimation();
//                                JEVisFile file = sample.getValueAsFile();
//
//                                FileChooser fileChooser = new FileChooser();
//                                fileChooser.setInitialFileName(file.getFilename());
//                                fileChooser.setTitle(I18n.getInstance().getString("plugin.object.attribute.file.download.title"));
//                                fileChooser.getExtensionFilters().addAll(
//                                        new FileChooser.ExtensionFilter("All Files", "*.*"));
//                                File selectedFile = fileChooser.showSaveDialog(null);
//                                if (selectedFile != null) {
//                                    JEConfig.setLastPath(selectedFile);
//                                    file.saveToFile(selectedFile);
//                                }
//                            } catch (Exception ex) {
//                                logger.fatal(ex);
//                            }
//
//
//                        } catch (Exception ex) {
//                            logger.fatal(ex);
//                        }
//                    }
//                });
//                valueCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent event) {
//                        event.consume();
//                    }
//                });
//
//            } else {
//                valueCol.setOnEditCommit(
//                        new EventHandler<TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String>>() {
//                            @Override
//                            public void handle(TableColumn.CellEditEvent<org.jevis.jeconfig.sampletable.TableSample, String> t) {
//                                t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
//                            }
//                        }
//                );
//            }
//
//        } catch (Exception ex) {
//            logger.fatal(ex);
//        }

    }


    /**
     * How to do custom edit:
     * overwrite startEdit in bzw isEdditing
     */

    private TableColumn<TableSample, DateTime> createTimeStampColumn(String columnName) {

        TableColumn<TableSample, DateTime> dateColumn = new TableColumn<>(columnName);
        dateColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper(param.getValue().getTimeStamp()));

        /** cellFactory defines the view while editing **/
        dateColumn.setCellFactory(new Callback<TableColumn<TableSample, DateTime>, TableCell<TableSample, DateTime>>() {
            @Override
            public TableCell<TableSample, DateTime> call(TableColumn<TableSample, DateTime> param) {
                return new TableCell<TableSample, DateTime>() {

                    @Override
                    public void startEdit() {
                        super.startEdit();
                        System.out.println("StartEdit");
                    }

                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
//                            setStyle("");
                        } else {
//                            setText(fmtDate.print(item));
                            TextField textField = new TextField(fmtDate.print(item));
                            textField.setStyle("-fx-background-color: transparent;" +
                                    "    -fx-background-insets: 0;" +
                                    "    -fx-padding: 0 0 0 0;");
                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                try {
                                    DateTime newDate = fmtDate.parseDateTime(textField.getText());
                                    System.out.println("NewDate: " + newDate);

                                    /**
                                     * TODO: - check if the new Date already exist, if so ask the user to replace(delete->new) the sample
                                     *       - check if the new date works with the timezone
                                     *       - ?add also an date and time selector gui?
                                     *       - if error set background color red and show error while save event
                                     **/

                                    TableSample tableSample = (TableSample) getTableRow().getItem();
                                    tableSample.setTimeStamp(newDate);
                                    setBackground(Background.EMPTY);
                                } catch (Exception ex) {
                                    setBackground(new Background(new BackgroundFill(COLOR_ERROR, CornerRadii.EMPTY, Insets.EMPTY)));
                                    System.out.println("Error while parsing date: " + ex);
                                    setNodeError(textField, ex);
                                }
                            });
                            textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue) {
                                    getSelectionModel().clearAndSelect(getTableRow().getIndex());
                                }
                            });


                            setGraphic(textField);

                        }

                    }
                };
            }

        });

        dateColumn.setEditable(true);
        return dateColumn;
    }

    private void setNodeError(Control node, Exception ex) {
//        node.setBackground(new Background(new BackgroundFill()));
//        node.setBackground(Background.EMPTY);
//        node.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        Tooltip tt = new Tooltip(ex.getMessage());
        node.setTooltip(tt);
    }

    /**
     * Example on how to use the column without the java reflection setValueFactory
     *
     * @param columnName
     * @return
     */
//    private TableColumn<TableSample, DateTime> createTimeStampColumnWithoutvalueFactory(String columnName) {
//
//        TableColumn<TableSample, DateTime> dateColumn = new TableColumn<>(columnName);
//        /** cellFactory defines the view while editing **/
//        dateColumn.setCellFactory(new Callback<TableColumn<TableSample, DateTime>, TableCell<TableSample, DateTime>>() {
//            @Override
//            public TableCell<TableSample, DateTime> call(TableColumn<TableSample, DateTime> param) {
//                return new TableCell<TableSample, DateTime>() {
//
//
//                    @Override
//                    protected void updateItem(DateTime item, boolean empty) {
//                        super.updateItem(item, empty);
//
//                        if (getTableRow() == null || getTableRow().getItem() == null) {
//                            setText(null);
//                            setStyle("");
//                        } else {
//                            TableSample tableSample = (TableSample) getTableRow().getItem();
//                            TextField textField = new TextField(fmtDate.print(tableSample.getTimeStamp()));
//                            textField.setStyle("-fx-background-color: transparent;" +
//                                    "    -fx-background-insets: 0;" +
//                                    "    -fx-padding: 0 0 0 0;");
//                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
//                                try {
//                                    DateTime newDate = fmtDate.parseDateTime(textField.getText());
//                                    System.out.println("NewDate: " + newDate);
//                                    tableSample.setTimeStamp(newDate);
//
//                                } catch (Exception ex) {
//                                    System.out.println("Error while parsing date: " + ex);
//                                }
//                            });
//                            textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
//                                if (newValue) {
//                                    getSelectionModel().clearAndSelect(getTableRow().getIndex());
//                                }
//                            });
//
//                            setGraphic(textField);
//                        }
//
//                    }
//                }
//
//                        ;
//            }
//
//        });
//
//        dateColumn.setEditable(true);
//        return dateColumn;
//    }
    private TableColumn<TableSample, Object> createValueColumn(String columnName) {

        TableColumn<TableSample, Object> valueCol = new TableColumn<>(columnName);
        valueCol.setCellValueFactory(param -> {
            SimpleObjectProperty<Object> property = new SimpleObjectProperty<>();
            property.setValue(param.getValue().getValue());
            return property;
        });
        valueCol.setEditable(true);

        return valueCol;
    }

    private TableColumn<TableSample, Object> createNoteColumn(String columnName) {

        TableColumn<TableSample, Object> valueCol = new TableColumn<>(columnName);
        valueCol.setCellValueFactory(param -> {
            SimpleObjectProperty<Object> property = new SimpleObjectProperty<>();
            property.setValue(param.getValue().getNote());
            return property;
        });

        valueCol.setCellFactory(new Callback<TableColumn<TableSample, Object>, TableCell<TableSample, Object>>() {
            @Override
            public TableCell<TableSample, Object> call(TableColumn<TableSample, Object> param) {
                return new TableCell() {
                    @Override
                    public void startEdit() {
                        super.startEdit();
                        System.out.println("Start edit");
                    }

                    @Override
                    public void updateIndex(int i) {
                        super.updateIndex(i);
                        System.out.println("Update Item: " + i);
                    }

                    @Override
                    protected void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        System.out.println("updateItem: " + item + " empty2: " + empty);
                    }
                };
            }
        });

        valueCol.setEditable(true);
        return valueCol;
    }


    public class TableSample {

        private SimpleObjectProperty value;
        private SimpleStringProperty note;
        private SimpleObjectProperty<DateTime> timeStamp;

        private JEVisSample jevisSample = null;


        public TableSample(JEVisSample sample) {
            try {
                this.timeStamp = new SimpleObjectProperty(sample.getTimestamp());
                this.value = new SimpleObjectProperty(sample.getValue());
                this.note = new SimpleStringProperty(sample.getNote());
                jevisSample = sample;

                timeStamp.addListener((observable, oldValue, newValue) -> {
                    System.out.println("TableSample: New value for: " + value + " " + newValue);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public Object getValue() {
            return value.get();
        }

        public void setValue(Object value) {
            this.value.set(value);
        }

        public SimpleObjectProperty valueProperty() {
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
