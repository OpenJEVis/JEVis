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
import org.jevis.commons.ws.sql.CachedAccessControl;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.ws.WebServiceContext;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jersey REST resource that handles all JEVisObject CRUD operations.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /objects} — list objects, optionally filtered by class, root, parent, child</li>
 *   <li>{@code GET /objects/{id}} — fetch a single object by ID</li>
 *   <li>{@code GET /objects/{id}/relationships} — list relationships for one object</li>
 *   <li>{@code POST /objects} — create a new object (or move an existing one)</li>
 *   <li>{@code POST /objects/{id}} — update an existing object</li>
 *   <li>{@code DELETE /objects/{id}} — delete or mark-as-deleted an object</li>
 * </ul>
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/objects")
public class ResourceObject {

    private static final Logger logger = LogManager.getLogger(ResourceObject.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private SQLDataSource ds = null;
    private List<JsonObject> returnList;

    /**
     * Returns all JEVisObjects visible to the authenticated user, with optional filters.
     *
     * @param httpHeaders     HTTP headers (used for authentication)
     * @param request         JAX-RS request context
     * @param url             URI info context
     * @param serviceContext  web service context
     * @param root            if {@code true}, return only root objects
     * @param jclass          if non-empty, filter by JEVis class name
     * @param inherit         if {@code true}, include objects of subclasses when filtering by class
     * @param name            reserved (currently unused filter)
     * @param detailed        reserved (currently unused)
     * @param rel             if {@code true}, embed relationships in each returned object
     * @param deletedObjects  if {@code true}, include soft-deleted objects; otherwise only live objects
     * @param parent          filter by parent object ID (0 = not used)
     * @param child           filter to return only the given child ID (0 = not used)
     * @return 200 OK with JSON array of {@link org.jevis.commons.ws.json.JsonObject},
     *         401 UNAUTHORIZED, or 500 on error
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
            @DefaultValue("false") @QueryParam("deleted") boolean deletedObjects,
            @QueryParam("parent") long parent,
            @QueryParam("child") long child) {

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);

            if (!deletedObjects) this.ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            this.ds.preload(SQLDataSource.PRELOAD.ALL_REL);


            if (root) {
                this.returnList = this.ds.getRootObjects();
            } else {
                this.returnList = this.ds.getUserManager().filterList(this.ds.getObjects());
            }

            if (!deletedObjects) {
                this.returnList = returnList.stream().filter(jsonObject -> jsonObject.getDeleteTS() == null).collect(Collectors.toList());
            }

            if (!jclass.isEmpty()) {
                this.returnList = this.ds.filterObjectByClass(this.returnList, jclass, inherit);
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
     * Returns all relationships involving the object with the given ID, filtered to those the
     * authenticated user may read.
     *
     * @param id          the JEVis object ID
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param httpHeaders HTTP headers (used for authentication)
     * @return 200 OK with JSON array of {@link org.jevis.commons.ws.json.JsonRelationship},
     *         401 UNAUTHORIZED, or 500 on error
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

    /**
     * Deletes or soft-deletes the object with the given ID.
     *
     * @param httpHeaders   HTTP headers (used for authentication)
     * @param request       JAX-RS request context
     * @param url           URI info context
     * @param deleteForever if {@code true}, permanently removes the object;
     *                      otherwise marks it as deleted (recoverable)
     * @param id            the JEVis object ID to delete
     * @return 200 OK on success, 401 UNAUTHORIZED, or 500 on error
     */
    @DELETE
    @Logged
    @Path("/{id}")
    public Response deleteObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("false") @QueryParam("deleteForever") boolean deleteForever,
            @PathParam("id") long id) {

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);


            JsonObject obj = this.ds.getObject(id);
            if (this.ds.getUserManager().canDelete(obj)) {

                if (deleteForever) {
                    ds.logUserAction(SQLDataSource.LOG_EVENT.DELETE_OBJECT, String.format("%s:%s", id, obj.getName()));
                    logger.error("Delete forever: " + id);
                    this.ds.deleteObject(obj);
                } else {
                    ds.logUserAction(SQLDataSource.LOG_EVENT.MARK_AS_DELETE_OBJECT, String.format("%s:%s", id, obj.getName()));
                    logger.debug("Mark as delete: " + id);
                    this.ds.markAsDeletedObject(obj);
                }

            }

