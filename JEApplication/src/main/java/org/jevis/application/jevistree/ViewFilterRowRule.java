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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ViewFilterRowRule {

    private Map<String, Boolean> _visibleColumns = new HashMap<>();

    private String _jevisClass = "";
    private String _attribute = "";
    private boolean _showUnknowColumns = false;
    
    private boolean _showRow = true;

    public ViewFilterRowRule(String jclass, String attribute, boolean show) {
        _jevisClass = jclass;
        _attribute = attribute;
        _showRow = show;
    }

    public String getKey() {
        if (_attribute != null || _attribute.isEmpty()) {
            return _jevisClass;
        }

        return _jevisClass + "." + _attribute;
    }

    protected void setVisibleColumn(String columnName, boolean visible) {
        _visibleColumns.put(columnName, visible);
    }

    public String getJEVisClass() {
        return _jevisClass;
    }

    public String getAttribute() {
        return _attribute;
    }

    public void setShowUnkonwColumns(boolean show) {
        _showUnknowColumns = show;
    }

    public boolean showRow(JEVisClass jclass) {
        try {
            return jclass.getName().equals(_jevisClass);
        } catch (JEVisException ex) {
            Logger.getLogger(ViewFilterRowRule.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean showRow(JEVisAttribute att) {
        if (_attribute.equals(att.getName())) {
            return _showRow;
        }
        return false;
    }

    public boolean showRow(JEVisTreeRow row) {
        try {
            if (row.getJEVisObject().getJEVisClass().getName().equals(_jevisClass)) {
                if (row.getType() == JEVisTreeRow.TYPE.OBJECT) {
                    return true;
                }

                if (!_attribute.isEmpty() && row.getType() == JEVisTreeRow.TYPE.ATTRIBUTE && _attribute.equals(row.getJEVisAttribute().getName())) {
                    return _showRow;
                }
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ViewFilter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public boolean showColumn(String column) {
        return true;//workaround
    }
}
