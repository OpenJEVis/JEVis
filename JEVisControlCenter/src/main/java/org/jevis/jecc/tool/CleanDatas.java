package org.jevis.jecc.tool;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.jevistree.JEVisTree;
import org.jevis.jecc.application.jevistree.JEVisTreeItem;
import org.jevis.jecc.application.jevistree.methods.CommonMethods;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

public class CleanDatas {

    private static final Logger logger = LogManager.getLogger(CleanDatas.class);


    public static void createTask(JEVisTree tree) {
        logger.debug("Setting default timezone to UTC");
        TimeZone defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateTimeZone defaultDateTimeZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);

        tree.getSelectionModel().getSelectedItems().forEach(o -> {

            JEVisTreeItem jeVisTreeItem = (JEVisTreeItem) o;

            try {
                JEVisObject obj = jeVisTreeItem.getValue().getJEVisObject();

                if (obj.getJEVisClassName().equals("Clean Data") || obj.getJEVisClassName().equals("Math Data")) {
                    recalculate(obj);
                } else if (obj.getJEVisClassName().equals("Data")) {
                    obj.getChildren().forEach(object -> {
                        try {
                            if (object.getJEVisClassName().equals("Clean Data")) {
                                recalculate(object);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        Task<Void> waitTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    CommonMethods.checkForActiveRecalculation(defaultTimeZone, defaultDateTimeZone);
                } catch (Exception e) {
                    failed();
                } finally {
                    succeeded();
                }

                return null;
            }
        };

        if (!ControlCenter.getStatusBar().getTaskList().containsValue(CommonMethods.WAIT_FOR_TIMEZONE)) {
            ControlCenter.getStatusBar().addTask(CommonMethods.WAIT_FOR_TIMEZONE, waitTask, ControlCenter.getImage("1476369770_Sync.png"), true);
        }
    }

    public static void recalculate(JEVisObject cleanObject) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {

                    updateMessage("Recalculate " + cleanObject.getParents().get(0).getName());
                    org.jevis.commons.utils.CommonMethods.processCleanData(cleanObject);

                    this.succeeded();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.failed();
                }
                return null;
            }
        };

        ControlCenter.getStatusBar().addTask(CommonMethods.RECALCULATION, task, ControlCenter.getImage("calc.png"), true);

    }
}
