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
 * WebService-backed implementation of {@link JEVisSample}.
 *
 * <p>A sample represents a single time-stamped measurement stored in JEVis.
 * Values are typed according to the owning attribute's
 * {@link JEVisConstants.PrimitiveType primitive type}: {@code DOUBLE}, {@code LONG},
 * {@code BOOLEAN}, or {@code STRING}. File-type samples contain a byte payload
 * that is fetched on demand from the REST endpoint.</p>
 *
 * <p>Instances are constructed by {@link JEVisAttributeWS} from JSON DTOs returned
 * by {@code ResourceSample} on the server. They are not intended to be created
 * directly by application code.</p>
 *
 * @author fs
 */
public class JEVisSampleWS implements JEVisSample {

    public static final DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();
    public static final NumberFormat nf = NumberFormat.getInstance(Locale.US);
    private static final Logger logger = LogManager.getLogger(JEVisSampleWS.class);
    private final JEVisAttribute attribute;
    private final JsonSample json;
    private final JEVisDataSourceWS ds;
    private JEVisFile file = null;
    private Object valueObj = null;
    private DateTime tsObj = null;

    /**
     * Constructs a sample from its JSON DTO. The timestamp and value are
     * immediately deserialized into strongly-typed objects.
     *
     * @param ds   the owning data source
     * @param json the raw JSON representation of the sample
     * @param att  the attribute this sample belongs to
     */
    public JEVisSampleWS(JEVisDataSourceWS ds, JsonSample json, JEVisAttribute att) {
        this.attribute = att;
        this.ds = ds;
        this.json = json;
        objectifyValue();
        objectifyTimeStamp();
    }

    /**
     * Constructs a file-type sample. The value is set to the file's filename
     * and the byte payload is held in {@code file} for later retrieval via
     * {@link #getValueAsFile()}.
     *
     * @param ds   the owning data source
     * @param json the raw JSON representation of the sample
     * @param att  the attribute this sample belongs to
     * @param file the file payload associated with this sample
     */
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

    /**
     * Returns the timestamp of this sample.
     *
     * @return the sample timestamp; never {@code null}
     */
    @Override
    public DateTime getTimestamp() {
        return tsObj;
    }

    /**
     * Returns the sample value as a typed object.  The concrete type depends on
     * the attribute's primitive type: {@link Double}, {@link Long},
     * {@link Boolean}, or {@link String}.  Falls back to the raw JSON string if
     * the value could not be typed.
     *
     * @return the typed value, or the raw JSON string as a fallback; may be {@code null}
     */
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

