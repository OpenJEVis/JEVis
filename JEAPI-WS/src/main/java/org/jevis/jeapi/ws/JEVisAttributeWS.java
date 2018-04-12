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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisOption;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisType;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonSample;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 *
 * @author fs
 */
public class JEVisAttributeWS implements JEVisAttribute {

    private String name = "";
    private JEVisDataSourceWS ds;
    private JEVisObject object;
    private long objectID;
    private final Logger logger = LogManager.getLogger(JEVisAttributeWS.class);
    private static final DateTimeFormatter attDTF = ISODateTimeFormat.dateTime();
    private JsonAttribute json;

    public JEVisAttributeWS(JEVisDataSourceWS ds, JsonAttribute json, JEVisObject obj) {
        this.ds = ds;
        this.objectID = obj.getID();
        this.json = json;
        name = json.getType();
        object = obj;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisType getType() throws JEVisException {
        return object.getJEVisClass().getType(name);
    }

    @Override
    public JEVisObject getObject() {
        return object;
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
    public int addSamples(List<JEVisSample> samples) throws JEVisException {
        List<JsonSample> jsonSamples = new ArrayList<>();

        boolean isFile = getPrimitiveType() == JEVisConstants.PrimitiveType.FILE;
        int primType = getPrimitiveType();

        try {
            for (JEVisSample s : samples) {
                JsonSample jsonSample = JsonFactory.buildSample(s, primType);
                jsonSamples.add(jsonSample);
                //TODO: reolace Workaround, getPrimitiveType() will call getType fromthe Webservice and this for all the samples
//                if (getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
//                if (isFile) {
//                    jsonSample.setValue(s.getValueAsFile().getFilename());
//                }
            }

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + getObjectID() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH
                    + getName() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH;

            String requestjson = new Gson().toJson(jsonSamples, new TypeToken<List<JsonSample>>() {
            }.getType());
            logger.debug("Playload. {}", requestjson);

            StringBuffer response = ds.getHTTPConnection().postRequest(resource, requestjson);

            logger.trace("Response.payload: {}", response);

        } catch (Exception ex) {
            logger.catching(ex);
        }

        if (getType().getPrimitiveType() == JEVisConstants.PrimitiveType.FILE) {
            //Also upload die byte file, filename is in json
            for (JEVisSample s : samples) {
                try {
                    String resource = REQUEST.API_PATH_V1
                            + REQUEST.OBJECTS.PATH
                            + getObjectID() + "/"
                            + REQUEST.OBJECTS.ATTRIBUTES.PATH
                            + getName() + "/"
                            + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH
                            + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.FILES.PATH
                            + HTTPConnection.FMT.print(s.getTimestamp());
//                        + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.FILES.OPTIONS.filename + s.getValueAsFile().getFilename();

                    HttpURLConnection connection = ds.getHTTPConnection().getPostFileConnection(resource);

                    logger.trace("Upload file-------------: {}", s.getValueAsFile().getBytes().length);
                    try (OutputStream os = connection.getOutputStream()) {

                        os.write(s.getValueAsFile().getBytes());
                        os.flush();
                        os.close();
                    }
                    int responseCode = connection.getResponseCode();
                } catch (Exception ex) {
                    logger.catching(ex);
                }
            }

//
        }

        return 1;
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

        //TODO: replace this , the getPrimitiveType() is very bad because it will call the Webservice for every sampble
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
        try {
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + getObjectID() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH
                    + getName() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.PATH
                    + "?"
                    + REQUEST.OBJECTS.ATTRIBUTES.SAMPLES.OPTIONS.LASTEST + "true";

            StringBuffer response = ds.getHTTPConnection().getRequest(resource);

            if (response != null && response.toString() != null && !response.toString().isEmpty()) {
                JsonSample sampleJson = new Gson().fromJson(response.toString(), JsonSample.class);

                return new JEVisSampleWS(ds, sampleJson, this);
            } else {
                return null;
            }
        } catch (Exception ex) {
            logger.catching(ex);
            return null;
        }
    }

    @Override
    public int getPrimitiveType() throws JEVisException {
        return getType().getPrimitiveType();
    }

    @Override
    public boolean hasSample() {
        if (getTimestampFromFirstSample() != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DateTime getTimestampFromFirstSample() {
        try {
            return attDTF.parseDateTime(json.getBegins());
        } catch (NullPointerException nex) {
            return null;
        }
    }

    @Override
    public DateTime getTimestampFromLastSample() {
        try {
            return attDTF.parseDateTime(json.getEnds());
        } catch (NullPointerException nex) {
            return null;
        }
    }

    @Override
    public boolean deleteAllSample() throws JEVisException {

        return deleteSamplesBetween(null, null);
    }

    @Override
    public boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException {
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
                resource += HTTPConnection.FMT.print(from);
            }

            Gson gson = new Gson();
            HttpURLConnection conn = ds.getHTTPConnection().getDeleteConnection(resource);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public JEVisUnit getDisplayUnit() throws JEVisException {
        try {
            return new JEVisUnitImp(json.getDisplayUnit());
        } catch (Exception ex) {
            return new JEVisUnitImp(Unit.ONE);
        }
    }

    @Override
    public void setDisplayUnit(JEVisUnit unit) throws JEVisException {
        json.setDisplayUnit(JsonFactory.buildUnit(unit));
    }

    @Override
    public JEVisUnit getInputUnit() throws JEVisException {
        try {
            return new JEVisUnitImp(json.getInputUnit());
        } catch (Exception ex) {
            return new JEVisUnitImp(Unit.ONE);
        }
    }

    @Override
    public void setInputUnit(JEVisUnit unit) throws JEVisException {
        json.setInputUnit(JsonFactory.buildUnit(unit));
    }

    @Override
    public Period getDisplaySampleRate() {
        try {
            return Period.parse(json.getDisplaySampleRate());
        } catch (Exception ex) {
            return Period.ZERO;
        }

    }

    @Override
    public Period getInputSampleRate() {
        try {
            return Period.parse(json.getInputSampleRate());
        } catch (Exception ex) {
            return Period.ZERO;
        }
    }

    @Override
    public void setInputSampleRate(Period period
    ) {
//        inputSapleRate = period;
        json.setInputSampleRate(period.toString());
    }

    @Override
    public void setDisplaySampleRate(Period period
    ) {
        json.setDisplaySampleRate(period.toString());
//        displaySamplerate = period;
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
        return objectID;
    }

    @Override
    public List<JEVisOption> getOptions() {
        return new ArrayList<>();
    }

    @Override
    public void addOption(JEVisOption option
    ) {
        ;
    }

    @Override
    public void removeOption(JEVisOption option
    ) {
        ;
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
        try {
//            JsonAttribute json = JsonFactory.buildAttribute(this);
            Gson gson = new Gson();

            //Path("/JEWebService/v1/objects/{id}/attributes")
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH
                    + getObjectID() + "/"
                    + REQUEST.OBJECTS.ATTRIBUTES.PATH
                    + getName();

            logger.debug("Payload: {}", gson.toJson(json));
            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));

            JsonAttribute newJson = gson.fromJson(response.toString(), JsonAttribute.class);
            this.json = newJson;

        } catch (Exception ex) {
            logger.catching(ex);
        }

    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        return true;//TODO: implement
    }

    @Override
    public int compareTo(JEVisAttribute o
    ) {
        return o.getName().compareTo(name);
    }

}
