/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.NaturalOrderComparator;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.classes.ISO50001;
import org.jevis.rest.Config;
import org.jevis.ws.sql.SQLDataSource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/navbar")
public class Navbar {

    /**
     * @param httpHeaders
     * @param site
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site
    ) throws Exception {
        SQLDataSource ds = null;

        try {
            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);
            ISO50001 iso = new ISO50001(ds);
            String SupMeetingsDirName = iso.getOrganisation().getSuperiorMeetingsDirName();
            List<String> sites = iso.getOrganisation().getSiteNames();
            sites.sort(new NaturalOrderComparator());

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            root.put("sites", sites);
            root.put("ISODirectoryID", iso.getOrganisation().getID());
            root.put("SupMeetingsDirName", SupMeetingsDirName);

            TemplateChooser tc = new TemplateChooser(root, "navbar");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
