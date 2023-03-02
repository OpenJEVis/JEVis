package org.jevis.jeconfig.plugin.action.data;

import com.google.gson.Gson;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.plugin.action.ActionPlugin;
import org.jevis.jeconfig.tool.gson.GsonBuilder;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class ActionPlanData {

    protected static final Logger logger = LogManager.getLogger(ActionPlanData.class);

    public static String STATUS_DONE = I18n.getInstance().getString("plugin.action.status.done");
    private JEVisObject object;
    private ObservableList<String> statusTags;
    private ObservableList<String> mediumTags;
    private ObservableList<String> fieldsTags;
    private ObservableList<String> significantEnergyUseTags;
    private ObservableList<JEVisObject> enpis;
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty nrPrefix = new SimpleStringProperty("");
    private String initNrPrefix = "";
    private ObservableList<ActionData> actions = FXCollections.observableArrayList();
    private AtomicInteger biggestActionNr = new AtomicInteger(0);
    private String initCustomStatus = "";
    private String initCustomFields = "";
    private String initCustomMedium = "";
    private String initCustomSEU = "";
    private String ATTRIBUTE_CSTATUS = "Custom Status";
    private String ATTRIBUTE_CFIELD = "Custom Fields";
    private String ATTRIBUTE_CMEDIUM = "Custom Medium";
    private String ATTRIBUTE_SEU = "Custom SEU";
    private String ATTRIBUTE_EnPI = "EnPI";
    private String ATTRIBUTE_NrPrefix = "Nr Prefix";


    private AtomicBoolean actionsLoaded = new AtomicBoolean(false);

    public ActionPlanData() {
    }

    public ActionPlanData(JEVisObject obj) {
        System.out.println("New ActionPlan from Object: " + obj);
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

        if (!statusTags.contains(STATUS_DONE)) {
            statusTags.add(STATUS_DONE);
        }

        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_NrPrefix);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                nrPrefix.set(sample.getValueAsString());
                initNrPrefix = sample.getValueAsString();
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

        significantEnergyUseTags = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_SEU);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomSEU = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    significantEnergyUseTags.add(s);
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

        actions.addAll(FXCollections.observableArrayList());
        actions.addListener(new ListChangeListener<ActionData>() {
            @Override
            public void onChanged(Change<? extends ActionData> c) {
                while (c.next()) {
                    Optional<ActionData> maxNr = actions.stream().max((o1, o2) -> Integer.compare(o1.nrProperty().get(), o2.nrProperty().get()));
                    //System.out.println("New Action Nr Max: " + maxNr.get().nrProperty().get());
                    biggestActionNr.set(maxNr.get().nrProperty().get());
                }
            }
        });

        enpis = FXCollections.observableArrayList();

        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_EnPI);
            TargetHelper targetHelper = new TargetHelper(attribute.getDataSource(), attribute);

            enpis.setAll(targetHelper.getObject());

        } catch (Exception e) {
            logger.error(e);
        }

        if (!enpis.contains(FreeObject.getInstance())) {
            enpis.add(FreeObject.getInstance());
        }

        loadActionList();

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

    public ObservableList<JEVisObject> getEnpis() {
        return enpis;
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
                                System.out.println("new Action from JEVis: " + actionObj);
                                try {
                                    ActionData action = loadAction(actionObj);
                                    actions.add(action);
                                    // if (!action.isDeletedProperty().get()) actions.add(action);

                                } catch (Exception e) {
                                    logger.error("Could not load Action: {},{},{}", actionObj, e, e);
                                }
                            });
                        }
                        actions.sort(Comparator.comparingInt(value -> value.nrProperty().get()));

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

    public ActionData loadAction(JEVisObject actionObj) throws JEVisException, NullPointerException {
        JEVisAttribute att = actionObj.getAttribute("Data");
        JEVisSample sample = att.getLatestSample();
        JEVisFile file = sample.getValueAsFile();
        String s = new String(file.getBytes(), StandardCharsets.UTF_8);
        //System.out.println("Parsed Json:\n" + s);
        Gson gson = GsonBuilder.createDefaultBuilder().create();
        ActionData actionData = gson.fromJson(s, ActionData.class);
        actionData.setObject(actionObj);
        actionData.setActionPlan(this);
        logger.debug("Load Action JSon: {}", gson.toJson(actionData));
        return actionData;
    }

    public ActionData reloadAction(ActionData actionObj) throws JEVisException {
        actions.remove(actionObj);
        ActionData data = loadAction(actionObj.getObject());
        actions.add(data);
        return data;


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

        if (!initNrPrefix.equals(nrPrefix.get())) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_NrPrefix);
                JEVisSample sample = attribute.buildSample(now, nrPrefix.get());
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }

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


        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_SEU);
            JEVisSample sample = attribute.buildSample(now, listToString(significantEnergyUseTags()));
            sample.commit();
        } catch (Exception e) {
            logger.error(e);
        }


        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_EnPI);
            //TargetHelper targetHelper = new TargetHelper(attribute.getDataSource(), enpis.sorted(), attribute);

            String targetStrg = "";
            boolean first = true;
            for (JEVisObject jeVisObject : enpis) {
                targetStrg += jeVisObject.getID() + ";";
            }
            JEVisSample sample = attribute.buildSample(now, targetStrg);
            sample.commit();
        } catch (Exception e) {
            logger.error(e);
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

    public ObservableList<String> significantEnergyUseTags() {
        return significantEnergyUseTags;
    }

    public String getNrPrefix() {
        return nrPrefix.get();
    }

    public StringProperty nrPrefixProperty() {
        return nrPrefix;
    }
}
