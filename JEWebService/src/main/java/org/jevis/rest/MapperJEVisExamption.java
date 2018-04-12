/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jevis.api.JEVisException;

/**
 *
 * @author fs
 */
@Provider
public class MapperJEVisExamption implements ExceptionMapper<JEVisException> {

    @Override
    public Response toResponse(JEVisException exception) {

        return Response.status(404).entity(exception.getMessage()).type("text/plain")
                .build();
    }
}
