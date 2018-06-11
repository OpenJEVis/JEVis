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

import org.jevis.api.*;
import org.joda.time.DateTime;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimalic implementaions of an JEVisSample to handel temporary Samples.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class VirtualSample implements JEVisSample {

    private Double _value;
    private DateTime _timeSatmp;
    private JEVisAttribute _att;
    private String _note;
    JEVisDataSource _ds;

    public VirtualSample(DateTime ts, double value) {
        _value = value;
        _timeSatmp = ts;
    }

    public VirtualSample(DateTime ts, double value, JEVisDataSource ds, JEVisAttribute att) {
        _value = value;
        _timeSatmp = ts;
        _att = att;
        _ds = ds;
    }

    @Override
    public DateTime getTimestamp() {
        return _timeSatmp;
    }

    @Override
    public Object getValue() {
        return _value;
    }

    @Override
    public void setValue(Object value) throws ClassCastException {
        _value = (Double) value;
    }

    @Override
    public String getValueAsString() {
        return _value.toString();
    }

    @Override
    public Long getValueAsLong() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double getValueAsDouble() {
        return _value;
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) {
        //TODO implement Unit
        return _value;
    }

    @Override
    public Boolean getValueAsBoolean() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisFile getValueAsFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisSelection getValueAsSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisMultiSelection getValueAsMultiSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(Object value, JEVisUnit unit) throws ClassCastException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisAttribute getAttribute() {
        return _att;
    }

    @Override
    public String getNote() {
        return _note;
    }

    @Override
    public void setNote(String note) {
        _note = note;
    }

    @Override
    public JEVisUnit getUnit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() {
        return _ds;
    }

    @Override
    public void commit() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VirtualSample)) {
            return false;
        }
        VirtualSample otherSample = (VirtualSample) o;
        try {
            return otherSample.getTimestamp().equals(this.getTimestamp()) && otherSample.getValue().equals(this.getValue());
        } catch (JEVisException ex) {
            Logger.getLogger(VirtualSample.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
