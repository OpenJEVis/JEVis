package org.jevis.jeconfig.tool.template;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Templates {

    public static ObservableList<Template> getAllTemplates() {
        ObservableList templateList = FXCollections.observableArrayList();

        templateList.add(new EnergyMeterCounter());
        templateList.add(new EnergyMeterKWH());
        templateList.add(new EnergyMeterKW());
        templateList.add(new VolumeMeterCounterCubic());
        templateList.add(new VolumeMeterCubic());
        templateList.add(new VolumeMeterCounterLiter());
        templateList.add(new VolumeMeterLiter());
        templateList.add(new Temperature());
        templateList.add(new BuildingObject());

        return templateList;
    }

}
