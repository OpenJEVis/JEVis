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
import java.util.List;

/**
 * TODO: is this service in use yet?
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/relationships")
public class ResourceRelationship {

    private static final Logger logger = LogManager.getLogger(ResourceRelationship.class);

    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ResourceRelationship", "getObject");
            List<JsonRelationship> rels = ds.getUserManager().filterRelationships(ds.getRelationships());
            ds.getProfiler().addEvent("ResourceRelationship", "done");
            return Response.ok(rels).build();

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

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ResourceRelationship", "post");
            JsonRelationship json = (new Gson()).fromJson(input, JsonRelationship.class);
            JsonObject fromObj = ds.getObject(json.getFrom());
            JsonObject toObj = ds.getObject(json.getTo());

            ds.getUserManager().canWrite(fromObj);
            ds.getUserManager().canWrite(toObj);

            JsonRelationship newJSON = ds.setRelationships(json);
            ds.getProfiler().addEvent("ResourceRelationship", "done");
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

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("ResourceRelationship", "delete");

            if (from == -1 || to == -1 || type == -1) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            JsonObject fromObj = ds.getObject(from);
            JsonObject toObj = ds.getObject(to);

            ds.getUserManager().canWrite(fromObj);
            ds.getUserManager().canWrite(toObj);

            boolean delete = ds.deleteRelationship(from, to, type);
            if (delete) {
                ds.getProfiler().addEvent("ResourceRelationship", "done");
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

}
