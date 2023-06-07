package org.jevis.jecc.taskmanager;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.jevis.jecc.tool.Layouts;

public class TaskPane extends AnchorPane {

    final ObservableList<Task> tasksList;
    TableView<Task> taskTableView = new TableView<>();

    public TaskPane(ObservableList<Task> tasks) {
        this.tasksList = tasks;


        taskTableView.setEditable(false);

        TableColumn<Task, String> taskNameColumn = new TableColumn("Task");
        taskNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Task, String> param) {
                return new SimpleStringProperty(param.getValue().getTitle());
            }
        });
        taskNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());


        TableColumn<Task, MFXProgressBar> taskProgressColumn = new TableColumn("Progress");
        taskProgressColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, MFXProgressBar>, ObservableValue<MFXProgressBar>>() {
            @Override
            public ObservableValue<MFXProgressBar> call(TableColumn.CellDataFeatures<Task, MFXProgressBar> param) {
                MFXProgressBar progressBar = new MFXProgressBar();
                progressBar.progressProperty().bind(param.getValue().progressProperty());
                return new SimpleObjectProperty<>(progressBar);
            }
        });
        taskProgressColumn.setCellFactory(new Callback<TableColumn<Task, MFXProgressBar>, TableCell<Task, MFXProgressBar>>() {
            @Override
            public TableCell<Task, MFXProgressBar> call(TableColumn<Task, MFXProgressBar> param) {
                return new TableCell<Task, MFXProgressBar>() {
                    @Override
                    protected void updateItem(MFXProgressBar item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        setGraphic(item);
                    }
                };
            }
        });
        taskTableView.getColumns().addAll(taskNameColumn, taskProgressColumn);
        this.getChildren().add(taskTableView);
        Layouts.setAnchor(taskTableView, 0);

    }

    public void addTask(Task task) {
        tasksList.add(task);
    }

}
