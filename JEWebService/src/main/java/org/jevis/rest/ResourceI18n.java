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


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonI18nClass;
import org.jevis.commons.ws.sql.Config;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * resource service to translate jevis
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/i18n")
public class ResourceI18n {

    private static final Logger logger = LogManager.getLogger(ResourceI18n.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<JsonI18nClass> files;

    private static List<File> listClassI18nFiles(File dir) {
        List<File> fileTree = new ArrayList<>();
        if (dir == null || dir.listFiles() == null) {
            return fileTree;
        }
        for (File entry : dir.listFiles()) {
            if (entry.isFile()) {
                if (entry.getName().endsWith(".json")) {
                    fileTree.add(entry);
                }
            } else {
                fileTree.addAll(listClassI18nFiles(entry));
            }
        }
        return fileTree;
    }

    /**
     * @param jclass
     * @param request
     * @param url
     * @param httpHeaders
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClassI18n(
            @DefaultValue("") @QueryParam("jclass") String jclass,
            @Context Request request,
            @Context UriInfo url,
            @Context HttpHeaders httpHeaders) {
        try {

            if (files == null) {
                files = new ArrayList<>();

                for (File file : listClassI18nFiles(Config.getI18nDir())) {
                    try {
                        if (file.getName().endsWith(".json")) {
                            files.add(loadFile(file));
                        }
                    } catch (Exception fex) {
                        logger.error("Error while loading i18n file '{}':", file.getName(), fex);
                    }
                }
            }

            if (!jclass.isEmpty()) {
                for (JsonI18nClass i18class : files) {
                    if (i18class.getJevisclass().equalsIgnoreCase(jclass)) {
                        return Response.ok(i18class).build();
                    }
                }
            }

            return Response.ok(files).build();
        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    private JsonI18nClass loadFile(File file) throws IOException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper.readValue(file, JsonI18nClass.class);
    }
}
