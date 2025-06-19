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
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.ms.MSOauth2;
import org.jevis.commons.ws.sql.*;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
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
            logger.error("Login user with Session:");
            ds = new SQLDataSource(httpHeaders, request, url,false);

            if(httpHeaders.getRequestHeader("token")==null){
                throw new AuthenticationException("token header is missing");
            }
            String token = httpHeaders.getRequestHeader("token").get(0);


            //Check Token
            CachedAccessControl cac = CachedAccessControl.getInstance(ds,true);
            System.out.println("Users: ");
            cac.getUsers().forEach((s, jeVisUserNew) -> {
                System.out.println("User: "+s+ " obj: "+jeVisUserNew);
            });

            MSOauth2 msOauth2 = new MSOauth2(Config.getEntraAUTHORITY(),Config.getEntraClientID(),Config.getEntraClientSecret());
            String userName = msOauth2.getUserDisplayName(token);
            System.out.println("User creating session: "+userName);
            List<String> msGroups = msOauth2.getUserGroups(token);
            List<UserRolePojo> roles = ds.getSampleTable().getEntraIds();
            List<UserRolePojo> filteredList = roles.stream()
                    .filter(role -> msGroups.contains(role.getEntraID()))
                    .collect(Collectors.toList());
            filteredList.forEach(userRolePojo -> {
                System.out.println("found roles: "+userRolePojo.getRoleID());
                //JEVisUserNew user  = new JEVisUserNew(ds,userName,userRolePojo.getRoleID(),false,true,"");
                ds.setUser(cac.getUser(userRolePojo.getUserName()));
                ds.getUserManager().init();
            });

            //test
            JsonObject testObj = new JsonObject();
            testObj.setId(49363l);
            UserRightManagerForWS urm = ds.getUserManager();
            boolean canRead = urm.canRead(testObj);
            System.out.println("Can read: "+canRead);



            //ds.getUserManager().
           // ds.getrol

            //Add Session

            Session session = new Session(true,-1,msOauth2.getUserDisplayName(token),token);

            cac.getSessions().put(session.getId(),session);
            System.out.println("Session ID: "+session.getId());


            return Response.ok(session.getId()).build();

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
    @Path("/sessions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSessions(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url) {


        try {
            logger.error("Sessions:");
            ds = new SQLDataSource(httpHeaders, request, url);
            CachedAccessControl cac = CachedAccessControl.getInstance(ds,false);

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
