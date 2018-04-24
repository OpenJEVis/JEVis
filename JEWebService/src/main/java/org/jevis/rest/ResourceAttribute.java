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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.ws.sql.SQLDataSource;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Date;
import java.util.List;

/**
 * This Class handels all request for JEVisAttributes
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
//@Path("/objects/{id}/attributes/{attribute}")
@Path("/JEWebService/v1/objects/{id}/attributes")
public class ResourceAttribute {

    private final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceAttribute.class);
    private Date start = new Date();

    
    
    /**
     * Returns an list of all attributes under the given JEVisClass
     *
     * @param context
     * @param httpHeaders
     * @param id
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
            @PathParam("id") long id) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("AttributeResource", "Start");

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                logger.debug("Can not access Object: {}", id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (ds.getUserManager().canRead(obj)) {
                List<JsonAttribute> atts = ds.getAttributes(id);
                ds.getProfiler().addEvent("AttributeResource", "done");
                return Response.ok(atts).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    /**
     * Returns an specific attribute
     *
     * @param context
     * @param httpHeaders
     * @param id
     * @param attribute
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{attribute}")
    public Response getAttribute(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                logger.debug("Can not access Object: {}", id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            logger.debug("Found Object: {}", id);

            if (ds.getUserManager().canRead(obj)) {
                List<JsonAttribute> atts = ds.getAttributes(id);
                for (JsonAttribute att : atts) {
                    if (att.getType().equals(attribute)) {
                        return Response.ok(att).build();
                    }
                }

            }
            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @POST
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{attribute}")
    public Response postAttribute(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @PathParam("id") long id,
            @PathParam("attribute") String attribute,
            String payload) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            JsonObject obj = ds.getObject(id);
            if (obj == null) {
                logger.debug("Can not access Object: {}", id);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (ds.getUserManager().canWrite(obj)) {
                JsonAttribute json = (new Gson()).fromJson(payload, JsonAttribute.class);
                ds.setAttribute(id, json);
                return Response.ok(json).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
           Config.CloseDS(ds);
        }

    }
}
