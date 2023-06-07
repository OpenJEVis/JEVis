package org.jevis.jecc.plugin.dashboard.common;

import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.stage.Popup;
import org.controlsfx.control.TaskProgressView;
import org.jevis.jecc.ControlCenter;

public class ProcessMonitor {

    final Popup popup = new Popup();
    TaskProgressView view = new TaskProgressView<>();

    public ProcessMonitor() {
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.getContent().add(view);


        view.getTasks().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                if (c.next()) {
                    if (c.wasAdded() && !popup.isShowing()) {
                        popup.show(ControlCenter.getStage());
                    }
                }
            }
        });
    }

    public void addTask(Task task) {
        view.getTasks().add(task);

        task.setOnSucceeded(event -> {
//            boolean someArerunning = false;
//            for (Task otherTask : (List<Task>) view.getTasks()) {
//                if (((Task) otherTask).isDone()) {
//                    someArerunning = true;
//                }
//            }
//            if (someArerunning = true) {
//                Platform.runLater(() -> {
//                    popup.hide();
//                });
//            }

        });


//        if (task.ge) {
//
//        }
//        Platform.runLater(() -> {
//            popup.hide();
//        });
//        task.setOnSucceeded(Platform.runLater(() -> {
//        }));
    }

    public void showAndWait() {


    }
}
