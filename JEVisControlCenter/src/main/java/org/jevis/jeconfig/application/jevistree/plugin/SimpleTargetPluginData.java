/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jeconfig.application.jevistree.plugin;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.jeconfig.application.jevistree.JEVisTreeRow;
import org.joda.time.DateTime;

/**
 *
 * @author fs
 */
public class SimpleTargetPluginData {

    private JEVisObject obj;
    private JEVisAttribute _att;

    private JEVisTreeRow row;
    private boolean selected = false;
    private String uuid = "";

    public SimpleTargetPluginData() {
    }

    public SimpleTargetPluginData(JEVisTreeRow row) {
        this.row = row;
        uuid = getObj().getID() + "--" + DateTime.now().toString();
    }

    public JEVisTreeRow getRow() {
        return row;
    }

    public void setRow(JEVisTreeRow row) {
        this.row = row;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public JEVisObject getObj() {
        return row.getJEVisObject();
    }

    public void setObj(JEVisObject obj) {
        this.obj = obj;
    }

    public JEVisAttribute getAtt() {
        return row.getJEVisAttribute();
//        return _att;
    }

    public void setAtt(JEVisAttribute _att) {
        this._att = _att;
    }

    @Override
    public String toString() {
        return "SimpleTargetPluginData{" + "uuid=" + uuid + '}';
    }

}
