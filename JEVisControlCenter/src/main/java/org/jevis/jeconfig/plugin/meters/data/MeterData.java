package org.jevis.jeconfig.plugin.meters.data;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisObject;

import java.util.HashMap;
import java.util.Map;

public class MeterData {
    JEVisObject jeVisObject;
    Map<JEVisTypeWrapper, SampleData> jeVisAttributeJEVisSampleMap = new HashMap<>();

    private JEVisClass jeVisClass;

    private String jEVisClassName;
    private String name;

    public MeterData(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
        load();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void load() {
        try {
            name = jeVisObject.getName();
            jeVisClass = jeVisObject.getJEVisClass();
            jEVisClassName = jeVisClass.getName();
            for (JEVisAttribute jeVisAttribute : jeVisObject.getAttributes()) {
                if (jeVisAttribute.hasSample()) {
                    jeVisAttributeJEVisSampleMap.put(new JEVisTypeWrapper(jeVisAttribute.getType()), new SampleData(jeVisAttribute));
                } else {
                    jeVisAttributeJEVisSampleMap.put(new JEVisTypeWrapper(jeVisAttribute.getType()), new SampleData(jeVisAttribute));
                }

            }
        } catch (Exception e) {

        }
    }


    public Map<JEVisTypeWrapper, SampleData> getJeVisAttributeJEVisSampleMap() {
        return jeVisAttributeJEVisSampleMap;
    }

    public void setJeVisAttributeJEVisSampleMap(Map<JEVisTypeWrapper, SampleData> jeVisAttributeJEVisSampleMap) {
        this.jeVisAttributeJEVisSampleMap = jeVisAttributeJEVisSampleMap;
    }

    public JEVisClass getJeVisClass() {
        return jeVisClass;
    }

    public void setJeVisClass(JEVisClass jeVisClass) {
        this.jeVisClass = jeVisClass;
    }

    public JEVisObject getJeVisObject() {
        return jeVisObject;
    }

    public void setJeVisObject(JEVisObject jeVisObject) {
        this.jeVisObject = jeVisObject;
    }

    public String getjEVisClassName() {
        return jEVisClassName;
    }

    @Override
    public String toString() {
        return "MeterData{" +
                "jeVisObject=" + jeVisObject +
                ", name='" + name + '\'' +
                '}';
    }
}
