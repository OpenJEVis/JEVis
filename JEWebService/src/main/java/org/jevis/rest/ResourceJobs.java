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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;

import javax.annotation.PostConstruct;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * This Class handels all the JEVisObject related requests
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/task")
public class ResourceJobs {

    private SQLDataSource ds = null;
    private List<JsonObject> returnList;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LogManager.getLogger(ResourceJobs.class);

    /**
     * Get an list of JEVisObject Resource.
     * <p>
     * https://jersey.java.net/documentation/latest/async.html
     *
     * @param httpHeaders
     * @param request
     * @param url
     * @param detailed
     * @return
     */
    @GET
    @Logged
    @Path("dataprocessor")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("false") @QueryParam("detail") boolean detailed
    ) {

        try {
            this.ds = new SQLDataSource(httpHeaders, request, url);
            //this.ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            this.ds.preload(SQLDataSource.PRELOAD.ALL_REL);


            this.returnList = this.ds.getUserManager().filterList(ds.getAttributeTable().getDataProcessorTodoList());


            if (detailed) {
                //TODO
            }

            //TODO add attributes if needed
            return Response.ok(this.returnList).build();

        } catch (AuthenticationException ex) {
            logger.catching(ex);
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        } finally {
            Config.CloseDS(this.ds);
        }
    }


    @PostConstruct
    public void postConstruct() {
        if (this.returnList != null) {
            this.returnList.clear();
            this.returnList = null;
        }
        if (this.ds != null) {
            this.ds.clear();
            this.ds = null;
        }
    }

//    @PreDestroy
//    public void preDestroy() {
//        System.out.println("PerRequest @PreDestroy invoked!");
//
//    }

}
