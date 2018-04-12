/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisMultiSelection;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisSelection;
import org.jevis.api.JEVisUnit;
import org.joda.time.DateTime;

/**
 * Minimalic implementaions of an JEVisSample to handel temporary Samples.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class VirtuelSample implements JEVisSample {

    private Double _value;
    private DateTime _timeSatmp;
    private JEVisAttribute _att;
    private String _note;
    JEVisDataSource _ds;

    public VirtuelSample(DateTime ts, double value) {
        _value = value;
        _timeSatmp = ts;
    }

    public VirtuelSample(DateTime ts, double value, JEVisDataSource ds, JEVisAttribute att) {
        _value = value;
        _timeSatmp = ts;
        _att = att;
        _ds = ds;
    }

    @Override
    public DateTime getTimestamp() throws JEVisException {
        return _timeSatmp;
    }

    @Override
    public Object getValue() throws JEVisException {
        return _value;
    }

    @Override
    public String getValueAsString() throws JEVisException {
        return _value.toString();
    }

    @Override
    public Long getValueAsLong() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getValueAsDouble() throws JEVisException {
        return _value;
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        //TODO implement Unit
        return _value;
    }

    @Override
    public Boolean getValueAsBoolean() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisFile getValueAsFile() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisSelection getValueAsSelection() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisMultiSelection getValueAsMultiSelection() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(Object value) throws JEVisException, ClassCastException {
        _value = (Double) value;
    }

    @Override
    public void setValue(Object value, JEVisUnit unit) throws JEVisException, ClassCastException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisAttribute getAttribute() throws JEVisException {
        return _att;
    }

    @Override
    public String getNote() throws JEVisException {
        return _note;
    }

    @Override
    public void setNote(String note) throws JEVisException {
        _note = note;
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return _ds;
    }

    @Override
    public void commit() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtuelSample)){
            return false;
        }
        VirtuelSample otherSample = (VirtuelSample) o;
        try {
            return otherSample.getTimestamp().equals(this.getTimestamp()) && otherSample.getValue().equals(this.getValue());
        } catch (JEVisException ex) {
            Logger.getLogger(VirtuelSample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
