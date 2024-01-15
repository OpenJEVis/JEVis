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

import javax.measure.unit.Unit;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fs
 */
public class JEVisAttributeWS implements JEVisAttribute {

    public static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    private static final Logger logger = LogManager.getLogger(JEVisAttributeWS.class);
    private final JEVisDataSourceWS ds;
    private JsonAttribute json;

    public JEVisAttributeWS(JEVisDataSourceWS ds, JsonAttribute json, Long obj) {
        this.ds = ds;
        this.json = json;
        this.json.setObjectID(obj);

        Optimization.getInstance().addAttribute(this);
    }

    public JEVisAttributeWS(JEVisDataSourceWS ds, JsonAttribute json) {
        this.ds = ds;
        this.json = json;

        Optimization.getInstance().addAttribute(this);
    }

    public void update(JsonAttribute json) {
        this.json = json;
        /**
         * TODO: may call event?
         */
    }

    @Override
    public String getName() {
        return json.getType();
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisType getType() throws JEVisException {
        return ds.getObject(getObjectID()).getJEVisClass().getType(getName());
    }

    @Override
    public JEVisObject getObject() {
        try {
            return ds.getObject(getObjectID());
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public List<JEVisSample> getAllSamples() {
        return ds.getSamples(this, null, null);
    }

    @Override
    public List<JEVisSample> getSamples(DateTime from, DateTime to) {
        return ds.getSamples(this, from, to);
    }

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

    @Override
    public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
        return buildSample(ts, value, "");
    }

    @Override
    public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
        return buildSample(ts, value, "", unit);
    }

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

    @Override
    public JEVisSample getLatestSample() {
        if (json.getLatestValue() != null && json.getLatestValue().getTs() != null) {
            return new JEVisSampleWS(ds, json.getLatestValue(), this);
        } else {
            return null;
        }
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return getType().getPrimitiveType();//saver
//        return json.getPrimitiveType();//faster
    }

    @Override
    public boolean hasSample() {
        return getTimestampOfFirstSample() != null;
    }

    @Override
    public DateTime getTimestampOfFirstSample() {
        try {
            return attDTF.parseDateTime(json.getBegins());
        } catch (Exception nex) {
            return null;
        }
    }

    @Override
    public DateTime getTimestampOfLastSample() {
        try {
            return attDTF.parseDateTime(json.getEnds());
        } catch (Exception nex) {
            return null;
        }

    }

    @Override
    public boolean deleteAllSample() {
        boolean delete = deleteSamplesBetween(null, null);
        ds.reloadAttribute(this);
        return delete;
    }

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

    @Override
    public JEVisUnit getDisplayUnit() {
        try {
            return new JEVisUnitImp(json.getDisplayUnit());
        } catch (Exception ex) {
            return new JEVisUnitImp(Unit.ONE);
        }
    }

    @Override
    public void setDisplayUnit(JEVisUnit unit) {
        json.setDisplayUnit(JsonFactory.buildUnit(unit));
    }

    @Override
    public JEVisUnit getInputUnit() {
        try {
            return new JEVisUnitImp(json.getInputUnit());
        } catch (Exception ex) {
            logger.debug("No unit selected using fallback unit.one");
            return new JEVisUnitImp(Unit.ONE);
        }
    }

    @Override
    public void setInputUnit(JEVisUnit unit) {
        json.setInputUnit(JsonFactory.buildUnit(unit));
    }

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

    @Override
    public void setDisplaySampleRate(Period period) {
        logger.debug("setDisplaySampleRate: " + period.toString());
        json.setDisplaySampleRate(period.toString());
    }

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

    @Override
    public void setInputSampleRate(Period period) {
        json.setInputSampleRate(period.toString());
    }

    @Override
    public boolean isType(JEVisType type
    ) {
        try {
            return (getType().getName().equals(type.getName()));
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public long getSampleCount() {
        return json.getSampleCount();
    }

    @Override
    public Long getObjectID() {
        return json.getObjectID();
    }

    @Override
    public List<JEVisOption> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public void addOption(JEVisOption option
    ) {
    }

    @Override
    public void removeOption(JEVisOption option
    ) {
    }

    @Override
    public JEVisDataSource getDataSource() {
        return ds;
    }

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

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        return true;//TODO: implement
    }

    @Override
    public int compareTo(JEVisAttribute otherObject) {
        return otherObject.getName().compareTo(json.getType());
    }

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
