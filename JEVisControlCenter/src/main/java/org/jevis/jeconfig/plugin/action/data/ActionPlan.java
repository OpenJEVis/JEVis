package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class ActionPlan {

    protected static final Logger logger = LogManager.getLogger(ActionPlan.class);
    private JEVisObject object;
    private ObservableList<String> statusTags;
    private ObservableList<String> mediumTags;
    private ObservableList<String> fieldsTags;
    private StringProperty name = new SimpleStringProperty("");
    private ObservableList<ActionData> actions = FXCollections.observableArrayList();

    private AtomicInteger biggestActionNr = new AtomicInteger(0);

    private String initCustomStatus = "";
    private String initCustomFields = "";
    private String initCustomMedium = "";

    private String ATTRIBUTE_CSTATUS = "Custom Status";
    private String ATTRIBUTE_CFIELD = "Custom Fields";
    private String ATTRIBUTE_CMEDIUM = "Custom Medium";

    private AtomicBoolean actionsLoaded = new AtomicBoolean(false);

    public ActionPlan(JEVisObject obj) {
        this.object = obj;

        name.set(obj.getName());

        statusTags = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CSTATUS);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomStatus = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    statusTags.add(s);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }

        fieldsTags = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CFIELD);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomFields = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    fieldsTags.add(s);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }

        mediumTags = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CMEDIUM);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomMedium = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    mediumTags.add(s);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }

        actions.addAll(createTestData());
        actions.addListener(new ListChangeListener<ActionData>() {
            @Override
            public void onChanged(Change<? extends ActionData> c) {
                while (c.next()) {
                    Optional<ActionData> maxNr = actions.stream().max((o1, o2) -> Integer.compare(o1.actionNrProperty().get(), o2.actionNrProperty().get()));
                    System.out.println("New Action Nr Max: " + maxNr.get().actionNrProperty().get());
                    biggestActionNr.set(maxNr.get().actionNrProperty().get());
                }
            }
        });
        //Optional<TableData> maxNr = actions.stream().max((o1, o2) -> Integer.compare(o1.actionNrProperty().get(), o2.actionNrProperty().get()));
        /*
        actions.forEach(tableData -> {
            if (tableData.actionNrProperty().get() > actionNr.get()) {
                actionNr.set(tableData.actionNrProperty().get());
            }
        });

         */


    }

    public static String listToString(ObservableList<String> list) {
        boolean first = true;
        String string = "";
        for (String s : list) {
            if (first) {
                first = false;
                string += s;
            } else {
                string += ";" + s;
            }
        }
        return string;
    }

    public void reloadActionList() {
        actionsLoaded.set(false);
        loadActionList();
    }

    public void loadActionList() {
        if (!actionsLoaded.get()) {
            actionsLoaded.set(true);
            //System.out.println("loadIntoList for: " + name.get());
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {

                        JEVisClass actionDirClass = object.getDataSource().getJEVisClass("Action Plan Directory v2");
                        JEVisClass actionClass = object.getDataSource().getJEVisClass("Action");
                        for (JEVisObject dirObj : getObject().getChildren(actionDirClass, false)) {
                            //System.out.println("Action Dir: " + dirObj);
                            dirObj.getChildren(actionClass, false).forEach(actionObj -> {
                                //System.out.println("new Action from JEVis: " + actionObj);
                                ActionData actionData = new ActionData(actionObj);
                                actions.add(actionData);
                            });
                        }
                        actions.sort(Comparator.comparingInt(value -> value.actionNrProperty().get()));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    super.done();
                    return null;
                }
            };
            Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");
            JEConfig.getStatusBar().addTask(ActionPlugin.class.getName(), task, widgetTaskIcon, true);
        }


    }

    public void commit() {

        if (!getName().get().equals(object.getName())) {
            try {
                object.setName(getName().get());
                object.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        DateTime now = new DateTime();

        if (!initCustomStatus.equals(listToString(statusTags))) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CSTATUS);
                JEVisSample sample = attribute.buildSample(now, listToString(statusTags));
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (!initCustomStatus.equals(listToString(fieldsTags))) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CFIELD);
                JEVisSample sample = attribute.buildSample(now, listToString(fieldsTags));
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (!initCustomStatus.equals(listToString(mediumTags))) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_CMEDIUM);
                JEVisSample sample = attribute.buildSample(now, listToString(mediumTags));
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }


    }

    public void delete() throws Exception {
        object.delete();
    }

    public JEVisObject getObject() {
        return object;
    }

    public void removeAction(ActionData actionData) {
        actions.remove(actionData);
    }

    public void addAction(ActionData actionData) {
        actions.add(actionData);
    }

    public Integer getNextActionNr() {
        biggestActionNr.set(biggestActionNr.get() + 1);
        return biggestActionNr.get();
    }

    public StringProperty getName() {
        return name;
    }

    public ObservableList<String> getStatustags() {
        return statusTags;
    }

    public ObservableList<String> getMediumTags() {
        return mediumTags;
    }

    public ObservableList<String> getFieldsTags() {
        return fieldsTags;
    }

    public ObservableList<ActionData> getActionData() {
        return actions;
    }

    private ObservableList<ActionData> createTestData() {
        ObservableList<ActionData> data = FXCollections.observableArrayList();

        //(long toUserID, String fromUserID, String objectID, int actionNr, String desciption, String note, String status,
        // double investment, DateTime createDate) {
        //
        /*
        data.add(new TableData(new ActionData("Florian Simon", "Nils Heinrich", "9999", 1,
                "Erstellen einen Action Plugins", "PS: Aufstehen", "Offen;Prüfen", 1000000, new DateTime())));
        data.add(new TableData(new ActionData("Daniel Klincker", "Gerrit Schutz", "8888", 2,
                "Messstellenkonzept erstellen", "PS: Rechtschreibung!", "Geschlossen", 1000000, new DateTime())));


         */
        /*
        try {

            data.add(new ActionData(object.getChildren().get(0).getChildren().get(0)));
        } catch (Exception ex) {

        }
*/

        return data;
    }
}
