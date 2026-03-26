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
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.Optimization;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import tech.units.indriya.AbstractUnit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * REST-backed implementation of {@link JEVisAttribute} that communicates with
 * a JEWebService instance via {@link JEVisDataSourceWS}.
 *
 * <p>Attribute metadata (name, unit, sample-rate, min/max timestamps) is stored
 * in a {@link org.jevis.commons.ws.json.JsonAttribute} DTO. Samples are fetched
 * on demand via the data source. The primitive type is cached after the first
 * lookup to avoid repeated class-definition round-trips.
 *
 * @author fs
 */
public class JEVisAttributeWS implements JEVisAttribute {

    public static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    private static final Logger logger = LogManager.getLogger(JEVisAttributeWS.class);
    private final JEVisDataSourceWS ds;
    private JsonAttribute json;
    /**
     * Lazily cached result of {@link #getPrimitiveType()} to avoid repeated class lookups.
     */
    private Integer cachedPrimitiveType = null;

    /**
     * Creates an attribute backed by the given data source, associating it with the specified object ID.
     *
     * @param ds   the data source managing this attribute
     * @param json the DTO containing attribute metadata
     * @param obj  the owning object's ID
     */
    public JEVisAttributeWS(JEVisDataSourceWS ds, JsonAttribute json, Long obj) {
        this.ds = ds;
        this.json = json;
        this.json.setObjectID(obj);

        Optimization.getInstance().addAttribute(this);
    }

    /**
     * Creates an attribute backed by the given data source. The owning object ID must
     * already be set in {@code json}.
     *
     * @param ds   the data source managing this attribute
     * @param json the DTO containing attribute metadata (including object ID)
     */
    public JEVisAttributeWS(JEVisDataSourceWS ds, JsonAttribute json) {
        this.ds = ds;
        this.json = json;

        Optimization.getInstance().addAttribute(this);
    }

    /**
     * Refreshes this attribute's metadata from a new DTO and invalidates the
     * cached primitive type so it is re-fetched on the next access.
     *
     * @param json updated attribute DTO from the server
     */
    public void update(JsonAttribute json) {
        this.json = json;
        this.cachedPrimitiveType = null;
        /**
         * TODO: may call event?
         */
    }

    /** @return the attribute type name (e.g., {@code "Value"}). */
    @Override
    public String getName() {
        return json.getType();
    }

    /** Not supported. Always throws {@link UnsupportedOperationException}. */
    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Returns the {@link JEVisType} definition for this attribute by querying the owning
     * object's class.
     *
     * @return the type definition
     * @throws JEVisException if the type cannot be resolved
     */
    @Override
    public JEVisType getType() throws JEVisException {
        return ds.getObject(getObjectID()).getJEVisClass().getType(getName());
    }

    /**
     * Returns the {@link JEVisObject} that owns this attribute, or {@code null} on error.
     *
     * @return the owning object
     */
    @Override
    public JEVisObject getObject() {
        try {
            return ds.getObject(getObjectID());
        } catch (Exception ex) {
            return null;
        }
    }

    /** @return all samples for this attribute with no time bounds. */
    @Override
    public List<JEVisSample> getAllSamples() {
        return ds.getSamples(this, null, null);
    }

