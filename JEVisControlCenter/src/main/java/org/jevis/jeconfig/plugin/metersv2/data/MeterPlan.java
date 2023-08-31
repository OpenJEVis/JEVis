package org.jevis.jeconfig.plugin.metersv2.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

public class MeterPlan {
    private JEVisObject jeVisObject;
    private String name;
    ObservableList<MeterData> meterDataList = FXCollections.observableArrayList();

    public MeterPlan(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
        this.name = jeVisObject.getName();
    }



    public ObservableList<MeterData> getMeterDataList() {
        return meterDataList;
    }

    public void loadMeterList() {

        List<JEVisObject> directory = new ArrayList<>();
        directory.add(jeVisObject);
        for (JEVisObject jeVisObject1 : load(directory)) {
            System.out.println(jeVisObject1);
            MeterData meterData = new MeterData(jeVisObject1);
            meterDataList.add(meterData);
        }


//        if (!actionsLoaded.get()) {
//            actionsLoaded.set(true);
//            try {
//
//                JEVisClass measurementClass =  object.getDataSource().getJEVisClass(JC.MeasurementInstrument.name);
//                for (JEVisObject measurementObject : getObject().getChildren(measurementClass, true)) {
//
//                    System.out.println(measurementObject);
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            nonconformityList.sort(Comparator.comparingInt(value -> value.nrProperty().get()));
//        }


    }

    private List<JEVisObject> load(List<JEVisObject> jeVisObjects) {
        List<JEVisObject> result = new ArrayList<>();
        try {
            JEVisClass measurementDirectory = jeVisObject.getDataSource().getJEVisClass(JC.Directory.MeasurementDirectory.name);
            JEVisClass measurementClass = jeVisObject.getDataSource().getJEVisClass(JC.MeasurementInstrument.name);

            for (JEVisObject object : jeVisObjects) {
                List<JEVisObject> plans = object.getChildren();
                result.addAll(load(plans));
                List<JEVisObject> meter = object.getChildren(measurementClass, true);
                result.addAll(meter);
            }
            //result.addAll(jeVisObjects);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    @Override
    public String toString() {
        return "MeterPlan{" +
                "jeVisObject=" + jeVisObject +
                ", name='" + name + '\'' +
                ", meterDataList=" + meterDataList +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
