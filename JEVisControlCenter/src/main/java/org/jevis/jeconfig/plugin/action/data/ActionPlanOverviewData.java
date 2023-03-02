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

        /*
        System.out.println("############### Overview ############");
        System.out.println("controller.getActionPlans(): " + controller.getActionPlans().size());
        controller.getActionPlans().forEach(actionPlanData -> {

            System.out.println("for actionPlanData.getActionData: " + actionPlanData.getActionData().size());

            actionPlanData.getStatustags().forEach(s -> {
                System.out.println("getStatustags: " + s);
                if (!s.contains(s)) statusTags.add(s);
            });


            actionPlanData.getFieldsTags().forEach(s -> {
                if (!s.contains(s)) fieldsTags.add(s);
            });


            actionPlanData.getMediumTags().forEach(s -> {
                if (!s.contains(s)) mediumTags.add(s);
            });
        });


        controller.getActionPlans().addListener(new ListChangeListener<ActionPlanData>() {
            @Override
            public void onChanged(Change<? extends ActionPlanData> c) {
                while (c.next()) {
                    System.out.println("controller.getActionPlans().listener: " + c);
                    if (c.wasAdded()) {

                        c.getAddedSubList().forEach(actionPlanData -> {
                            System.out.println("actionPlanData.was added: " + actionPlanData.getName());
                            System.out.println("Actions: " + actionPlanData.getActionData());

                            actions.addAll(actionPlanData.getActionData());
                            actionPlanData.getActionData().addListener(new ListChangeListener<ActionData>() {
                                @Override
                                public void onChanged(Change<? extends ActionData> c) {
                                    while (c.next()) {
                                        if (c.wasAdded()) {
                                            System.out.println("Action was added: " + c.getAddedSubList());
                                            actions.addAll(c.getAddedSubList());
                                        } else if (c.wasRemoved()) {
                                            actions.addAll(c.getRemoved());
                                        }
                                    }
                                }
                            });


                            actionPlanData.getStatustags().addListener(new ListChangeListener<String>() {
                                @Override
                                public void onChanged(Change<? extends String> c) {
                                    while (c.next()) {
                                        if (c.wasRemoved()) {
                                            System.out.println("Status to overview: " + c.getAddedSubList());
                                            statusTags.addAll(c.getAddedSubList());
                                        } else if (c.wasAdded()) {
                                            statusTags.removeAll(c.getRemoved());
                                        }
                                    }
                                    System.out.println("actionPlanData.getStatustags().changed: " + actionPlanData.getStatustags());
                                }
                            });

                            actionPlanData.getFieldsTags().addListener(new ListChangeListener<String>() {
                                @Override
                                public void onChanged(Change<? extends String> c) {
                                    while (c.next()) {
                                        if (c.wasRemoved()) {
                                            fieldsTags.addAll(c.getAddedSubList());
                                        } else if (c.wasAdded()) {
                                            fieldsTags.removeAll(c.getRemoved());
                                        }
                                    }
                                }
                            });

                            actionPlanData.getMediumTags().addListener(new ListChangeListener<String>() {
                                @Override
                                public void onChanged(Change<? extends String> c) {
                                    while (c.next()) {
                                        if (c.wasRemoved()) {
                                            mediumTags.addAll(c.getAddedSubList());
                                        } else if (c.wasAdded()) {
                                            mediumTags.removeAll(c.getRemoved());
                                        }
                                    }
                                }
                            });


                        });

                    } else if (c.wasRemoved()) {
                        c.getRemoved().forEach(actionPlanData -> {
                            actions.removeAll(actionPlanData.getActionData());
                            statusTags.removeAll(actionPlanData.getStatustags());
                            mediumTags.removeAll(actionPlanData.getMediumTags());
                            fieldsTags.removeAll(actionPlanData.getFieldsTags());
                        });

                    }
                }

            }
        });

         */

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
            actions.addAll(actionPlanData.getActionData().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

            //System.out.println("Add: " + actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            statusTags.addAll(actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            mediumTags.addAll(actionPlanData.getMediumTags().stream().filter(obj -> !mediumTags.contains(obj)).collect(Collectors.toList()));
            fieldsTags.addAll(actionPlanData.getFieldsTags().stream().filter(obj -> !fieldsTags.contains(obj)).collect(Collectors.toList()));
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
