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
import org.jevis.iso.classes.ActionPlan;
import org.jevis.iso.classes.DocumentsDirectory;
import org.jevis.iso.classes.ISO50001;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/actionplans")
public class ActionPlans {
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

            DocumentsDirectory doc = iso.getOrganisation().getSite(site).getDocuments();
            long ActionPlansDirectoryID = doc.getActionPlansDirID();
            String ActionPlansDirName = doc.getActionPlansName();
            List<ActionPlan> listActionPlans = doc.getActionPlans();

            List<Long> listYears = doc.getListYearsActionPlans();

            root.put("ActionPlansDirectoryID", ActionPlansDirectoryID);
            root.put("ActionPlansDirName", ActionPlansDirName);
            root.put("actionplans", listActionPlans);
            root.put("listYears", listYears);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addActionPlan", t.getTranslatedKey(lang, "Add Action Plan"));
                root.put("addImplementedAction", t.getTranslatedKey(lang, "Add Implemented Action"));
                root.put("addPlannedAction", t.getTranslatedKey(lang, "Add Planned Action"));

            } else {
                root.put("addActionPlan", "Add Action Plan");
                root.put("addImplementedAction", "Add Implemented Action");
                root.put("addPlannedAction", "Add Planned Action");
            }

            TemplateChooser tc = new TemplateChooser(root, "actionplans");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("print")
    public Response getPrintObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @DefaultValue("") @QueryParam("site") String site,
            @QueryParam("id") Long id
    ) throws Exception {
        SQLDataSource ds = null;
        try {

            Login l = new Login();
            ds = new SQLDataSource(httpHeaders, request, url);

            ISO50001 iso = new ISO50001(ds);

            Map<String, Object> root = new HashMap<>();

            DocumentsDirectory doc = iso.getOrganisation().getSite(site).getDocuments();

            ActionPlan actionPlan = doc.getActionPlan(id);

            root.put("organisationLocation", iso.getOrganisation().getLocation());
            root.put("actionPlan", actionPlan);
            root.put("siteName", site);

            TemplateChooser tc = new TemplateChooser(root, "actionplanprint");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
