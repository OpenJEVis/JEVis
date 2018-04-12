/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fs
 */
@Provider
public class MapperNullPointerExeption implements ExceptionMapper<NullPointerException> {

    private static final Logger logger = LogManager.getLogger(MapperNullPointerExeption.class);

    @Override
    public Response toResponse(NullPointerException exception) {

        logger.catching(Level.DEBUG, exception);

        return Response.serverError().entity(exception.getMessage()).type("text/plain")
                .build();
    }

}
