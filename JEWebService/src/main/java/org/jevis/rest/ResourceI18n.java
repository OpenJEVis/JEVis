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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.corba.se.spi.legacy.connection.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonI18n;
import org.jevis.commons.ws.json.JsonJEVisClass;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * resource service to translate jevis
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/JEWebService/v1/i18n")
public class ResourceI18n {

    private static final Logger logger = LogManager.getLogger(ResourceI18n.class);


    /**
     * @param id
     * @param lang      language_country code e.c. de_DE
     * @param key
     * @param request
     * @param url
     * @param httpHeaders
     * @return
     */
    @GET
    @Logged
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationship(
            @PathParam("id") long id,
            @DefaultValue("en") @QueryParam("lang") String lang,
            @DefaultValue("") @QueryParam("key") String key,
            @Context Request request,
            @Context UriInfo url,
            @Context HttpHeaders httpHeaders) {


        try {
            List<List<JsonI18n>> files = new ArrayList<>();
            if (Config.getI18nDir().exists() && Config.getI18nDir().isDirectory()) {
                System.out.println("Locale: "+lang+ "    key: "+key);
                for (File file : Config.getI18nDir().listFiles()) {
                    try {

                        if (file.getName().endsWith(".json")
                                && file.getName().split("_",2)[1].replace(".json","").toLowerCase().startsWith(lang.toLowerCase())) {
                            System.out.println("File end with .json & has lang code");
                            files.add(loadFile(file));
                        }else{
                            System.out.println("----- NOPE!");
                        }
                    } catch (Exception fex) {

                        logger.error("Error while loading i18n file '{}':", file.getName(), fex);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(fex.getMessage()).build();
                    }
                }
            }

            List<JsonI18n> result = new ArrayList<>();
            for (List<JsonI18n> i18list : files) {
                for (JsonI18n i18n : i18list) {
                    if (key.trim().isEmpty()) {
                        result.add(i18n);
                    } else if (key.equals(i18n.getKey())) {
                        result.add(i18n);
                    }else {
                        System.out.println("no key match");
                    }
                }
            }

            return Response.ok(result).build();


        } catch (Exception ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }

    }



    private List<JsonI18n> loadFile(File file) throws FileNotFoundException {
        Type listtype = new TypeToken<List<JsonI18n>>() {
        }.getType();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(file));
        List<JsonI18n> data = gson.fromJson(reader, listtype);
        return data;
    }


}