    /**
     * Sets the value of this sample. The supplied object is converted to its
     * string representation for JSON storage; the typed in-memory reference is
     * also updated. Changes are not persisted until {@link #commit()} is called.
     *
     * @param value the new value; pass {@code null} to store an empty value
     * @throws ClassCastException if the value cannot be stored
     */
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
            throw new ClassCastException("Value object does not match the PrimitiveType of the Attribute: " + this);
        }
    }

    /**
     * Returns the sample value as a {@code long}. Returns {@code 0} if the
     * value cannot be parsed.
     *
     * @return the value as a {@link Long}
     */
    @Override
    public Long getValueAsLong() {
        try {
            return valueObj instanceof Long ? (Long) valueObj : nf.parse(getValueAsString()).longValue();
        } catch (Exception ex) {
            return 0L;
        }
    }

    /**
     * Returns the sample value converted to the specified unit and then cast to
     * {@code long}.
     *
     * @param unit the target unit to convert to
     * @return the converted value as a {@link Long}
     * @throws JEVisException if unit conversion fails
     */
    @Override
    public Long getValueAsLong(JEVisUnit unit) throws JEVisException {
        double lValue = getValueAsLong().doubleValue();
        Double dValue = getUnit().convertTo(unit, lValue);
        return dValue.longValue();
    }

    /**
     * Returns the sample value as a {@code double}.
     *
     * @return the value as a {@link Double}
     */
    @Override
    public Double getValueAsDouble() {
        return valueObj instanceof Double ? (Double) valueObj : Double.parseDouble(getValueAsString());
    }

    /**
     * Returns the sample value converted to the specified unit as a
     * {@code double}.
     *
     * @param unit the target unit to convert to
     * @return the converted value as a {@link Double}
     * @throws JEVisException if unit conversion fails
     */
    @Override
    public Double getValueAsDouble(JEVisUnit unit) throws JEVisException {
        return getUnit().convertTo(unit, getValueAsDouble());
    }

    /**
     * Returns the sample value as a {@code boolean}. The strings {@code "1"}
     * and {@code "0"} are interpreted as {@code true} and {@code false}
     * respectively; all other values are parsed via {@link Boolean#parseBoolean}.
     *
     * @return the value as a {@link Boolean}
     */
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

    /**
     * Returns the raw string value exactly as stored in the JSON payload.
     *
     * @return the raw string value; may be {@code null} for empty samples
     */
    @Override
    public String getValueAsString() {
        return json.getValue();
    }

    /**
     * Returns the file payload of this sample. If the file content is already
     * cached locally it is returned immediately; otherwise the bytes are
     * fetched from the REST endpoint on demand.
     *
     * @return the {@link JEVisFile} with the file name and byte content,
     * or {@code null} if the file cannot be retrieved
     */
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

    /**
     * Returns the value as a {@link JEVisSelection}.
     *
     * @throws UnsupportedOperationException always — not yet implemented
     */
    @Override
    public JEVisSelection getValueAsSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the value as a {@link JEVisMultiSelection}.
     *
     * @throws UnsupportedOperationException always — not yet implemented
     */
    @Override
    public JEVisMultiSelection getValueAsMultiSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Sets the value of this sample, converting it from {@code unit} into the
     * attribute's input unit before storing. Only {@link Double} and
     * {@link Long} values are supported.
     *
     * @param value the value to set (must be {@link Double} or {@link Long})
     * @param unit  the unit that {@code value} is expressed in
     * @throws JEVisException   if unit conversion fails
     * @throws ClassCastException if the value type is not {@code Double} or {@code Long}
     */
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

    /**
     * Returns the {@link JEVisAttribute} that owns this sample.
     *
     * @return the owning attribute; never {@code null}
     */
    @Override
    public JEVisAttribute getAttribute() {
        return attribute;
    }

    /**
     * Returns the note (annotation) attached to this sample, or an empty
     * string if none is set.
     *
     * @return the note; never {@code null}
     */
    @Override
    public String getNote() {
        if (json.getNote() == null) {
            return "";
        } else {
            return json.getNote();
        }
    }

    /**
     * Sets the note (annotation) for this sample. Changes are not persisted
     * until {@link #commit()} is called.
     *
     * @param note the note to set; may be {@code null}
     */
    @Override
    public void setNote(String note) {
        json.setNote(note);
    }

    /**
     * Returns the unit associated with this sample's value, delegating to the
     * owning attribute's input unit.
     *
     * @return the sample's unit; never {@code null}
     * @throws JEVisException if the attribute's unit cannot be retrieved
     */
    @Override
    public JEVisUnit getUnit() throws JEVisException {
        return getAttribute().getInputUnit();
    }

    /**
     * Returns the {@link JEVisDataSource} this sample belongs to.
     *
     * @return the owning data source; never {@code null}
     */
    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    /**
     * Persists this sample to the server by delegating to
     * {@link JEVisAttribute#addSamples(List)}. This causes a POST request
     * containing the sample's timestamp and value.
     *
     * @throws JEVisException if the upload fails
     */
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

    /**
     * Rolls back any uncommitted changes to this sample.
     *
     * @throws UnsupportedOperationException always — not yet implemented
     */
    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns whether this sample has uncommitted local changes.
     *
     * @throws UnsupportedOperationException always — not yet implemented
     */
    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "JEVisSampleWS{ ts:" + getTimestamp() + " Value: " + getValueAsString() + "}";
    }
}
