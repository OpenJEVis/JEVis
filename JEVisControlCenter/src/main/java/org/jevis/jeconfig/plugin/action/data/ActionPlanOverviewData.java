package org.jevis.jeconfig.plugin.action.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.plugin.action.ActionController;

import java.util.stream.Collectors;


public class ActionPlanOverviewData extends ActionPlanData {

    protected static final Logger logger = LogManager.getLogger(ActionPlanOverviewData.class);
    private final ObservableList<String> statusTags;
    private final ObservableList<String> mediumTags;
    private final ObservableList<String> fieldsTags;
    private final ObservableList<Medium> medium;
    private final ObservableList<String> significantEnergyUseTags;
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty nrPrefix = new SimpleStringProperty("");
    private final String initNrPrefix = "";
    private final ObservableList<ActionData> actions;
    private final ActionController controller;

    public ActionPlanOverviewData(ActionController controller) {
        //System.out.println("New OverViewData from Object: " + controller);
        this.controller = controller;
        name.set(I18n.getInstance().getString("plugin.action.plan.overview"));

        statusTags = FXCollections.observableArrayList();
        fieldsTags = FXCollections.observableArrayList();
        mediumTags = FXCollections.observableArrayList();
        actions = FXCollections.observableArrayList();
        medium = FXCollections.observableArrayList();

        significantEnergyUseTags = FXCollections.observableArrayList();

        controller.getActionPlans().addListener(new ListChangeListener<ActionPlanData>() {
            @Override
            public void onChanged(Change<? extends ActionPlanData> c) {
                while (c.next()) {
                    // System.out.println("!!!!!! Overview: " + c);
                    if (c.getList().equals(actions)) {
                        // System.out.println("is master Liste");
                    } else {
                        //System.out.println("is not master list");
                        if (c.wasAdded() || c.wasRemoved()) {
                            // System.out.println("new list: " + c.getList());
                            updateData();
                        }
                    }


                }
            }
        });


    }


    public void updateData() {
        //System.out.println("Update Overview Data");
        statusTags.clear();
        fieldsTags.clear();
        mediumTags.clear();
        actions.clear();

        controller.getActionPlans().forEach(actionPlanData -> {
            if (actionPlanData instanceof ActionPlanOverviewData) {
                return;
            }
            actions.addAll(actionPlanData.getActionData());
            //actions.addAll(actionPlanData.getActionData().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

            //System.out.println("Add: " + actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            statusTags.addAll(actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            mediumTags.addAll(actionPlanData.getMediumTags().stream().filter(obj -> !mediumTags.contains(obj)).collect(Collectors.toList()));
            fieldsTags.addAll(actionPlanData.getFieldsTags().stream().filter(obj -> !fieldsTags.contains(obj)).collect(Collectors.toList()));
            significantEnergyUseTags.addAll(actionPlanData.significantEnergyUseTags().stream().filter(obj -> !significantEnergyUseTags.contains(obj)).collect(Collectors.toList()));

            actionPlanData.getMedium().forEach(medium1 -> {
                if (!medium.contains(medium1)) {
                    medium.add(medium1);
                }
            });

            actionPlanData.getActionData().addListener(new ListChangeListener<ActionData>() {
                @Override
                public void onChanged(Change<? extends ActionData> c) {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            actions.addAll(c.getAddedSubList());
                        }
                        if (c.wasRemoved()) {
                            actions.removeAll(c.getRemoved());
                        }
                    }

                }
            });
        });

    }

    @Override
    public void reloadActionList() {

    }

    @Override
    public void commit() {

    }

    @Override
    public void delete() throws Exception {

    }

    @Override
    public StringProperty getName() {
        return name;
    }

    @Override
    public ObservableList<String> getStatustags() {
        return statusTags;
    }

    @Override
    public ObservableList<String> getMediumTags() {
        return mediumTags;
    }

    @Override
    public ObservableList<Medium> getMedium() {
        return medium;
    }

    @Override
    public ObservableList<String> getFieldsTags() {
        return fieldsTags;
    }

    @Override
    public ObservableList<ActionData> getActionData() {
        return actions;
    }

    @Override
    public ObservableList<String> significantEnergyUseTags() {
        return significantEnergyUseTags;
    }

    @Override
    public String getNoPrefix() {
        return nrPrefix.get();
    }

    @Override
    public StringProperty noPrefixProperty() {
        return nrPrefix;
    }
}
