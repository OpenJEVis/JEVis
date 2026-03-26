/**
 * Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>
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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.json.JsonTools;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Jersey REST resource that handles JEVisClass (schema) CRUD operations and icon serving.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET    /classes}          — list all JEVisClasses</li>
 *   <li>{@code GET    /classes/{name}}   — fetch one class by name</li>
 *   <li>{@code POST   /classes/{name}}   — create or update a class (sysadmin only)</li>
 *   <li>{@code DELETE /classes/{name}}   — delete a class definition (sysadmin only)</li>
 *   <li>{@code GET    /classes/{name}/icon} — fetch the class icon image</li>
 *   <li>{@code POST   /classes/{name}/icon} — upload a new class icon (sysadmin only)</li>
 * </ul>
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/classes")
public class ResourceClasses {

    private static final Logger logger = LogManager.getLogger(ResourceClasses.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private SQLDataSource ds = null;
    private List<File> classIconFiles;

    private static List<File> listClassIconFiles(File dir) {
        List<File> fileTree = new ArrayList<>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                if (entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg") || entry.getName().endsWith(".gif")) {
                    fileTree.add(entry);
                }
            } else {
                fileTree.addAll(listClassIconFiles(entry));
            }
        }
        return fileTree;
    }

    /**
     * Returns all known JEVisClasses as a JSON collection.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @return 200 OK with a collection of {@link org.jevis.commons.ws.json.JsonJEVisClass},
     *         401 UNAUTHORIZED, or 500 on error
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            return getClassResponse(null);
        } catch (JEVisException jex) {
            logger.error(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns the JEVisClass with the given name.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param name        class name (e.g. {@code "Data"})
     * @return 200 OK with the {@link org.jevis.commons.ws.json.JsonJEVisClass},
     *         401 UNAUTHORIZED, 404 NOT FOUND if the class is unknown, or 500 on error
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

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            return getClassResponse(name);

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Deletes the JEVisClass definition file for the given class name.
     * Only system administrators may call this endpoint.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param name        class name to delete
     * @return 200 OK on success, 401 UNAUTHORIZED, 404 NOT FOUND, or 500 on error
     */
    @DELETE
    @Logged
    @Path("/{name}")
    public Response deleteJEVisClass(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name) {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);


            JsonJEVisClass jclass = Config.getClassCache().get(name);
            if (jclass != null || ds.getUserManager().isSysAdmin()) {
                //TODO: delete orphaned relationships on other classes
//                for (Map.Entry<String, JsonJEVisClass> jc : Config.getClassCache().entrySet()) {
//                    //delete.relationship with delete class
//                }

                for (File file : Config.getClassDir().listFiles()) {
                    if (file.getName().equalsIgnoreCase(name) && file.canWrite()) {
                        file.delete();
                        //TODO: Also delete Icon
                        return Response.ok().build();
                    }
                }

                return Response.status(Response.Status.NOT_FOUND).build();

            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
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

    /**
     * Creates or replaces the JEVisClass definition for the given name.
     * Only system administrators may call this endpoint; changes are immediately reflected in the
     * class cache.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param name        class name to create or replace
     * @param input       JSON body of the {@link org.jevis.commons.ws.json.JsonJEVisClass}
     * @return 200 OK with the stored class, 401 UNAUTHORIZED, or 500 on error
     */
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

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (ds.getUserManager().isSysAdmin()) {
//                JsonJEVisClass json = (new Gson()).fromJson(input, JsonJEVisClass.class);//parse it again to be save and to make it pretty
                JsonJEVisClass json = OBJECT_MAPPER.readValue(input, JsonJEVisClass.class);
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                PrintWriter writer = new PrintWriter(Config.getClassDir().getAbsoluteFile() + "/" + name + ".json", "UTF-8");
//                writer.println(gson.toJson(json));
                JsonTools.prettyObjectMapper().writeValueAsString(json);
                writer.close();


                Config.getClassCache().clear();
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
     * Returns the icon image for the requested JEVisClass.
     *
     * <p>Searches the configured class icon directory for a file whose base name (without extension)
     * matches {@code name} (case-insensitive). Falls back to {@code MissingPlaceholder.png} if no
     * match is found.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param name        class name whose icon is requested
     * @return 200 OK with the image bytes, 401 UNAUTHORIZED, or 500 on error
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
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (classIconFiles == null) {
                classIconFiles = listClassIconFiles(Config.getClassDir());
            }

            for (File icon : classIconFiles) {
                int lastDot = icon.getName().lastIndexOf(".");
                if (name.equalsIgnoreCase(icon.getName().substring(0, lastDot))) {
                    return Response.ok(ImageIO.read(icon)).build();
                }
            }
            File placeholder = new File(Config.getClassDir().getAbsolutePath() + "MissingPlaceholder.png");
            return Response.ok(ImageIO.read(placeholder)).build();


        } catch (JEVisException jex) {
            logger.fatal(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (IOException ioex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ioex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Helper that builds the class response from the in-memory cache.
     *
     * @param classname if {@code null} or empty, returns all classes; otherwise returns the single
     *                  named class or 404 if not found
     * @return JAX-RS {@link Response}
     */
    public Response getClassResponse(String classname) {
        if (classname == null || classname.isEmpty()) {
            return Response.ok(Config.getClassCache().values()).build();
        } else {
            if (Config.getClassCache().containsKey(classname)) {
                return Response.ok(Config.getClassCache().get(classname)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }


    /**
     * Uploads a new icon image for the given JEVisClass.
     * Only system administrators may call this endpoint. The uploaded image is stored as a PNG and
     * the icon file cache is invalidated.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param name        class name for which to store the icon
     * @param imageBytes  raw image bytes (PNG, JPG, or GIF)
     * @return 200 OK on success, 401 UNAUTHORIZED, or 500 on error
     */
    @POST
    @Path("/{name}/icon")
    @Consumes({"image/png", "image/jpg", "image/gif"})
    public Response postClassIcon(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name, byte[] imageBytes) {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            try {
                File newFile = new File(Config.getClassDir().getAbsoluteFile() + "/" + name + ".png");
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                //TODO: maybe resize the image to an default size
                ImageIO.write(img, "png", newFile);

                //clean cache
                File tmpZipFile = new File(FileCache.CLASS_ICON_FILE);
                tmpZipFile.delete();

                return Response.status(Response.Status.OK).build();
            } catch (IOException ioex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ioex.getMessage()).build();
            } finally {
                Config.CloseDS(ds);
            }


        } catch (JEVisException jex) {
            logger.error("ClassIcon upload error: {}", jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception ex) {
            logger.error("Error while uploading class icon {}", ex);
            return Response.serverError().entity(ex).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Jersey lifecycle callback — clears transient per-request state after the response is committed.
     */
    @PostConstruct
    public void postConstruct() {
        if (ds != null) {
            ds.clear();
            ds = null;
        }

    }

}
