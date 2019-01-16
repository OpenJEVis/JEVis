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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.ws.sql.SQLDataSource;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * This Class handels all request for JEVisAttributes
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
//@Path("/objects/{id}/attributes/{attribute}")
@Path("/JEWebService/v1/attributes")
public class ResourceAllAttribute {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceAllAttribute.class);


    @GET
    @Logged
    @Produces({MediaType.APPLICATION_JSON, "application/zip"})
    public Response getAll(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {

        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.getProfiler().addEvent("AllAttributeResource", "Start");
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            List<JsonAttribute> attributes = ds.getAttributes();
            ds.getProfiler().addEvent("AttributeResource", "done");
            logger.debug("Total amount of attributes: {}", attributes.size());
            return Response.ok(attributes).build();


        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

}
