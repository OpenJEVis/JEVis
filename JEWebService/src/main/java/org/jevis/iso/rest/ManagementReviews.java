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
import org.jevis.iso.classes.ManagementReview;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/managementreviews")
public class ManagementReviews {

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

            DocumentsDirectory docDir = iso.getOrganisation().getSite(site).getDocuments();
            long ManagementReviewsDirectoryID = docDir.getManagementReviewsDirID();
            String ManagementReviewsDirectoryName = docDir.getManagementReviewsName();
            List<ManagementReview> listManagementReviews = docDir.getManagementReviews();
            List<Long> listYears = docDir.getListYearsManagementReviews();

            root.put("ManagementReviewsDirectoryID", ManagementReviewsDirectoryID);
            root.put("ManagementReviewsDirectoryName", ManagementReviewsDirectoryName);
            root.put("managementreviews", listManagementReviews);
            root.put("listYears", listYears);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addManagementReview", t.getTranslatedKey(lang, "Add Management Review"));
            } else {
                root.put("addManagementReview", "Add Management Review");
            }

            TemplateChooser tc = new TemplateChooser(root, "managementreviews");

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

            ManagementReview managementReview = doc.getManagementReview(id);
            ActionPlan actionPlan = doc.getActionPlanByYear(managementReview.getYear());

            root.put("organisationLocation", iso.getOrganisation().getLocation());
            root.put("managementReview", managementReview);
            root.put("actionPlan", actionPlan);
            root.put("siteName", site);

            TemplateChooser tc = new TemplateChooser(root, "managementreviewprint");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
