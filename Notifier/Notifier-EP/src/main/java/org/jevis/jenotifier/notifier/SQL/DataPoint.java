/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JENotifier-EP.
 *
 * JENotifier-EP is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JENotifier-EP is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JENotifier-EP. If not, see <http://www.gnu.org/licenses/>.
 *
 * JENotifier-EP is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jenotifier.notifier.SQL;

import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.joda.time.DateTime;

/**
 *
 * @author jb
 */
public class DataPoint {

    private Long _datapointID;
    private String _periodStart;
    private String _periodEnd;

    public static final String DATA_POINT = "JEVis ID";
    public static final String PERIOD_START = "Period Start";
    public static final String PERIOD_END = "Period End";

    public DataPoint() {
    }
    
    public DataPoint(DataPoint dp){
        _datapointID = dp.getDatapoint();
        _periodStart = dp.getPeriodStart();
        _periodEnd = dp.getPeriodEnd();
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

    /**
     * Call the function getAttribute(,) to get parameters of the notification
     * in Database and use the setter to assign the global variables. If there
     * is an IllegalArgumentException, the complex variable will be assigned
     * with null and the simple variables will not be dealed. The information of
     * the exception will also be printed.
     *
     * @param obj
     * @throws JEVisException
     */
    public void setDataPointObject(JEVisObject obj) throws JEVisException {
        try {
            setDatapoint(Long.valueOf(String.valueOf(getAttribute(obj, DATA_POINT))));
        } catch (IllegalArgumentException ex) {
            setDatapoint(null);
            Logger.getLogger(DataPoint.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setPeriodeStart(String.valueOf(getAttribute(obj, PERIOD_START)));
        } catch (IllegalArgumentException ex) {
            setPeriodeStart(null);
            Logger.getLogger(DataPoint.class.getName()).log(Level.ERROR, ex);
        }
        try {
            setPeriodeEnd(String.valueOf(getAttribute(obj, PERIOD_END)));
        } catch (IllegalArgumentException ex) {
            setPeriodeEnd(null);
            Logger.getLogger(DataPoint.class.getName()).log(Level.ERROR, ex);
        }
    }

    /**
     * To check wether the parameters are configured.
     *
     * @return
     */
    public boolean isDataPointConfigured() {
        return null != getDatapoint();
    }

    /**
     * Adjust the given time format
     *
     * @param dt
     * @return
     */
    public String timeformatAdjustment(String dt) {
        String adjustment;
        adjustment = dt.replaceAll(" ", "T");
        return adjustment;
    }

    public List<JEVisSample> selectSamples(JEVisAttribute attribute) {
        List<JEVisSample> samples;
        if (_periodStart != null && _periodEnd != null && !_periodStart.isEmpty() && !_periodEnd.isEmpty()) {
            samples = attribute.getSamples(DateTime.parse(timeformatAdjustment(_periodStart)), DateTime.parse(timeformatAdjustment(_periodEnd)));
        } else {
            samples = attribute.getAllSamples();
        }
        return samples;
    }

//    /**
//     * Call the function getConnectedOutputDatabase() to connect to the JEVis
//     * Database, and call the functions of JEAPI-SQL to fetch the samples with
//     * the given datapoints. So that the Notification will be more easily
//     * established in the SQLNotification. The List of fetched JEVis Samples
//     * will be returned.
//     *
//     * @param ds
//     * @return
//     * @throws JEVisException
//     */
//    public List<JEVisSample> fetchsamples(JEVisDataSource ds) throws JEVisException {
//        List<JEVisSample> samples = new ArrayList<JEVisSample>();
//        JEVisObject object = ds.getObject(getDatapoint());
//        JEVisAttribute value = object.getAttribute("Value");
//        List<JEVisSample> sample = selectSamples(value);
//        samples.addAll(sample);
//        return samples;
//    }

    /**
     * Set the global variable _dataPoint. It will call the function
     * splitImport(dp, ";") to get a list, which stores the data points. The
     * function isDataPointLegal will also be called to filter the data points.
     *
     * @param dp
     */
    public void setDatapoint(Long dp) {
        _datapointID = dp;
    }

    @Override
    public String toString() {
        if (_periodStart != null && !_periodStart.isEmpty() && _periodEnd != null && !_periodEnd.isEmpty()) {
            return "DataPoint[" + " ,_dataPoint" + getDatapoint() + ", _periodeStart" + _periodStart + ", _periodeEnd" + _periodEnd + "]";
        } else {
            return "DataPoint[" + " ,_dataPoint" + getDatapoint() + "allsamples" + "]";
        }
    }

    /**
     * @return the _periodeStart
     */
    public String getPeriodStart() {
        return _periodStart;
    }

    /**
     * @return the _periodeEnd
     */
    public String getPeriodEnd() {
        return _periodEnd;
    }

    /**
     * set start time of a periode
     *
     * @param periodeStart
     */
    public void setPeriodeStart(String periodeStart) {
        if (periodeStart != null && !periodeStart.isEmpty()) {
            _periodStart = periodeStart;
        }
    }

    /**
     * set an end time of a periode
     *
     * @param periodeEnd
     */
    public void setPeriodeEnd(String periodeEnd) {
        if (periodeEnd != null && !periodeEnd.isEmpty()) {
            _periodEnd = periodeEnd;
        }
    }

    /**
     * @return the _datapoint
     */
    public Long getDatapoint() {
        return _datapointID;
    }

}
