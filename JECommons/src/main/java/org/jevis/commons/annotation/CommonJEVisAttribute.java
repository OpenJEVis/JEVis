/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.annotation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author Florian Simon
 */
public class CommonJEVisAttribute implements JEVisAttribute {

    private JEVisAttribute attribute;

    public CommonJEVisAttribute(JEVisAttribute att) {
        attribute = att;
    }

    public CommonJEVisAttribute(JEVisObject object) {

        for (Annotation ano : CommonJEVisAttribute.class.getAnnotations()) {
            System.out.println("ano.string: " + ano.toString());
            System.out.println("ano.type: " + ano.annotationType());

        }

        if (CommonJEVisAttribute.class.isAnnotationPresent(JEVisAttributeResource.class)) {
            System.out.println("isAnnotationPresent: " + true);
            String typeName = CommonJEVisAttribute.class.getAnnotation(JEVisAttributeResource.class).type();
            try {
                this.attribute = object.getAttribute(typeName);
                System.out.println("SUCCESS!!!!!!!!!!");
            } catch (JEVisException ex) {
                Logger.getLogger(CommonJEVisAttribute.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("isAnnotationPresent: " + false);
        }
    }

    @Override
    public String getName() {
        return attribute.getName();
    }

    @Override
    public boolean delete() {
        return attribute.delete();
    }

    @Override
    public JEVisType getType() throws JEVisException {
        return attribute.getType();
    }

    @Override
    public JEVisObject getObject() {
        return attribute.getObject();
    }

    @Override
    public List<JEVisSample> getAllSamples() {
        return attribute.getAllSamples();
    }

    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        return attribute.getSamples(from, to);
    }

    @Override
    public int addSamples(List<JEVisSample> samples) throws JEVisException {
        return attribute.addSamples(samples);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
        return attribute.buildSample(ts, value);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
        return attribute.buildSample(ts, value, unit);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException {
        return attribute.buildSample(ts, value, note);
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException {
        return attribute.buildSample(ts, value, note, unit);
    }

    @Override
    public JEVisSample getLatestSample() {
        return attribute.getLatestSample();
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return attribute.getPrimitiveType();
    }

    @Override
    public boolean hasSample() {
        return attribute.hasSample();
    }

    @Override
    public DateTime getTimestampFromFirstSample() {
        return attribute.getTimestampFromFirstSample();
    }

    @Override
    public DateTime getTimestampFromLastSample() {
        return attribute.getTimestampFromLastSample();
    }

    @Override
    public boolean deleteAllSample() throws JEVisException {
        return attribute.deleteAllSample();
    }

    @Override
    public boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException {
        return attribute.deleteSamplesBetween(from, to);
    }

    @Override
    public JEVisUnit getDisplayUnit() throws JEVisException {
        return attribute.getDisplayUnit();
    }

    @Override
    public void setDisplayUnit(JEVisUnit unit) throws JEVisException {
        attribute.setDisplayUnit(unit);
    }

    @Override
    public JEVisUnit getInputUnit() throws JEVisException {
        return attribute.getInputUnit();
    }

    @Override
    public void setInputUnit(JEVisUnit unit) throws JEVisException {
        attribute.setInputUnit(unit);
    }

    @Override
    public Period getDisplaySampleRate() {
        return attribute.getDisplaySampleRate();
    }

    @Override
    public Period getInputSampleRate() {
        return attribute.getInputSampleRate();
    }

    @Override
    public void setInputSampleRate(Period period) {
        attribute.setInputSampleRate(period);
    }

    @Override
    public void setDisplaySampleRate(Period period) {
        attribute.setDisplaySampleRate(period);
    }

    @Override
    public boolean isType(JEVisType type) {
        return attribute.isType(type);
    }

    @Override
    public long getSampleCount() {
        return attribute.getSampleCount();
    }

    @Override
    public List<JEVisOption> getOptions() {
        return attribute.getOptions();
    }

    @Override
    public void addOption(JEVisOption option) {
        attribute.addOption(option);
    }

    @Override
    public void removeOption(JEVisOption option) {
        attribute.removeOption(option);
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return attribute.getDataSource();
    }

    @Override
    public void commit() throws JEVisException {
        attribute.commit();
    }

    @Override
    public void rollBack() throws JEVisException {
        attribute.rollBack();
    }

    @Override
    public boolean hasChanged() {
        return attribute.hasChanged();
    }

    @Override
    public int compareTo(JEVisAttribute o) {
        return attribute.compareTo(o);
    }

    @Override
    public Long getObjectID() {
        return getObject().getID();
    }

}
