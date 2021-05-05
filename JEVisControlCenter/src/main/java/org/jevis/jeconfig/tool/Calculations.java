package org.jevis.jeconfig.tool;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.jevistree.JEVisTree;
import org.jevis.jeconfig.application.jevistree.JEVisTreeItem;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Calculations {

    private static final Logger logger = LogManager.getLogger(Calculations.class);


    public static void createCalcJobs(JEVisTree tree) {
        tree.getSelectionModel().getSelectedItems().forEach(o -> {
            JEVisTreeItem jeVisTreeItem = (JEVisTreeItem) o;
            try {
                JEVisObject obj = jeVisTreeItem.getValue().getJEVisObject();

                if (obj.getJEVisClassName().equals("Calculation")) {
                    recalculate(obj);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

    }

    public static void recalculate(JEVisObject calcObject) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    updateMessage("Recalculate " + calcObject.getName());
                    boolean wasEnabled = false;
                    if (calcObject.getAttribute("Enabled").hasSample()) {
                        wasEnabled = calcObject.getAttribute("Enabled").getLatestSample().getValueAsBoolean();
                    }
                    calcObject.getAttribute("Enabled").buildSample(new DateTime(), false).commit();

                    JEVisClass output = calcObject.getDataSource().getJEVisClass("Output");
                    List<JEVisObject> outputs = calcObject.getChildren(output, true);
                    List<JEVisObject> targets = new ArrayList<>();
                    for (JEVisObject jeVisObject : outputs) {
                        try {
                            JEVisAttribute attribute = jeVisObject.getAttribute("Output");
                            if (attribute != null && attribute.hasSample()) {
                                TargetHelper th = new TargetHelper(calcObject.getDataSource(), attribute);
                                targets.addAll(th.getObject());
                                for (JEVisObject dataObject : th.getObject()) {
                                    CommonMethods.deleteAllSamples(dataObject, true, true);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error with output {}:{}", jeVisObject, e);
                        }
                    }

                    CalcJob calcJob;
                    CalcJobFactory calcJobCreator = new CalcJobFactory();
                    do {
                        calcJob = calcJobCreator.getCurrentCalcJob(new SampleHandler(), calcObject.getDataSource(), calcObject);
                        calcJob.execute();
                    } while (!calcJob.hasProcessedAllInputSamples());

                    if (wasEnabled) {
                        calcObject.getAttribute("Enabled").buildSample(new DateTime(), true).commit();
                    }

                    for (JEVisObject jeVisObject : targets) {
                        for (JEVisObject jeVisObject1 : jeVisObject.getChildren()) {
                            CommonMethods.processAllCleanData(jeVisObject1);
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.failed();
                }
                return null;
            }
        };

        JEConfig.getStatusBar().addTask("Calculations", task, JEConfig.getImage("calc.png"), true);

    }
}
