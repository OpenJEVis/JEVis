package org.jevis.jeconfig.plugin.legal.data;

import com.google.gson.Gson;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.classes.JC;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.tool.gson.GsonBuilder;
import org.joda.time.DateTime;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class LegalCadastre {

    protected static final Logger logger = LogManager.getLogger(LegalCadastre.class);
    private JEVisObject object;
    protected SimpleStringProperty prefix = new SimpleStringProperty();
    protected StringProperty name = new SimpleStringProperty("name", "name", "");

    protected ObservableList<LegislationData> legislationDataList = FXCollections.observableArrayList();

    private ObservableList<String> scopes;
    private ObservableList<String> categories;
    protected ObservableList<String> relevanzTags = FXCollections.observableArrayList(I18n.getInstance().getString("plugin.Legalcadastre.legislation.relvant"), I18n.getInstance().getString("plugin.Legalcadastre.legislation.notrelvant"));


    private String initCustomCategory = "";
    private String initCustomValidity = "";


    private AtomicInteger biggestActionNr = new AtomicInteger(0);

    public void removeLegislation(LegislationData nonconformityData) {
        this.legislationDataList.remove(nonconformityData);
    }


    private AtomicBoolean actionsLoaded = new AtomicBoolean(false);

    public LegalCadastre(JEVisObject obj) {

        this.object = obj;

        name.set(obj.getName());


        legislationDataList.addAll(createTestData());
        legislationDataList.addListener(new ListChangeListener<LegislationData>() {
            @Override
            public void onChanged(Change<? extends LegislationData> c) {
                while (c.next()) {
                    Optional<LegislationData> maxNr = legislationDataList.stream().max((o1, o2) -> Integer.compare(o1.nrProperty().get(), o2.nrProperty().get()));
                    biggestActionNr.set(maxNr.get().getNr());
                }
            }
        });


        scopes = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(JC.LegalCadastre.a_scope);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomValidity = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    scopes.add(s);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
        categories = FXCollections.observableArrayList();
        try {
            JEVisAttribute attribute = this.object.getAttribute(JC.LegalCadastre.a_category);
            JEVisSample sample = attribute.getLatestSample();
            if (sample != null && !sample.getValueAsString().isEmpty()) {
                initCustomCategory = sample.getValueAsString();
                for (String s : sample.getValueAsString().split(";")) {
                    categories.add(s);
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }


    }

    public LegalCadastre() {
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
            try {

                JEVisClass actionDirClass = object.getDataSource().getJEVisClass(JC.LegalCadastre.LegalCadastreDirectory.name);
                JEVisClass actionClass = object.getDataSource().getJEVisClass(JC.LegalCadastre.LegalCadastreDirectory.Legislation.name);
                for (JEVisObject dirObj : getObject().getChildren(actionDirClass, false)) {
                    dirObj.getChildren(actionClass, false).forEach(actionObj -> {
                        System.out.println("new Action from JEVis: " + actionObj);
                        try {
                            legislationDataList.add(loadNonconformties(actionObj));
                        } catch (Exception e) {
                            logger.error("Could not load Action: {},{},{}", actionObj, e, e);
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            legislationDataList.sort(Comparator.comparingInt(value -> value.nrProperty().get()));
        }


    }

    public LegislationData loadNonconformties(JEVisObject actionObj) throws JEVisException, NullPointerException {
        JEVisAttribute att = actionObj.getAttribute("Data");
        ;
        JEVisSample sample = att.getLatestSample();
        JEVisFile file = sample.getValueAsFile();
        String s = new String(file.getBytes(), StandardCharsets.UTF_8);

        logger.info("Json: {}", s);
        Gson gson = GsonBuilder.createDefaultBuilder().create();
        LegislationData legislationData = gson.fromJson(s, LegislationData.class);
        legislationData.setObject(actionObj);
        legislationData.setLegalCadastre(this);
        return legislationData;
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
        if (!initCustomCategory.equals(listToString(categories))) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(JC.LegalCadastre.a_category);
                JEVisSample sample = attribute.buildSample(now, listToString(categories));
                sample.commit();
            } catch (Exception e) {
                logger.error(e);
            }
        }
        if (!initCustomValidity.equals(listToString(scopes))) {
            try {
                JEVisAttribute attribute = this.object.getAttribute(JC.LegalCadastre.a_scope);
                JEVisSample sample = attribute.buildSample(now, listToString(scopes));
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


    public Integer getNextNonconformityNr() {
        biggestActionNr.set(biggestActionNr.get() + 1);
        return biggestActionNr.get();
    }

    public StringProperty getName() {
        return name;
    }


    private ObservableList<LegislationData> createTestData() {
        ObservableList<LegislationData> data = FXCollections.observableArrayList();

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

    public ObservableList<LegislationData> getLegislationDataList() {
        return legislationDataList;
    }

    public void setLegislationDataList(ObservableList<LegislationData> legislationDataList) {
        this.legislationDataList = legislationDataList;
    }

    public void addLegislation(LegislationData legislationData) {
        this.legislationDataList.add(legislationData);
    }

    @Override
    public String toString() {
        return "LegalCadastre{" +
                "object=" + object +
                ", prefix=" + prefix +
                ", name=" + name +
                '}';
    }

    public ObservableList<String> getScopes() {
        return scopes;
    }

    public void setScopes(ObservableList<String> scopes) {
        this.scopes = scopes;
    }

    public ObservableList<String> getCategories() {
        return categories;
    }

    public void setCategories(ObservableList<String> categories) {
        this.categories = categories;
    }

    public ObservableList<String> getRelevanzTags() {
        return relevanzTags;
    }

    public void setRelevanzTags(ObservableList<String> relevanzTags) {
        this.relevanzTags = relevanzTags;
    }
}