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
import org.jevis.commons.unit.JEVisUnitImp;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class VirtualAttribute implements JEVisAttribute {

    private final JEVisObject object;
    private final List<JEVisSample> sampleList = new ArrayList<>();
    private final String name;

    public VirtualAttribute(JEVisObject object, String name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisType getType() throws JEVisException {
        return null;
    }

    @Override
    public JEVisObject getObject() {
        return object;
    }

    @Override
    public List<JEVisSample> getAllSamples() {
        return sampleList;
    }

    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to, boolean customWorkDay, String aggregationPeriod, String manipulationMode, String timeZone) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int addSamples(List<JEVisSample> samples) throws JEVisException {
        int prevSize = sampleList.size();
        samples.stream().filter(sample -> !sampleList.contains(sample)).forEach(sampleList::add);
        samples.sort((o1, o2) -> {
            try {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            return 0;
        });
        return sampleList.size() - prevSize;
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
        JEVisSample newSample = new VirtualSample(ts, value.toString());
        sampleList.add(newSample);
        return newSample;
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisSample getLatestSample() {
        if (!sampleList.isEmpty()) {
            return sampleList.get(sampleList.size() - 1);
        } else return null;
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasSample() {
        return sampleList.size() > 0;
    }

    @Override
    public DateTime getTimestampFromFirstSample() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DateTime getTimestampFromLastSample() {
        if (!sampleList.isEmpty()) {
            try {
                sampleList.get(sampleList.size() - 1).getTimestamp();
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public boolean deleteAllSample() throws JEVisException {
        sampleList.clear();
        return true;
    }

    @Override
    public boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisUnit getDisplayUnit() throws JEVisException {
        return new JEVisUnitImp(Unit.ONE);
    }

    @Override
    public void setDisplayUnit(JEVisUnit unit) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisUnit getInputUnit() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInputUnit(JEVisUnit unit) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Period getDisplaySampleRate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Period getInputSampleRate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setInputSampleRate(Period period) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDisplaySampleRate(Period period) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isType(JEVisType type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getSampleCount() {
        return sampleList.size();
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commit() throws JEVisException {

    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(JEVisAttribute o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisOption> getOptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public Long getObjectID() {
        return object.getID();
    }

}
