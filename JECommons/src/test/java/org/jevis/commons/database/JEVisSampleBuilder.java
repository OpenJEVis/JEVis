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
package org.jevis.commons.database;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

/**
 *
 * @author broder
 */
public class JEVisSampleBuilder {

    public String unit;
    public Boolean booleanVal;
    public String stringVal;
    public Double doubleVal;
    public Long longVal;
    public DateTime timestamp;

    public JEVisSampleBuilder withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public JEVisSampleBuilder withBooleanValue(Boolean value) {
        this.booleanVal = value;
        return this;
    }

    public JEVisSampleBuilder withStringValue(String value) {
        this.stringVal = value;
        return this;
    }

    public JEVisSampleBuilder withDoubleValue(Double value) {
        this.doubleVal = value;
        return this;
    }

    public JEVisSampleBuilder withLongValue(Long value) {
        this.longVal = value;
        return this;
    }

    public JEVisSampleBuilder withTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public JEVisSample build() {
        JEVisSample sample = Mockito.mock(JEVisSample.class);
        try {
            when(sample.getTimestamp()).thenReturn(timestamp);
            if (booleanVal != null) {
                when(sample.getValue()).thenReturn(booleanVal);
                when(sample.getValueAsBoolean()).thenReturn(booleanVal);
            }
            if (stringVal != null) {
                when(sample.getValue()).thenReturn(stringVal);
                when(sample.getValueAsString()).thenReturn(stringVal);
            }
            if (doubleVal != null) {
                when(sample.getValue()).thenReturn(doubleVal);
                when(sample.getValueAsDouble()).thenReturn(doubleVal);
            }
            if (longVal != null) {
                when(sample.getValue()).thenReturn(longVal);
                when(sample.getValueAsLong()).thenReturn(longVal);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisSampleBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sample;
    }
}
