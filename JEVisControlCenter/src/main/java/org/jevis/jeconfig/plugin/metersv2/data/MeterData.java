package org.jevis.jeconfig.plugin.metersv2.data;

import org.jevis.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MeterData {
    JEVisObject jeVisObject;
    Map<JEVisTypeWrapper, Optional<JEVisSample>> jeVisAttributeJEVisSampleMap = new HashMap<>();

    private JEVisClass jeVisClass;

    private String jEVisClassName;

    public MeterData(JEVisObject jeVisObject) {
            this.jeVisObject = jeVisObject;
            load();
    }

    public void load() {
        try {
            jeVisClass = jeVisObject.getJEVisClass();
            jEVisClassName = jeVisClass.getName();
            for (JEVisAttribute jeVisAttribute : jeVisObject.getAttributes()) {
                if (jeVisAttribute.hasSample()) {
                    jeVisAttributeJEVisSampleMap.put(new JEVisTypeWrapper(jeVisAttribute.getType()), Optional.of(jeVisAttribute.getLatestSample()));
                }else {
                    jeVisAttributeJEVisSampleMap.put(new JEVisTypeWrapper(jeVisAttribute.getType()), Optional.empty());
                }

            }
        } catch (Exception e) {

        }
    }


    public Map<JEVisTypeWrapper, Optional<JEVisSample>> getJeVisAttributeJEVisSampleMap() {
        return jeVisAttributeJEVisSampleMap;
    }

    public void setJeVisAttributeJEVisSampleMap(Map<JEVisTypeWrapper, Optional<JEVisSample>> jeVisAttributeJEVisSampleMap) {
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

    @Override
    public String toString() {
        return "MeterData{" +
                "jeVisObject=" + jeVisObject.getName() +
                '}';
    }

    public String getjEVisClassName() {
        return jEVisClassName;
    }
}
