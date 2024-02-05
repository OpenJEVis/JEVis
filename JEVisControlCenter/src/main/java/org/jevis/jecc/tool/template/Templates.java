package org.jevis.jecc.tool.template;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Templates {

    public static ObservableList<Template> getAllTemplates() {
        ObservableList templateList = FXCollections.observableArrayList();

        templateList.add(new ElectricMeterCounter());
        templateList.add(new ElectricMeterKWH());
        templateList.add(new ElectricMeterKW());
        templateList.add(new GasMeterCubicCounter());
        templateList.add(new WaterCubicMetreCounter());
        templateList.add(new GasMeterCubic());
        templateList.add(new WaterCubicMetre());
        templateList.add(new Temperature());
        templateList.add(new BuildingObject());

        return templateList;
    }

}
