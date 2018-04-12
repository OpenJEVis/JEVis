/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import javax.security.sasl.AuthenticationException;
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
public class MapperAuthenticationException implements ExceptionMapper<AuthenticationException> {

    private static final Logger logger = LogManager.getLogger(MapperAuthenticationException.class);

    @Override
    public Response toResponse(AuthenticationException exception) {
        logger.catching(Level.DEBUG, exception);
        return Response.status(Response.Status.UNAUTHORIZED).entity(exception.getMessage()).type("text/plain")
                .build();
    }

}
