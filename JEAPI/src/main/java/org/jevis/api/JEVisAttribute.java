/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI.
 *
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * The JEVisAttribute is the basis of a JEVisType. A JEVisAttribute is always
 * unique under a JEVisObject. The attribute will be configured by its
 * JEVisType.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisAttribute extends JEVisComponent, JEVisCommittable, Comparable<JEVisAttribute> {

    /**
     * Get the Name of the attribute. The name is unique under this JEVisObject
     *
     * @return
     */
    String getName();

    /**
     * Delete this object and remove all references to it.
     *
     * @deprecated
     * @return
     */
    boolean delete();

    /**
     * Get the JEVisType of this attribute
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisType getType() throws JEVisException;

    /**
     * Get the JEVisObject this attribute belongs to
     *
     * @return
     */
    JEVisObject getObject();

    /**
     * Get the Object ID
     *
     * @return
     */
    Long getObjectID();

    /**
     * Get all samples the attribute may hold
     *
     * @return
     */
    List<JEVisSample> getAllSamples();

    /**
     * Get all samples from ">=" to "<=" a certain date
     *
     * @param from (>=)
     * @param to (<=)
     *
     * @return
     */
    List<JEVisSample> getSamples(DateTime from, DateTime to);

    /**
     * Add and commit all samples
     *
     * @param samples
     * @return
     * @throws JEVisException
     */
    int addSamples(List<JEVisSample> samples) throws JEVisException;

    /**
     * Create a new JEViSample for this attribute with the input unit.
     *
     * @param ts Timestamp of the sample, null if now()
     * @param value
     * @return
     * @throws JEVisException
     */
    JEVisSample buildSample(DateTime ts, Object value) throws JEVisException;

    /**
     * Create a new JEViSample for this attribute in the given unit.
     *
     * @param ts of the sample, null if now()
     * @param value
     * @param unit
     * @return
     * @throws JEVisException
     */
    JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException;

    /**
     * Create an new JEViSample for this attribute with a note.
     *
     * @param ts of the sample, null if now()
     * @param value
     * @param note
     * @return
     * @throws JEVisException
     */
    JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException;

    /**
     * Create an new JEViSample for this attribute with a note in the the given
     * unit.
     *
     * @param ts of the sample, null if now()
     * @param value
     * @param note
     * @param unit
     * @return
     * @throws JEVisException
     */
    JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException;

    /**
     * Get the latest sample by date
     *
     * @return
     */
    JEVisSample getLatestSample();

    /**
     * Get the primitive type of the samples
     *
     * @throws org.jevis.api.JEVisException
     * @see org.jevis.jeapi.JEVisConstants.PrimitiveType
     *
     * @return
     */
    int getPrimitiveType() throws JEVisException;

    /**
     * Returns true if the attribute holds any samples
     *
     * @return
     */
    boolean hasSample();

    /**
     * Get the timestamp from the first sample of the attribute
     *
     * @return
     */
    DateTime getTimestampFromFirstSample();

    /**
     * Get the last timestamp of the attribute
     *
     * @return
     */
    DateTime getTimestampFromLastSample();

    /**
     * Delete all samples this attribute may holds
     *
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean deleteAllSample() throws JEVisException;

    /**
     * Deletes all samples from ">=" to "<=" a certain date
     *
     * @param from (>=)
     * @param to (<=)
     * @return
     * @throws org.jevis.api.JEVisException
     */
    boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException;

    /**
     * Returns the displayed unit of this attribute.
     *
     * @return
     * @throws JEVisException
     */
    JEVisUnit getDisplayUnit() throws JEVisException;

    /**
     * Set the displayed unit of this attribute.
     *
     * @param unit
     * @throws JEVisException
     */
    void setDisplayUnit(JEVisUnit unit) throws JEVisException;

    /**
     * Returns the unit in which the data is stored in the datasource
     *
     * @return @throws JEVisException
     */
    JEVisUnit getInputUnit() throws JEVisException;

    /**
     * Set the Unit in which the data will be stored in the data-source
     *
     * @param unit
     * @throws JEVisException
     */
    void setInputUnit(JEVisUnit unit) throws JEVisException;

    /**
     * Returns the default sample rate for the end-user representation
     *
     * @return
     */
    Period getDisplaySampleRate();

    /**
     * returns the sample rate in which the data is stored in the data-source
     *
     * @return
     */
    Period getInputSampleRate();

    /**
     * Set the sample rate for in which the data is stored in the data-source
     *
     * @param period
     */
    void setInputSampleRate(Period period);

    /**
     * default sample rate for the end-user representation
     *
     * @param period
     */
    void setDisplaySampleRate(Period period);

    /**
     * Check if the attribute is from the given JEVisType
     *
     * @param type the type to check for
     * @return
     */
    boolean isType(JEVisType type);

    /**
     * Get the count of all samples allocated to this attribute
     *
     * @return
     */
    long getSampleCount();

    /**
     * Get all additonal options for this attribute.
     *
     * @return list of all options
     */
    List<JEVisOption> getOptions();

    /**
     * Add an new option to this attribute. Will overwrite an existion option
     * wthe the same name
     *
     * @deprecated
     * @param option
     */
    void addOption(JEVisOption option);

    /**
     * Remove an option from this attribute.
     *
     * @deprecated
     * @param option
     */
    void removeOption(JEVisOption option);

    
    
}
