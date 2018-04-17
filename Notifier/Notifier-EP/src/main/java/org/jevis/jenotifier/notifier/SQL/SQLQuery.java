/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jenotifier.notifier.SQL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;

/**
 *
 * @author jb
 */
public class SQLQuery {

    private String _table;
    private String _sqlQuery;
    private Long _positionValue;
    private String _typeValue;
    private String _columnValue;
    private Long _positionTimeStamp;
    private String _typeTimeStamp;
    private String _columnTimeStamp;
    private List<DataPoint> _datapoints = new ArrayList<DataPoint>();

    public static final String SQL_TABLE = "SQL Table";
    public static final String SQL_QUERY = "SQL Query";
    public static final String Position_Value = "Position In Query";
    public static final String Type_Value = "Value Type";
    public static final String Column_Value = "Column Value";
    public static final String Position_TimeStamp = "Position In Query";
    public static final String Type_TimeStamp = "TimeStamp Type";
    public static final String Format_TimeStamp = "TimeStamp Format";
    public static final String Column_TimeStamp = "Column TimeStamp";

    public SQLQuery(SQLQuery sqlquery) {
        _datapoints = sqlquery.getDatapoints();
        _sqlQuery = sqlquery.getSqlQuery();
        _table = sqlquery.getTable();
        _positionValue = sqlquery.getPositionValue();
        _columnValue = sqlquery.getColumnValue();
        _typeValue = sqlquery.getTypeValue();
        _positionTimeStamp = sqlquery.getPositionTimeStamp();
        _typeTimeStamp = sqlquery.getTypeTimeStamp();
        _columnTimeStamp = sqlquery.getColumnTimeStamp();
    }

    public SQLQuery() {
    }

