/*
  Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>

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

import org.jevis.commons.ws.json.JSonError;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Class will build the returned error massages as JSon.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ErrorBuilder {

    private static final String ERROR_BASE_URL = "http://openjevis.org/projects/openjevis/wiki/";
    private static final String ERROR_PREFIX = "JEWebServiceError";

    /**
     * Creates an default JSon error massage with the given parameters.
     *
     * @param status
     * @param code
     * @param message
     *
     * @return
     */
    public static WebApplicationException ErrorBuilder(int status, int code, String message) {
        JSonError jerror = new JSonError(status, code, message, ERROR_BASE_URL + ERROR_PREFIX + code);

        Response.ResponseBuilder builder = Response.status(status).entity(jerror);
        builder.type(MediaType.APPLICATION_JSON);

        return new WebApplicationException(builder.build());
    }

}
