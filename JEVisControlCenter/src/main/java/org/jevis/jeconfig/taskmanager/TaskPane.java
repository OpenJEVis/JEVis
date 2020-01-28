package org.jevis.jeconfig.taskmanager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.jevis.jeconfig.tool.Layouts;

public class TaskPane extends AnchorPane {

    TableView<Task> taskTableView = new TableView<>();
    final ObservableList<Task> tasksList;

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


        TableColumn<Task, ProgressBar> taskProgressColumn = new TableColumn("Progress");
        taskProgressColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Task, ProgressBar>, ObservableValue<ProgressBar>>() {
            @Override
            public ObservableValue<ProgressBar> call(TableColumn.CellDataFeatures<Task, ProgressBar> param) {
                ProgressBar progressBar = new ProgressBar();
                progressBar.progressProperty().bind(param.getValue().progressProperty());
                return new SimpleObjectProperty<>(progressBar);
            }
        });
        taskProgressColumn.setCellFactory(new Callback<TableColumn<Task, ProgressBar>, TableCell<Task, ProgressBar>>() {
            @Override
            public TableCell<Task, ProgressBar> call(TableColumn<Task, ProgressBar> param) {
                return new TableCell<Task, ProgressBar>(){
                    @Override
                    protected void updateItem(ProgressBar item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        if(item!=null){
                            setGraphic(item);
                        }else{
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        taskTableView.getColumns().addAll(taskNameColumn,taskProgressColumn);
        this.getChildren().add(taskTableView);
        Layouts.setAnchor(taskTableView,0);

    }

    public void addTask(Task task){
        tasksList.add(task);
    }

}
