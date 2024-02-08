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
import org.jevis.commons.utils.JEVisDates;
import org.jevis.commons.ws.json.JsonFile;
import org.jevis.commons.ws.sql.Config;
import org.joda.time.DateTime;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/jecc")
public class ResourceJECCVersion {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(ResourceJECCVersion.class);
    private static final ConcurrentHashMap<UUID, JsonFile> jeccFiles = new ConcurrentHashMap<>();

    @GET
    @Path("/version")
    public Response get() {
        return Response.ok(Config.getLatestJECCVersion()).build();
    }

    @GET
    @Logged
    @Path("/files")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFiles() {
        try {
            List<String> pathList = Config.getJECCFilesPath();

            List<JsonFile> fileList = new ArrayList<>();
            for (String s : pathList) {
                File file = new File(s);
                if (file.exists() && file.canRead()) {
                    UUID uuid = UUID.randomUUID();

                    JsonFile internJsonFile = new JsonFile();
                    JsonFile externJsonFile = new JsonFile();

                    internJsonFile.setName(file.getName());
                    externJsonFile.setName(file.getName());
                    internJsonFile.setPath(file.getAbsolutePath());

                    DateTime mod = new DateTime(file.lastModified());
                    String modString = mod.toString(JEVisDates.DEFAULT_DATE_FORMAT);
                    internJsonFile.setLastModified(modString);
                    externJsonFile.setLastModified(modString);
                    internJsonFile.setSize(file.length());
                    externJsonFile.setSize(file.length());

                    jeccFiles.put(uuid, internJsonFile);
                    fileList.add(externJsonFile);
                }
            }

            if (!fileList.isEmpty()) {
                return Response.ok(fileList).build();
            }

            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception jex) {
            logger.catching(jex);
            return Response.serverError().build();
        }
    }

    @GET
    @Logged
    @Path("/file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getJECCFile(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @QueryParam("name") String name
    ) {
        try {
            if (name != null) {
                UUID id = UUID.fromString(name);
                JsonFile jsonFile = jeccFiles.get(id);

                if (jsonFile != null) {
                    File file = new File(jsonFile.getPath());
                    Response.ResponseBuilder response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, file.getName())
                            .header(HttpHeaders.CONTENT_LENGTH, file.length())
                            .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
                    return response.build();
                }
            } else {
                File file = new File(Config.getLatestJECCPath());
                if (file.exists() && file.canRead()) {
                    Response.ResponseBuilder response = Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                            .header(HttpHeaders.CONTENT_DISPOSITION, file.getName())
                            .header(HttpHeaders.CONTENT_LENGTH, file.length())
                            .header(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()));
                    return response.build();
                }
            }
        } catch (Exception jex) {
            logger.catching(jex);

            return Response.serverError().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PostConstruct
    public void postConstruct() {
        //collect garbage
    }
}
