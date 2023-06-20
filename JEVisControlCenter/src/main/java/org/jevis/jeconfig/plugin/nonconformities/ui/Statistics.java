package org.jevis.jeconfig.plugin.nonconformities.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.jevis.jeconfig.plugin.nonconformities.data.NonconformityData;

public class Statistics {

    private final ObservableList<NonconformityData> nonconformityDataObservableList;

    public Statistics(ObservableList<NonconformityData> nonconformityDataObservableList) {
        this.nonconformityDataObservableList = nonconformityDataObservableList;
    }

    public StringProperty getAll(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.size(),nonconformityDataObservableList));
        return stringProperty;

    }

    public StringProperty getActive(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.stream().filter(actionData -> actionData.getDoneDate()== null).count(),nonconformityDataObservableList));
        return stringProperty;

    }


    public StringProperty getFinished(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text+ nonconformityDataObservableList.stream().filter(data1 -> data1.getDoneDate()!= null).count(),nonconformityDataObservableList));
        return stringProperty;

    }
}
