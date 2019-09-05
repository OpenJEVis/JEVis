/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEWebService.
 * <p>
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.unit.JEVisUnitImp;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * this Class handles all the JEVisSample related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/objects/{id}/attributes/{attribute}/virtualsamples")
public class ResourceVirtualSample {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceVirtualSample.class);
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();
    private SQLDataSource ds = null;
    private List<JsonSample> list;
    private DateTimeFormatter sampleDTF = ISODateTimeFormat.dateTime();

    /**
     * Get the samples from an object/Attribute
     *
     * @param httpHeaders
     * @param id
     * @param attribute
     * @param start
     * @param end
     * @param onlyLatest
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSamples(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end,
            @QueryParam("manMode") ManipulationMode manipulationMode,
            @QueryParam("aggMode") AggregationPeriod aggregationPeriod,
            @DefaultValue("1000000") @QueryParam("limit") long limit,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) {

        try {
            logger.error("Object/att: {}/{} from/until: {}/{} mM:aM: {}/{} ", id, attribute, start, end, manipulationMode, aggregationPeriod);
            this.ds = new SQLDataSource(httpHeaders, request, url);
            Benchmark benchmark = new Benchmark();
            JsonObject obj = this.ds.getObject(id);
            if (obj == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessable").build();
            }

            if (obj.getJevisClass().equals("User") && obj.getId() == this.ds.getCurrentUser().getUserID()) {
                if (attribute.equals("Enabled") || attribute.equals("Sys Admin")) {
                    throw new JEVisException("permission denied", 3022);
                }
            } else {
                this.ds.getUserManager().canRead(obj);
            }

            logger.trace("got Object: {}", obj);

            for (JsonAttribute att : this.ds.getAttributes(id)) {
                if (att.getType().equals(attribute)) {
                    DateTime startDate = null;
                    DateTime endDate = null;
                    if (start != null) {
                        startDate = fmt.parseDateTime(start);
                        if (startDate.getYear() < 1980) {
                            Response.ok(new ArrayList<JsonSample>()).build();
                        }
                    }
                    if (end != null) {
                        endDate = fmt.parseDateTime(end);
                        if (endDate.getYear() < 1980) {
                            Response.ok(new ArrayList<JsonSample>()).build();
                        }
                    }


                    if (onlyLatest == true) {
                        logger.trace("Lastsample mode");

                        JsonSample sample = this.ds.getLastSample(id, attribute);
                        if (sample != null) {
                            return Response.ok(sample).build();
                        } else {
                            return Response.status(Status.NOT_FOUND).entity("Has no samples").build();
                        }

                    }
                    this.list = this.ds.getSamples(id, attribute, startDate, endDate, limit);
                    benchmark.printBenchmarkDetail("Total JsonSample: " + this.list.size());
                    List<JEVisSample> jeVisSampleList = tmpSampleList(this.list);
                    benchmark.printBenchmarkDetail("Total JEvisSample: " + jeVisSampleList.size());

                    SampleGenerator sampleGenerator = new SampleGenerator(null, tmpObject(obj), tmpAttribute(att, jeVisSampleList),
                            startDate, endDate, manipulationMode, aggregationPeriod);
                    benchmark.printBenchmarkDetail("Done building generator");
                    List<JEVisSample> virtualSamples = sampleGenerator.getAggregatedSamples(jeVisSampleList);
                    benchmark.printBenchmarkDetail("Total VirtualSample: " + virtualSamples.size());
                    List<JsonSample> jsonSamples = tmpJsonSampleList(virtualSamples);
                    benchmark.printBenchmarkDetail("Total result: " + jsonSamples.size());
                    virtualSamples.clear();
                    jeVisSampleList.clear();
                    sampleGenerator = null;

                    return Response.ok(jsonSamples).build();

                }
            }
            return Response.status(Status.NOT_FOUND)
                    .entity("No such Attribute").build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().entity(jex).build();
        } catch (AuthenticationException ex) {
            return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(this.ds);
        }

    }

    private List<JsonSample> tmpJsonSampleList(List<JEVisSample> samples) {
        List<JsonSample> jsonSamples = new ArrayList<>();
        for (JEVisSample jeVisSample : samples) {
            try {
                JsonSample jsonSample = new JsonSample();
                jsonSample.setTs(this.sampleDTF.print(jeVisSample.getTimestamp()));
                jsonSample.setValue(jeVisSample.getValueAsString());
                jsonSample.setNote(jeVisSample.getNote());
                jsonSamples.add(jsonSample);
            } catch (Exception ex) {

            }
        }
        return jsonSamples;
    }

    private List<JEVisSample> tmpSampleList(List<JsonSample> jsamples) {
        List<JEVisSample> result = new ArrayList<>();
        for (JsonSample sample : jsamples) {
            try {
                result.add(tmpSample(sample));
            } catch (Exception ex) {

            }
        }
        return result;

    }

    private JEVisSample tmpSample(JsonSample jsample) {
        return new VirtualSample(this.sampleDTF.parseDateTime(jsample.getTs()), Double.parseDouble(jsample.getValue()));
    }

    private JEVisAttribute tmpAttribute(JsonAttribute jsonAttribute, List<JEVisSample> samples) {
        return new JEVisAttribute() {
            @Override
            public String getName() {
                return jsonAttribute.getType();
            }

            @Override
            public boolean delete() {
                return false;
            }

            @Override
            public JEVisType getType() throws JEVisException {
                return null;
            }

            @Override
            public JEVisObject getObject() {
                return null;
            }

            @Override
            public Long getObjectID() {
                return jsonAttribute.getObjectID();
            }

            @Override
            public List<JEVisSample> getAllSamples() {
                return null;
            }

            @Override
            public List<JEVisSample> getSamples(DateTime from, DateTime to) {
                return samples;
            }

            @Override
            public int addSamples(List<JEVisSample> samples) throws JEVisException {
                return 0;
            }

            @Override
            public JEVisSample buildSample(DateTime ts, Object value) throws JEVisException {
                return null;
            }

            @Override
            public JEVisSample buildSample(DateTime ts, double value, JEVisUnit unit) throws JEVisException {
                return null;
            }

            @Override
            public JEVisSample buildSample(DateTime ts, Object value, String note) throws JEVisException {
                return null;
            }

            @Override
            public JEVisSample buildSample(DateTime ts, double value, String note, JEVisUnit unit) throws JEVisException {
                return null;
            }

            @Override
            public JEVisSample getLatestSample() {
                return samples.get(samples.size() - 1);
            }

            @Override
            public int getPrimitiveType() throws JEVisException {
                return 0;
            }

            @Override
            public boolean hasSample() {
                return !samples.isEmpty();
            }

            @Override
            public DateTime getTimestampFromFirstSample() {
                try {
                    return samples.get(0).getTimestamp();
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            public DateTime getTimestampFromLastSample() {
                try {
                    return samples.get(samples.size() - 1).getTimestamp();
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            public boolean deleteAllSample() throws JEVisException {
                return false;
            }

            @Override
            public boolean deleteSamplesBetween(DateTime from, DateTime to) throws JEVisException {
                return false;
            }

            @Override
            public JEVisUnit getDisplayUnit() throws JEVisException {
                return new JEVisUnitImp(jsonAttribute.getDisplayUnit());
            }

            @Override
            public void setDisplayUnit(JEVisUnit unit) throws JEVisException {
                ;
            }

            @Override
            public JEVisUnit getInputUnit() throws JEVisException {
                return new JEVisUnitImp(jsonAttribute.getInputUnit());
            }

            @Override
            public void setInputUnit(JEVisUnit unit) throws JEVisException {

            }

            @Override
            public Period getDisplaySampleRate() {
                return Period.parse(jsonAttribute.getDisplaySampleRate());
            }

            @Override
            public Period getInputSampleRate() {
                return Period.parse(jsonAttribute.getInputSampleRate());
            }

            @Override
            public void setInputSampleRate(Period period) {

            }

            @Override
            public void setDisplaySampleRate(Period period) {

            }

            @Override
            public boolean isType(JEVisType type) {
                return true;
            }

            @Override
            public long getSampleCount() {
                return 0;
            }

            @Override
            public List<JEVisOption> getOptions() {
                return null;
            }

            @Override
            public void addOption(JEVisOption option) {

            }

            @Override
            public void removeOption(JEVisOption option) {

            }

            @Override
            public int compareTo(JEVisAttribute o) {
                return 0;
            }

            @Override
            public void commit() throws JEVisException {

            }

            @Override
            public void rollBack() throws JEVisException {

            }

            @Override
            public boolean hasChanged() {
                return false;
            }

            @Override
            public JEVisDataSource getDataSource() throws JEVisException {
                return null;
            }
        };
    }

    private JEVisObject tmpObject(JsonObject jsonObject) {
        return new JEVisObject() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public void setName(String name) throws JEVisException {

            }

            @Override
            public Long getID() {
                return jsonObject.getId();
            }

            @Override
            public JEVisClass getJEVisClass() throws JEVisException {
                return null;
            }

            @Override
            public String getJEVisClassName() throws JEVisException {
                return jsonObject.getJevisClass();
            }

            @Override
            public List<JEVisObject> getParents() throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisObject> getChildren() throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisObject> getChildren(JEVisClass type, boolean inherit) throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisAttribute> getAttributes() throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
                return null;
            }

            @Override
            public JEVisAttribute getAttribute(String type) throws JEVisException {
                return null;
            }

            @Override
            public boolean delete() throws JEVisException {
                return false;
            }

            @Override
            public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
                return null;
            }

            @Override
            public JEVisObject getLinkedObject() throws JEVisException {
                return null;
            }

            @Override
            public JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) throws JEVisException {
                return null;
            }

            @Override
            public void deleteRelationship(JEVisRelationship rel) throws JEVisException {

            }

            @Override
            public List<JEVisRelationship> getRelationships() throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public List<JEVisClass> getAllowedChildrenClasses() throws JEVisException {
                return new ArrayList<>();
            }

            @Override
            public boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException {
                return false;
            }

            @Override
            public boolean isPublic() throws JEVisException {
                return false;
            }

            @Override
            public void setIsPublic(boolean ispublic) throws JEVisException {

            }

            @Override
            public void addEventListener(JEVisEventListener listener) {

            }

            @Override
            public void removeEventListener(JEVisEventListener listener) {

            }

            @Override
            public void notifyListeners(JEVisEvent event) {

            }

            @Override
            public int compareTo(JEVisObject o) {
                return 0;
            }

            @Override
            public void commit() throws JEVisException {

            }

            @Override
            public void rollBack() throws JEVisException {

            }

            @Override
            public boolean hasChanged() {
                return false;
            }

            @Override
            public JEVisDataSource getDataSource() throws JEVisException {
                return null;
            }
        };
    }


    @PostConstruct
    public void postConstruct() {
        if (this.list != null) {
            this.list.clear();
            this.list = null;
        }

        if (this.ds != null) {
            this.ds.clear();
            this.ds = null;
        }
    }

}
