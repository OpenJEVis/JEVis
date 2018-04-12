/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.SQLtoJsonFactory;

/**
 * This class handels all the JEVIsOBjects related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/classes")
public class ResourceClasses {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceClasses.class);
    public final static String TEMPDIR = "JEWebService";
    public final static String TEMPFILE = "Classes.json";

    /**
     * Returns an List of JEVisClasses as Json
     *
     * @param httpHeaders
     * @return
     * @throws JEVisException
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("false") @QueryParam("includeType") boolean includeType) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            return getCacheFile(ds);
        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns the requested JEVisClass
     *
     * @param httpHeaders
     * @param context
     * @param name
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    public Response getJEVisClass(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            JsonJEVisClass jclass = ds.getJEVisClass(name);

            if (jclass != null) {
                return Response.ok(jclass).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    @Logged
    @Path("/{name}")
    public Response deleteJEVisClass(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonJEVisClass jclass = ds.getJEVisClass(name);
            if (jclass != null || ds.getUserManager().isSysAdmin()) {
                boolean delete = ds.deleteClass(name);
                if (delete) {
                    FileCache.deleteClassCachFiles();
                    return Response.ok().build();
                } else {

                    return Response.notModified().build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @POST
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    public Response postJEVisClass(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name,
            String input) {

        SQLDataSource ds = null;
        try {
            System.out.println("PostClass: " + input);
            ds = new SQLDataSource(httpHeaders, request, url);

            if (ds.getUserManager().isSysAdmin()) {
                JsonJEVisClass json = (new Gson()).fromJson(input, JsonJEVisClass.class);
                ds.setJEVisClass(name, json);
                FileCache.deleteClassCachFiles();
                return Response.ok(ds.getJEVisClass(name)).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns the Icon of the requested JEVisClass
     *
     * @param httpHeaders
     * @param context
     * @param name
     * @return
     */
    @GET
    @Logged
    @Path("/{name}/icon")
    @Produces({"image/png", "image/jpg", "image/gif"})
    public Response getClassIcon(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name
    ) {
        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            BufferedImage img = ds.getJEVisClassIcon(name);
            if (img != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ImageIO.write(img, "png", baos);
                } catch (IOException ex) {
                    Logger.getLogger(ResourceClasses.class.getName()).log(Level.SEVERE, null, ex);
                }
                byte[] imageData = baos.toByteArray();
                return Response.ok(new ByteArrayInputStream(imageData), MediaType.valueOf("image/png")).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (JEVisException jex) {
            Logger.getLogger(ResourceClasses.class.getName()).log(Level.SEVERE, null, jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    public Response getCacheFile(SQLDataSource ds) throws JEVisException {

        try {
            ds.getProfiler().addEvent("ResourceClasses", "getCachedClasses");

            File tmpZipFile = FileCache.getClassFile();
            if (tmpZipFile.exists()) {
                Type REVIEW_TYPE = new TypeToken<List<JsonJEVisClass>>() {
                }.getType();
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(tmpZipFile));
                List<JsonJEVisClass> data = gson.fromJson(reader, REVIEW_TYPE);
                ds.getProfiler().addEvent("ResourceClasses", "done from cache");
                return Response.ok(data).build();
            }

            List<JsonJEVisClass> jclasses = ds.getJEVisClasses();
            Map<String, JsonJEVisClass> map = SQLtoJsonFactory.toMap(jclasses);
            SQLtoJsonFactory.addTypesToClasses(map, ds.getAllTypes());
            SQLtoJsonFactory.addRelationhipsToClasses(map, ds.getClassRelationships());

            try (Writer writer = new FileWriter(tmpZipFile)) {
                Gson gson = new GsonBuilder().create();
                gson.toJson(jclasses, writer);

            }
            ds.getProfiler().addEvent("ResourceIcons", "done build cache");

            Response re = Response.ok(jclasses).build();
            ds.getProfiler().addEvent("ResourceIcons", "done");
            return re;

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     *
     * @param httpHeaders
     * @param fileInputStream
     * @param fileMetaData
     * @param name
     * @param imageBytes
     * @return
     */
    @POST
    @Path("/{name}/icon")
    @Consumes({"image/png", "image/jpg", "image/gif"})
    public Response postClassIcon(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name, byte[] imageBytes) {

        SQLDataSource ds = null;
        try {
            System.out.println("Post icon for: " + name + " byte: " + imageBytes.length);
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ResourceClasses", "putClassIcon");

            JsonJEVisClass jclass = ds.getJEVisClass(name);
            System.out.println("found jclass?: " + jclass);

            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (jclass == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("JEVisClass not found").build();
            }

            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                System.out.println("ClassIcon.width: " + img.getWidth());
                ds.setJEVisClassIcon(name, img);
            } catch (IOException ioex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ioex.getMessage()).build();
            }
            System.out.println("Pre clean cache");
            FileCache.deleteClassCachFiles();
            System.out.println("---- return OK");
            return Response.status(Response.Status.OK).build();

        } catch (JEVisException jex) {
            logger.error("ClassIcon upload error: {}", jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Error while uploadign classicon {}", ex);
            return Response.serverError().entity(ex).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
