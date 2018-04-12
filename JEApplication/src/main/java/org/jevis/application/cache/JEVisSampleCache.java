/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.application.cache;

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
 *
 * @author fs
 */
public class JEVisSampleCache implements JEVisSample {

    private final JEVisDataSourceCache cache;
    private final JEVisSample otherSample;
    private final JEVisAttribute attribute;

    public JEVisSampleCache(JEVisDataSourceCache cache, JEVisSample otherSample, JEVisAttribute att) {
        this.cache = cache;
        this.otherSample = otherSample;
        this.attribute = att;
    }

    @Override
    public DateTime getTimestamp() throws JEVisException {
        return otherSample.getTimestamp();
    }

    @Override
    public Object getValue() throws JEVisException {
        return otherSample.getValue();
    }

    @Override
    public String getValueAsString() throws JEVisException {
        return otherSample.getValueAsString();
    }

    @Override
    public Long getValueAsLong() throws JEVisException {
        return otherSample.getValueAsLong();
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        return otherSample.getValueAsLong(unit);
    }

    @Override
    public Double getValueAsDouble() throws JEVisException {
        return otherSample.getValueAsDouble();
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        return otherSample.getValueAsDouble(unit);
    }

    @Override
    public Boolean getValueAsBoolean() throws JEVisException {
        return otherSample.getValueAsBoolean();
    }

    @Override
    public JEVisFile getValueAsFile() throws JEVisException {
        return otherSample.getValueAsFile();
    }

    @Override
    public JEVisSelection getValueAsSelection() throws JEVisException {
        return otherSample.getValueAsSelection();
    }

    @Override
    public JEVisMultiSelection getValueAsMultiSelection() throws JEVisException {
        return otherSample.getValueAsMultiSelection();
    }

    @Override
    public void setValue(Object value) throws JEVisException, ClassCastException {
        otherSample.setValue(value);
    }

    @Override
    public void setValue(Object value, JEVisUnit unit) throws JEVisException, ClassCastException {
        otherSample.setValue(value, unit);
    }

    @Override
    public JEVisAttribute getAttribute() throws JEVisException {
        return attribute;
    }

    @Override
    public String getNote() throws JEVisException {
        return otherSample.getNote();
    }

    @Override
    public void setNote(String note) throws JEVisException {
        otherSample.setNote(note);
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return otherSample.getUnit();
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return cache;
    }

    @Override
    public void commit() throws JEVisException {
        otherSample.commit();
    }

    @Override
    public void rollBack() throws JEVisException {
        otherSample.rollBack();
    }

    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
