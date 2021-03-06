/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.utils.PrettyError;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author fs
 */
public class JEVisSampleWS implements JEVisSample {

    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
    public static final NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private static final Logger logger = LogManager.getLogger(JEVisSampleWS.class);
    private JEVisAttribute attribute;
    private JsonSample json;
    private JEVisDataSourceWS ds;
    private JEVisFile file = null;
    private Object valueObj = null;
    private DateTime tsObj = null;

    public JEVisSampleWS(JEVisDataSourceWS ds, JsonSample json, JEVisAttribute att) {
        this.attribute = att;
        this.ds = ds;
        this.json = json;
        objectifyValue();
        objectifyTimeStamp();
    }

    public JEVisSampleWS(JEVisDataSourceWS ds, JsonSample json, JEVisAttribute att, JEVisFile file) {
        this.attribute = att;
        this.ds = ds;
        this.json = json;
        this.file = file;
        try {
            setValue(file.getFilename());
        } catch (Exception ex) {
            logger.catching(ex);
        }

    }

    private void objectifyTimeStamp() {
        tsObj = sampleDTF.parseDateTime(json.getTs());
    }

    private void objectifyValue() {
        try {
            valueObj = null;
            if (json.getValue() != null) {
                if (attribute.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                    valueObj = new Double(json.getValue());
                    return;
                } else if (attribute.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                    valueObj = new Long(json.getValue());
                    return;
                } else if (attribute.getType().getPrimitiveType() == JEVisConstants.PrimitiveType.BOOLEAN) {
                    valueObj = getValueAsBoolean();
                    return;
                }
            } else {
                valueObj = null;
            }

        } catch (Exception ex) {
            logger.error("Error while casting Attribute Type: '{}' in {}", PrettyError.getJEVisLineFilter(ex), json);
            valueObj = null;
        }
        valueObj = json.getValue();

    }

    @Override
    public DateTime getTimestamp() {
        return tsObj;
    }

    @Override
    public Object getValue() {
        if (valueObj != null) {
            return valueObj;
        }

        //fallback to String
        return json.getValue();
    }

    private boolean validateValue(Object value) throws ClassCastException {
        try {
            if (getAttribute().getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                Double.valueOf(value.toString());
            } else if (getAttribute().getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                Long.valueOf(value.toString());
            }
        } catch (Exception ex) {
//            throw new ClassCastException("Value object does not match the PrimitiveType of the Attribute: " + this.toString());
            return false;
        }
        return true;
    }

    @Override
    public void setValue(Object value) throws ClassCastException {
        //logger.debug("setValue: {} Value: {}", getAttribute().getName(), value);
        try {
            //TODO validateValue(value)
            valueObj = value;
            if (value == null) {
                json.setValue("");
            } else {
                json.setValue(value.toString());
            }


        } catch (Exception ex) {
            throw new ClassCastException("Value object does not match the PrimitiveType of the Attribute: " + this.toString());
        }
    }

    @Override
    public Long getValueAsLong() {
        try {
            return valueObj instanceof Long ? (Long) valueObj : nf.parse(getValueAsString()).longValue();
        } catch (Exception ex) {
            return 0l;
        }
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        double lValue = getValueAsLong().doubleValue();
        Double dValue = getUnit().convertTo(unit, lValue);
        return dValue.longValue();
    }

    @Override
    public Double getValueAsDouble() {
        return valueObj instanceof Double ? (Double) valueObj : Double.parseDouble(getValueAsString());
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        return getUnit().convertTo(unit, getValueAsDouble());
    }

    @Override
    public Boolean getValueAsBoolean() {
        if (valueObj instanceof Boolean) {
            return (Boolean) valueObj;
        } else {
            if (json.getValue().equals("1")) {
                return true;
            } else if (json.getValue().equals("0")) {
                return false;
            }

            return Boolean.parseBoolean(getValueAsString());
        }
    }

    @Override
    public String getValueAsString() {
        return json.getValue();
    }

    @Override
    public JEVisFile getValueAsFile() {

        if (file != null && file.getBytes() != null) {
            return file;
        } else {
            try {
                String resource = REQUEST.API_PATH_V1
                        + REQUEST.OBJECTS.PATH
                        + getAttribute().getObjectID() + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.PATH
                        + getAttribute().getName() + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH
                        + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.FILES.PATH
                        + HTTPConnection.FMT.print(getTimestamp());

                byte[] response = ds.getHTTPConnection().getByteRequest(resource);

                file = new JEVisFileImp(getValueAsString(), response);
                return file;

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

    }

    @Override
    public JEVisSelection getValueAsSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisMultiSelection getValueAsMultiSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(Object value, JEVisUnit unit) throws JEVisException, ClassCastException {
        if (value instanceof Double) {
            json.setValue(unit.convertTo(getUnit(), Double.parseDouble(value.toString())) + "");
        } else if (value instanceof Long) {
            json.setValue(unit.convertTo(getUnit(), Long.parseLong(value.toString())) + "");
        } else {
            throw new ClassCastException();
        }

    }

    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    @Override
    public String getNote() {
        if (json.getNote() == null) {
            return "";
        } else {
            return json.getNote();
        }
    }

    @Override
    public void setNote(String note) {
        json.setNote(note);
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return getAttribute().getInputUnit();
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
        logger.trace("Commit: {} {}", getTimestamp(), getValueAsString());
        List<JEVisSample> tmp = new ArrayList<>();
        tmp.add(this);
        getAttribute().addSamples(tmp);
//        ds.reloadAttribute(getAttribute());
//        try {
//            JEVisObjectWS obj = (JEVisObjectWS) getAttribute().getObject();
//            obj.notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.ATTRIBUTE_UPDATE));
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
    }

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "JEVisSampleWS{ ts:" + getTimestamp() + " Value: " + getValueAsString() + "}";
    }
}
