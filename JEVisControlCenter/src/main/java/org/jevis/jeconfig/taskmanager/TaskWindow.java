package org.jevis.jeconfig.taskmanager;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class TaskWindow {

    public TaskWindow(ObservableList<Task> tasks) {

        TaskPane taskPane = new TaskPane(tasks);
        Notifications notification = Notifications.create()
                .title("Tasks")
                //.text("Tasks....")
                .graphic(taskPane)
                .hideAfter(Duration.minutes(5));

        SimpleBooleanProperty isShowing = new SimpleBooleanProperty(false);
        isShowing.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                Platform.runLater(() -> {

                    /** TODO:check if allready shown**/
                    notification.show();
                });
            }
        });



        tasks.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {
                while (c.next()) {
                    System.out.println("Task List Changed");
                   if (c.wasPermutated()) {
                        for (int i = c.getFrom(); i < c.getTo(); ++i) {
                            //permutate
                        }
                    } else if (c.wasUpdated()) {
                        //update item
                    } else {
                       if( c.wasRemoved() || c.wasAdded()){
                           if(!isShowing.get()){
                               isShowing.setValue(true);
                           }
                       }

                    }
                }
            }
        });


        //notification.showInformation();

    }
}
