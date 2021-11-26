package org.jevis.jeconfig.plugin.object.childrentableeditor;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.util.ArrayList;
import java.util.List;

public class TableData {


    private JEVisObject object = null;
    private List<JEVisAttribute> attributeList = new ArrayList<>();

    public TableData() {
    }


    public TableData(JEVisObject object) {
        this.object = object;
        try {
            this.attributeList = object.getAttributes();
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    public JEVisObject getObject() {
        return this.object;
    }

    public List<JEVisAttribute> getAttributeList() {
        return attributeList;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }
}
