package org.jevis.jeconfig.plugin.meters;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.relationship.ObjectRelations;

import java.util.Map;

public class RegisterTableRow {
    private final String name;
    private JEVisObject object;
    private Map<JEVisType, JEVisAttribute> attributeMap;
    private ObjectRelations objectRelations;
    private String fullName;

    public RegisterTableRow(Map<JEVisType, JEVisAttribute> attributeMap, JEVisObject object, Boolean isMultiSite) {
        this.object = object;
        this.attributeMap = attributeMap;
        try {
            this.objectRelations = new ObjectRelations(object.getDataSource());
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        if (!isMultiSite)
            this.name = object.getName();
        else {
            String prefix = objectRelations.getObjectPath(object);
            this.name = prefix + object.getName();
        }
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
}
