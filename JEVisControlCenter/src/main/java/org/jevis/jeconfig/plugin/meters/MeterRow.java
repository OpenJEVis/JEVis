package org.jevis.jeconfig.plugin.meters;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;

import java.util.Map;

public class MeterRow {
    private JEVisObject object;
    private Map<JEVisType, JEVisAttribute> attributeMap;

    public MeterRow(Map<JEVisType, JEVisAttribute> attributeMap, JEVisObject object) {
        this.object = object;
        this.attributeMap = attributeMap;
    }

    public JEVisObject getObject() {
        return object;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }

    public Map<JEVisType, JEVisAttribute> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(Map<JEVisType, JEVisAttribute> attributeMap) {
        this.attributeMap = attributeMap;
    }
}
