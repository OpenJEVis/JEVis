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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jevis.api.JEVisException;
import org.jevis.ws.sql.SQLDataSource;
import org.jevis.ws.sql.tables.Service;

import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/manage")
public class ResourceHidden {

    @GET
    @Path("cleanup")
    public Response cleanup(@Context HttpHeaders httpHeaders,
                            @Context Request request,
                            @Context UriInfo url) {
        SQLDataSource ds = null;
        try {
            ds = new SQLDataSource(httpHeaders, request, url);

            if (ds.getUserManager().isSysAdmin()) {
                Service service = new Service(ds);
                service.cleanup();
                return Response.ok().build();
            } else {
                throw new AuthenticationException("No sysadmin");
            }

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    @GET
    @Path("gc")
    public Response gc(@Context HttpHeaders httpHeaders,
                       @Context Request request,
                       @Context UriInfo url) {
        SQLDataSource ds = null;
        try {
            System.out.println("manage.gc");
            System.gc();

            ds = new SQLDataSource(httpHeaders, request, url);

            if (ds.getUserManager().isSysAdmin()) {
                System.gc();

                return Response.ok().build();
            } else {
                throw new AuthenticationException("No sysadmin");
            }

        } catch (JEVisException jex) {
            return Response.serverError().entity(ExceptionUtils.getStackTrace(jex)).build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
