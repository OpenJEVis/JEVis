package org.jevis.jeconfig.plugin.object.childrentableeditor;

import org.jevis.api.JEVisObject;

public class TableData {


    private JEVisObject object = null;


    public TableData() {
    }


    public TableData(JEVisObject object) {
        this.object = object;
    }

    public JEVisObject getObject() {
        return this.object;
    }

    public void setObject(JEVisObject object) {
        this.object = object;
    }
}
