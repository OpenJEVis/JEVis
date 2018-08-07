/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.jevistree;

import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ViewFilter {

    private Map<String, ViewFilterRowRule> _rowRules = new HashMap<>();

    private boolean _defaultObjectShow = true;
    private boolean _defaultAttributeShow = true;
    private boolean _defaultShowColumn = false;

    public enum TYPE {

        OBJECT, ATTRIBUTE
    }

    public ViewFilter() {
    }

    public void putRule(ViewFilterRowRule setting) {
        _rowRules.put(setting.getKey(), setting);
    }

    /**
     *
     * @deprecated will be removed with the real implementaion
     * @param show
     */
    public void showAttributes(boolean show) {
        _defaultAttributeShow = show;
    }

    protected boolean showAttributes() {
        return _defaultAttributeShow;
    }

    private String getRowKey(JEVisTreeRow row) {
        try {
            if (row.getType() == JEVisTreeRow.TYPE.OBJECT) {
                return row.getJEVisObject().getJEVisClassName();

            } else {
                return row.getJEVisObject().getJEVisClassName() + "." + row.getJEVisAttribute().getName();
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ViewFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public boolean showJEvisClass(JEVisClass jclass) {
        try {
            if (jclass != null && _rowRules.containsKey(jclass.getName())) {
                return _rowRules.get(jclass.getName()).showRow(jclass);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ViewFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return _defaultObjectShow;

    }

    public boolean showAttribute(JEVisAttribute att) {

        try {
            String key = att.getObject().getJEVisClassName() + "." + att.getName();
            if (_rowRules.containsKey(key)) {
                return _rowRules.get(key).showRow(att);
            } else {
                return _defaultAttributeShow;
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ViewFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return _defaultAttributeShow;
    }

    public boolean showRow(JEVisTreeRow row) {

        if (_rowRules.containsKey(getRowKey(row))) {
            return _rowRules.get(getRowKey(row)).showRow(row);
        } else {
            return _defaultObjectShow;
        }
    }

    public boolean showColumn(JEVisTreeRow row, String columnName) {
//        System.out.println("find column key: " + getRowKey(row));
//
//        for (Map.Entry<String, ViewFilterRowRule> entrySet : _rowRules.entrySet()) {
//            String key = entrySet.getKey();
//            ViewFilterRowRule value = entrySet.getValue();
//
//            System.out.println("       --rule: " + value.getKey());
//        }

        if (_rowRules.containsKey(getRowKey(row))) {
            return _rowRules.get(getRowKey(row)).showColumn(columnName);
        } else {
            return _defaultAttributeShow;
        }

    }

}
