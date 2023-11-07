package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

public class Statistics {

    private final ObservableList<NonconformityData> nonconformityDataObservableList;

    private final BooleanProperty updateTrigger;

    public Statistics(ObservableList<NonconformityData> nonconformityDataObservableList, BooleanProperty updateTrigger) {
        this.nonconformityDataObservableList = nonconformityDataObservableList;
        this.updateTrigger = updateTrigger;
    }

    public StringProperty getAll(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.size(),nonconformityDataObservableList,updateTrigger));
        return stringProperty;

    }

    public StringProperty getActive(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.stream().filter(actionData -> actionData.getDoneDate()== null).count(),nonconformityDataObservableList,updateTrigger));
        return stringProperty;

    }


    public StringProperty getFinished(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.stream().filter(data1 -> data1.getDoneDate()!= null).count(),nonconformityDataObservableList,updateTrigger));
        return stringProperty;

    }
}
