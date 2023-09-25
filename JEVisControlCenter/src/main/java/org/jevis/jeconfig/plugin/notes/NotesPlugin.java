package org.jevis.jeconfig.plugin.notes;

import com.jfoenix.controls.*;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.datetime.DateHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.*;
import org.jevis.jeconfig.application.Chart.TimeFrame;
import org.jevis.jeconfig.application.jevistree.methods.DataMethods;
import org.jevis.jeconfig.application.tools.JEVisHelp;
import org.jevis.jeconfig.plugin.AnalysisRequest;
import org.jevis.jeconfig.plugin.charts.ChartPlugin;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class NotesPlugin implements Plugin {
    private static final Logger logger = LogManager.getLogger(NotesPlugin.class);
    public static String PLUGIN_NAME = "Notes Plugin";
    public static String NOTES_CLASS = "Data Notes";
    private static Method columnToFitMethod;

    static {
        try {
            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
            columnToFitMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private final JEVisDataSource ds;
    private final String title;
    private final BorderPane borderPane = new BorderPane();
    private final ToolBar toolBar = new ToolBar();
    private final int iconSize = 20;
    private final DateHelper dateHelper = new DateHelper(DateHelper.TransformType.PREVIEW);
    private final List<Task<List<NotesRow>>> runningUpdateTaskList = new ArrayList<>();
    private final Image taskImage = JEConfig.getImage("rodentia-icons_text-x-playlist.png");

    private final TableView<NotesRow> tableView = new TableView<>();
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(I18n.getInstance().getLocale());
    private final JFXDatePicker startDatePicker = new JFXDatePicker();
    private final JFXDatePicker endDatePicker = new JFXDatePicker();
    private final boolean init = false;
    private DateTime start;
    private DateTime end;
    private TimeFrame timeFrame = TimeFrame.LAST_WEEK;


    ObservableList<NotesRow> data = FXCollections.observableArrayList();
    FilteredList<NotesRow> filteredData = new FilteredList<>(data);
    BooleanProperty searchInNote = new SimpleBooleanProperty(false);
    BooleanProperty searchInUser = new SimpleBooleanProperty(false);
    BooleanProperty searchInDataRow = new SimpleBooleanProperty(false);
    StringProperty searchTextProperty = new SimpleStringProperty("");
    ObservableList<String> selectedTags = FXCollections.observableArrayList();
    ObservableList<String> allTags = FXCollections.observableArrayList();
    HashMap<String, BooleanProperty> activeTags = new HashMap<>();

    public NotesPlugin(JEVisDataSource ds, String title) {
        this.ds = ds;
        this.title = title;
        this.borderPane.setTop(searchPanel());

        this.borderPane.setCenter(this.tableView);
        Label label = new Label(I18n.getInstance().getString("plugin.notes.nonotes"));
        label.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        this.tableView.setPlaceholder(label);

        this.tableView.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        tableView.setBorder(new Border(new BorderStroke(Paint.valueOf("#b5bbb7"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN, new Insets(20, 20, 20, 20))));
        //this.tableView.setPadding(new Insets(20));


        this.numberFormat.setMinimumFractionDigits(2);
        this.numberFormat.setMaximumFractionDigits(2);

        this.startDatePicker.setPrefWidth(120d);
        this.startDatePicker.getStyleClass().add("ToolBarDatePicker");
        this.endDatePicker.setPrefWidth(120d);
        this.endDatePicker.getStyleClass().add("ToolBarDatePicker");

        tableView.setItems(filteredData);

        createColumns();
    }

    private GridPane searchPanel() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(12));
        gridPane.setHgap(12);

        JFXTextField searchbar = new JFXTextField();
        searchbar.setPromptText(I18n.getInstance().getString("plugin.notes.search.prompt"));
        searchbar.setMinWidth(250);

        searchbar.textProperty().addListener((observable, oldValue, newValue) -> {
            searchTextProperty.set(newValue);
        });


        Label filterLabel = new Label(I18n.getInstance().getString("plugin.notes.search.filterlabel"));
        JFXToggleButton toggleUser = new JFXToggleButton();
        toggleUser.setText(I18n.getInstance().getString("plugin.notes.search.toggle.user"));
        toggleUser.setSelected(true);
        JFXToggleButton toggleNote = new JFXToggleButton();
        toggleNote.setText(I18n.getInstance().getString("plugin.notes.search.toggle.note"));
        toggleNote.setSelected(true);
        JFXToggleButton toggleDR = new JFXToggleButton();
        toggleDR.setText(I18n.getInstance().getString("plugin.notes.search.toggle.data"));
        toggleDR.setSelected(true);

        JFXToggleButton toggleTag = new JFXToggleButton();
        toggleTag.setText(I18n.getInstance().getString("plugin.notes.search.toggle.tag"));
        toggleTag.setSelected(true);


        searchInUser = toggleUser.selectedProperty();
        searchInNote = toggleNote.selectedProperty();
        searchInDataRow = toggleDR.selectedProperty();

        searchTextProperty.addListener((observable, oldValue, newValue) -> {
            filter();
        });
        searchInUser.addListener((observable, oldValue, newValue) -> {
            filter();
        });
        searchInNote.addListener((observable, oldValue, newValue) -> {
            filter();
        });
        searchInDataRow.addListener((observable, oldValue, newValue) -> {
            filter();
        });

/*
        ObservableList<NoteTag> noteTags = FXCollections.observableArrayList();
        noteTags.add(NoteTag.TAG_ERROR);
        noteTags.add(NoteTag.TAG_REMINDER);
        noteTags.add(NoteTag.TAG_AUDIT);
        noteTags.add(NoteTag.TAG_TASK);
        noteTags.add(NoteTag.TAG_EVENT);
        noteTags.add(NoteTag.TAG_REPORT);
*/
        NoteTag.getAllTags().forEach(noteTag -> {
            allTags.add(noteTag.getName());
            activeTags.put(noteTag.getName(), new SimpleBooleanProperty(true));
        });

        Node tagMenu = createContextMenu();

        HBox hBox = new HBox();
        hBox.setFillHeight(true);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(filterLabel, toggleUser, toggleNote, toggleDR, toggleTag);

        Region spacer = new Region();
        spacer.setMinHeight(12);

        gridPane.add(searchbar, 0, 1, 1, 1);
        gridPane.add(tagMenu, 1, 1, 1, 1);
        //gridPane.add(spacer, 0, 1, 2, 1);
        gridPane.add(hBox, 0, 2, 2, 1);

        return gridPane;
    }


    private Node createContextMenu() {

        ContextMenu cm = new ContextMenu();
        cm.setOnHidden(ev -> {
            //System.out.println("Hide");
        });


        MenuItem selectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectall"));
        selectAllMenuItem.setOnAction(event -> {
            selectedTags.clear();
            selectedTags.addAll(allTags);
            activeTags.forEach((s, booleanProperty) -> {
                booleanProperty.setValue(true);
                filter();
            });
        });

        MenuItem deselectAllMenuItem = new MenuItem(I18n.getInstance().getString("plugin.notes.contextmenu.selectnone"));
        deselectAllMenuItem.setOnAction(event -> {
            selectedTags.clear();
            activeTags.forEach((s, booleanProperty) -> {
                booleanProperty.setValue(false);
                filter();
            });
        });
        cm.getItems().addAll(selectAllMenuItem, deselectAllMenuItem);

        activeTags.forEach((tagKey, tagAktiv) -> {
            JFXCheckBox cb = new JFXCheckBox(tagKey);
            cb.selectedProperty().bindBidirectional(tagAktiv);
            //cb.setSelected(tagAktiv.get());
            cb.setOnAction(event -> {
                //tagAktiv.setValue(!tagAktiv.getValue());
                filter();
            });
            CustomMenuItem cmi = new CustomMenuItem(cb);
            cm.getItems().add(cmi);
        });

        JFXButton tagButton = new JFXButton(I18n.getInstance().getString("plugin.notes.contextmenu.tags"));
        tagButton.setContextMenu(cm);
        tagButton.setOnAction(event -> {
            cm.show(tagButton, Side.BOTTOM, 0, 0);
        });

        return tagButton;

    }


    private void filter() {
        //System.out.println("---------------------------------------------------------------------------------------------");
        //System.out.println("Searchabr: " + searchTextProperty.get());
        //System.out.println("Finter: " + searchTextProperty.get() + " U: " + searchInUser.get() + " O: " + searchInDataRow.get() + " N: " + searchInNote.get());
        //System.out.println("List: " + data.size());
        filteredData.setPredicate(
                new Predicate<NotesRow>() {
                    @Override
                    public boolean test(NotesRow notesRow) {
                        // System.out.println("Filter.predict: " + notesRow.getTags());
                        // System.out.println("Filter.Node: " + notesRow);
                        try {
                            AtomicBoolean tagMatch = new AtomicBoolean(false);
                            activeTags.forEach((s, booleanProperty) -> {
                                try {
                                    if (booleanProperty.get()) {
                                        notesRow.getTags().forEach(noteTag -> {
                                            if (noteTag.getName().equals(s)) {
                                                tagMatch.set(true);
                                            }
                                        });
                                    }
                                } catch (Exception ex) {

                                }
                            });


                            if (!tagMatch.get()) {
                                // System.out.println(".... no tag machts found");
                                return false;
                            }

                            //if (tagMatch.get() && searchTextProperty.get().isEmpty()) {
                            //   return true;
                            //}

                            if (searchTextProperty.get().isEmpty()) {
                                return true;
                            }

                            if (searchInUser.get() && notesRow.getUser().contains(searchTextProperty.get())) {
                                return true;
                            }

                            if (searchInDataRow.get() && getFullName(notesRow.getObject()).contains(searchTextProperty.get())) {
                                return true;
                            }

                            if (searchInNote.get() && notesRow.getNote().contains(searchTextProperty.get())) {
                                return true;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        return false;
                    }
                });
        Platform.runLater(() -> autoFitTable(tableView));
        Platform.runLater(() -> tableView.sort());

    }

    private void updateList() {

        Platform.runLater(this::initToolBar);

        /**
         if (init) {
         //restartExecutor();
         } else {
         init = true;
         }
         **/

        JEConfig.getStatusBar().stopTasks(NotesPlugin.class.getName());
        this.runningUpdateTaskList.clear();
        data.clear();
        filteredData.clear();

        List<JEVisObject> noteObjects = getAllNoteObjects();
        JEConfig.getStatusBar().startProgressJob(NotesPlugin.class.getName(), noteObjects.size(), I18n.getInstance().getString("plugin.alarms.message.loadingconfigs"));
        noteObjects.forEach(noteObject -> {
            Task<List<NotesRow>> task = new Task<List<NotesRow>>() {
                @Override
                protected List<NotesRow> call() {
                    //List<NotesRow> list = new ArrayList<>();
                    try {
                        Platform.runLater(() -> this.updateTitle(I18n.getInstance().getString("plugin.notes.loading") + " '" + noteObject.getName() + "'"));
                        JEVisAttribute userNotes = noteObject.getAttribute("Value");
                        //list.addAll(getNotesRow(userNotes));
                        List<NotesRow> notesRow = getNotesRow(userNotes);
                        Platform.runLater(() -> {
                            data.addAll(notesRow);
                            filter();
                        });
                        //Platform.runLater(() -> autoFitTable(tableView));
                        //if (noteObjects.indexOf(noteObject) % 5 == 0
                        //       || noteObjects.indexOf(noteObject) == noteObjects.size() - 1) {
                        //  Platform.runLater(() -> tableView.sort());
                        //}
                        this.succeeded();
                    } catch (Exception e) {
                        logger.error(e);
                        this.failed();
                    } finally {
                        this.done();
                        JEConfig.getStatusBar().progressProgressJob(
                                NotesPlugin.class.getName(),
                                1,
                                I18n.getInstance().getString("plugin.notes.message.finishedloading") + " " + noteObject.getName());
                    }

                    return null;
                }
            };
            JEConfig.getStatusBar().addTask(NotesPlugin.class.getName(), task, taskImage, true);//,


            /** check if all Jobs are done/failed to set statusbar **/
            /**
             EventHandler<WorkerStateEvent> doneEvent = event -> {
             if (allJobsDone(futures)) {
             JEConfig.getStatusBar().finishProgressJob("AlarmConfigs", "");
             Platform.runLater(() -> tableView.sort());
             Platform.runLater(() -> autoFitTable(tableView));
             }
             };
             Platform.runLater(() -> {
             task.setOnSucceeded(doneEvent);
             task.setOnFailed(doneEvent);
             });
             **/

            this.runningUpdateTaskList.add(task);
        });


    }

    private final JFXComboBox<TimeFrame> timeFrameComboBox = getTimeFrameComboBox();

    public static void autoFitTable(TableView<NotesRow> tableView) {
        for (TableColumn<NotesRow, ?> column : tableView.getColumns()) {
            try {
                if (tableView.getSkin() != null) {
                    columnToFitMethod.invoke(tableView.getSkin(), column, -1);
                }
            } catch (Exception e) {
            }
        }
    }

    private final ChangeListener<LocalDate> startDateChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != oldValue) {
            start = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 0, 0, 0);
            timeFrame = TimeFrame.CUSTOM;

            updateList();
            Platform.runLater(this::initToolBar);
        }
    };

    private void restartExecutor() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(I18n.getInstance().getString("plugin.alarms.info.wait"));
            alert.show();
            JEConfig.getStatusBar().startProgressJob("stoppingAlarms", runningUpdateTaskList.size(), I18n.getInstance().getString("plugin.alarms.message.stoppingthreads"));

            JEConfig.getStatusBar().stopTasks(NotesPlugin.class.getName());
            this.runningUpdateTaskList.clear();
            JEConfig.getStatusBar().finishProgressJob("stoppingAlarms", I18n.getInstance().getString("plugin.alarms.message.stoppedall"));

            alert.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }    //    private ObservableList<AlarmRow> alarmRows = FXCollections.observableArrayList();

    private final ChangeListener<LocalDate> endDateChangeListener = (observable, oldValue, newValue) -> {
        if (newValue != oldValue) {
            end = new DateTime(newValue.getYear(), newValue.getMonthValue(), newValue.getDayOfMonth(), 23, 59, 59);
            timeFrame = TimeFrame.CUSTOM;

            updateList();
            Platform.runLater(this::initToolBar);
        }
    };

    private void createColumns() {
        TableColumn<NotesRow, DateTime> dateColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.date"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, DateTime>("date"));
        dateColumn.setStyle("-fx-alignment: CENTER;");
        dateColumn.setSortable(true);
//        dateColumn.setPrefWidth(160);
        dateColumn.setMinWidth(100);
        dateColumn.setSortType(TableColumn.SortType.DESCENDING);

        dateColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getTimeStamp() != null)
                return new SimpleObjectProperty<>(param.getValue().getTimeStamp());
            else return new SimpleObjectProperty<>();
        });

        dateColumn.setCellFactory(new Callback<TableColumn<NotesRow, DateTime>, TableCell<NotesRow, DateTime>>() {
            @Override
            public TableCell<NotesRow, DateTime> call(TableColumn<NotesRow, DateTime> param) {
                return new TableCell<NotesRow, DateTime>() {
                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.toString("yyyy-MM-dd HH:mm:ss"));
                        }
                    }
                };
            }
        });

        TableColumn<NotesRow, String> noteColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.configname"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, String>("configname"));
        noteColumn.setStyle("-fx-alignment: CENTER;");
        noteColumn.setSortable(true);
        noteColumn.setPrefWidth(500);
        noteColumn.setMinWidth(100);

        noteColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null) {
                return param.getValue().notePropertyProperty();
            } else return new SimpleObjectProperty<>();

            /**

             if (param != null && param.getValue() != null && param.getValue().getNote() != null) {


             SimpleObjectProperty newB = new SimpleObjectProperty<>(param.getValue().getNote());
             newB.addListener(new ChangeListener() {
            @Override public void changed(ObservableValue observable, Object oldValue, Object newValue) {
            param.getValue().setNode(newValue.toString());
            System.out.println("NEw value to save: " + newValue.toString());
            }
            });

             return newB;
             } else return new SimpleObjectProperty<>();
             **/
        });

        noteColumn.setEditable(true);

        noteColumn.setCellFactory(new Callback<TableColumn<NotesRow, String>, TableCell<NotesRow, String>>() {
            @Override
            public TableCell<NotesRow, String> call(TableColumn<NotesRow, String> param) {
                return new TableCell<NotesRow, String>() {
                    @Override
                    public void startEdit() {
                        super.startEdit();

                        NotesRow tableSample = (NotesRow) getTableRow().getItem();
                        JFXTextArea jfxTextArea = new JFXTextArea(tableSample.getNoteProperty().get());
                        jfxTextArea.setWrapText(true);
                        jfxTextArea.setPrefRowCount(8);
                        jfxTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue) {
                                //commitEdit(jfxTextArea.getText());
                                tableSample.getNoteProperty().setValue(jfxTextArea.getText());
                                commitEdit(jfxTextArea.getText());
                            }
                        });

                        setGraphic(jfxTextArea);
                    }


                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            //setText(item);
                            Button button = new Button("...");
                            Label label = new Label(item);
                            label.setWrapText(false);
                            label.setMaxHeight(12);


                            setOnMouseClicked(event -> {
                                if (event.getClickCount() == 2) {
                                    startEdit();
                                }
                            });
                            GridPane gridPane = new GridPane();
                            gridPane.addRow(0, label, button);
                            GridPane.setHgrow(label, Priority.ALWAYS);
                            GridPane.setValignment(button, VPos.TOP);

                            if (item.contains("\n")) {
                                button.setVisible(true);
                            } else {
                                button.setVisible(true);
                            }

                            button.setOnAction(event -> {
                                if (label.isWrapText()) {
                                    label.setMaxHeight(12);
                                    label.setWrapText(false);
                                } else {
                                    label.setMaxHeight(60);
                                    label.setWrapText(true);
                                }


                            });

                            gridPane.setCache(true);
                            gridPane.setCacheHint(CacheHint.QUALITY);
                            setGraphic(gridPane);
                        }
                    }
                };
            }
        });

        TableColumn<NotesRow, JEVisObject> objectNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.objectname"));
        objectNameColumn.setCellValueFactory(new PropertyValueFactory<NotesRow, JEVisObject>("objectname"));
        objectNameColumn.setStyle("-fx-alignment: CENTER;");
        objectNameColumn.setSortable(true);
