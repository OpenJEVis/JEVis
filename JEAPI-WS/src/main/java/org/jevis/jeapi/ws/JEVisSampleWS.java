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

import org.apache.commons.validator.routines.LongValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author fs
 */
public class JEVisSampleWS implements JEVisSample {

    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
    private static final Logger logger = LogManager.getLogger(JEVisSampleWS.class);
    private JEVisAttribute attribute;
    private JsonSample json;
    private JEVisDataSourceWS ds;
    private JEVisFile file = null;

    public JEVisSampleWS(JEVisDataSourceWS ds, JsonSample json, JEVisAttribute att) {
        this.attribute = att;
        this.ds = ds;
        this.json = json;

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

    @Override
    public DateTime getTimestamp() {
        return sampleDTF.parseDateTime(json.getTs());
    }

    @Override
    public Object getValue() {

        return json.getValue();//TODO cast to type?!
    }

    @Override
    public void setValue(Object value) throws ClassCastException {
        //logger.debug("setValue: {} Value: {}", getAttribute().getName(), value);
        try {
            if (getAttribute().getPrimitiveType() == JEVisConstants.PrimitiveType.DOUBLE) {
                Double.valueOf(value.toString());
            } else if (getAttribute().getPrimitiveType() == JEVisConstants.PrimitiveType.LONG) {
                Long.valueOf(value.toString());
            }

            json.setValue(value.toString());

        } catch (Exception ex) {
            throw new ClassCastException("Value object does not match the PrimitiveType of the Attribute: " + this.toString());
        }
    }

    @Override
    public Long getValueAsLong() {
        LongValidator validator = LongValidator.getInstance();
        return validator.validate(getValueAsString(), Locale.US);
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        double lValue = getValueAsLong().doubleValue();
        Double dValue = getUnit().converteTo(unit, lValue);
        return dValue.longValue();

    }

    @Override
    public Double getValueAsDouble() {
        return Double.parseDouble(getValueAsString());
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        Double dValue = Double.parseDouble(getValueAsString());
        return getUnit().converteTo(unit, dValue);
    }

    @Override
    public Boolean getValueAsBoolean() {
        if (json.getValue().equals("1")) {
            return true;
        } else if (json.getValue().equals("0")) {
            return false;
        }

        return Boolean.parseBoolean(getValueAsString());
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
            json.setValue(unit.converteTo(getUnit(), Double.parseDouble(value.toString())) + "");
        } else if (value instanceof Long) {
            json.setValue(unit.converteTo(getUnit(), Long.parseLong(value.toString())) + "");
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
        ds.reloadAttribute(getAttribute());
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