    public void setSQLQueryObject(JEVisObject obj) throws JEVisException {
        try {
            setTable(String.valueOf(getAttribute(obj, SQL_TABLE)));
        } catch (Exception ex) {
            setTable(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setSqlQuery(String.valueOf(getAttribute(obj, SQL_QUERY)));
        } catch (Exception ex) {
            setSqlQuery(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        List<JEVisObject> children = obj.getChildren();
        if (children != null && !children.isEmpty()) {
            for (JEVisObject child : children) {
                if (child != null) {
                    String name = child.getJEVisClass().getName();
                    if (name.equalsIgnoreCase("SQL Link Directory")) {
                        List<JEVisObject> datapoints = child.getChildren();
                        if (datapoints != null && !datapoints.isEmpty()) {
                            for (JEVisObject dp : datapoints) {
                                if (dp != null) {
                                    DataPoint datapoint = new DataPoint();
                                    datapoint.setDataPointObject(dp);
                                    _datapoints.add(datapoint);
                                } else {
                                    Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, dp + "is null .");
                                }
                            }
                        } else {
                            Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, _datapoints + "is null or empty.");
                        }
                    } else if (name.equalsIgnoreCase("Variable Directory")) {
                        List<JEVisObject> variables = child.getChildren();
                        setVariablesObject(variables);
                    } else {
                        Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, child + "is illegal.");
                    }
                } else {
                    Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, child + "is null.");
                }
            }
        } else {
            Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, children + "is null or empty .");
        }

    }

    public void setVariablesObject(List<JEVisObject> objs) throws JEVisException {
        if (objs != null && !objs.isEmpty()) {
            for (JEVisObject obj : objs) {
                if (obj != null) {
                    String name = obj.getJEVisClass().getName();
                    if (name.equalsIgnoreCase("Value")) {
                        setValueObject(obj);
                    } else if (name.equalsIgnoreCase("Time Stamp")) {
                        setTimeStampObject(obj);
                    } else {
                        Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, obj + "is illegal.");
                    }
                } else {
                    Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, obj + "is null or empty .");
                }
            }
        } else {
            Logger.getLogger(SQLQuery.class.getName()).log(Level.INFO, objs + "is null or empty .");
        }
    }

    /**
     * Call the function getAttribute to get parameters of the Value in Database
     * and use the setter to assign the global variables. If there is an
     * IllegalArgumentException, the complex variable will be assigned with null
     * and the simple variables will not be dealed. The information of the
     * exception will also be printed.
     *
     * @param obj
     * @throws JEVisException
     */
    public void setValueObject(JEVisObject obj) throws JEVisException {
        try {
            setPositionValue(Long.valueOf(String.valueOf(getAttribute(obj, Position_Value))));
        } catch (Exception ex) {
            setPositionValue(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setTypeValue(String.valueOf(getAttribute(obj, Type_Value)));
        } catch (Exception ex) {
            setTypeValue(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setColumnValue(String.valueOf(getAttribute(obj, Column_Value)));
        } catch (Exception ex) {
            setColumnValue(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
    }

    /**
     * Call the function getAttribute to get parameters of the TimeStamp in
     * Database and use the setter to assign the global variables. If there is
     * an IllegalArgumentException, the complex variable will be assigned with
     * null and the simple variables will not be dealed. The information of the
     * exception will also be printed.
     *
     * @param obj
     * @throws JEVisException
     */
    public void setTimeStampObject(JEVisObject obj) throws JEVisException {
        try {
            setPositionTimeStamp(Long.valueOf(String.valueOf(getAttribute(obj, Position_TimeStamp))));
        } catch (Exception ex) {
            setPositionTimeStamp(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setTypeTimeStamp(String.valueOf(getAttribute(obj, Type_TimeStamp)));
        } catch (Exception ex) {
            setTypeTimeStamp(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setColumnTimeStamp(String.valueOf(getAttribute(obj, Column_TimeStamp)));
        } catch (Exception ex) {
            setColumnTimeStamp(null);
            Logger.getLogger(SQLQuery.class.getName()).log(Level.ERROR, ex);
        }

    }

    /**
     * To get the value of the attribute of a JEVisObject.
     *
     * @param obj the JEVis Object
     * @param attName the name of the attribute
     * @return the value of the attribute
     * @throws JEVisException
     */
    private Object getAttribute(JEVisObject obj, String attName) throws JEVisException {
        JEVisAttribute att = obj.getAttribute(attName);
        if (att != null) { //check, if the attribute exists.
            if (att.hasSample()) { //check, if this attribute has values.
                JEVisSample sample = att.getLatestSample();
                if (sample.getValue() != null) { //check, if the value of this attribute is null.
                    return sample.getValue();
                } else {
                    throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
                }
            } else {
                throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute'value is not filled: " + attName);
            }
        } else {
            throw new IllegalArgumentException(obj.getName() + " " + obj.getID() + " Attribute is missing: " + attName);
        }
    }

    public List<JEVisSample> fetchSamples(JEVisDataSource ds) throws JEVisException {
        List<JEVisSample> samples = new ArrayList<JEVisSample>();
        for (DataPoint dp : _datapoints) {
            JEVisObject object = ds.getObject(dp.getDatapoint());
            JEVisAttribute value = object.getAttribute("Value");
            List<JEVisSample> sample = dp.selectSamples(value);
            samples.addAll(sample);
        }
        return samples;
    }

    public boolean isDataPointsConfigured() {
        boolean is = true;
        for (DataPoint dp : _datapoints) {
            is = is && dp.isDataPointConfigured();
        }
        return is;
    }

    @Override
    public String toString() {
        return "SQLNotification{" + getDatapoints().toString() + ", _sqlQuery" + _sqlQuery + ", _table" + _table + ", " + "Value[" + "_columnValue" + _columnValue + ", _type" + _typeValue + ", _position" + _positionValue + "]" + ", " + "TimeStamp[" + "_columnTimeStamp" + _columnTimeStamp + ", _type" + _typeTimeStamp + ", _position" + _positionTimeStamp
                + "]" + "}";
    }

    public boolean isSQLQueryConfigured() {
        boolean isconfigured;
        isconfigured = (isDataPointsConfigured() && isQueryConfigured() && (isTimeStampConfigured() || isValueConfigured()));
        return isconfigured;
    }

    public boolean isQueryConfigured() {
        boolean isconfigured;
        isconfigured = (_sqlQuery != null && !_sqlQuery.isEmpty() && _table != null && !_table.isEmpty());
        return isconfigured;
    }

    /**
     * To check wether the parameters are configured.
     *
     * @return
     */
    public boolean isValueConfigured() {
        if (_columnValue != null && !_columnValue.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check wether the necesssary paramters of TimeStamp are filled. Return
     * true when they are given. And false when they are not given.
     *
     * @return
     */
    public boolean isTimeStampConfigured() {
        if (_columnTimeStamp != null && !_columnTimeStamp.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

//    /**
//     * Handle the given SQL Query. Replace myID, myTbale, myValue of name of
//     * table, name of the columns, which storages value and timestamp.
//     *
//     * @param sqlquery
//     * @return the handled SQLQuery
//     */
//    public String handleSQLQuery(JEVisObject sqlquery) {
//        String string;
//        try {
//            string = sqlquery.getAttribute("SQL Query").getLatestSample().getValueAsString();
//            string = string.replaceAll("Table", sqlquery.getAttribute("SQL Table").getLatestSample().getValueAsString());
//            List<JEVisObject> children = sqlquery.getChildren();
//            for (JEVisObject child : children) {
//                String name = child.getJEVisClass().getName();
//                if (name.equalsIgnoreCase("Variable Directory")) {
//                    List<JEVisObject> variables = child.getChildren();
//                    for (JEVisObject var : variables) {
//                        String varname = var.getJEVisClass().getName();
//                        if (varname.equalsIgnoreCase("Value")) {
//                            if (string.contains("Value")) {
//                                if (_columnValue != null && !_columnValue.isEmpty()) {
//                                    string = string.replaceAll("Value", var.getAttribute("Column Value").getLatestSample().getValueAsString());
//                                }
//                            }
//                        } else if (varname.equalsIgnoreCase("Timee Stamp")) {
//                            if (string.contains("TimeStamp")) {
//                                if (_columnTimeStamp != null && !_columnTimeStamp.isEmpty()) {
//                                    string = string.replaceAll("TimeStamp", _columnTimeStamp);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (JEVisException ex) {
//            java.util.logging.Logger.getLogger(SQLNotification.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//            string = null;
//        }
//        return string;
//    }
    /**
     * Handle the given SQL Query. Replace myID, myTbale, myValue of name of
     * table, name of the columns, which storages value and timestamp.
     *
     * @return the handled SQLQuery
     */
    public String handleSQLQuery() {
        String string = getSqlQuery();
        string = string.replaceAll("Table", _table);
        if (string.contains("Value")) {
            if (_columnValue != null && !_columnValue.isEmpty()) {
                string = string.replaceAll("Value", _columnValue);
            }
        }
        if (string.contains("TimeStamp")) {
            if (_columnTimeStamp != null && !_columnTimeStamp.isEmpty()) {
                string = string.replaceAll("TimeStamp", _columnTimeStamp);
            }
        }

        return string;
    }

    public PreparedStatement setValueInprepareStatement(PreparedStatement pre, JEVisSample sam) throws JEVisException, SQLException {
        String compare = _typeValue.toLowerCase();
        String value = sam.getValue().toString();
        if (_positionValue != null) {
            int position = _positionValue.intValue();
            if (compare.equalsIgnoreCase("double")) {
                pre.setDouble(position, Double.valueOf(value));
            } else if (compare.equalsIgnoreCase("float")) {
                pre.setFloat(position, Float.valueOf(value));
            } else if (compare.equalsIgnoreCase("long")) {
                pre.setLong(position, Long.valueOf(value));
            } else if (compare.equalsIgnoreCase("int")) {
                pre.setInt(position, Integer.valueOf(value));
            } else {
                pre.setString(position, value);
            }
        }
        return pre;
    }

    public PreparedStatement setTimestampInprepareStatement(PreparedStatement pre, JEVisSample sam) throws JEVisException, SQLException {
        if (_positionTimeStamp != null) {
            int position = _positionTimeStamp.intValue();
//        if (_formatTimeStamp != null) {
//            String timestamp = sam.getTimestamp().toString(_formatTimeStamp);
            if (_typeTimeStamp != null && !_typeTimeStamp.isEmpty()) {
                if (_typeTimeStamp.toLowerCase().equalsIgnoreCase("date")) {
                    pre.setDate(position, Date.valueOf(sam.getTimestamp().toString("yyyy-MM-dd")));
                } else if (_typeTimeStamp.toLowerCase().equalsIgnoreCase("time")) {
                    pre.setTime(position, Time.valueOf(sam.getTimestamp().toString("hh:mm:ss")));
                } else if (_typeTimeStamp.toLowerCase().equalsIgnoreCase("datetime")) {
                    pre.setString(position, sam.getTimestamp().toString("yyyy-MM-dd hh:mm:ss"));
                } else if (_typeTimeStamp.toLowerCase().equalsIgnoreCase("string")) {
                    pre.setString(position, sam.getTimestamp().toString());
                } else if (_typeTimeStamp.toLowerCase().equalsIgnoreCase("timestamp")) {
                    pre.setString(position, sam.getTimestamp().toString("yyyy-MM-d'T'hh:mm:ss.SS"));
                } else {
                    pre.setString(position, sam.getTimestamp().toString());
                }
//        } else {
//            pre.setString(position, String.valueOf(sam.getTimestamp().toString()));
//        }
            } else {
                pre.setString(position, String.valueOf(sam.getTimestamp().toString()));
            }
        }
        return pre;
    }

    /**
     * @return the _datapoints
     */
    public List<DataPoint> getDatapoints() {
        return _datapoints;
    }

    /**
     * @param datapoints
     */
    public void setDatapoints(List<DataPoint> datapoints) {
        _datapoints = datapoints;
    }

    /**
     * To set the global variable _table. If the param is null or "", _table
     * remains null.
     *
     * @param table
     */
    public void setTable(String table) {
        if (table != null && !table.isEmpty()) {
            _table = table;
        }
    }

    /**
     * To set the global variable _sqlQuery. If the param is null or "",
     * _sqlQuery remains null.
     *
     * @param sqlQuery
     */
    public void setSqlQuery(String sqlQuery) {
        if (sqlQuery != null && !sqlQuery.isEmpty()) {
            _sqlQuery = sqlQuery;
        }
    }

    /**
     * @return the _table
     */
    public String getTable() {
        return _table;
    }

    /**
     * @return the _sqlQuery
     */
    public String getSqlQuery() {
        return _sqlQuery;
    }

    /**
     * @return the _position
     */
    public Long getPositionValue() {
        return _positionValue;
    }

    /**
     * @return the _type
     */
    public String getTypeValue() {
        return _typeValue;
    }

    /**
     * @return the _columnValue
     */
    public String getColumnValue() {
        return _columnValue;
    }

    /**
     * @param position
     */
    public void setPositionValue(Long position) {
        _positionValue = position;
    }

    /**
     * @param type
     */
    public void setTypeValue(String type) {
        _typeValue = type;
    }

    /**
     * @param columnValue
     */
    public void setColumnValue(String columnValue) {
        _columnValue = columnValue;
    }

    /**
     * @return the _position
     */
    public Long getPositionTimeStamp() {
        return _positionTimeStamp;
    }

    /**
     * @return the _type
     */
    public String getTypeTimeStamp() {
        return _typeTimeStamp;
    }

    /**
     * @param position
     */
    public void setPositionTimeStamp(Long position) {
        _positionTimeStamp = position;
    }

    /**
     * @param type
     */
    public void setTypeTimeStamp(String type) {
        _typeTimeStamp = type;

    }

    /**
     * @return the _columnTimeStamp
     */
    public String getColumnTimeStamp() {
        return _columnTimeStamp;
    }

    /**
     * @param columnTimeStamp
     */
    public void setColumnTimeStamp(String columnTimeStamp) {
        _columnTimeStamp = columnTimeStamp;
    }

}