//        objectNameColumn.setPrefWidth(500);
        objectNameColumn.setMinWidth(100);

        objectNameColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getObject() != null)
                return new SimpleObjectProperty<>(param.getValue().getObject());
            else return new SimpleObjectProperty<>();
        });

        objectNameColumn.setCellFactory(new Callback<TableColumn<NotesRow, JEVisObject>, TableCell<NotesRow, JEVisObject>>() {
            @Override
            public TableCell<NotesRow, JEVisObject> call(TableColumn<NotesRow, JEVisObject> param) {
                return new TableCell<NotesRow, JEVisObject>() {
                    @Override
                    protected void updateItem(JEVisObject item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String text = "";
                            try {

                                text += getFullName(item);

                                if (getTableRow() != null && getTableRow().getItem() != null) {
                                    NotesRow notesRow = (NotesRow) getTableRow().getItem();


                                    this.setOnMouseClicked(event -> JEConfig.openObjectInPlugin(ChartPlugin.PLUGIN_NAME, getAnalysisRequest(notesRow, item)));
                                    this.hoverProperty().addListener((observable, oldValue, newValue) -> {
                                        if (newValue) {
                                            this.getScene().setCursor(Cursor.HAND);
                                        } else {
                                            this.getScene().setCursor(Cursor.DEFAULT);
                                        }
                                    });

                                    /*
                                    if (getTableRow().isSelected()) {
                                        setTextFill(Paint.valueOf("white"));//Color.BLUE
                                    } else {
                                        setTextFill(Paint.valueOf("#51aaa5"));//Color.BLUE
                                    }
                                    */
                                    //LinkColor

                                    setUnderline(true);

                                }
                            } catch (JEVisException e) {
                                e.printStackTrace();
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        TableColumn<NotesRow, List<NoteTag>> tagColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.tag"));
        tagColumn.setStyle("-fx-alignment: CENTER;");
        tagColumn.setSortable(true);
        tagColumn.setMinWidth(100);

        tagColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getObject() != null) {
                return new SimpleObjectProperty<>(param.getValue().getTags());
            } else return new SimpleObjectProperty<>();
        });

        tagColumn.setCellFactory(new Callback<TableColumn<NotesRow, List<NoteTag>>, TableCell<NotesRow, List<NoteTag>>>() {
            @Override
            public TableCell<NotesRow, List<NoteTag>> call(TableColumn<NotesRow, List<NoteTag>> param) {
                return new TableCell<NotesRow, List<NoteTag>>() {
                    @Override
                    protected void updateItem(List<NoteTag> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            String text = "";
                            try {
                                for (NoteTag noteTag : item) {
                                    text += noteTag.getName() + ", ";
                                }
                                text = text.substring(0, text.length() - 2);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            setText(text);
                        }
                    }
                };
            }
        });

        TableColumn<NotesRow, String> userNameColumn = new TableColumn<>(I18n.getInstance().getString("plugin.notes.table.username"));
        userNameColumn.setStyle("-fx-alignment: CENTER;");
        userNameColumn.setSortable(true);
        userNameColumn.setMinWidth(100);

        userNameColumn.setCellValueFactory(param -> {
            if (param != null && param.getValue() != null && param.getValue().getObject() != null)
                return new SimpleStringProperty(param.getValue().getUser());
            else return new SimpleStringProperty("");
        });

        userNameColumn.setCellFactory(new Callback<TableColumn<NotesRow, String>, TableCell<NotesRow, String>>() {
            @Override
            public TableCell<NotesRow, String> call(TableColumn<NotesRow, String> param) {
                return new TableCell<NotesRow, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                        }
                    }
                };
            }
        });


        tableView.getColumns().setAll(dateColumn, noteColumn, objectNameColumn, tagColumn, userNameColumn);
        Platform.runLater(() -> {
            tableView.getSortOrder().clear();
            tableView.getSortOrder().setAll(dateColumn);
        });


    }

    private String getFullName(JEVisObject item) throws JEVisException {
        String name = "";
        JEVisObject firstParentalDataObject = DataMethods.getFirstParentalDataObject(item);

        if (firstParentalDataObject != null) {
            name += firstParentalDataObject.getName();
            name += getFollowUpName(firstParentalDataObject, item);
        }
        return name;
    }

    private String getFollowUpName(JEVisObject firstParentalDataObject, JEVisObject item) throws JEVisException {
        String name = "";
        for (JEVisObject parent : item.getParents()) {
            if (!parent.equals(firstParentalDataObject)) {
                name += " - ";
                name += parent.getName();
                name += getFollowUpName(firstParentalDataObject, parent);
            }
        }
        return name;
    }

    private Object getAnalysisRequest(NotesRow notesRow, JEVisObject noteItem) {
        logger.debug("getAnalysisRequest: " + notesRow + " item: " + noteItem);
        DateTime start = notesRow.getTimeStamp().minusHours(12);
        DateTime end = notesRow.getTimeStamp().plusHours(12);

        JEVisObject parallelItem = null;

        try {
            JEVisObject parent = noteItem.getParents().get(0);
            parallelItem = parent;
            /**
             for (JEVisObject child : parent.getChildren()) {
             if (!child.equals(noteItem) && child.getName().equals(noteItem.getName())) {
             parallelItem = child;
             break;
             }
             }
             **/
        } catch (Exception e) {
            logger.error("Could not get parallel item for note object {}:{}", noteItem.getName(), noteItem.getID());
        }

        return new AnalysisRequest(parallelItem, AggregationPeriod.NONE, ManipulationMode.NONE, start, end);
    }

    private void initToolBar() {
        ToggleButton reload = new ToggleButton("", JEConfig.getSVGImage(Icon.REFRESH, iconSize, iconSize));
        Tooltip reloadTooltip = new Tooltip(I18n.getInstance().getString("plugin.alarms.reload.progress.tooltip"));
        reload.setTooltip(reloadTooltip);

        ToggleButton newB = new ToggleButton("", JEConfig.getSVGImage(Icon.PLUS_CIRCLE, 18, 18));
        newB.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.notes.tooltip.add")));
        ToggleButton save = new ToggleButton("", JEConfig.getSVGImage(Icon.SAVE, this.iconSize, this.iconSize));
        save.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.notes.tooltip.save")));
        ToggleButton delete = new ToggleButton("", JEConfig.getSVGImage(Icon.DELETE, this.iconSize, this.iconSize));
        delete.setTooltip(new Tooltip(I18n.getInstance().getString("plugin.notes.tooltip.delete")));

        GlobalToolBar.changeBackgroundOnHoverUsingBinding(reload);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(newB);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(save);
        GlobalToolBar.changeBackgroundOnHoverUsingBinding(delete);

        reload.setOnAction(event -> {

            final String loading = I18n.getInstance().getString("plugin.alarms.reload.progress.message");
            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() {
                            updateMessage(loading);
                            try {
                                ds.clearCache();
                                ds.preload();

                                updateList();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };
                }
            };
            ProgressDialog pd = new ProgressDialog(service);
            pd.setHeaderText(I18n.getInstance().getString("plugin.reports.reload.progress.header"));
            pd.setTitle(I18n.getInstance().getString("plugin.reports.reload.progress.title"));
            pd.getDialogPane().setContent(null);

            service.start();

        });



        tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    addNote();
                }
            }
        });

        newB.setOnAction(event -> {
            addNote();
        });

        delete.setOnAction(event ->

    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(I18n.getInstance().getString("plugin.note.delete.title"));
        alert.setHeaderText(I18n.getInstance().getString("plugin.note.delete.message"));
        //alert.setContentText("Before : ");

        ButtonType buttonYes = new ButtonType(I18n.getInstance().getString("plugin.note.delete.delete"));
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(buttonYes, ButtonType.CANCEL);

        Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        noButton.setDefaultButton(true);

        final Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonYes) {
            tableView.getSelectionModel().getSelectedItems().forEach(notesRow -> {
                notesRow.delete();
                data.remove(notesRow);
            });
            filter();
        }

    });

        save.setOnAction(event ->

    {
        this.data.forEach(notesRow -> {
            if (notesRow.hasChanged()) {
                logger.debug("Note Has changed commit:");
                notesRow.commit();
            }
        });
    });

    Separator sep1 = new Separator(Orientation.VERTICAL);
    Separator sep2 = new Separator(Orientation.VERTICAL);

        if(start !=null)

    {
        startDatePicker.valueProperty().removeListener(startDateChangeListener);
        startDatePicker.setValue(LocalDate.of(start.getYear(), start.getMonthOfYear(), start.getDayOfMonth()));
    }


        if(end !=null)

    {
        endDatePicker.valueProperty().removeListener(endDateChangeListener);
        endDatePicker.setValue(LocalDate.of(end.getYear(), end.getMonthOfYear(), end.getDayOfMonth()));
    }


        startDatePicker.valueProperty().

    addListener(startDateChangeListener);
        endDatePicker.valueProperty().

    addListener(endDateChangeListener);

    ToggleButton infoButton = JEVisHelp.getInstance().buildInfoButtons(iconSize, iconSize);
    ToggleButton helpButton = JEVisHelp.getInstance().buildHelpButtons(iconSize, iconSize);

        timeFrameComboBox.setTooltip(new

    Tooltip(I18n.getInstance().

    getString("plugin.alarms.reload.timebox.tooltip")));
        startDatePicker.setTooltip(new

    Tooltip(I18n.getInstance().

    getString("plugin.alarms.reload.startdate.tooltip")));
        endDatePicker.setTooltip(new

    Tooltip(I18n.getInstance().

    getString("plugin.alarms.reload.enddate.tooltip")));

        toolBar.getItems().

    setAll(timeFrameComboBox, sep1, startDatePicker, endDatePicker, sep2, reload, newB, save, delete);
        toolBar.getItems().

    addAll(JEVisHelp.getInstance().

    buildSpacerNode(),helpButton,infoButton);

        JEVisHelp.getInstance().

    addHelpItems(NotesPlugin .class.getSimpleName(), "",JEVisHelp.LAYOUT.VERTICAL_BOT_CENTER,toolBar.getItems());

}

    private void addNote() {
        NotePane notePane = new NotePane(allTags, ds, Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()));

        ButtonType okButtonType = new ButtonType(I18n.getInstance().getString("plugin.note.pane.ok"), ButtonBar.ButtonData.APPLY);
        ButtonType cancelButtonType = new ButtonType(I18n.getInstance().getString("plugin.note.pane.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);


        //JFXButton okButton = new JFXButton(I18n.getInstance().getString("plugin.note.pane.ok"));
        //JFXButton cancelButton = new JFXButton(I18n.getInstance().getString("plugin.note.pane.cancel"));

        notePane.getDialogPane().getButtonTypes().setAll(okButtonType, cancelButtonType);

        //HBox buttonBox = new HBox(cancelButton, okButton);
        //buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        //buttonBox.setSpacing(12);
        //Separator separator = new Separator();
        //separator.setOrientation(Orientation.HORIZONTAL);
        //okButton.setDefaultButton(true);


        //VBox vBox = new VBox(notePane, buttonBox);
        //vBox.setSpacing(12);
        //vBox.setAlignment(Pos.BOTTOM_RIGHT);
        //vBox.setPadding(new Insets(12));


        //Dialog Dialog = new Dialog();
        //Dialog.setResizable(true);
        Stage stage = (Stage) notePane.getDialogPane().getScene().getWindow();
        TopMenu.applyActiveTheme(stage.getScene());
        stage.setAlwaysOnTop(true);

        //Dialog.getDialogPane().setContent(vBox);

        final Button btOk = (Button) notePane.getDialogPane().lookupButton(okButtonType);
        final Button btCancel = (Button) notePane.getDialogPane().lookupButton(cancelButtonType);

        btOk.setOnAction(event1 -> {
            try {
                Optional<NotesRow> notesRow = notePane.commit();
                data.add(notesRow.orElseThrow(() -> new RuntimeException("could not commit")));
                filter();

            } catch (Exception e) {
                logger.error(e);
            }
                notePane.close();
    });
        btCancel.setOnAction(event1 -> {
            notePane.close();
        });

        notePane.show();
    }

    private JFXComboBox<TimeFrame> getTimeFrameComboBox() {
        JFXComboBox<TimeFrame> box = new JFXComboBox<>();

        final String today = I18n.getInstance().getString("plugin.graph.changedate.buttontoday");
        final String yesterday = I18n.getInstance().getString("plugin.graph.changedate.buttonyesterday");
        final String last7Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast7days");
        final String thisWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonthisweek");
        final String lastWeek = I18n.getInstance().getString("plugin.graph.changedate.buttonlastweek");
        final String last30Days = I18n.getInstance().getString("plugin.graph.changedate.buttonlast30days");
        final String thisMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonthismonth");
        final String lastMonth = I18n.getInstance().getString("plugin.graph.changedate.buttonlastmonth");
        final String thisYear = I18n.getInstance().getString("plugin.graph.changedate.buttonthisyear");
        final String lastYear = I18n.getInstance().getString("plugin.graph.changedate.buttonlastyear");
        final String custom = I18n.getInstance().getString("plugin.graph.changedate.buttoncustom");
        final String preview = I18n.getInstance().getString("plugin.graph.changedate.preview");

        ObservableList<TimeFrame> timeFrames = FXCollections.observableArrayList(TimeFrame.values());
        timeFrames.remove(TimeFrame.values().length - 2, TimeFrame.values().length - 1);
        box.setItems(timeFrames);

        Callback<ListView<TimeFrame>, ListCell<TimeFrame>> cellFactory = new Callback<ListView<TimeFrame>, ListCell<TimeFrame>>() {
            @Override
            public ListCell<TimeFrame> call(ListView<TimeFrame> param) {
                return new ListCell<TimeFrame>() {
                    @Override
                    protected void updateItem(TimeFrame timeFrame, boolean empty) {
                        super.updateItem(timeFrame, empty);
                        setText(null);
                        setGraphic(null);

                        if (timeFrame != null && !empty) {
                            String text = "";
                            switch (timeFrame) {
                                case TODAY:
                                    text = today;
                                    break;
                                case YESTERDAY:
                                    text = yesterday;
                                    break;
                                case LAST_7_DAYS:
                                    text = last7Days;
                                    break;
                                case THIS_WEEK:
                                    text = thisWeek;
                                    break;
                                case LAST_WEEK:
                                    text = lastWeek;
                                    break;
                                case LAST_30_DAYS:
                                    text = last30Days;
                                    break;
                                case THIS_MONTH:
                                    text = thisMonth;
                                    break;
                                case LAST_MONTH:
                                    text = lastMonth;
                                    break;
                                case THIS_YEAR:
                                    text = thisYear;
                                    break;
                                case LAST_YEAR:
                                    text = lastYear;
                                    break;
                                case CUSTOM: {
                                    text = custom;
                                    break;
                                }
                                case PREVIEW:
                                    text = preview;
                                    break;
                            }
                            setText(text);
                        }
                    }
                };
            }
        };
        box.setCellFactory(cellFactory);
        box.setButtonCell(cellFactory.call(null));
        box.getSelectionModel().select(timeFrame);

        box.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                switch (newValue) {
                    case CUSTOM:
                        break;
                    case TODAY:
                        dateHelper.setType(DateHelper.TransformType.TODAY);
                        break;
                    case YESTERDAY:
                        dateHelper.setType(DateHelper.TransformType.YESTERDAY);
                        break;
                    case LAST_7_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST7DAYS);
                        break;
                    case THIS_WEEK:
                        dateHelper.setType(DateHelper.TransformType.THISWEEK);
                        break;
                    case LAST_WEEK:
                        dateHelper.setType(DateHelper.TransformType.LASTWEEK);
                        break;
                    case LAST_30_DAYS:
                        dateHelper.setType(DateHelper.TransformType.LAST30DAYS);
                        break;
                    case THIS_MONTH:
                        dateHelper.setType(DateHelper.TransformType.THISMONTH);
                        break;
                    case LAST_MONTH:
                        dateHelper.setType(DateHelper.TransformType.LASTMONTH);
                        break;
                    case THIS_YEAR:
                        dateHelper.setType(DateHelper.TransformType.THISYEAR);
                        break;
                    case LAST_YEAR:
                        dateHelper.setType(DateHelper.TransformType.LASTYEAR);
                        break;
                }

                if (newValue != TimeFrame.CUSTOM) {
                    timeFrame = newValue;
                    start = dateHelper.getStartDate();
                    end = dateHelper.getEndDate();

                    updateList();
                }
            }
        });

        return box;
    }

    @Override
    public String getClassName() {
        return "Notes Plugin";
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public StringProperty nameProperty() {
        return null;
    }

    @Override
    public String getUUID() {
        return null;
    }

    @Override
    public void setUUID(String id) {

    }

    @Override
    public String getToolTip() {
        return I18n.getInstance().getString("plugin.notes.tooltip");
    }

    @Override
    public StringProperty uuidProperty() {
        return null;
    }

    @Override
    public Node getMenu() {
        return null;
    }

    @Override
    public boolean supportsRequest(int cmdType) {
        return false;
    }

    @Override
    public Node getToolbar() {
        return toolBar;
    }

    @Override
    public void updateToolbar() {

    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void setDataSource(JEVisDataSource ds) {

    }

    @Override
    public void handleRequest(int cmdType) {

    }

    @Override
    public Node getContentNode() {
        return borderPane;
    }


    private List<NotesRow> getNotesRow(JEVisAttribute notesAttribute) throws JEVisException, IOException {
        List<NotesRow> list = new ArrayList<>();
        if (notesAttribute.hasSample()) {
            for (JEVisSample jeVisSample : notesAttribute.getSamples(start, end)) {
                String tags = "";
                String user = "";
                try {
                    tags = notesAttribute.getObject().getAttribute("Tag").getSamples(jeVisSample.getTimestamp(), jeVisSample.getTimestamp()).get(0).getValueAsString();
                    //System.out.println("Tags: " + tags);
                    //System.out.println("Tags.list: " + NoteTag.parseTags(tags).size());
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
                try {
                    user = notesAttribute.getObject().getAttribute("User").getSamples(jeVisSample.getTimestamp(), jeVisSample.getTimestamp()).get(0).getValueAsString();
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }

                NotesRow alarmRow = new NotesRow(jeVisSample.getTimestamp(), jeVisSample.getValueAsString(), notesAttribute.getObject(), tags, user);

                list.add(alarmRow);
            }
        }
        return list;
    }

    private synchronized boolean allJobsDone(List<Future<?>> futures) {
        boolean allDone = true;
        Iterator<Future<?>> itr = futures.iterator();
        while (itr.hasNext()) {
            if (!itr.next().isDone()) {
                allDone = false;
            }
        }

        return allDone;
    }

    private List<JEVisObject> getAllNoteObjects() {
        List<JEVisObject> list = new ArrayList<>();
        JEVisClass dataNotesClass = null;
        try {
            dataNotesClass = ds.getJEVisClass(NOTES_CLASS);
            list.addAll(ds.getObjects(dataNotesClass, true));
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Region getIcon() {
        return JEConfig.getSVGImage(Icon.NOTE, Plugin.IconSize, Plugin.IconSize, Icon.CSS_PLUGIN);
    }

    @Override
    public void fireCloseEvent() {

    }

    @Override
    public void setHasFocus() {


        this.timeFrameComboBox.getSelectionModel().select(TimeFrame.LAST_30_DAYS);
        Platform.runLater(() -> autoFitTable(tableView));
    }

    @Override
    public void lostFocus() {

    }

    @Override
    public void openObject(Object object) {

    }

    @Override
    public int getPrefTapPos() {
        return 5;
    }


}
