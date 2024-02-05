package org.jevis.jecc.tool;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.ClassHelper;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.jecc.ControlCenter;
import org.jevis.jecc.application.jevistree.JEVisTree;
import org.jevis.jecc.application.jevistree.JEVisTreeItem;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class CreateAlarms {

    private static final Logger logger = LogManager.getLogger(CreateAlarms.class);


    public static void createTask(JEVisTree tree) {

        tree.getSelectionModel().getSelectedItems().forEach(o -> {

            JEVisTreeItem jeVisTreeItem = (JEVisTreeItem) o;

            try {
                JEVisObject obj = jeVisTreeItem.getValue().getJEVisObject();

                if (obj.getJEVisClassName().equals("Data Directory")) {
                    createAlarms(obj);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }

    public static void createAlarms(JEVisObject dataDirectory) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {

                    updateMessage("Creating alarms... ");
                    JEVisDataSource ds = dataDirectory.getDataSource();

                    JEVisClass buildingClass = ds.getJEVisClass("Building");
                    JEVisClass alarmDirectoryClass = ds.getJEVisClass("Alarm Directory");
                    JEVisObject parent = dataDirectory.getParents().get(0);

                    if (parent.getJEVisClass().equals(buildingClass)) {
                        List<JEVisObject> children = parent.getChildren(alarmDirectoryClass, true);
                        if (!children.isEmpty()) {
                            AlarmReference alarmReference = new AlarmReference(dataDirectory, children.get(0), null);
                        }

                        this.succeeded();
                    } else {
                        this.failed();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.failed();
                }
                return null;
            }
        };

        ControlCenter.getStatusBar().addTask(CreateAlarms.class.toString(), task, ControlCenter.getImage("alarm_icon.png"), true);

    }

    static class AlarmReference {
        private final JEVisObject dataReference;
        private final JEVisObject alarmReference;
        private final List<AlarmReference> children = new ArrayList<>();

        public AlarmReference(JEVisObject dataReference, JEVisObject alarmReference, JEVisObject lastPossibleParent) {
            this.dataReference = dataReference;
            this.alarmReference = alarmReference;

            try {
                if (alarmReference != null) {
                    boolean isDataReferenceDirectory = ClassHelper.isDirectory(alarmReference.getJEVisClass());
                    if (isDataReferenceDirectory) {
                        lastPossibleParent = alarmReference;
                    }

                    for (JEVisObject dataChild : dataReference.getChildren()) {
                        try {
                            JEVisObject foundCorrespondingAlarmObject = null;
                            for (JEVisObject alarmChild : alarmReference.getChildren()) {
                                if (alarmChild.getName().equals(dataChild.getName())) {
                                    foundCorrespondingAlarmObject = alarmChild;
                                    break;
                                }
                            }

                            JEVisClass dataChildClass = dataChild.getJEVisClass();
                            boolean isDirectory = ClassHelper.isDirectory(dataChildClass);

                            if (foundCorrespondingAlarmObject == null) {
                                try {

                                    JEVisClass dataClass = dataChild.getDataSource().getJEVisClass("Data");
                                    JEVisClass cleanDataClass = dataChild.getDataSource().getJEVisClass("Clean Data");

                                    if (isDirectory || dataChildClass.equals(dataClass)) {
                                        JEVisClass alarmDirectoryClass = null;
                                        try {
                                            alarmDirectoryClass = dataChild.getDataSource().getJEVisClass("Alarm Directory");
                                            foundCorrespondingAlarmObject = lastPossibleParent.buildObject(dataChild.getName(), alarmDirectoryClass);
                                            foundCorrespondingAlarmObject.commit();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else if (dataChild.getJEVisClass().equals(cleanDataClass)) {
                                        try {
                                            JEVisClass alarmConfigurationClass = dataChild.getDataSource().getJEVisClass("Alarm Configuration");

                                            foundCorrespondingAlarmObject = lastPossibleParent.buildObject(lastPossibleParent.getName() + " - " + dataChild.getName(), alarmConfigurationClass);
                                            foundCorrespondingAlarmObject.commit();

                                            JEVisAttribute enabledAttribute = foundCorrespondingAlarmObject.getAttribute("Enabled");
                                            JEVisSample enabledSample = enabledAttribute.buildSample(new DateTime(), true);
                                            enabledSample.commit();

                                            JEVisAttribute alarmScopeAttribute = foundCorrespondingAlarmObject.getAttribute("Alarm Scope");
                                            JEVisSample alarmScopeSample = alarmScopeAttribute.buildSample(new DateTime(), "SELECTED");
                                            alarmScopeSample.commit();

                                            JEVisAttribute alarmObjectsAttribute = foundCorrespondingAlarmObject.getAttribute("Alarm Objects");
                                            JEVisSample alarmObjectsSample = alarmObjectsAttribute.buildSample(new DateTime(), dataChild.getID() + ":Value");
                                            alarmObjectsSample.commit();

                                            JEVisAttribute timeStampAttribute = foundCorrespondingAlarmObject.getAttribute("Time Stamp");
                                            DateTime startOfData = dataChild.getAttribute("Value").getTimestampOfFirstSample();
                                            if (startOfData == null) startOfData = new DateTime();
                                            JEVisSample timeStampSample = timeStampAttribute.buildSample(new DateTime(), startOfData);
                                            timeStampSample.commit();

                                            JEVisAttribute alarmPeriodAttribute = foundCorrespondingAlarmObject.getAttribute("Alarm Period");
                                            JEVisSample alarmPeriodSample = null;
                                            Period period = CleanDataObject.getPeriodForDate(dataChild, startOfData);

                                            if (period.equals(Period.minutes(1))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "MINUTELY");
                                            } else if (period.equals(Period.hours(1))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "HOURLY");
                                            } else if (period.equals(Period.days(1))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "DAILY");
                                            } else if (period.equals(Period.days(7))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "WEEKLY");
                                            } else if (period.equals(Period.months(1))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "MONTHLY");
                                            } else if (period.equals(Period.months(3))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "QUARTERLY");
                                            } else if (period.equals(Period.years(1))) {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "YEARLY");
                                            } else {
                                                alarmPeriodSample = alarmPeriodAttribute.buildSample(new DateTime(), "QUARTER_HOURLY");
                                            }

                                            alarmPeriodSample.commit();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            AlarmReference childAlarmReference = new AlarmReference(dataChild, foundCorrespondingAlarmObject, lastPossibleParent);
                            children.add(childAlarmReference);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public JEVisObject getDataReference() {
            return dataReference;
        }

        public JEVisObject getAlarmReference() {
            return alarmReference;
        }

        public List<AlarmReference> getChildren() {
            return children;
        }
    }
}
