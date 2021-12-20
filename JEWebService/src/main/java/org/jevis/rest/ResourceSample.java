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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.utils.Samples;
import org.jevis.commons.ws.json.*;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.JEVisClassHelper;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * this Class handles all the JEVisSample related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/objects/{id}/attributes/{attribute}/samples")
public class ResourceSample {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceSample.class);
    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmss").withZoneUTC();
    private SQLDataSource ds = null;
    private List<JsonSample> list;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * List of classes which can be updated with the execute permission
     **/
    public final List<String> executeUpdateExceptions = Lists.newArrayList("Data Notes", "User Data", "Clean Data");


    //JEWebService/v1/files/8598/attributes/File/samples/files/20180604T141441?filename=nb-configuration.xml
    //JEWebService/v1/objects/{id}/attributes/{attribute}/samples
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
            @DefaultValue("nameless.file") @QueryParam("filename") String filename,
            @DefaultValue("now") @PathParam("timestamp") String timestamp,
            //            @DefaultValue("file.file") @QueryParam("filename") String filename,
            InputStream payload
    ) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessable").build();
            }

            if (obj.getJevisClass().equals("User") && obj.getId() == ds.getCurrentUser().getUserID()) {
                if (attribute.equals("Enabled") || attribute.equals("Sys Admin")) {
                    throw new JEVisException("permission denied", 3022);
                }
            } else {
                ds.getUserManager().canWrite(obj);//thows exception
            }

            if (timestamp.equals("now")) {
                timestamp = fmt.print(new DateTime());
            }

            ds.getUserManager().canWrite(obj);//can throw exception

            //Your local disk path where you want to store the file
            String uploadedFileLocation = createFilePattern(id, attribute, filename, fmt.parseDateTime(timestamp));

            File objFile = new File(uploadedFileLocation);
            if (objFile.exists()) {
                objFile.delete();
            } else {
                if (!objFile.getParentFile().exists()) {
                    objFile.getParentFile().mkdirs();
                }
            }

            saveToFile(payload, uploadedFileLocation);

            JsonSample sample = new JsonSample();
            sample.setTs(JsonFactory.sampleDTF.print(fmt.parseDateTime(timestamp)));
            sample.setValue(filename);
            sample.setNote("Uploaded by " + ds.getCurrentUser().getAccountName());

            List<JsonSample> samples = new ArrayList<>();
            samples.add(sample);

            JsonType type = JEVisClassHelper.getType(obj.getJevisClass(), attribute);

            int result = ds.setSamples(id, attribute, type.getPrimitiveType(), samples);
            samples.clear();
            return Response.status(200).build();

        } catch (AuthenticationException ex) {
            logger.error("Auth error: {}", ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
//            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();

        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }


    private String createFilePattern(long id, String attribute, String fileName, DateTime dateTime) {
        String absoluteFileDir = Config.getFileDir().getAbsolutePath()
                + File.separator + id
                + File.separator + attribute
                + File.separator + DateTimeFormat.forPattern("yyyyMMddHHmmss").withZoneUTC().print(dateTime)
                + "_" + fileName;
        return absoluteFileDir;
    }

    private void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
        try {
            OutputStream out = null;
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    ) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null || !ds.getUserManager().canRead(obj)) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessible").build();
            }

            DateTime ts = null;
            if (!timestamp.equals("latest")) {
                ts = fmt.parseDateTime(timestamp).withZone(DateTimeZone.UTC);
            }

            List<JsonSample> samples = ds.getSamples(id, attribute, ts, ts, 1);

            if (!samples.isEmpty()) {
                JsonSample sample = samples.get(0);
                DateTime dbTS = JsonFactory.sampleDTF.parseDateTime(samples.get(0).getTs());

                //Pattern  /path/to/filedir/yyyyMMdd/ID_HHmmss_filename
                String fileName = createFilePattern(id, attribute, sample.getValue(), dbTS);
                File file = new File(fileName);
                if (file.exists() && file.canRead()) {
                    ResponseBuilder response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM);
                    response.header("Content-Disposition",
                            "attachment; filename=\"" + sample.getValue() + "\"");
                    return response.build();
                } else {
                    Response.status(Status.INTERNAL_SERVER_ERROR).build();
                }
            }

            return Response.status(Status.NOT_FOUND).build();


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
            @DefaultValue("false") @QueryParam("cwd") boolean customWorkDay,
            @DefaultValue("") @QueryParam("ap") String aggregationPeriod,
            @DefaultValue("") @QueryParam("mm") String manipulationMode,
            @DefaultValue("UTC") @QueryParam("tz") String timeZone,
            @DefaultValue("1000000") @QueryParam("limit") long limit,
            @DefaultValue("false") @QueryParam("onlyLatest") boolean onlyLatest
    ) {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("Object is not accessible").build();
            }

            if (obj.getJevisClass().equals("User") && obj.getId() == ds.getCurrentUser().getUserID()) {
                if (attribute.equals("Enabled") || attribute.equals("Sys Admin")) {
                    throw new JEVisException("permission denied", 3022);
                }
            } else {
                ds.getUserManager().canRead(obj);
            }

            logger.trace("got Object: {}", obj);

            JsonAttribute att = ds.getAttribute(id, attribute);
            if (att == null) {
                return Response.status(Status.NOT_FOUND)
                        .entity("No such Attribute").build();
            }

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

            if (onlyLatest) {
                logger.trace("Last sample mode");

                JsonSample sample = ds.getLastSample(id, attribute);
                if (sample != null) {
                    return Response.ok(sample).build();
                } else {
                    return Response.status(Status.NOT_FOUND).entity("Has no samples").build();
                }

            }

            if ((aggregationPeriod.equals("") && manipulationMode.equals(""))
                    || (aggregationPeriod.equals("NONE") && manipulationMode.equals("NONE"))) {
                list = ds.getSamples(id, attribute, startDate, endDate, limit);
            } else {
                AggregationPeriod ap = AggregationPeriod.parseAggregation(aggregationPeriod);
                ManipulationMode mm = ManipulationMode.parseManipulation(manipulationMode);
                DateTimeZone dateTimeZone = DateTimeZone.forID(timeZone);
                JsonSampleGenerator sg = new JsonSampleGenerator(ds, obj, att, startDate, endDate, customWorkDay, mm, ap, dateTimeZone);

                list = sg.getAggregatedSamples();
            }

            return Response.ok(list).build();

            /*
            for (JsonAttribute att : attributes) {
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

                    if (onlyLatest) {
                        logger.trace("Last sample mode");

                        JsonSample sample = ds.getLastSample(id, attribute);
                        if (sample != null) {
                            return Response.ok(sample).build();
                        } else {
                            return Response.status(Status.NOT_FOUND).entity("Has no samples").build();
                        }

                    }

                    if ((aggregationPeriod.equals("") && manipulationMode.equals(""))
                            || (aggregationPeriod.equals("NONE") && manipulationMode.equals("NONE"))) {
                        list = ds.getSamples(id, attribute, startDate, endDate, limit);
                    } else {
                        AggregationPeriod ap = AggregationPeriod.parseAggregation(aggregationPeriod);
                        ManipulationMode mm = ManipulationMode.parseManipulation(manipulationMode);
                        DateTimeZone dateTimeZone = DateTimeZone.forID(timeZone);
                        JsonSampleGenerator sg = new JsonSampleGenerator(ds, obj, att, startDate, endDate, customWorkDay, mm, ap, dateTimeZone);

                        list = sg.getAggregatedSamples();
                        sg = null;
                    }

                    return Response.ok(list).build();

                }
            }

            return Response.status(Status.NOT_FOUND)
                    .entity("No such Attribute").build();

            */
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.error("Error while fetching sample: {}-{} {}->{}", id, attribute, start, end);
            logger.error(jex);
            //jex.printStackTrace();
            return Response.serverError().entity(jex).build();
        } finally {
            Config.CloseDS(ds);
        }

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

        if (input != null && input.length() > 0) {
            try {
                ds = new SQLDataSource(httpHeaders, request, url);
                logger.debug("Post: {}-{}  input: {}", id, attribute, input);
                JsonObject object = ds.getObject(id);
                boolean canExecute = false;
                boolean canWrite = false;


                if (object.getJevisClass().equals("User") && !ds.getUserManager().isSysAdmin()) {
                    if (attribute.equals("Sys Admin")) {
                        throw new JEVisException("permission denied", 3023);
                    }
                }

                if (object.getJevisClass().equals("User") && object.getId() == ds.getCurrentUser().getUserID()) {
                    if (attribute.equals("Enabled") || attribute.equals("Sys Admin")) {
                        throw new JEVisException("permission denied", 3022);
                    }
                } else {

                    /**
                     * hotfix implementation to allow users to update notes if they have execute permission but not write
                     */
                    canWrite = ds.getUserManager().canWriteWOE(object);
                    logger.debug("canWrite {}", canWrite);
                    if (!canWrite) {
                        if (ds.getUserManager().canExecuteWOE(object) && executeUpdateExceptions.contains(object.getJevisClass())) {
                            canExecute = true;
                            logger.debug("canExecute {}", canExecute);
                        }

                    }

                    if (!canWrite && !canExecute) {
                        throw new JEVisException("permission denied", 3021);
                    }
                }


                List<JsonAttribute> attributes = ds.getAttributes(id);
                for (JsonAttribute att : attributes) {
                    if (att.getType().equals(attribute)) {
                        List<JsonSample> samples = new ArrayList<>(Arrays.asList(objectMapper.readValue(input, JsonSample[].class)));
                        JsonType type = JEVisClassHelper.getType(object.getJevisClass(), att.getType());


                        // If user can write -> OK
                        // If user can execute and only Note changed -> OK
                        int result = 0;
                        if (canWrite || (canExecute &&
                                (object.getJevisClass().equals("Data Notes") || object.getJevisClass().equals("User Data")))) {
                            logger.debug("canWrite import");
                            result = ds.setSamples(id, attribute, type.getPrimitiveType(), samples);
                        } else if (canExecute && object.getJevisClass().equals(this.executeUpdateExceptions.get(2))) {
                            /** update notes but not samples **/
                            logger.debug("canExecute Sample: {}-{} for: {}", id, att, ds.getCurrentUser().getAccountName());
                            for (JsonSample jsonSample : samples) {
                                logger.debug("jsonSample: {}", jsonSample);
                                List<JsonSample> sampleList = new ArrayList<>();
                                try {
                                    List<JsonSample> onDB = ds.getSampleTable().getSamples(id, attribute, JsonFactory.sampleDTF.parseDateTime(jsonSample.getTs()), JsonFactory.sampleDTF.parseDateTime(jsonSample.getTs()), 1);
                                    for (JsonSample dbSample : onDB) {
                                        if (dbSample.getValue().equals(jsonSample.getValue())) {
                                            sampleList.add(jsonSample);
                                            logger.debug("dbSample: {}", dbSample);
                                        }
                                    }

                                } catch (JEVisException jex) {
                                    logger.error(jex);
                                }
                                logger.debug("Add Notes: {}", sampleList);
                                result += ds.setSamples(id, attribute, type.getPrimitiveType(), sampleList);
                            }
                        }

                        ds.logUserAction(SQLDataSource.LOG_EVENT.CREATE_SAMPLE, String.format("%s:%s|%s|%s", id, attribute, result, Samples.getDuration(samples)));
                        samples.clear();
                        samples = null;

                        return Response.status(Status.CREATED).build();
                    }
                }

                return Response.status(Status.NOT_MODIFIED).build();

            } catch (AuthenticationException ex) {
                return Response.status(Status.UNAUTHORIZED).entity(ex.getMessage()).build();
            } catch (JsonParseException jex) {
                logger.error("Json parse exception. Error while posting sample(s) ", jex);
                return Response.status(Status.BAD_REQUEST).build();
            } catch (JsonMappingException jex) {
                logger.error("Json mapping exception. Error while posting sample(s) ", jex);
                return Response.status(Status.BAD_REQUEST).build();
            } catch (IOException jex) {
                logger.error("IO exception. Error while posting sample(s) ", jex);
                return Response.status(Status.BAD_REQUEST).build();
            } catch (JEVisException jex) {
                logger.catching(jex);
                return Response.serverError().build();
            } finally {
                Config.CloseDS(ds);
            }
        } else {
            return Response.status(Status.NO_CONTENT).build();
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

        try {
            logger.debug("Delete Sample: {}:{} from:{} to:{}", id, attribute, start, end);
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject object = ds.getObject(id);
            if (object.getJevisClass().equals("User") && object.getId() == ds.getCurrentUser().getUserID()) {
                if (attribute.equals("Enabled") || attribute.equals("Sys Admin")) {
                    throw new JEVisException("permission denied", 3022);
                }
            } else if (object.getJevisClass().equals("Data Notes") || object.getJevisClass().equals("User Data")) {
                if (!ds.getUserManager().canExecuteWOE(object)) {
                    ds.getUserManager().canDelete(object);
                }
            } else {
                ds.getUserManager().canDelete(object);
            }
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

            ds.logUserAction(SQLDataSource.LOG_EVENT.DELETE_SAMPLE, String.format("%s:%s|%s -> %s", id, attribute, startDate, endDate));
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

    @PostConstruct
    public void postConstruct() {
        if (list != null) {
            list.clear();
            list = null;
        }

        if (ds != null) {
            ds.clear();
            ds = null;
        }
    }

}
