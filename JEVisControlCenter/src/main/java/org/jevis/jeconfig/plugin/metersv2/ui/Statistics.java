package org.jevis.jeconfig.plugin.metersv2.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisException;
import org.jevis.commons.i18n.I18n;
import org.jevis.jeconfig.application.application.I18nWS;
import org.jevis.jeconfig.plugin.metersv2.data.JEVisTypeWrapper;
import org.jevis.jeconfig.plugin.metersv2.data.MeterData;
import org.joda.time.DateTime;

public class Statistics {

    private final ObservableList<MeterData> meterDatas;
    private final MeterPlanTable meterPlanTable;

    private BooleanProperty updateTrigger = new SimpleBooleanProperty();


    public Statistics(ObservableList<MeterData> nonconformityDataObservableList, MeterPlanTable meterPlanTable) {
        this.meterDatas = nonconformityDataObservableList;
        this.meterPlanTable = meterPlanTable;


        meterPlanTable.getMeterEventHandler().addEventListener(event -> {
            System.out.println(event);
            switch (event.getType()) {
                case UPDATE:
                    updateTrigger.set(!updateTrigger.get());
                    System.out.println(meterDatas);
                    break;
                case ADD:
                    System.out.println(meterDatas);
                    updateTrigger.set(!updateTrigger.get());
                    break;
                case FILTER:
                    System.out.println(meterDatas);
                    updateTrigger.set(!updateTrigger.get());
                    break;
                case REMOVE:
                    System.out.println(meterDatas);
                    updateTrigger.set(!updateTrigger.get());
                    break;
            }
        });
    }

    public StringProperty getType(JEVisTypeWrapper jeVisTypeWrapper, String name) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> name + ": " + meterDatas.stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().get(jeVisTypeWrapper))
                .filter(sampleData -> sampleData != null).map(sampleData -> sampleData.getOptionalJEVisSample()).filter(optionalJEVisSample -> optionalJEVisSample.isPresent())
                .map(optionalJEVisSample -> {
                    try {
                        return optionalJEVisSample.get().getValueAsString();
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(s -> s.equals(name)).count(), updateTrigger));
        return stringProperty;

    }


    public StringProperty getAllOfMedium(String jeVisClass) {


        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> I18nWS.getInstance().getClassName(jeVisClass) + ": " + String.valueOf(meterDatas.stream().filter(meterData -> {
            try {
                return meterData.getJeVisClass().getName().equals(jeVisClass);
            } catch (JEVisException e) {
                throw new RuntimeException(e);
            }
        }).count()), updateTrigger));


        return stringProperty;

    }

    public StringProperty getOverdue(JEVisTypeWrapper jeVisTypeWrapper, String name) {
        StringProperty stringProperty = new SimpleStringProperty();
        stringProperty.bind(Bindings.createStringBinding(() -> name + ": " + meterDatas.stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap()
                        .get(jeVisTypeWrapper)).filter(sampleData -> sampleData != null).map(sampleData -> sampleData.getOptionalJEVisSample()).filter(optionalJEVisSample -> optionalJEVisSample.isPresent())
                .map(optionalJEVisSample -> {
                    try {
                        return new DateTime(optionalJEVisSample.get().getValueAsString());
                    } catch (JEVisException e) {
                        throw new RuntimeException(e);
                    }
                }).filter(dateTime -> dateTime.isBeforeNow()).count(), updateTrigger));

        return stringProperty;
    }


}