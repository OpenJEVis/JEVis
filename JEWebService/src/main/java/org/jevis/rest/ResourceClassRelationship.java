/*
  Copyright (C) 2013 - 2016 Envidatec GmbH <info@envidatec.com>

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
import org.jevis.commons.ws.json.JsonClassRelationship;
import org.jevis.ws.sql.SQLDataSource;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.List;

/**
 * TODO: is this service in use yet?
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/classrelationships")
public class ResourceClassRelationship {

    private static final Logger logger = LogManager.getLogger(ResourceObject.class);
    private Date start = new Date();

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
            List<JsonClassRelationship> rels = ds.getClassRelationships();
            return Response.ok(rels).build();

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    @Logged
    public Response delete(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("null") @QueryParam("from") String from,
            @DefaultValue("null") @QueryParam("to") String to,
            @DefaultValue("-1") @QueryParam("type") int type) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (from.equalsIgnoreCase("null") || to.equalsIgnoreCase("null") || type == -1) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            boolean delete = ds.deleteClassRelationship(from, to, type);
            if (delete) {
                FileCache.deleteClassCachFiles();
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
            System.out.println("new Class Relationhisp");
            ds = new SQLDataSource(httpHeaders, request, url);

            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            //TODO: Check if the classes exist to give a better error
            JsonClassRelationship json = (new Gson()).fromJson(input, JsonClassRelationship.class);
            JsonClassRelationship newRel = ds.buildClassRelationship(json.getStart(), json.getEnd(), json.getType());
            System.out.println("rel");
            FileCache.deleteClassCachFiles();

            return Response.ok(newRel).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
