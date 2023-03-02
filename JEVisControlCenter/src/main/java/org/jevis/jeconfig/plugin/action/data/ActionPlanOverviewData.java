package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.action.ActionController;

import java.util.stream.Collectors;


public class ActionPlanOverviewData extends ActionPlanData {

    protected static final Logger logger = LogManager.getLogger(ActionPlanOverviewData.class);
    private ObservableList<String> statusTags;
    private ObservableList<String> mediumTags;
    private ObservableList<String> fieldsTags;
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty nrPrefix = new SimpleStringProperty("");
    private String initNrPrefix = "";
    private ObservableList<ActionData> actions;
    private ActionController controller;

    public ActionPlanOverviewData(ActionController controller) {
        System.out.println("New OverVieData from Object: " + controller);
        this.controller = controller;
        name.set("Übersicht");

        statusTags = FXCollections.observableArrayList();
        fieldsTags = FXCollections.observableArrayList();
        mediumTags = FXCollections.observableArrayList();
        actions = FXCollections.observableArrayList();

        controller.getActionPlans().addListener(new ListChangeListener<ActionPlanData>() {
            @Override
            public void onChanged(Change<? extends ActionPlanData> c) {
                while (c.next()) {

                }
                updateData();
            }
        });


    }


    public void updateData() {
        System.out.println("Update Overview Data");
        statusTags.clear();
        fieldsTags.clear();
        mediumTags.clear();
        actions.clear();

        controller.getActionPlans().forEach(actionPlanData -> {
            actionPlanData.loadActionList();
            // System.out.println("Action to add: " + actionPlanData.getName());
            actions.addAll(actionPlanData.getActionData());
            //actions.addAll(actionPlanData.getActionData().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

            //System.out.println("Add: " + actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            statusTags.addAll(actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            mediumTags.addAll(actionPlanData.getMediumTags().stream().filter(obj -> !mediumTags.contains(obj)).collect(Collectors.toList()));
            fieldsTags.addAll(actionPlanData.getFieldsTags().stream().filter(obj -> !fieldsTags.contains(obj)).collect(Collectors.toList()));


            actionPlanData.getActionData().addListener(new ListChangeListener<ActionData>() {
                @Override
                public void onChanged(Change<? extends ActionData> c) {
                    while (c.next()) {
                        //actions.addAll(c.getAddedSubList().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

                        c.getAddedSubList().forEach(actionData -> {
                            if (!actions.contains(actionData)) {
                                actions.add(actionData);
                            }
                        });
                        //actions.addAll(c.getAddedSubList());
                        actions.removeAll(c.getRemoved());
                    }

                }
            });


        });


        System.out.println("Status nach update: " + statusTags);

    }

    @Override
    public void reloadActionList() {

    }

    @Override
    public void commit() {

    }

    public void delete() throws Exception {

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

    public String getNrPrefix() {
        return nrPrefix.get();
    }

    public StringProperty nrPrefixProperty() {
        return nrPrefix;
    }
}
