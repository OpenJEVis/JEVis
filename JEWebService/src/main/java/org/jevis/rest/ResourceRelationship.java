/*
  Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>

  This file is part of JEWebService.

  JEWebService is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JEWebService is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  details.

  You should have received a copy of the GNU General Public License along with
  JEWebService. If not, see <http://www.gnu.org/licenses/>.

  JEWebService is part of the OpenJEVis project, further project information
  are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest;


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
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: is this service in use yet?
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/relationships")
public class ResourceRelationship {

    private static final Logger logger = LogManager.getLogger(ResourceRelationship.class);
    private SQLDataSource ds = null;
    private List<JsonRelationship> jsonRelationships;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            jsonRelationships = ds.getUserManager().filterRelationships(ds.getRelationships());
            return Response.ok(jsonRelationships).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
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
    public Response post(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            String input) throws Exception {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);
//            JsonRelationship json = (new Gson()).fromJson(input, JsonRelationship.class);
            JsonRelationship json = objectMapper.readValue(input, JsonRelationship.class);
            JsonObject fromObj = ds.getObject(json.getFrom());
            JsonObject toObj = ds.getObject(json.getTo());

            ds.getUserManager().canWrite(fromObj);
            ds.getUserManager().canWrite(toObj);

            JsonRelationship newJSON = ds.setRelationships(json);
            try {
                CachedAccessControl.getInstance(ds,true).checkForChanges(newJSON);
            } catch (Exception ex) {
                logger.error(ex, ex);
            }

            ds.logUserAction(SQLDataSource.LOG_EVENT.CREATE_RELATIONSHIP, String.format("%s", newJSON));
            return Response.ok(newJSON).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    /**
     * TODO: why did i made it with parameters und not with json in this case?
     *
     * @param httpHeaders
     * @param from
     * @param to
     * @param type
     * @return
     */
    @DELETE
    @Logged
    public Response delete(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("-1") @QueryParam("from") Long from,
            @DefaultValue("-1") @QueryParam("to") Long to,
            @DefaultValue("-1") @QueryParam("type") int type) {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (from == -1 || to == -1 || type == -1) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            JsonObject fromObj = ds.getObject(from);
            JsonObject toObj = ds.getObject(to);

            ds.getUserManager().canWrite(fromObj);
            ds.getUserManager().canWrite(toObj);

            boolean delete = ds.deleteRelationship(from, to, type);
            if (delete) {
                try {
                    CachedAccessControl.getInstance(ds,true).checkForChanges(type);
                } catch (Exception ex) {
                    logger.error(ex, ex);
                }
                ds.logUserAction(SQLDataSource.LOG_EVENT.DELETE_RELATIONSHIP, String.format("%s-%s-%s", from, to, type));
                return Response.ok().build();
            } else {
                return Response.notModified().build();
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

    @GET
    @Logged
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSingleRel(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id) {

        try {
            ds = new SQLDataSource(httpHeaders, request, url);

//            List<JsonRelationship> jsonRelationships = ds.getUserManager().filterRelationships(ds.getRelationshipTable().getAllForObject(id));
            List<JsonRelationship> jsonRelationships = ds.getRelationshipTable().getAllForObject(id);
            this.jsonRelationships = new ArrayList<>();
            for (JsonRelationship rel : jsonRelationships) {
                if (rel.getFrom() == id || rel.getTo() == id) {
                    this.jsonRelationships.add(rel);
                }
            }

            return Response.ok(this.jsonRelationships).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @PostConstruct
    public void postConstruct() {
        if (jsonRelationships != null) {
            jsonRelationships.clear();
            jsonRelationships = null;
        }
        if (ds != null) {
            ds.clear();
            ds = null;
        }
    }
}
