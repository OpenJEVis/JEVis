/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.Meter;
import org.jevis.iso.classes.MonitoringRegisterDirectory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/meters")
public class Meters {

    /**
     * @param httpHeaders
     * @param site
     * @param lang
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site,
            @DefaultValue("") @QueryParam("lang") String lang
    ) throws Exception {
        SQLDataSource ds = null;
        try {

            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            ISO50001 iso = new ISO50001(ds);

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            MonitoringRegisterDirectory mr = iso.getOrganisation().getSite(site).getMonitoringregister();
            long MeterDirectoryID = mr.getMeterDirID();
            String MeterDirectoryName = mr.getMeterDirName();
            List<Meter> listMeters = mr.getMeterDir();

            root.put("MeterDirectoryID", MeterDirectoryID);
            root.put("MeterDirectoryName", MeterDirectoryName);
            root.put("listMeters", listMeters);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addMeter", t.getTranslatedKey(lang, "Add Meter"));
            } else {
                root.put("addMeter", "Add Meter");
            }

            TemplateChooser tc = new TemplateChooser(root, "meters");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
