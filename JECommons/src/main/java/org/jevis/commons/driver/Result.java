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
package org.jevis.commons.driver;

import org.joda.time.DateTime;

import java.util.Objects;

/**
 * @author broder
 */
public class Result {

    private final Object value;
    private final DateTime date;
    private final String targetString;
    private final String attribute;

    public Result(String datapoint, Object val, DateTime date) {
        targetString = datapoint;
        value = val;
        this.date = date;
        attribute = "Value";//fallback
    }

    public Result(String datapoint, String attribute, Object val, DateTime date) {
        targetString = datapoint;
        value = val;
        this.date = date;
        this.attribute = attribute;//fallback
    }

    public Object getValue() {
        return value;
    }

    public DateTime getDate() {
        return date;
    }

    public String getTargetStr() {
        return targetString;
    }

    public String getAttribute() {
        return attribute;
    }


//    /**
//     * 
//     * @param obj
//     * @return 
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (obj instanceof Result){
//            return false;
//        }
//        final Result other = (Result) obj;
//        
//        if(this._value.equals(obj))
//        return true;
//    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.value);
        hash = 67 * hash + Objects.hashCode(this.date);
        return hash;
    }

    /**
     * Returns true if the value and timestamp in results are equivalent, otherwise - false.
     * Do not check the datapoint and the attribute fieldss!
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Result other = (Result) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.date, other.date);
    }

}
