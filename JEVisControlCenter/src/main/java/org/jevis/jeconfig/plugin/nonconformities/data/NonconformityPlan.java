package org.jevis.jeconfig.plugin.nonconformities.data;

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
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesPlugin;
import org.jevis.jeconfig.tool.gson.GsonBuilder;
import org.joda.time.DateTime;
import sun.nio.cs.ext.IBM833;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class NonconformityPlan {

    protected static final Logger logger = LogManager.getLogger(NonconformityPlan.class);
    private JEVisObject object;
    private SimpleStringProperty prefix = new SimpleStringProperty();
    private ObservableList<String> mediumTags;
    private ObservableList<JEVisObject> enpis;
    private StringProperty name = new SimpleStringProperty("");
    private ObservableList<NonconformityData> nonconformityList = FXCollections.observableArrayList();

    private AtomicInteger biggestActionNr = new AtomicInteger(0);


    private String initCustomMedium = "";

    private String ATTRIBUTE_CMEDIUM = "Custom Medium";
    private String ATTRIBUTE_EnPI = "EnPI";

    private String ATTRIBUTE_PREFIX = "prefix";
    private String initNrPrefix = "";

    private ObservableList<String> fieldsTags;

    private ObservableList<String> stausTags = FXCollections.observableArrayList(OPEN,CLOSE);
    private String ATTRIBUTE_CFIELD = "Custom Fields";

    private String initCustomFields = "";

    private String ATTRIBUTE_SEU = "Custom SEU";

    private String initCustomSEU = "";

    public static String OPEN = I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.staus.open");
    public static String CLOSE = I18n.getInstance().getString("plugin.nonconformities.delete.nonconformity.staus.completed");

    private ObservableList<String> significantEnergyUseTags;



    private AtomicBoolean actionsLoaded = new AtomicBoolean(false);

    public NonconformityPlan(JEVisObject obj) {

        this.object = obj;

        name.set(obj.getName());

        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_PREFIX);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                setPrefix(sample.getValueAsString());

            }else{
                setPrefix("");
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

        nonconformityList.addAll(createTestData());
        nonconformityList.addListener(new ListChangeListener<NonconformityData>() {
            @Override
            public void onChanged(Change<? extends NonconformityData> c) {
                while (c.next()) {
                    Optional<NonconformityData> maxNr = nonconformityList.stream().max((o1, o2) -> Integer.compare(o1.nrProperty().get(), o2.nrProperty().get()));
                    biggestActionNr.set(maxNr.get().nrProperty().get());
                }
            }
        });

        enpis = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_EnPI);
            // JEVisSample sample = attribute.getLatestSample();
            TargetHelper targetHelper = new TargetHelper(attribute.getDataSource(), attribute);

            enpis.setAll(targetHelper.getObject());

        } catch (Exception e) {
            logger.error(e);
        }


    }

    public NonconformityPlan() {
    }


    public ObservableList<JEVisObject> getEnpis() {
        return enpis;
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
        loadNonconformityList();
    }

    public void loadNonconformityList() {
        if (!actionsLoaded.get()) {
            actionsLoaded.set(true);
            //System.out.println("loadIntoList for: " + name.get());
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    try {

                        JEVisClass actionDirClass = object.getDataSource().getJEVisClass("Nonconformities Directory");
                        JEVisClass actionClass = object.getDataSource().getJEVisClass("Nonconformity");
                        for (JEVisObject dirObj : getObject().getChildren(actionDirClass, false)) {
                            dirObj.getChildren(actionClass, false).forEach(actionObj -> {
                                System.out.println("new Action from JEVis: " + actionObj);
                                try {
                                    nonconformityList.add(loadNonconformties(actionObj));
                                } catch (Exception e) {
                                    logger.error("Could not load Action: {},{},{}", actionObj, e, e);
                                }
                            });
                        }
                        nonconformityList.sort(Comparator.comparingInt(value -> value.nrProperty().get()));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    super.done();
                    return null;
                }
            };
            Image widgetTaskIcon = JEConfig.getImage("if_dashboard_46791.png");
            JEConfig.getStatusBar().addTask(NonconformitiesPlugin.class.getName(), task, widgetTaskIcon, true);
        }


    }

    public NonconformityData loadNonconformties(JEVisObject actionObj) throws JEVisException, NullPointerException {
        JEVisAttribute att = actionObj.getAttribute("Data");;
        JEVisSample sample = att.getLatestSample();
        JEVisFile file = sample.getValueAsFile();
        String s = new String(file.getBytes(), StandardCharsets.UTF_8);

        logger.info("Json: {}",s);
        Gson gson = GsonBuilder.createDefaultBuilder().create();
        NonconformityData nonconformityData = gson.fromJson(s, NonconformityData.class);
        nonconformityData.setObject(actionObj);
        nonconformityData.setNonconformityPlan(this);
        return nonconformityData;
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
        if (!initNrPrefix.equals(getPrefix())) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(ATTRIBUTE_PREFIX);
                JEVisSample sample = attribute.buildSample(now, getPrefix());
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }

        if (!initCustomMedium.equals(listToString(mediumTags))) {
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

    public void removeNonconformity(NonconformityData nonconformityData) {
        this.nonconformityList.remove(nonconformityData);
    }

    public void addAction(NonconformityData nonconformityData) {
        this.nonconformityList.add(nonconformityData);
    }

    public Integer getNextNonconformityNr() {
        biggestActionNr.set(biggestActionNr.get() + 1);
        return biggestActionNr.get();
    }

    public StringProperty getName() {
        return name;
    }


    public ObservableList<String> getMediumTags() {
        return mediumTags;
    }


    public ObservableList<NonconformityData> getActionData() {
        return nonconformityList;
    }

    private ObservableList<NonconformityData> createTestData() {
        ObservableList<NonconformityData> data = FXCollections.observableArrayList();

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

    public void showNotification() {

    }

    public String getPrefix() {
        return prefix.get();
    }

    public SimpleStringProperty prefixProperty() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix.set(prefix);
    }

    public ObservableList<NonconformityData> getNonconformityList() {
        return nonconformityList;
    }

    public void setNonconformityList(ObservableList<NonconformityData> nonconformityList) {
        this.nonconformityList = nonconformityList;
    }

    public ObservableList<String> getFieldsTags() {
        return fieldsTags;
    }

    public void setFieldsTags(ObservableList<String> fieldsTags) {
        this.fieldsTags = fieldsTags;
    }

    public ObservableList<String> getSignificantEnergyUseTags() {
        return significantEnergyUseTags;
    }

    public void setSignificantEnergyUseTags(ObservableList<String> significantEnergyUseTags) {
        this.significantEnergyUseTags = significantEnergyUseTags;
    }

    public ObservableList<String> getStausTags() {
        return stausTags;
    }

    public void setStausTags(ObservableList<String> stausTags) {
        this.stausTags = stausTags;
    }
}
