package org.jevis.jecc.plugin.meters;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;

import java.util.Map;

public class RegisterTableRow {
    private final String name;
    private JEVisObject object;
    private boolean isMultiSite = false;
    private Map<JEVisType, JEVisAttribute> attributeMap;
    private String fullName;

    public RegisterTableRow(Map<JEVisType, JEVisAttribute> attributeMap, JEVisObject object, Boolean isMultiSite) {
        this.object = object;
        this.attributeMap = attributeMap;
        this.isMultiSite = isMultiSite;
        this.name = object.getName();
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

    public String getName() {
        return name;
    }

    public boolean isMultiSite() {
        return isMultiSite;
    }
}
