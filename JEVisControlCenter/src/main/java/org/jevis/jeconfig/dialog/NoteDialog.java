package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.apache.commons.validator.routines.DoubleValidator;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.AlphanumComparator;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPluginTree;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public class NoteDialog extends Dialog<ButtonType> {
    private final Image imgExpand = new Image(ChartPluginTree.class.getResourceAsStream("/icons/" + "if_ExpandMore.png"));
    private Map<String, RowNote> noteMap;
    private ObservableList<RowNote> observableList = FXCollections.observableArrayList();

    public NoteDialog(Map<String, RowNote> map) {
        this.noteMap = map;
        init();
    }

    private void init() {
        HBox hbox = new HBox();

        this.setTitle(I18n.getInstance().getString("graph.dialog.note"));


        for (Map.Entry<String, RowNote> entry : noteMap.entrySet()) {

            observableList.add(entry.getValue());
        }

        AlphanumComparator ac = new AlphanumComparator();
        observableList.sort((o1, o2) -> ac.compare(o1.getName(), o2.getName()));

        TableColumn<RowNote, String> columnName = new TableColumn<>(I18n.getInstance().getString("graph.dialog.column.name"));
        columnName.setSortable(false);
        columnName.setCellValueFactory(param -> {
            String name = param.getValue().getName();
            return new ReadOnlyObjectWrapper<>(name);
        });


        TableColumn<RowNote, String> columnTimeStamp = new TableColumn<>(I18n.getInstance().getString("graph.dialog.column.timestamp"));
        columnTimeStamp.setSortable(false);
        columnTimeStamp.setCellValueFactory(param -> {
            String s = "";
            try {
                DateTime timestamp = param.getValue().getSample().getTimestamp();
                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                s = timestamp.toString(dtf);
            } catch (Exception e) {
            }

            return new ReadOnlyObjectWrapper<>(s);
        });

        TableColumn<RowNote, String> columnNote = new TableColumn<>(I18n.getInstance().getString("graph.dialog.column.note"));
        columnNote.setCellValueFactory(param -> {
            String note = param.getValue().getNote();
            return new ReadOnlyObjectWrapper<>(note);
        });

        TableColumn<RowNote, String> columnUserNote = new TableColumn<>(I18n.getInstance().getString("graph.dialog.column.usernote"));
        columnUserNote.setSortable(false);
        columnUserNote.setEditable(true);
        columnUserNote.setMinWidth(310d);
        columnUserNote.setCellValueFactory(param -> {
            String userNote = param.getValue().getUserNote();
            return new ReadOnlyObjectWrapper<>(userNote);
        });


        columnUserNote.setCellFactory(new Callback<TableColumn<RowNote, String>, TableCell<RowNote, String>>() {
            @Override
            public TableCell<RowNote, String> call(TableColumn<RowNote, String> param) {

                return new TableCell<RowNote, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RowNote rowNote = (RowNote) getTableRow().getItem();
                            TextArea textArea = new TextArea(rowNote.getUserNote());

                            Button expand = new Button(null);
                            expand.setBackground(new Background(new BackgroundImage(
                                    imgExpand,
                                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                                    new BackgroundSize(expand.getWidth(), expand.getHeight(),
                                            true, true, true, false))));

                            expand.setOnAction(event -> {
                                Platform.runLater(() -> {
                                    if (textArea.getPrefRowCount() == 20) {
                                        textArea.setPrefRowCount(1);
                                        textArea.setWrapText(false);
                                    } else {
                                        textArea.setPrefRowCount(20);
                                        textArea.setWrapText(true);
                                    }

                                });

                            });

                            textArea.setWrapText(false);
                            textArea.setPrefRowCount(1);
                            textArea.autosize();

                            textArea.textProperty().addListener((observable, oldValue, newValue) -> {

                                rowNote.setUserNote(newValue);
                                rowNote.setChanged(true);

                            });

                            HBox box = new HBox(5, textArea, expand);

                            setGraphic(box);
                        }
                    }
                };
            }
        });

        TableColumn<RowNote, String> columnUserData = new TableColumn<>(I18n.getInstance().getString("graph.dialog.column.uservalue"));
        columnUserData.setSortable(false);
        columnUserData.setEditable(true);
        columnUserData.setMinWidth(310d);
        columnUserData.setCellValueFactory(param -> {
            String userValue = param.getValue().getUserValue();
            return new ReadOnlyObjectWrapper<>(userValue);
        });


        columnUserData.setCellFactory(new Callback<TableColumn<RowNote, String>, TableCell<RowNote, String>>() {
            @Override
            public TableCell<RowNote, String> call(TableColumn<RowNote, String> param) {

                return new TableCell<RowNote, String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            RowNote rowNote = (RowNote) getTableRow().getItem();

                            TextField textField = new TextField();
                            if (!rowNote.getUserValue().equals("")) {
                                double userValue = Double.parseDouble(rowNote.getUserValue()) * rowNote.getScaleFactor();
                                textField.setText(String.valueOf(userValue));
                            }

                            textField.setAlignment(Pos.CENTER);

                            textField.textProperty().addListener((observable, oldValue, newValue) -> {
                                if (!newValue.equals(oldValue)) {
                                    if (!newValue.equals("")) {
                                        DoubleValidator validator = DoubleValidator.getInstance();
                                        try {
                                            double parsedValue = validator.validate(newValue, I18n.getInstance().getLocale());
                                            rowNote.setChanged(true);
                                            rowNote.setUserValue(String.valueOf(parsedValue * (1 / rowNote.getScaleFactor())));
                                        } catch (Exception e) {
                                            textField.setText(oldValue);
                                        }
                                    } else {
                                        rowNote.setChanged(true);
                                        rowNote.setUserValue("");
                                    }
                                }
                            });
                            try {
                                if (rowNote.getDataObject().getDataSource().getCurrentUser().canWrite(rowNote.getDataObject().getID())) {
                                    textField.setDisable(false);
                                } else {
                                    textField.setDisable(true);
                                }
                            } catch (Exception ex) {
                                textField.setDisable(true);
                            }

                            HBox hBox = new HBox(textField);
                            hBox.setSpacing(4);
                            if (!UnitManager.getInstance().format(rowNote.getUserValueUnit()).equals("")) {
                                Label unitLabel = new Label();
                                unitLabel.setAlignment(Pos.CENTER);
                                unitLabel.setText(UnitManager.getInstance().format(rowNote.getUserValueUnit()));
                                hBox.getChildren().add(unitLabel);
                            }

                            hBox.setAlignment(Pos.CENTER);
                            setGraphic(new VBox(hBox));
                        }
                    }
                };
            }
        });

        TableView<RowNote> tv = new TableView<>();
        tv.getColumns().setAll(columnName, columnTimeStamp, columnNote, columnUserNote, columnUserData);
        tv.setItems(observableList);

        hbox.getChildren().add(tv);
        HBox.setHgrow(tv, Priority.ALWAYS);

        final ButtonType ok = new ButtonType(I18n.getInstance().getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(I18n.getInstance().getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(ok, cancel);
        this.getDialogPane().setContent(hbox);

        this.setResizable(true);
        this.initOwner(JEConfig.getStage());

        this.getDialogPane().setPrefWidth(1220);
    }

    public Map<String, RowNote> getNoteMap() {
        return noteMap;
    }
}
