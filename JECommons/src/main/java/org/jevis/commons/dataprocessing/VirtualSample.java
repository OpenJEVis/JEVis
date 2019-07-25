/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing;

import org.jevis.api.*;
import org.joda.time.DateTime;

/**
 * Minimalic implementaions of an JEVisSample to handel temporary Samples.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class VirtualSample implements JEVisSample {

    JEVisDataSource _ds;
    private Double _value;
    private Long _longValue;
    private DateTime _timeStamp;
    private JEVisAttribute _att;
    private String _note;
    private JEVisUnit _unit;

    public VirtualSample(DateTime ts, Double value) {
        _value = value;
        _timeStamp = ts;
    }

    public VirtualSample(DateTime ts, Long value) {
        _longValue = value;
        _timeStamp = ts;
    }

    public VirtualSample(DateTime ts, Boolean value) {
        if (value) {
            _longValue = 1L;
        } else {
            _longValue = 0L;
        }
        _timeStamp = ts;
    }

    public VirtualSample(DateTime ts, Double value, JEVisUnit unit) {
        _value = value;
        _timeStamp = ts;
        _unit = unit;
    }

    public VirtualSample(DateTime ts, Double value, JEVisDataSource ds, JEVisAttribute att) {
        _value = value;
        _timeStamp = ts;
        _att = att;
        _ds = ds;
    }

    public VirtualSample(DateTime ts, Double value, JEVisUnit unit, JEVisDataSource ds, JEVisAttribute att) {
        _value = value;
        _unit = unit;
        _timeStamp = ts;
        _att = att;
        _ds = ds;
    }

    @Override
    public DateTime getTimestamp() {
        return _timeStamp;
    }

    @Override
    public Object getValue() {
        if (_value != null)
            return _value;
        else return _longValue;
    }

    @Override
    public void setValue(Object value) throws ClassCastException {
        _value = (Double) value;
    }

    @Override
    public String getValueAsString() {
        if (_value != null)
            return _value.toString();
        else return _longValue.toString();
    }

    @Override
    public Long getValueAsLong() {
        return _longValue;
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
        if (getValue().equals("1")) {
            return true;
        } else if (getValue().equals("0")) {
            return false;
        }

        return Boolean.parseBoolean(getValueAsString());
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
        return _unit;
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
        if (!(o instanceof JEVisSample)) {
            return false;
        }
        if (o instanceof VirtualSample) {
            VirtualSample otherSample = (VirtualSample) o;
            return otherSample.getTimestamp().equals(this.getTimestamp());
        } else {
            JEVisSample otherSample = (JEVisSample) o;
            try {
                return otherSample.getTimestamp().equals(this.getTimestamp());
            } catch (JEVisException e) {
                return false;
            }
        }
    }

    @Override
    public String toString() {
        return "VirtualSample{" +
                "value=" + _value +
                ", timeStamp=" + _timeStamp +
                ", note='" + _note + '\'' +
                '}';
    }
}
