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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.json.JsonType;
import org.jevis.ws.sql.SQLDataSource;

/**
 *
 * THis Class handels all the JEVisType requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/classes/{name}/types")
public class ResourceType {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceType.class);

    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            return Response.ok(ds.getTypes(name)).build();

        } catch (JEVisException jex) {
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @DELETE
    @Logged
    @Path("/{typename}")
    public Response delete(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name,
            @PathParam("typename") String typename) throws JEVisException {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            if (ds.deleteType(name, typename)) {
                return Response.ok().build();
            } else {
                return Response.notModified().build();
            }
        } catch (JEVisException jex) {
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
    @Path("/{typename}")
    public Response post(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("name") String name,
            @PathParam("typename") String typename,
            String input) throws Exception {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (!ds.getUserManager().isSysAdmin()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            JsonJEVisClass parent = ds.getJEVisClass(name);
            if (parent == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Class not found").build();
            }

            JsonType json = (new Gson()).fromJson(input, JsonType.class);

            if (ds.setType(name, json, typename)) {
                System.out.println("delete cache type");
                FileCache.deleteClassFile();
                return Response.ok(json).build();
            } else {
                System.out.println("delete cache type notttttttttt");
                return Response.notModified().build();
            }

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