    /**
     * @param from lower time bound (inclusive), or {@code null} for unbounded
     * @param to   upper time bound (inclusive), or {@code null} for unbounded
     * @return samples within the given range
     */
    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        return ds.getSamples(this, from, to);
    }

    /**
     * @param from               lower time bound (inclusive)
     * @param to                 upper time bound (inclusive)
     * @param customWorkDay      whether to apply custom work-day filtering
     * @param aggregationPeriod  server-side aggregation period
     * @param manipulationPeriod server-side manipulation period
     * @param timeZone           target time zone ID
     * @return server-side aggregated samples
     */
    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to, boolean customWorkDay, String aggregationPeriod, String manipulationPeriod, String timeZone) {
        return ds.getSamples(this, from, to, customWorkDay, aggregationPeriod, manipulationPeriod, timeZone);
    }

    @Override
    public int addSamples(List<JEVisSample> samples) throws JEVisException {
        logger.debug("addSamples toWS: O|a: {}|{} samples: {}", getObjectID(), getName(), samples.size());
        List<JsonSample> jsonSamples = new ArrayList<>();
        int imported = 0;

        int primType = getPrimitiveType();
        if (primType != JEVisConstants.PrimitiveType.FILE) {
            try {

                for (JEVisSample s : samples) {
                    JsonSample jsonSample = JsonFactory.buildSample(s, primType);
                    jsonSamples.add(jsonSample);
                }


                //JEWebService/v1/files/8598/attributes/File/samples/files/20180604T141441?filename=nb-configuration.xml
                //JEWebService/v1/objects/{id}/attributes/{attribute}/samples
                String resource = REQUEST.API_PATH_V1
                        + REQUEST.OBJECTS.PATH
                        + getObjectID() + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.PATH
                        + getName() + "/"
                        + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

//                String requestjson = new Gson().toJson(jsonSamples, new TypeToken<List<JsonSample>>() {
//                }.getType());
                String json = this.ds.getObjectMapper().writeValueAsString(jsonSamples);

//                logger.debug("Payload. {}", requestjson);
                /** TODO: implement this function into ws **/
                StringBuffer response = ds.getHTTPConnection().postRequest(resource, json);

                logger.debug("Response.payload: {}", response);

            } catch (Exception ex) {
                logger.catching(ex);
            }

        } else {
            //Also upload die byte file, filename is in json
            for (JEVisSample s : samples)
                try {
                    String resource = REQUEST.API_PATH_V1
                            + REQUEST.OBJECTS.PATH
                            + getObjectID() + "/"
                            + REQUEST.OBJECTS.ATTRIBUTES.PATH
                            + getName() + "/"
                            + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH
                            + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.FILES.PATH
                            + HTTPConnection.FMT.print(s.getTimestamp())
                            + "?" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.FILES.OPTIONS.FILENAME + s.getValueAsFile().getFilename();

                    HttpURLConnection connection = ds.getHTTPConnection().getPostFileConnection(resource);
//                    logger.trace("Upload file-------------: {}", s.getValueAsFile().getBytes().length);
                    try (OutputStream os = connection.getOutputStream()) {

                        os.write(s.getValueAsFile().getBytes());
                        os.flush();
                        os.close();
                    }
                    int responseCode = connection.getResponseCode();
                    connection.disconnect();
                    imported = 1;/** TODO: fix server side, missing response **/
                } catch (Exception ex) {
                    logger.catching(ex);
                }

//
        }
        ds.reloadAttribute(this);

        /**
         * cast needs to be removed
         */
        JEVisObjectWS obj = (JEVisObjectWS) getObject();
        obj.notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.ATTRIBUTE_UPDATE, this));


        return imported;
    }

    /**
     * Builds a new in-memory sample with an empty note.
     *
     * @param ts    the sample timestamp
     * @param value the sample value (any supported type)
     * @return the new, unsaved sample
     * @throws JEVisException if the primitive type cannot be resolved
     */
    @Override
    public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
        return buildSample(ts, value, "");
    }

    /**
     * Builds a new double-valued sample with a specific unit and empty note.
     *
     * @param ts    the sample timestamp
     * @param value the double value
     * @param unit  the unit to associate with the sample
     * @return the new, unsaved sample
     * @throws JEVisException if the primitive type cannot be resolved
     */
    @Override
    public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
        return buildSample(ts, value, "", unit);
    }

    /**
     * Builds a new in-memory sample with a note.
     *
     * @param ts    the sample timestamp
     * @param value the sample value (any supported type, including {@link org.jevis.api.JEVisFile})
     * @param note  a free-text note attached to the sample
     * @return the new, unsaved sample
     * @throws JEVisException if the primitive type cannot be resolved
     */
    @Override
    public JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException {
        JsonSample newJson = new JsonSample();
        newJson.setTs(attDTF.print(ts));

        JEVisSample newSample = null;

        //TODO: replace this , the getPrimitiveType() is very bad because it will call the Webservice for every sample
//        if (getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
        if (value instanceof JEVisFile) {// workaround
            JEVisFile file = (JEVisFile) value;
            newSample = new JEVisSampleWS(ds, newJson, this, file);

        } else {
            newSample = new JEVisSampleWS(ds, newJson, this);
            newSample.setValue(value);
        }

        newSample.setNote(note);

        return newSample;
    }

    /**
     * Builds a new double-valued sample with a note and a specific unit.
     *
     * @param ts    the sample timestamp
     * @param value the double value
     * @param note  a free-text note
     * @param unit  the unit ({@code null} to use the attribute's default unit)
     * @return the new, unsaved sample
     * @throws JEVisException if the primitive type cannot be resolved
     */
    @Override
    public JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException {
        JsonSample newJson = new JsonSample();
        newJson.setTs(attDTF.print(ts));

        JEVisSample newSample = new JEVisSampleWS(ds, newJson, this);
        newSample.setNote(note);
        if (unit != null) {
            newSample.setValue(value, unit);
        } else {
            newSample.setValue(value);
        }

        return newSample;
    }

    /**
     * Returns the latest sample as cached in the attribute metadata DTO, or {@code null}
     * if no sample has been stored yet.
     *
     * @return the most recent {@link JEVisSample}, or {@code null}
     */
    @Override
    public JEVisSample getLatestSample() {
        if (json.getLatestValue() != null && json.getLatestValue().getTs() != null) {
            return new JEVisSampleWS(ds, json.getLatestValue(), this);
        } else {
            return null;
        }
    }

    /**
     * Returns the JEVis primitive type constant for this attribute's type.
     * The result is cached after the first call to avoid repeated class-definition requests.
     *
     * @return a {@link org.jevis.api.JEVisConstants.PrimitiveType} constant
     * @throws JEVisException if the class definition cannot be fetched
     */
    @Override
    public int getPrimitiveType() throws JEVisException {
        if (cachedPrimitiveType == null) {
            cachedPrimitiveType = getType().getPrimitiveType();
        }
        return cachedPrimitiveType;
    }

    /** @return {@code true} if at least one sample exists (i.e., the min-timestamp is known). */
    @Override
    public boolean hasSample() {
        return getTimestampOfFirstSample() != null;
    }

    /**
     * Returns the timestamp of the oldest sample as stored in the attribute metadata.
     *
     * @return the earliest sample timestamp, or {@code null} if none exists
     */
    @Override
    public DateTime getTimestampOfFirstSample() {
        try {
            return attDTF.parseDateTime(json.getBegins());
        } catch (Exception nex) {
            return null;
        }
    }

    /**
     * Returns the timestamp of the most recent sample as stored in the attribute metadata.
     *
     * @return the latest sample timestamp, or {@code null} if none exists
     */
    @Override
    public DateTime getTimestampOfLastSample() {
        try {
            return attDTF.parseDateTime(json.getEnds());
        } catch (Exception nex) {
            return null;
        }

    }

    /**
     * Deletes all samples for this attribute (no time bounds) and reloads the attribute metadata.
     *
     * @return {@code true} if the server accepted the deletion
     */
    @Override
    public boolean deleteAllSample() {
        boolean delete = deleteSamplesBetween(null, null);
        ds.reloadAttribute(this);
        return delete;
    }

    /**
     * Deletes samples within the given time range (either bound may be {@code null} for unbounded).
     *
     * @param from lower bound (inclusive), or {@code null}
     * @param to   upper bound (inclusive), or {@code null}
     * @return {@code true} if the server returned HTTP 200
     */
    @Override
    public boolean deleteSamplesBetween(DateTime from, DateTime to) {
        try {
            logger.trace("Delete samples for: {}", getName());

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + getObjectID() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH
                    + getName() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

            if (from != null || to != null) {
                resource += "?";
            }

//            resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM;
            if (from == null) {
//                resource += "null";
            } else {
                resource += REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.FROM;
                resource += HTTPConnection.FMT.print(from);
            }

//            resource += "&" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL;
            if (to == null) {
//                resource += "null";
            } else {
                resource += "&" + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.UNTIL;
                resource += HTTPConnection.FMT.print(to);
            }

            HttpURLConnection conn = ds.getHTTPConnection().getDeleteConnection(resource);

            ds.reloadAttribute(this);
            boolean response = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
            conn.disconnect();
            return response;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns the display unit configured for this attribute.
     * Falls back to {@code AbstractUnit.ONE} if the unit cannot be parsed.
     *
     * @return the display {@link JEVisUnit}
     */
    @Override
    public JEVisUnit getDisplayUnit() {
        try {
            return new JEVisUnitImp(json.getDisplayUnit());
        } catch (Exception ex) {
            return new JEVisUnitImp(AbstractUnit.ONE);
        }
    }

    /**
     * Sets the display unit for this attribute (local change only; call {@link #commit()} to persist).
     *
     * @param unit the display unit to set
     */
    @Override
    public void setDisplayUnit(JEVisUnit unit) {
        json.setDisplayUnit(JsonFactory.buildUnit(unit));
    }

    /**
     * Returns the input unit configured for this attribute.
     * Falls back to {@code AbstractUnit.ONE} if the unit cannot be parsed.
     *
     * @return the input {@link JEVisUnit}
     */
    @Override
    public JEVisUnit getInputUnit() {
        try {
            return new JEVisUnitImp(json.getInputUnit());
        } catch (Exception ex) {
            logger.debug("No unit selected using fallback unit.one");
            return new JEVisUnitImp(AbstractUnit.ONE);
        }
    }

    /**
     * Sets the input unit for this attribute (local change only; call {@link #commit()} to persist).
     *
     * @param unit the input unit to set
     */
    @Override
    public void setInputUnit(JEVisUnit unit) {
        json.setInputUnit(JsonFactory.buildUnit(unit));
    }

    /**
     * Returns the display sample rate as a Joda-Time {@link Period}.
     * Returns {@link Period#ZERO} if none is configured or if parsing fails.
     *
     * @return the display sample rate period
     */
    @Override
    public Period getDisplaySampleRate() {
        try {
            if (json.getDisplaySampleRate() == null || json.getDisplaySampleRate().isEmpty()) {
                return Period.ZERO;
            } else {
                return Period.parse(json.getDisplaySampleRate());
            }
        } catch (Exception ex) {
            return Period.ZERO;
        }

    }

    /**
     * Sets the display sample rate (local change only; call {@link #commit()} to persist).
     *
     * @param period the display sample rate period
     */
    @Override
    public void setDisplaySampleRate(Period period) {
        logger.debug("setDisplaySampleRate: " + period.toString());
        json.setDisplaySampleRate(period.toString());
    }

    /**
     * Returns the input sample rate as a Joda-Time {@link Period}.
     * Returns {@link Period#ZERO} if none is configured or if parsing fails.
     *
     * @return the input sample rate period
     */
    @Override
    public Period getInputSampleRate() {
        try {
            if (json.getInputSampleRate() == null || json.getInputSampleRate().isEmpty()) {
                return Period.ZERO;
            } else {
                return Period.parse(json.getInputSampleRate());
            }
        } catch (Exception ex) {
            return Period.ZERO;
        }
    }

    /**
     * Sets the input sample rate (local change only; call {@link #commit()} to persist).
     *
     * @param period the input sample rate period
     */
    @Override
    public void setInputSampleRate(Period period) {
        json.setInputSampleRate(period.toString());
    }

    /**
     * Returns {@code true} if this attribute's type name matches the given {@link JEVisType}.
     *
     * @param type the type to compare against
     * @return {@code true} if names match
     */
    @Override
    public boolean isType(JEVisType type
    ) {
        try {
            return (getType().getName().equals(type.getName()));
        } catch (Exception ex) {
            return false;
        }
    }

    /** @return the cached sample count as returned by the server. */
    @Override
    public long getSampleCount() {
        return json.getSampleCount();
    }

    /** @return the ID of the {@link JEVisObject} that owns this attribute. */
    @Override
    public Long getObjectID() {
        return json.getObjectID();
    }

    /** @return an empty list (options not implemented for WS attributes). */
    @Override
    public List<JEVisOption> getOptions() {
        return new ArrayList<>();
    }

    /** No-op — options are not supported for WS attributes. */
    @Override
    public void addOption(JEVisOption option
    ) {
    }

    /** No-op — options are not supported for WS attributes. */
    @Override
    public void removeOption(JEVisOption option
    ) {
    }

    /** @return the underlying {@link JEVisDataSource}. */
    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

    /**
     * Persists local metadata changes (unit, sample rate) to the server via a POST request.
     *
     * @throws JEVisException if serialization or the HTTP request fails
     */
    @Override
    public void commit() throws JEVisException {
        try {
//            Gson gson = new Gson();

            //Path("/JEWebService/v1/objects/{id}/attributes")
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + getObjectID() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH
                    + getName();

//            logger.debug("Payload: {}", gson.toJson(json));
//            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));
            StringBuffer response = ds.getHTTPConnection().postRequest(resource, this.ds.getObjectMapper().writeValueAsString(json));

            this.json = this.ds.getObjectMapper().readValue(response.toString(), JsonAttribute.class);

        } catch (Exception ex) {
            logger.catching(ex);
        }

    }

    /** Not supported. Always throws {@link UnsupportedOperationException}. */
    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /** @return {@code true} always (change tracking not yet implemented). */
    @Override
    public boolean hasChanged() {
        return true;//TODO: implement
    }

    /**
     * Compares this attribute to another by type name, alphabetically.
     *
     * @param otherObject the attribute to compare to
     * @return negative, zero, or positive as defined by {@link String#compareTo}
     */
    @Override
    public int compareTo(JEVisAttribute otherObject) {
        return otherObject.getName().compareTo(json.getType());
    }

    /**
     * Returns {@code true} if the other object is a {@link JEVisAttribute} with the same
     * object ID and attribute name.
     *
     * @param otherObject the object to compare
     * @return {@code true} if equal
     */
    @Override
    public boolean equals(Object otherObject) {
        /**
         * cast needs to be removed
         */
        if (otherObject instanceof JEVisAttribute) {
            JEVisAttribute otherAttribute = (JEVisAttribute) otherObject;
            if (otherAttribute.getObjectID().equals(getObjectID()) && otherAttribute.getName().equals(getName())) {
                return otherAttribute.getName().equals(otherAttribute.getName());
            }
        }


        return false;
    }

    @Override
    public String toString() {
        return "JEVisAttributeWS [ type: '" + json.getType() + "' object: '" + json.getObjectID() + "' basicType: '"
                + json.getPrimitiveType() + "' maxTS: '" + json.getEnds() + "']";
    }
}
