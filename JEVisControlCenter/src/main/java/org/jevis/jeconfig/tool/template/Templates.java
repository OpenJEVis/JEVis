package org.jevis.jeconfig.tool.template;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Templates {

    public static ObservableList<Template> getAllTemplates() {
        ObservableList templateList = FXCollections.observableArrayList();

        templateList.add(new ElectricMeterImpuls());


        return templateList;
    }

}
