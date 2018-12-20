package org.jevis.jeconfig.dialog;

/**
 * @author Gerrit Schutz <gerrit.schutz@envidatec.com>
 */

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.util.Callback;
import org.jevis.api.JEVisException;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.RowNote;
import org.jevis.jeconfig.application.jevistree.plugin.ChartPlugin;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Map;

public class NoteDialog extends Dialog<ButtonType> {
    private final Image imgExpand = new Image(ChartPlugin.class.getResourceAsStream("/icons/" + "if_ExpandMore.png"));
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

        TableColumn<RowNote, String> columnName = new TableColumn(I18n.getInstance().getString("graph.dialog.column.name"));
        columnName.setSortable(false);
        columnName.setCellValueFactory(param -> {
            String name = param.getValue().getName();
            return new ReadOnlyObjectWrapper<>(name);
        });


        TableColumn<RowNote, String> columnTimeStamp = new TableColumn(I18n.getInstance().getString("graph.dialog.column.timestamp"));
        columnTimeStamp.setSortable(false);
        columnTimeStamp.setCellValueFactory(param -> {
            DateTime timestamp = null;
            try {
                timestamp = param.getValue().getSample().getTimestamp();
            } catch (JEVisException e) {
            }
            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            String s = timestamp.toString(dtf);
            return new ReadOnlyObjectWrapper<>(s);
        });

        TableColumn<RowNote, String> columnNote = new TableColumn(I18n.getInstance().getString("graph.dialog.column.note"));
        columnNote.setCellValueFactory(param -> {
            String note = param.getValue().getNote();
            return new ReadOnlyObjectWrapper<>(note);
        });

        TableColumn<RowNote, String> columnUserNote = new TableColumn(I18n.getInstance().getString("graph.dialog.column.usernote"));
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

                TableCell<RowNote, String> stringTableCell = new TableCell<RowNote, String>() {

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
                return stringTableCell;
            }
        });

        TableView tv = new TableView(observableList);
        tv.getColumns().addAll(columnName, columnTimeStamp, columnNote, columnUserNote);

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
