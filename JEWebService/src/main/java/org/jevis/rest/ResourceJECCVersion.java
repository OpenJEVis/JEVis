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
import org.jevis.commons.ws.sql.Config;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Date;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/jecc")
public class ResourceJECCVersion {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceJECCVersion.class);

    @GET
    @Path("/version")
    public Response get() {
        return Response.ok(Config.getJECCVersion()).build();
    }

    @GET
    @Logged
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
        try {
            File file = new File(Config.getJECCFilePath());
            if (file.exists() && file.canRead()) {
                Response.ResponseBuilder response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, file.getName())
                        .header(HttpHeaders.CONTENT_LENGTH, file.length())
                        .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
                return response.build();
            } else {
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        }
    }

    @PostConstruct
    public void postConstruct() {
        //collect garbage
    }
}