            try {
                CachedAccessControl.getInstance(ds, true).checkForChanges(obj, CachedAccessControl.Change.DELETE);
            } catch (Exception ex) {
                logger.error(ex, ex);
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
     * Creates a new JEVisObject under the parent specified in the request body, or moves an existing
     * object if {@code copy} is a valid source object ID.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param copyObject  if &gt; 0, move this object ID to the new parent instead of creating a new one
     * @param object      JSON body representing the new {@link org.jevis.commons.ws.json.JsonObject}
     * @return 200 OK with the created/moved {@link org.jevis.commons.ws.json.JsonObject},
     *         400 BAD REQUEST on parse error, 401 UNAUTHORIZED, 404 NOT FOUND if parent or class is missing,
     *         or 500 on error
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
            logger.error("postObject: {}", object);
            try {
                this.ds = new SQLDataSource(httpHeaders, request, url);

                JsonObject json = OBJECT_MAPPER.readValue(object, JsonObject.class);
                if (this.ds.getJEVisClass(json.getJevisClass()) == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("JEVisClass not found").build();
                }

                JsonObject parentObj = this.ds.getObject(json.getParent());
                boolean canCreate = this.ds.getUserManager().canCreateWOE(parentObj, json.getJevisClass());

                if (parentObj != null && canCreate) {

                    if (copyObject > 0) {
                        JsonObject toCopyObj = this.ds.getObject(copyObject);

                        if (toCopyObj != null && this.ds.getUserManager().canCreate(toCopyObj)) {
                            this.ds.moveObject(toCopyObj.getId(), parentObj.getId());
                            return Response.ok(this.ds.getObject(copyObject)).build();
                        } else {
                            return Response.status(Response.Status.UNAUTHORIZED).build();
                        }
                    } else {

                        String jsonString = null;
                        if (json.getI18n() != null && !json.getI18n().isEmpty()) {
                            jsonString = OBJECT_MAPPER.writeValueAsString(json.getI18n());
                        }

                        JsonObject newObj = this.ds.buildObject(json, parentObj.getId(), jsonString);
                        ds.logUserAction(SQLDataSource.LOG_EVENT.CREATE_OBJECT, String.format("%s:%s", newObj.getId(), newObj.getName()));
                        try {
                            CachedAccessControl.getInstance(ds, true).checkForChanges(json, CachedAccessControl.Change.ADD);
                        } catch (Exception ex) {
                            logger.error(ex, ex);
                        }

                        return Response.ok(newObj).build();
                    }

                } else {
                    logger.error("Parent not found");
                    return Response.status(Response.Status.NOT_FOUND).entity("Parent not found").build();
                }

            } catch (AuthenticationException ex) {
                logger.error("AuthenticationException: ", ex);
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


    /**
     * Updates an existing JEVisObject (name, public flag, and i18n metadata).
     *
     * <p>Only the object owner with WRITE permission may update it. The {@code public} flag
     * can only be changed by a system administrator.
     *
     * @param httpHeaders HTTP headers (used for authentication)
     * @param request     JAX-RS request context
     * @param url         URI info context
     * @param id          the JEVis object ID to update
     * @param object      JSON body with the updated {@link org.jevis.commons.ws.json.JsonObject}
     * @return 200 OK with the updated object, 304 NOT MODIFIED if permission denied,
     * 401 UNAUTHORIZED, or 500 on error
     */
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
            logger.debug("post Object: " + object);
            this.ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject json = OBJECT_MAPPER.readValue(object, JsonObject.class);
            JsonObject existingObj = this.ds.getObject(id);
            if (existingObj != null && this.ds.getUserManager().canWrite(json)) {
                String jsonstring = null;
                if (json.getI18n() != null && !json.getI18n().isEmpty()) {
                    jsonstring = OBJECT_MAPPER.writeValueAsString(json.getI18n());
                }

                if (existingObj.getDeleteTS() != null && json.getDeleteTS() == null) {
                    ds.getObjectTable().restoreObjectAsDeleted(json);
                }

                try {
                    CachedAccessControl.getInstance(ds, true).checkForChanges(json, CachedAccessControl.Change.CHANGE);
                } catch (Exception ex) {
                    logger.error(ex, ex);
                }


                /** TODO: note to self, why did i do an if and no the boolean as var? **/
                if (existingObj.getisPublic() != json.getisPublic()) {
                    ds.logUserAction(SQLDataSource.LOG_EVENT.UPDATE_OBJECT, String.format("%s:%s", existingObj.getId(), existingObj.getName()));


                    if (this.ds.getUserManager().isSysAdmin()) {
                        return Response.ok(this.ds.updateObject(id, json.getName(), json.getisPublic(), jsonstring)).build();
                    } else {
                        return Response.ok(this.ds.updateObject(id, json.getName(), existingObj.getisPublic(), jsonstring)).build();
                    }
                } else {


                    return Response.ok(this.ds.updateObject(id, json.getName(), existingObj.getisPublic(), jsonstring)).build();
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
     * Returns the JEVisObject with the given ID.
     *
     * @param httpHeaders     HTTP headers (used for authentication)
     * @param request         JAX-RS request context
     * @param url             URI info context
     * @param detailed        reserved (currently unused)
     * @param includeChildren if {@code true}, recursively include all child objects
     * @param id              the JEVis object ID to fetch
     * @return 200 OK with the {@link org.jevis.commons.ws.json.JsonObject},
     *         400 BAD REQUEST if ID is missing, 404 NOT FOUND if inaccessible,
     *         401 UNAUTHORIZED, or 500 on error
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

    /**
     * Jersey lifecycle callback — clears transient per-request state after the response is committed.
     */
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
