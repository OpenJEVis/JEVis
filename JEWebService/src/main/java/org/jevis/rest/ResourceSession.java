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
import org.jevis.commons.ws.json.JsonSSOConfig;
import org.jevis.commons.ws.ms.MSOauth2;
import org.jevis.commons.ws.sql.*;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: is this service in use yet?
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/session")
public class ResourceSession {

    private static final Logger logger = LogManager.getLogger(ResourceSession.class);
    private SQLDataSource ds;


    @GET
    @Logged
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenLogin(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            ds = new SQLDataSource(httpHeaders, request, url, false);

            if (httpHeaders.getRequestHeader("token") == null) {
                throw new AuthenticationException("token header is missing");
            }
            String token = httpHeaders.getRequestHeader("token").get(0);
            //Check Token
            CachedAccessControl cac = CachedAccessControl.getInstance(ds, true);
            MSOauth2 msOauth2 = new MSOauth2(Config.getEntraAUTHORITY(), Config.getEntraClientID(), Config.getEntraClientSecret());
            String userName = msOauth2.getUserDisplayName(token);
            List<String> msGroups = msOauth2.getUserGroups(token);
            List<JEVisUserSQL> foundUsers = cac.getUsers().values().stream()
                    .filter(user -> msGroups.contains(user.getEntraID()))
                    .collect(Collectors.toList());


            List<String> commonKeys = new ArrayList<>(cac.getUsers().keySet());
            commonKeys.retainAll(msGroups);

            foundUsers.get(0).getUserObject().setName(userName);
            foundUsers.get(0).setLastName(userName);
            ds.setUser(foundUsers.get(0));

            Session session = new Session(true, foundUsers.get(0).getUserObject(), msOauth2.getUserDisplayName(token), token);
            session.setJevisUser(foundUsers.get(0));
            cac.getSessions().put(session.getId(), session);

            return Response.ok(session).build();

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
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfig(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            if (Config.getEntraConfigToken().equals(httpHeaders.getRequestHeader("token").get(0))) {
                JsonSSOConfig config = new JsonSSOConfig();
                config.setAuthority(Config.getEntraAUTHORITY());
                config.setClientID(Config.getEntraClientID());
                config.setTenant(Config.getEntraTenantID());
                config.setClientSecret(Config.getEntraClientSecret());

                return Response.ok(config).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        }

    }

    @GET
    @Logged
    @Path("/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            logger.error("Sessions:");
            ds = new SQLDataSource(httpHeaders, request, url);
            CachedAccessControl cac = CachedAccessControl.getInstance(ds, false);

            return Response.ok(cac.getSessions()).build();
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
