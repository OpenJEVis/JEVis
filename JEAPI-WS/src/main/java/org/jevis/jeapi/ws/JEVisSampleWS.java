/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisMultiSelection;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisSelection;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author fs
 */
public class JEVisSampleWS implements JEVisSample {

    private DateTime timestamp;
    private JEVisAttribute attribute;
    private JsonSample json;
    private JEVisDataSourceWS ds;
    private Logger logger = LogManager.getLogger(JEVisSampleWS.class);
    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
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
    public DateTime getTimestamp() throws JEVisException {
        return sampleDTF.parseDateTime(json.getTs());
    }

    @Override
    public Object getValue() throws JEVisException {

        return json.getValue();//TODO cast to type?!
    }

    @Override
    public String getValueAsString() throws JEVisException {
        return json.getValue();
    }

    @Override
    public Long getValueAsLong() throws JEVisException {
        return Long.parseLong(getValueAsString());
    }

    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        Long lValue = getValueAsLong();
        Double dValue = getUnit().converteTo(unit, lValue);

        return dValue.longValue();

    }

    @Override
    public Double getValueAsDouble() throws JEVisException {
        return Double.parseDouble(getValueAsString());
    }

    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        Double dValue = Double.parseDouble(getValueAsString());

        return getUnit().converteTo(unit, dValue);
    }

    @Override
    public Boolean getValueAsBoolean() throws JEVisException {
        return Boolean.parseBoolean(getValueAsString());
    }

    @Override
    public JEVisFile getValueAsFile() throws JEVisException {

        if (file != null) {
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
                return null;
            }
        }

    }

    @Override
    public JEVisSelection getValueAsSelection() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisMultiSelection getValueAsMultiSelection() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setValue(Object value) throws JEVisException, ClassCastException {
        json.setValue(value.toString());
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
    public JEVisAttribute getAttribute() throws JEVisException {
        return attribute;
    }

    @Override
    public String getNote() throws JEVisException {
        return json.getNote();
    }

    @Override
    public void setNote(String note) throws JEVisException {
        json.setNote(note);
    }

    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return getAttribute().getInputUnit();
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
        logger.trace("Commit: {} {}", getTimestamp(), getValueAsString());
        List<JEVisSample> tmp = new ArrayList<>();
        tmp.add(this);
        getAttribute().addSamples(tmp);
    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
