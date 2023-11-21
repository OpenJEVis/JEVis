package org.jevis.jecc.plugin.legal.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.jevis.jecc.plugin.legal.data.ObligationData;

public class Statistics {

    private final ObservableList<ObligationData> obligationData;
    private final BooleanProperty update;

    public Statistics(ObservableList<ObligationData> obligationData, BooleanProperty update) {
        this.obligationData = obligationData;
        this.update = update;
    }

    public StringProperty getAll(String text) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text + obligationData.size(), obligationData, update));
        return stringProperty;

    }

    public StringProperty getRelevant(String text, boolean compare) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text + obligationData.stream().filter(obligationData1 -> obligationData1.getRelevant() == compare).count(), obligationData, update));
        return stringProperty;

    }


    public StringProperty getCategory(String text, String compare) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text + obligationData.stream().filter(obligationData1 -> obligationData1.getCategory().equals(compare)).count(), obligationData, update));
        return stringProperty;

    }

    public StringProperty getScope(String text, String compare) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> text + obligationData.stream().filter(obligationData1 -> obligationData1.getScope().equals(compare)).count(), obligationData, update));
        return stringProperty;

    }
}
