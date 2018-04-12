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

import org.joda.time.DateTime;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisSample extends JEVisComponent, JEVisCommittable {

    /**
     * Returns the sample's timestamp
     *
     * @return JevCalendar timestamp
     * @throws org.jevis.api.JEVisException
     */
    DateTime getTimestamp() throws JEVisException;

    /**
     * Returns the sample's value
     *
     * @return value of generic type T
     * @throws org.jevis.api.JEVisException
     */
    Object getValue() throws JEVisException;

    /**
     * Returns a String representation of the value.
     *
     * @return
     * @throws JEVisException
     */
    String getValueAsString() throws JEVisException;

    /**
     * Returns a long representation of the value.
     *
     * @return
     * @throws JEVisException
     */
    Long getValueAsLong() throws JEVisException;

    /**
     * Returns the value converted to the given unit.
     *
     * @param unit
     * @return
     * @throws JEVisException
     */
    Long getValueAsLong(JEVisUnit unit) throws JEVisException;

    /**
     * Returns a double representation of the value.
     *
     * @return
     * @throws JEVisException
     */
    Double getValueAsDouble() throws JEVisException;

    /**
     * Returns the value converted to the given unit.
     *
     * @param unit
     * @return
     * @throws JEVisException
     */
    Double getValueAsDouble(JEVisUnit unit) throws JEVisException;

    /**
     * Returns a boolean representation of the value.
     *
     * @return
     * @throws JEVisException
     */
    Boolean getValueAsBoolean() throws JEVisException;

    /**
     * Returns a JEVisFile representation of this value.
     *
     * @return
     * @throws JEVisException
     */
    JEVisFile getValueAsFile() throws JEVisException;

    /**
     * Returns a JEVisSelection representation of this value.
     *
     * @return
     * @throws JEVisException
     */
    JEVisSelection getValueAsSelection() throws JEVisException;

    /**
     * Returns a JEVisMultiSelection representation of this sample.
     *
     * @return
     * @throws JEVisException
     */
    JEVisMultiSelection getValueAsMultiSelection() throws JEVisException;

    /**
     * Set the value of this sample. The value has to be in the default unit of
     * the attribute
     *
     * @param value can be()
     * @throws org.jevis.api.JEVisException
     * @throws ClassCastException
     */
    void setValue(Object value) throws JEVisException, ClassCastException;

    /**
     * Set the value of this sample in the given unit. JEVisSample will try
     * to convert the value from the given unit to the set value for storage.
     *
     * @param value The value to set this sample to
     * @param unit The unit of the given value
     * @throws org.jevis.api.JEVisException
     */
    void setValue(Object value, JEVisUnit unit) throws JEVisException, ClassCastException;

    /**
     * Get the attribute for this sample.
     * 
     * @return
     * @throws org.jevis.api.JEVisException
     */
    JEVisAttribute getAttribute() throws JEVisException;

    /**
     * Get the human readable note for this sample.
     *
     * @return The human readable note for this sample.
     * @throws org.jevis.api.JEVisException
     */
    String getNote() throws JEVisException;

    /**
     * Set the human readable note for this sample
     *
     * @param note
     * @throws org.jevis.api.JEVisException
     */
    void setNote(String note) throws JEVisException;

    /**
     * Get the unit of sample
     *
     * @return
     * @throws JEVisException
     */
    public JEVisUnit getUnit() throws JEVisException;
}
