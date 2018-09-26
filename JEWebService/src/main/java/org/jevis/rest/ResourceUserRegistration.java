/*
  Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>

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
import org.jevis.api.JEVisDataSource;
import org.jevis.commons.ws.json.JsonUser;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Florian Simon
 * @deprecated TODO need an update to the new SQL Datasource
 */
@Path("/JEWebService/v1/registration")
public class ResourceUserRegistration {
    private static final Logger logger = LogManager.getLogger(ResourceUserRegistration.class);

    @POST
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUser(
            @Context HttpHeaders httpHeaders,
            JsonUser user
    ) {
        if (!hasAccess(httpHeaders)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JEVisDataSource ds = null;

//        try {
//
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "POST User ");
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "username: " + user.getLogin());
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "userpassword: " + user.getPassword());
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "email: " + user.getEmail());
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "firstname: " + user.getFirstname());
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "lastname: " + user.getLastname());
//            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "organisation: " + user.getOrganisation());
//
//            if (!userExists(Config.getDBConnection(), user.getLogin())) {
//                ds = Config.geSysAdminDS();
//                JEVisObject obj = ds.getObject(Config.getDemoRoot());
//
//                if (obj == null) {
//                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Demouser root is not configured").build();
//                }
//
//                List<JEVisObject> demoGroups = new ArrayList<>();
//                if (Config.getDemoGroup() > 0) {
//                    JEVisObject demoGroup = ds.getObject(Config.getDemoGroup());
//                    if (demoGroup != null) {
//                        demoGroups.add(demoGroup);
//                    } else {
//                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Demogroup does not exist").build();
//                    }
//
//                }
//
//                boolean isCreated = UserFactory.buildMobileDemoStructure(ds, obj, user.getLogin(), user.getPassword(), user.getEmail(), user.getFirstname(), user.getLastname(), user.getOrganisation(), demoGroups);
//
//                if (isCreated) {
////                    return Response.status(Response.Status.OK).build();
//                    return Response.ok(user).build();
//                } else {
//                    return Response.status(Response.Status.CONFLICT).build();
//                }
//            } else {
//                return Response.status(Response.Status.CONFLICT).build();
//            }
//
//        } catch (Exception jex) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jex.toString()).build();
//        } finally {
//            Config.CloseDS(ds);
//        }
        return null;
    }

    /**
     * fast dirty solution to make the registraion service a bit more save. The
     * registraion service works without a valid JEVis user and can create new
     * User.
     *
     * @param httpHeaders
     * @return
     */
    private boolean hasAccess(HttpHeaders httpHeaders) {
        if (httpHeaders.getRequestHeader("registraionapikey") == null || httpHeaders.getRequestHeader("registraionapikey").isEmpty()) {
            logger.fatal("Missing registraionapikey header");
            return false;
        }
        String auth = httpHeaders.getRequestHeader("registraionapikey").get(0);
        if (auth != null && !auth.isEmpty()) {
            return auth.equals(Config.getRigestrationAPIKey());
        }
        return false;

    }

    private boolean userExists(Connection con, String username) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            ps = con.prepareStatement("select id from object where type=? and name=? limit 1");
            ps.setString(1, "User");
            ps.setString(2, username);

            rs = ps.executeQuery();

            while (rs.next()) {
                logger.fatal("User allready exists: " + username);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            ps = null;
            rs = null;
        }
        return false;
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(
            @Context HttpHeaders httpHeaders,
            @PathParam("name") String name
    ) {
        logger.fatal("GET User: " + name);
        if (!hasAccess(httpHeaders)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
//        try {
////            Connection con = Config.getDBConnection();
////            ps = con.prepareStatement("select id from object where type=? and name=? limit 1");
////            ps.setString(1, "User");
////            ps.setString(2, name);
////            logger.info("Request: " + ps.toString());
//
//            if (name == null) {
//                return Response.status(Response.Status.BAD_REQUEST).entity("name parameter is missing").build();
//            }
//
//            if (userExists(Config.getDBConnection(), name)) {
//                //TODO: return existing user?
//                JsonUser user = new JsonUser();
//                user.setLogin(name);
//                user.setEmail("Placeholder");
//                user.setFirstname("Placeholder");
//                user.setLastname("Placeholder");
//                user.setOrganisation("Placeholder");
//                user.setPassword("Placeholder");
//                return Response.ok(user).build();
////                return Response.status(Response.Status.OK).build();
//            } else {
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getCause().getMessage()).build();
//        } finally {
//
//        }
        return null;
    }

}
