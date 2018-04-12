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

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 *
 * @author fs
 */
public class JEVisAttributeCache implements JEVisAttribute {

    private JEVisAttribute otherDBAttribute = null;
    private JEVisDataSourceCache cache;
    private long obj;
    private Integer primitiveType;
    private final Logger logger = LogManager.getLogger(JEVisAttributeCache.class);

    public JEVisAttributeCache(JEVisDataSourceCache cache, long obj, JEVisAttribute att) {
        this.otherDBAttribute = att;
        this.cache = cache;
        this.obj = obj;
    }

    @Override
    public String getName() {
        return otherDBAttribute.getName();
    }

    @Override
    public boolean delete() {
        return otherDBAttribute.delete();
    }

    @Override
    public JEVisType getType() throws JEVisException {
        return cache.getType(cache.getObject(getObjectID()).getJEVisClassName(), getName());
    }

    @Override
    public JEVisObject getObject() {
        try {
            return cache.getObject(obj);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Long getObjectID() {
        return obj;
    }

    @Override
    public List<JEVisSample> getAllSamples() {
        return otherDBAttribute.getAllSamples();
    }

    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        List<JEVisSample> samples = otherDBAttribute.getSamples(from, to);
        List<JEVisSample> cached = new ArrayList<>();
        for (JEVisSample s : samples) {
            cached.add(new JEVisSampleCache(cache, s, this));
        }
        return cached;
    }

    @Override
    public int addSamples(List<JEVisSample> samples) throws JEVisException {
        return otherDBAttribute.addSamples(samples);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
        return otherDBAttribute.buildSample(ts, value);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
        return otherDBAttribute.buildSample(ts, value, unit);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException {
        return otherDBAttribute.buildSample(ts, value, note);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException {
        return otherDBAttribute.buildSample(ts, value, note, unit);
    }

    @Override
    public JEVisSample getLatestSample() {
        JEVisSample otherSample = otherDBAttribute.getLatestSample();
        if (otherSample == null) {
            return null;
        } else {
            return new JEVisSampleCache(cache, otherSample, this);
        }

    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return getType().getPrimitiveType();
    }

    @Override
    public boolean hasSample() {
        return otherDBAttribute.hasSample();
    }

    @Override
    public DateTime getTimestampFromFirstSample() {
        return otherDBAttribute.getTimestampFromFirstSample();
    }

    @Override
    public DateTime getTimestampFromLastSample() {
        return otherDBAttribute.getTimestampFromLastSample();
    }

    @Override
    public boolean deleteAllSample() throws JEVisException {
        return otherDBAttribute.deleteAllSample();
    }

    @Override
    public boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException {
        return otherDBAttribute.deleteSamplesBetween(from, to);
    }

    @Override
    public JEVisUnit getDisplayUnit() throws JEVisException {
        //TODO: unit in cache
        return otherDBAttribute.getDisplayUnit();
    }

    @Override
    public void setDisplayUnit(JEVisUnit unit) throws JEVisException {
        otherDBAttribute.setDisplayUnit(unit);
    }

    @Override
    public JEVisUnit getInputUnit() throws JEVisException {
        //TODO: unit in cache
        return otherDBAttribute.getInputUnit();
    }

    @Override
    public void setInputUnit(JEVisUnit unit) throws JEVisException {
        otherDBAttribute.setInputUnit(unit);
    }

    @Override
    public Period getDisplaySampleRate() {
        //TODO: unit in cache
        return otherDBAttribute.getDisplaySampleRate();
    }

    @Override
    public Period getInputSampleRate() {
        return otherDBAttribute.getInputSampleRate();
    }

    @Override
    public void setInputSampleRate(Period period) {
        otherDBAttribute.setInputSampleRate(period);
    }

    @Override
    public void setDisplaySampleRate(Period period) {
        otherDBAttribute.setDisplaySampleRate(period);
    }

    @Override
    public boolean isType(JEVisType type) {
        //TODO: check also for class & make it faster my not loadying the type
        try {
            return type.equals(getType());
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public long getSampleCount() {
        return otherDBAttribute.getSampleCount();
    }

    @Override
    public List<JEVisOption> getOptions() {
        return otherDBAttribute.getOptions();
    }

    @Override
    public void addOption(JEVisOption option) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeOption(JEVisOption option) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return cache;
    }

    @Override
    public void commit() throws JEVisException {
        otherDBAttribute.commit();
        //TODO: reload from other source?!
    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        logger.trace("here we are");
        return false;
    }

    @Override
    public int compareTo(JEVisAttribute o) {
        return o.getName().compareTo(getName());
    }

}
