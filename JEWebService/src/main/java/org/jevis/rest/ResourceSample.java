/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisFile;
import org.jevis.api.JEVisSample;
import org.jevis.commons.JEVisFileImp;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonFactory;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.ws.sql.SQLDataSource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * this Class handles all the JEVisSample related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/objects/{id}/attributes/{attribute}/samples")
public class ResourceSample {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceSample.class);
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();

    /**
     * Get the samples from an object/Attribute
     *
     * @param context
     * @param httpHeaders
     * @param id
     * @param attribute
     * @param start
     * @param end
     * @param onlyLatest
     * @return
     * @throws JEVisException
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
            @DefaultValue("1000000") @QueryParam("limit") long limit,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("SampleResource", "Start");

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessable").build();
            }

            if (ds.getUserManager().canRead(obj)) {
                //will throw exception if not 
            }

            logger.trace("got Object: {}", obj);

            List<JsonAttribute> atts = ds.getAttributes(id);
            for (JsonAttribute att : atts) {
                if (att.getType().equals(attribute)) {
                    ds.getProfiler().addEvent("AttributeResource", "got attribute");
                    DateTime startDate = null;
                    DateTime endDate = null;
                    if (start != null) {
                        startDate = fmt.parseDateTime(start);
                    }
                    if (end != null) {
                        endDate = fmt.parseDateTime(end);
                    }

                    if (onlyLatest == true) {
                        logger.trace("Lastsample mode");

                        JsonSample sample = ds.getLastSample(id, attribute);
                        ds.getProfiler().addEvent("AttributeResource", "getlastsample");
                        if (sample != null) {
                            return Response.ok(sample).build();
                        } else {
                            return Response.status(Status.NOT_FOUND).entity("Has no samples").build();
                        }

                    }
                    List<JsonSample> list = ds.getSamples(id, attribute, startDate, endDate, limit);
                    ds.getProfiler().addEvent("AttributeResource", "get  " + list.size() + "  samples ");
//                        JsonSample[] returnList = list.toArray(new JsonSample[list.size()]);

                    return Response.ok(list).build();

                }
            }
            return Response.status(Status.NOT_FOUND)
                    .entity("No such Attribute").build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().entity(jex).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @POST
    @Logged
    @Path("/files/{timestamp}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response postSampleFiles(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @DefaultValue("nameless.file") @PathParam("filename") String filename,
            @DefaultValue("latest") @PathParam("timestamp") String timestamp,
            //            @DefaultValue("file.file") @QueryParam("filename") String filename,
            InputStream payload
    ) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessable").build();
            }

            ds.getUserManager().canWrite(obj);//thows exception

            DateTime ts = fmt.parseDateTime(timestamp).withZone(DateTimeZone.UTC);
           
            //TODO: check size an type
            byte[] bytes = IOUtils.toByteArray(payload);
            
            JEVisFile file = new JEVisFileImp(filename, bytes);
            if (ds.setFile(id, attribute, ts, file)) {
                return Response.ok().build();
            } else {
                return Response.notModified().build();
            }

        } catch (AuthenticationException ex) {
            logger.error("Auth errror: {}", ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
//            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();

        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @GET
    @Logged
    @Path("/files/{timestamp}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getSampleFile(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @DefaultValue("latest") @PathParam("timestamp") String timestamp
    ) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null || !ds.getUserManager().canRead(obj)) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessable").build();
            }

            DateTime ts = null;
            if (!timestamp.equals("latest")) {
                ts = fmt.parseDateTime(timestamp).withZone(DateTimeZone.UTC);
            }

            JEVisFile file = ds.getFile(id, attribute, ts);

            if (file == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            ResponseBuilder response = Response.ok(file.getBytes(), MediaType.APPLICATION_OCTET_STREAM);
            response.header("Content-Disposition",
                    "attachment; filename=\"" + file.getFilename() + "\"");
            return response.build();

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Get all Samples between the given time-range
     *
     * @param att
     * @param start
     * @param end
     * @return
     * @throws JEVisException
     */
    private List<JsonSample> getInBetween(JEVisAttribute att, DateTime start, DateTime end) throws JEVisException {
        List<JsonSample> samples = new LinkedList<JsonSample>();
        int primitivType = att.getPrimitiveType();
        for (JEVisSample sample : att.getSamples(start, end)) {
            samples.add(JsonFactory.buildSample(sample, primitivType));
        }
        return samples;
    }

    /**
     * Get all Samples for a JEVisAttribute
     *
     * @param att
     * @return
     * @throws JEVisException
     */
    private List<JsonSample> getAll(JEVisAttribute att) throws JEVisException {
        return getInBetween(att, null, null);
    }

    @POST
    @Logged
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postSamples(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            String input) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject object = ds.getObject(id);
            ds.getUserManager().canWrite(object);

            List<JsonAttribute> atts = ds.getAttributes(id);
            for (JsonAttribute att : atts) {
                if (att.getType().equals(attribute)) {
                    List<JsonSample> sampes = new Gson().fromJson(input, new TypeToken<List<JsonSample>>() {
                    }.getType());
                    JsonType type = ds.getType(object.getJevisClass(), att.getType());
                    int result = ds.setSamples(id, attribute, type.getPrimitiveType(), sampes);
                    return Response.status(Status.CREATED).build();
                }
            }

            return Response.status(Status.NOT_MODIFIED).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    @Logged
    public Response deleteSamples(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            @QueryParam("from") String start,
            @QueryParam("until") String end,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject object = ds.getObject(id);
            ds.getUserManager().canDelete(object);

            JsonAttribute att = ds.getAttribute(id, attribute);

            DateTime startDate = null;
            DateTime endDate = null;
            if (onlyLatest) {

                //TODO
            } else {
                // define the timerange to delete samples from
                if (start != null) {
                    startDate = fmt.parseDateTime(start);
                }
                if (end != null) {
                    endDate = fmt.parseDateTime(end);
                }
            }

            if (startDate == null && endDate == null) {
                logger.debug("Delete All");
                ds.deleteAllSample(object.getId(), attribute);
            } else {
                logger.debug("Delete Between");
                ds.deleteSamplesBetween(object.getId(), attribute, startDate, endDate);
            }

            return Response.status(Status.OK).build();

        } catch (AuthenticationException ex) {
            logger.catching(ex);
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
