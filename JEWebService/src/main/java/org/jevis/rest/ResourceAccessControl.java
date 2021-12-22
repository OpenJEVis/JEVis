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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.sql.CachedAccessControl;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * TODO: is this service in use yet?
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/accesscontrol")
public class ResourceAccessControl {

    private static final Logger logger = LogManager.getLogger(ResourceAccessControl.class);
    private SQLDataSource ds;

    @GET
    @Logged
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            logger.error("update Access Control: {}", ds.getCurrentUser().getUserObject().getName());
            CachedAccessControl.getInstance(ds).updateCache();
            return Response.ok().build();

        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @PostConstruct
    public void postConstruct() {
        if (ds != null) {
            ds.clear();
            ds = null;
        }
    }

}
