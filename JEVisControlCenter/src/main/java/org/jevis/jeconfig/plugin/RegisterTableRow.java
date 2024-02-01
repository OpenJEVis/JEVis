package org.jevis.jeconfig.plugin;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.datetime.WorkDays;

import java.util.Map;

public class RegisterTableRow {
    private final String name;
    private JEVisObject object;
    private final WorkDays workDays;
    private boolean isMultiSite = false;
    private Map<JEVisType, JEVisAttribute> attributeMap;
    private String fullName;

    public RegisterTableRow(Map<JEVisType, JEVisAttribute> attributeMap, JEVisObject object, Boolean isMultiSite) {
        this.object = object;
        this.workDays = new WorkDays(object);
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

    public WorkDays getWorkDays() {
        return workDays;
    }
}
