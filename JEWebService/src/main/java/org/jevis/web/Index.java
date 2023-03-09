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
package org.jevis.web;

import org.jevis.commons.ws.sql.Config;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/")
public class Index {

    File indexfile = new File(Config.getWebDir() + "/index.html");

    @GET
    public Response get() {
        return Response.ok(indexfile).build();
    }


}
