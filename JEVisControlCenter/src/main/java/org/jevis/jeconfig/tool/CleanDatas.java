package org.jevis.jeconfig.tool;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisObject;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;

public class CleanDatas {

    private static final Logger logger = LogManager.getLogger(CleanDatas.class);


    public static void createTask(JEVisTree tree) {
        tree.getSelectionModel().getSelectedItems().forEach(o -> {

            JEVisTreeItem jeVisTreeItem = (JEVisTreeItem) o;

            try {
                JEVisObject obj = jeVisTreeItem.getValue().getJEVisObject();

                if (obj.getJEVisClassName().equals("Clean Data")) {
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

    }

    public static void recalculate(JEVisObject cleanObject) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {

                    updateMessage("Recalculate " + cleanObject.getParents().get(0).getName());
                    CommonMethods.processCleanData(cleanObject);

                    this.succeeded();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.failed();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask("Clean Data", task, JEConfig.getImage("calc.png"), true);

    }
}
