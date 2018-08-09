package org.jevis.application.dialog;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;

import java.util.Map;

public class NoteDialog extends Dialog<ButtonType> {
    private Map<String, String> noteMap;
    private SaveResourceBundle rb = new SaveResourceBundle("jeapplication", AppLocale.getInstance().getLocale());
    private ObservableList<rowNote> observableList = FXCollections.observableArrayList();
    private TableView tv;

    public NoteDialog(Map<String, String> map) {
        this.noteMap = map;

        init();
    }

    private void init() {
        HBox hbox = new HBox();

        this.setTitle(rb.getString("graph.dialog.note"));


        for (Map.Entry<String, String> entry : noteMap.entrySet()) {
            rowNote rn = new rowNote(entry.getKey(), entry.getValue());
            observableList.add(rn);
        }

        TableColumn<rowNote, String> columnName = new TableColumn(rb.getString("graph.dialog.column.name"));
        columnName.setCellValueFactory(param -> {
            return new SimpleStringProperty(param.getValue().getName());
        });

        TableColumn<rowNote, String> columnNote = new TableColumn(rb.getString("graph.dialog.column.note"));
        columnNote.setCellValueFactory(param -> {
            return new SimpleStringProperty(param.getValue().getNote());
        });

        tv = new TableView(observableList);
        tv.getColumns().addAll(columnName, columnNote);

        hbox.getChildren().add(tv);
        HBox.setHgrow(tv, Priority.ALWAYS);

        final ButtonType ok = new ButtonType(rb.getString("graph.dialog.ok"), ButtonBar.ButtonData.OK_DONE);
        final ButtonType cancel = new ButtonType(rb.getString("graph.dialog.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        this.getDialogPane().getButtonTypes().addAll(ok, cancel);
        this.getDialogPane().setContent(hbox);

        this.setResizable(true);

        tv.getScene().getWindow().sizeToScene();
    }

    class rowNote {
        private SimpleStringProperty name;
        private SimpleStringProperty note;

        public rowNote(String name, String note) {
            this.name = new SimpleStringProperty(name);
            this.note = new SimpleStringProperty(note);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public SimpleStringProperty nameProperty() {
            return name;
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
    }
}
