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

import java.util.Objects;
import org.joda.time.DateTime;

/**
 *
 * @author broder
 */
public class Result {

    private final Object _value;
    private final DateTime _date;
    private final Long _datapoint;
    private final String _attribute;

    public Result(Long datapoint, Object val, DateTime date) {
        _datapoint = datapoint;
        _value = val;
        _date = date;
        _attribute = "Value";//fallback
    }

    public Result(Long datapoint, String attribute, Object val, DateTime date) {
        _datapoint = datapoint;
        _value = val;
        _date = date;
        _attribute = attribute;//fallback
    }

    public Object getValue() {
        return _value;
    }

    public DateTime getDate() {
        return _date;
    }

    public Long getOnlineID() {
        return _datapoint;
    }

    public String getAttribute() {
        return _attribute;
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
        hash = 67 * hash + Objects.hashCode(this._value);
        hash = 67 * hash + Objects.hashCode(this._date);
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
        if (!Objects.equals(this._value, other._value)) {
            return false;
        }
        if (!Objects.equals(this._date, other._date)) {
            return false;
        }
        return true;
    }

}
