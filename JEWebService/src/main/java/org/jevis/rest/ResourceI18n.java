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

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.ws.json.JsonI18nClass;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
            List<JsonI18nClass> files = new ArrayList<>();
            if (Config.getI18nDir().exists() && Config.getI18nDir().isDirectory()) {
                for (File file : Config.getI18nDir().listFiles()) {
                    try {
                        System.out.println("File: " + file);
                        if (file.getName().endsWith(".json")) {
                            files.add(loadFile(file));
                        }
                    } catch (Exception fex) {
                        logger.error("Error while loading i18n file '{}':", file.getName(), fex);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(fex.getMessage()).build();
                    }
                }
            }

            //@TODO add single class only support
            if (jclass.isEmpty()) {
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


    private JsonI18nClass loadFile(File file) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(file));
        JsonI18nClass data = gson.fromJson(reader, JsonI18nClass.class);
        return data;
    }


}
