package org.jevis.jecc.plugin.meters.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.classes.JC;
import org.jevis.commons.utils.CalcMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MeterList {
    private static final Logger logger = LogManager.getLogger(MeterList.class);
    private final JEVisObject jeVisObject;
    ObservableList<MeterData> meterDataList = FXCollections.observableArrayList();
    private String name;

    public MeterList(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;

        try {
            this.name = CalcMethods.getFirstParentalObjectOfClass(jeVisObject, "Building").getName();
        } catch (Exception e) {
            this.name = jeVisObject.getName();
            logger.error(e);
        }
    }


    public ObservableList<MeterData> getMeterDataList() {
        return meterDataList;
    }

    public void loadMeterList() {

        List<JEVisObject> directory = new ArrayList<>();
        directory.add(jeVisObject);
        for (JEVisObject jeVisObject1 : load(directory)) {
            MeterData meterData = new MeterData(jeVisObject1);
            meterDataList.add(meterData);
        }
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

    public JEVisObject getJeVisObject() {
        return jeVisObject;
    }

    public List<JEVisTypeWrapper> getAllAvailableTypes() {
        return meterDataList.stream().map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().keySet()).flatMap(jeVisTypes1 -> jeVisTypes1.stream()).distinct().collect(Collectors.toList());
    }

    public List<JEVisTypeWrapper> getAllAvailableTypesOfClass(String classname) {
        return meterDataList.stream().filter(meterData -> {
            try {
                return meterData.getJeVisClass().getName().equals(classname);
            } catch (JEVisException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        }).map(meterData -> meterData.getJeVisAttributeJEVisSampleMap().keySet()).flatMap(jeVisTypes1 -> jeVisTypes1.stream()).distinct().collect(Collectors.toList());

    }

    public List<MeterData> getMeterDataOfClass(String classname) {
        return meterDataList.stream().filter(meterData -> meterData.getjEVisClassName().equals(classname)).collect(Collectors.toList());
    }
}
