package org.jevis.jeconfig.plugin.legal.ui;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.legal.data.FileData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Optional;

public class FileTableView extends TableView<FileData> {

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    public FileTableView(ObservableList<FileData> data) {
        setItems(data);

        TableColumn<FileData, String> filenameCol = new TableColumn(I18n.getInstance().getString("plugin.action.attachment.filename"));
        filenameCol.setCellValueFactory(param -> param.getValue().nameProperty);
        filenameCol.setPrefWidth(600);
        //filenameCol.setCellFactory(buildShotTextFactory());

        TableColumn<FileData, DateTime> doneDatePropertyCol = new TableColumn(I18n.getInstance().getString("plugin.action.attachment.chagedate"));
        doneDatePropertyCol.setCellValueFactory(param -> param.getValue().changeDateProperty);
        doneDatePropertyCol.setCellFactory(buildDateTimeFactory());

        TableColumn<FileData, String> userCol = new TableColumn(I18n.getInstance().getString("plugin.action.attachment.user"));
        userCol.setCellValueFactory(param -> param.getValue().userProperty);

        this.getColumns().addAll(filenameCol, doneDatePropertyCol, userCol);

        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                    saveSelectedFile();//actionTable.getSelectionModel().getSelectedItem()
                }
            }
        });

    }

    public void saveSelectedFile() {
        if (getSelectionModel().getSelectedItem() != null) {
            try {
                getSelectionModel().getSelectedItem().saveFileDialog();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void deleteSelectedFile() {
        if (getSelectionModel().getSelectedItem() != null) {
            try {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(I18n.getInstance().getString("plugin.action.attachment.dialog.title"));
                //alert.setHeaderText("Look, a Confirmation Dialog");
                alert.setContentText(I18n.getInstance().getString("plugin.action.attachment.dialog.content"));

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    getSelectionModel().getSelectedItem().getFileObj().delete();
                    getItems().remove(getSelectionModel().getSelectedItem());
                } else {
                    // ... user chose CANCEL or closed the dialog
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private Callback<TableColumn<FileData, DateTime>, TableCell<FileData, DateTime>> buildDateTimeFactory() {
        return new Callback<TableColumn<FileData, DateTime>, TableCell<FileData, DateTime>>() {
            @Override
            public TableCell<FileData, DateTime> call(TableColumn<FileData, DateTime> param) {
                return new TableCell<FileData, DateTime>() {
                    @Override
                    protected void updateItem(DateTime item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(fmt.print(item));
                        }
                    }
                };
            }
        };

    }
}
