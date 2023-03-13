package org.jevis.jeconfig.plugin.nonconformities.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.nonconformities.NonconformitiesController;

import java.util.stream.Collectors;


public class NonconformtiesOverviewData extends NonconformityPlan {

    protected static final Logger logger = LogManager.getLogger(NonconformtiesOverviewData.class);

    private NonconformitiesController controller;

    public NonconformtiesOverviewData(NonconformitiesController controller) {
        System.out.println("New OverVieData from Object: " + controller);
        this.controller = controller;
        name.set("Übersicht");

        stausTags = FXCollections.observableArrayList();
        fieldsTags = FXCollections.observableArrayList();
        mediumTags = FXCollections.observableArrayList();
        significantEnergyUseTags = FXCollections.observableArrayList();
        nonconformityList = FXCollections.observableArrayList();

        controller.getNonconformityPlanList().addListener(new ListChangeListener<NonconformityPlan>() {
            @Override
            public void onChanged(Change<? extends NonconformityPlan> c) {
                while (c.next()) {

                }
                updateData();
            }
        });


    }


    public void updateData() {
        System.out.println("Update Overview Data");
        stausTags.clear();
        fieldsTags.clear();
        mediumTags.clear();
        nonconformityList.clear();

        controller.getNonconformityPlanList().forEach(nonconformityPlan -> {
            nonconformityPlan.loadNonconformityList();
            // System.out.println("Action to add: " + actionPlanData.getName());
            nonconformityList.addAll(nonconformityPlan.getNonconformityList());
            //actions.addAll(actionPlanData.getActionData().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

            //System.out.println("Add: " + actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
            mediumTags.addAll(nonconformityPlan.getMediumTags().stream().filter(obj -> !mediumTags.contains(obj)).collect(Collectors.toList()));
            stausTags.addAll(nonconformityPlan.getStausTags().stream().filter(s -> !stausTags.contains(s)).collect(Collectors.toList()));
            fieldsTags.addAll(nonconformityPlan.getFieldsTags().stream().filter(s -> !fieldsTags.contains(s)).collect(Collectors.toList()));
            significantEnergyUseTags.addAll(nonconformityPlan.getSignificantEnergyUseTags().stream().filter(s -> !significantEnergyUseTags.contains(s)).collect(Collectors.toList()));

            nonconformityPlan.getActionData().addListener(new ListChangeListener<NonconformityData>() {
                @Override
                public void onChanged(Change<? extends NonconformityData> c) {
                    while (c.next()) {
                        //actions.addAll(c.getAddedSubList().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

                        c.getAddedSubList().forEach(actionData -> {
                            if (!nonconformityList.contains(actionData)) {
                                nonconformityList.add(actionData);
                            }
                        });
                        //actions.addAll(c.getAddedSubList());
                        nonconformityList.removeAll(c.getRemoved());
                    }

                }
            });


        });


        System.out.println("Status nach update: " + stausTags);

    }

    @Override
    public void commit() {

    }

    public void delete() throws Exception {

    }


    public StringProperty getName() {
        return name;
    }


    public ObservableList<NonconformityData> getNonconformityList() {
        return nonconformityList;
    }

    public void setNonconformityList(ObservableList<NonconformityData> nonconformityList) {
        this.nonconformityList = nonconformityList;
    }
}
