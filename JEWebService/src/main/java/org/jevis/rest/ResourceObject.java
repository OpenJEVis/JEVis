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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.ws.WebServiceContext;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This Class handels all the JEVisObject related requests
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/objects")
public class ResourceObject {

    private SQLDataSource ds = null;
    private List<JsonObject> returnList;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            @Context WebServiceContext serviceContext,
            @DefaultValue("false") @QueryParam("root") boolean root,
            @DefaultValue("") @QueryParam("class") String jclass,
            @DefaultValue("true") @QueryParam("inherit") boolean inherit,
            @DefaultValue("") @QueryParam("name") String name,
            @DefaultValue("false") @QueryParam("detail") boolean detailed,
            @DefaultValue("true") @QueryParam("rel") boolean rel,
            @QueryParam("parent") long parent,
            @QueryParam("child") long child) {

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);
            this.ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            this.ds.preload(SQLDataSource.PRELOAD.ALL_REL);


            if (root) {
                this.returnList = this.ds.getRootObjects();
            } else {
                this.returnList = this.ds.getUserManager().filterList(this.ds.getObjects());
            }

            if (!jclass.isEmpty()) {
                this.returnList = this.ds.filterObjectByClass(this.returnList, jclass);
            }

            if (rel) {
                this.ds.addRelationshipsToObjects(this.returnList, this.ds.getUserManager().filterReadRelationships(this.ds.getRelationships()));
            }

            if (detailed) {
                //TODO
            }

            //TODO add attributes if needed
            return Response.ok(this.returnList).build();

        } catch (AuthenticationException ex) {
            logger.catching(ex);
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(this.ds);
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

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);

            List<JsonRelationship> list = new LinkedList<JsonRelationship>();

            if (this.ds.getUserManager().canRead(this.ds.getObject(id))) {
                list = this.ds.getUserManager().filterReadRelationships(this.ds.getRelationships(id));
            }

            return Response.ok(list).build();

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(this.ds);
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

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);


            JsonObject obj = this.ds.getObject(id);
            if (this.ds.getUserManager().canDelete(obj)) {
                ds.logUserAction(SQLDataSource.LOG_EVENT.DELETE_OBJECT, String.format("%s:%s|%s", id, obj.getName()));
                this.ds.deleteObject(obj);
            }

            return Response.status(Response.Status.OK).build();

        } catch (JEVisException jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(this.ds);
        }

    }

    /**
     * TODO: check this
     * Is this function in use, because some sub function are not implemented?
     *
     * @param httpHeaders
     * @param request
     * @param url
     * @param copyObject
     * @param object
     * @return
     */
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
        if (object != null && object.length() > 0) {
            try {
                this.ds = new SQLDataSource(httpHeaders, request, url);

                JsonObject json = objectMapper.readValue(object, JsonObject.class);
                if (this.ds.getJEVisClass(json.getJevisClass()) == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("JEVisClass not found").build();
                }

                JsonObject parentObj = this.ds.getObject(json.getParent());
                if (parentObj != null && this.ds.getUserManager().canCreate(parentObj)) {


                    //restful way of moving and object to an other parent while keeping the IDs?
                    if (copyObject > 0) {
                        JsonObject toCopyObj = this.ds.getObject(copyObject);

                        if (toCopyObj != null && this.ds.getUserManager().canCreate(toCopyObj)) {
                            this.ds.moveObject(toCopyObj.getId(), parentObj.getId());
                            return Response.ok(this.ds.getObject(copyObject)).build();
                        } else {
                            return Response.status(Response.Status.UNAUTHORIZED).build();
                        }
                    } else {
                        JsonObject newObj = this.ds.buildObject(json, parentObj.getId());
                        ds.logUserAction(SQLDataSource.LOG_EVENT.CREATE_OBJECT, String.format("%s:%s", newObj.getId(), newObj.getName()));
                        return Response.ok(newObj).build();
                    }

                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("Parent not found").build();
                }

            } catch (AuthenticationException ex) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
            } catch (JsonParseException jex) {
                logger.error("Json parse exception. Error while creating/updating object.", jex);
                return Response.status(Response.Status.BAD_REQUEST).build();
            } catch (JsonMappingException jex) {
                logger.error("Json mapping exception. Error while creating/updating object.", jex);
                return Response.status(Response.Status.BAD_REQUEST).build();
            } catch (IOException jex) {
                logger.error("IO exception. Error while creating/updating object.", jex);
                return Response.status(Response.Status.BAD_REQUEST).build();
            } catch (JEVisException e) {
                logger.catching(e);
                return Response.serverError().entity(e.toString()).build();
            } finally {
                Config.CloseDS(this.ds);
            }
        } else {
            return Response.status(Response.Status.NO_CONTENT).build();
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

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject json = objectMapper.readValue(object, JsonObject.class);
            JsonObject existingObj = this.ds.getObject(id);
            if (existingObj != null && this.ds.getUserManager().canWrite(json)) {
                if (existingObj.getisPublic() != json.getisPublic()) {
                    ds.logUserAction(SQLDataSource.LOG_EVENT.UPDATE_OBJECT, String.format("%s:%s", existingObj.getId(), existingObj.getName()));
                    if (this.ds.getUserManager().isSysAdmin()) {
                        return Response.ok(this.ds.updateObject(id, json.getName(), json.getisPublic())).build();
                    } else {
                        return Response.ok(this.ds.updateObject(id, json.getName(), existingObj.getisPublic())).build();
                    }
                } else {
                    return Response.ok(this.ds.updateObject(id, json.getName(), existingObj.getisPublic())).build();
                }
            } else {
                return Response.notModified().build();
            }

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (JsonMappingException e) {
            logger.catching(e);
            return Response.serverError().entity(e.toString()).build();
        } catch (JEVisException | IOException jex) {
            logger.catching(jex);
            return Response.serverError().entity(jex.toString()).build();
        } finally {
            Config.CloseDS(this.ds);
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

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);

            if (id == -999) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing id path parameter").build();
            }
            JsonObject existingObj = this.ds.getObject(id, includeChildren);
            if (existingObj != null || this.ds.getUserManager().canRead(existingObj)) {
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
            Config.CloseDS(this.ds);
        }
    }

    @PostConstruct
    public void postConstruct() {
        if (this.returnList != null) {
            this.returnList.clear();
            this.returnList = null;
        }
        if (this.ds != null) {
            this.ds.clear();
            this.ds = null;
        }
    }

//    @PreDestroy
//    public void preDestroy() {
//        System.out.println("PerRequest @PreDestroy invoked!");
//
//    }

}
