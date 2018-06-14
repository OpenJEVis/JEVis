/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.EnergyPlanningDirectory;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.Produce;
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
@Path("/JEWebService/v1/production")
public class Production {

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

            EnergyPlanningDirectory epDir = iso.getOrganisation().getSite(site).getenergyplanning();
            long ProductionDirID = epDir.getProductionDirID();
            String ProductionDirName = epDir.getProductionDirName();
            List<Produce> listProduction = epDir.getProduction();
            List<Long> listYears = epDir.getListYears();

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            root.put("ProductionDirID", ProductionDirID);
            root.put("ProductionDirName", ProductionDirName);
            root.put("listProduction", listProduction);
            root.put("listYears", listYears);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addProduce", t.getTranslatedKey(lang, "Add Produce"));
                root.put("productionoverview", t.getTranslatedKey(lang, "Overview"));
            } else {
                root.put("addProduce", "Add Produce");
                root.put("productionoverview", "Overview");
            }

            TemplateChooser tc = new TemplateChooser(root, "production");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
