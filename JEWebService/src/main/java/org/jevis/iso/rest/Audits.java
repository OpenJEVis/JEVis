/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.DocumentsDirectory;
import org.jevis.iso.classes.ExternalAudit;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.InternalAudit;
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
@Path("/JEWebService/v1/audits")
public class Audits {

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

            List<ExternalAudit> listExternalAudits = doc.getExternalAudits();
            List<InternalAudit> listInternalAudits = doc.getInternalAudits();
            for (InternalAudit ia : listInternalAudits) {
                listInternalAudits.get(listInternalAudits.indexOf(ia)).buildGeneralQuestions();
                listInternalAudits.get(listInternalAudits.indexOf(ia)).buildPlanQuestions();
                listInternalAudits.get(listInternalAudits.indexOf(ia)).buildDoQuestions();
                listInternalAudits.get(listInternalAudits.indexOf(ia)).buildCheckQuestions();
                listInternalAudits.get(listInternalAudits.indexOf(ia)).buildActQuestions();
            }

            List<Long> listEAYears = doc.getListYearsExternalAudits();
            List<Long> listIAYears = doc.getListYearsInternalAudits();

            root.put("AuditsDirectoryName", doc.getAuditsDirName());
            root.put("AuditsDirectoryID", doc.getAuditsDirID());
            root.put("externalaudits", listExternalAudits);
            root.put("listEAYears", listEAYears);
            root.put("internalaudits", listInternalAudits);
            root.put("listIAYears", listIAYears);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("externalAudits", t.getTranslatedKey(lang, "External Audits"));
                root.put("internalAudits", t.getTranslatedKey(lang, "Internal Audits"));
                root.put("addAuditQuestion", t.getTranslatedKey(lang, "Add Audit Question"));
                root.put("addExternalAudit", t.getTranslatedKey(lang, "Add External Audit"));
                root.put("addInternalAudit", t.getTranslatedKey(lang, "Add Internal Audit"));
                root.put("removeAudit", t.getTranslatedKey(lang, "Remove Audit"));
            } else {
                root.put("addExternalAudit", "Add External Audit");
                root.put("addInternalAudit", "Add Internal Audit");
                root.put("externalAudits", "External Audits");
                root.put("internalAudits", "Internal Audits");
                root.put("addAuditQuestion", "Add Audit Question");
                root.put("removeAudit", "Remove Audit");
            }

            TemplateChooser tc = new TemplateChooser(root, "audits");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
