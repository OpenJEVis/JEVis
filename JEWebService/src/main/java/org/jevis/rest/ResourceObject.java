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

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This Class handels all the JEVisObject related requests
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/objects")
public class ResourceObject {

    private static final Logger logger = LogManager.getLogger(ResourceObject.class);

    /**
     * Get an list of JEVisObject Resource.
     * <p>
     * TODO: maybe use an async response!?
     * https://jersey.java.net/documentation/latest/async.html
     *
     * @param httpHeaders
     * @param request
     * @param url
     * @param root
     * @param inherit
     * @param parent
     * @param name
     * @param detailed
     * @param rel
     * @param child
     * @param jclass
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("false") @QueryParam("root") boolean root,
            @DefaultValue("") @QueryParam("class") String jclass,
            @DefaultValue("true") @QueryParam("inherit") boolean inherit,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("false") @QueryParam("detail") boolean detailed,
            @DefaultValue("true") @QueryParam("rel") boolean rel,
            @QueryParam("parent") long parent,
            @QueryParam("child") long child) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "getObject");
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);
            List<JsonObject> returnList;


            if (root) {
                returnList = ds.getRootObjects();
            } else {
                returnList = ds.getUserManager().filterList(ds.getObjects());
            }

            if (!jclass.isEmpty()) {
                returnList = ds.filterObjectByClass(returnList, jclass);
            }

            if (rel) {
                ds.addRelationhsipsToObjects(returnList, ds.getUserManager().filterReadRelationships(ds.getRelationships()));
            }

            if (detailed) {
                //TODO
            }

            //TODO add attributes if needet
            ds.getProfiler().addEvent("ObjectResource", "done");
            return Response.ok(returnList).build();

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

    /**
     * @param id
     * @return
     */
    @GET
    @Logged
    @Path("{id}/relationships")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationship(
            @PathParam("id") long id,
            @Context Request request,
            @Context UriInfo url,
            @Context HttpHeaders httpHeaders) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "getRelationship");

            List<JsonRelationship> list = new LinkedList<JsonRelationship>();

            if (ds.getUserManager().canRead(ds.getObject(id))) {
                list = ds.getUserManager().filterReadRelationships(ds.getRelationships(id));
            }

            ds.getProfiler().addEvent("ObjectResource", "done");
            return Response.ok(list).build();

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
    @Path("/{id}")
    public Response deleteObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "deleteObject");

            JsonObject obj = ds.getObject(id);
            if (ds.getUserManager().canDelete(obj)) {
                ds.deleteObject(obj);
            }

            ds.getProfiler().addEvent("ObjectResource", "done");
            return Response.status(Response.Status.OK).build();

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
    public Response postObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("-999") @QueryParam("copy") long copyObject,
            String object) {

        SQLDataSource ds = null;
        try {
            System.out.println("Build OBject: " + object);
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "postObject");

            JsonObject json = (new Gson()).fromJson(object, JsonObject.class);
            if (ds.getJEVisClass(json.getJevisClass()) == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("JEVisClass not found").build();
            }

            JsonObject parentObj = ds.getObject(json.getParent());
            if (parentObj != null && ds.getUserManager().canCreate(parentObj)) {


                //resful way of moving and object to an other parent while keeping the IDs?
                if (copyObject != -999) {
                    JsonObject toCopyObj = ds.getObject(copyObject);

                    if (toCopyObj != null && ds.getUserManager().canCreate(toCopyObj)) {
                        ds.moveObject(toCopyObj.getId(), parentObj.getId());
                        return Response.ok(ds.getObject(copyObject)).build();
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } else {
                    System.out.println("Build object: [" + json.getId() + "]" + json.getName() + " under: " + parentObj.getId());
                    JsonObject newObj = ds.buildObject(json, parentObj.getId());
                    System.out.println("New Object: [" + newObj.getId() + "]" + newObj.getName() + " under: " + parentObj.getId());
                    return Response.ok(newObj).build();
                }


                //normal create object function

            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Parent not found").build();
            }

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().entity(jex.toString()).build();
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
    @Path("/{id}")
    public Response updateObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            String object) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "updateObject");

            JsonObject json = (new Gson()).fromJson(object, JsonObject.class);
            JsonObject existingObj = ds.getObject(id);
            if (existingObj != null && ds.getUserManager().canWrite(json)) {
                if (existingObj.getisPublic() != json.getisPublic()) {
                    if (ds.getUserManager().isSysAdmin()) {
                        ds.getProfiler().addEvent("ObjectResource", "done");
                        return Response.ok(ds.updateObject(id, json.getName(), json.getisPublic())).build();
                    } else {
                        //@TODO: Throw exeption??
                        ds.getProfiler().addEvent("ObjectResource", "done");
                        return Response.ok(ds.updateObject(id, json.getName(), existingObj.getisPublic())).build();
                    }
                } else {
                    ds.getProfiler().addEvent("ObjectResource", "done");
                    return Response.ok(ds.updateObject(id, json.getName(), existingObj.getisPublic())).build();
                }
            } else {
                return Response.notModified().build();
            }

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().entity(jex.toString()).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Get the JEVisObject with the given id.
     *
     * @param httpHeaders
     * @param detailed
     * @param id          jevis internal id of an JEVisObject
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("false") @QueryParam("detail") boolean detailed,
            @DefaultValue("false") @QueryParam("includeChildren") boolean includeChildren,
            @DefaultValue("-99999") @PathParam("id") long id) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ObjectResource", "getObject");

            if (id == -999) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing id path parameter").build();
            }
            JsonObject existingObj = ds.getObject(id, includeChildren);
            if (existingObj != null || ds.getUserManager().canRead(existingObj)) {
                ds.getProfiler().addEvent("ObjectResource", "done");
                return Response.ok(existingObj).build();
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

}
