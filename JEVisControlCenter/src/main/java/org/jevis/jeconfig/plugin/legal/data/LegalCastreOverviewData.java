package org.jevis.jeconfig.plugin.legal.data;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.jeconfig.plugin.legal.LegalCadastreController;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;


public class LegalCastreOverviewData extends LegalCadastre {

    protected static final Logger logger = LogManager.getLogger(LegalCastreOverviewData.class);


    private LegalCadastreController controller;

    public LegalCastreOverviewData(LegalCadastreController controller) {
        System.out.println("New OverVieData from Object: " + controller);
        this.controller = controller;
        name.set("Übersicht");



        controller.getLegalCadastres().addListener(new ListChangeListener<LegalCadastre>() {
            @Override
            public void onChanged(Change<? extends LegalCadastre> c) {
                while (c.next()) {
                    updateData();
                }

            }
        });


    }


    public void updateData() {

        System.out.println("overview update data");
        legislationDataList.clear();
        System.out.println(controller.getLegalCadastres());

        controller.getLegalCadastres().forEach(legalCadastre -> {
            legalCadastre.loadNonconformityList();

            System.out.println(legalCadastre.getLegislationDataList());
            // System.out.println("Action to add: " + actionPlanData.getName());
            legislationDataList.addAll(legalCadastre.getLegislationDataList());
            //actions.addAll(actionPlanData.getActionData().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

//            //System.out.println("Add: " + actionPlanData.getStatustags().stream().filter(obj -> !statusTags.contains(obj)).collect(Collectors.toList()));
//            mediumTags.addAll(legalCadastre.getMediumTags().stream().filter(obj -> !mediumTags.contains(obj)).collect(Collectors.toList()));
//            stausTags.addAll(legalCadastre.getStausTags().stream().filter(s -> !stausTags.contains(s)).collect(Collectors.toList()));
//            fieldsTags.addAll(legalCadastre.getFieldsTags().stream().filter(s -> !fieldsTags.contains(s)).collect(Collectors.toList()));
//            significantEnergyUseTags.addAll(legalCadastre.getSignificantEnergyUseTags().stream().filter(s -> !significantEnergyUseTags.contains(s)).collect(Collectors.toList()));

            legalCadastre.getLegislationDataList().addListener(new ListChangeListener<LegislationData>() {
                @Override
                public void onChanged(Change<? extends LegislationData> c) {
                    while (c.next()) {
                        //actions.addAll(c.getAddedSubList().stream().filter(actionData -> !actions.contains(actionData)).collect(Collectors.toList()));

                        c.getAddedSubList().forEach(actionData -> {
                            if (!legislationDataList.contains(actionData)) {
                                legislationDataList.add(actionData);
                            }
                        });
                        //actions.addAll(c.getAddedSubList());
                        legislationDataList.removeAll(c.getRemoved());
                    }

                }
            });


        });



    }

    @Override
    public void commit() {

    }

    public void delete() throws Exception {

    }


    public StringProperty getName() {
        return name;
    }


    public ObservableList<LegislationData> getLegislationData() {
        return legislationDataList;
    }

    public void setLegislationData(ObservableList<LegislationData> legislationData) {
        this.legislationDataList = legislationData;
    }
}
