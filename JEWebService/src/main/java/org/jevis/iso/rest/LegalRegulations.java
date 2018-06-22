/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.DocumentsDirectory;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.LegalRegulation;
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
@Path("/JEWebService/v1/legalregulations")
public class LegalRegulations {

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
            long LegalRegulationsDirectoryID = doc.getLegalRegulationsDirID();
            String LegalRegulationsDirectoryName = doc.getLegalRegulationsName();
            List<LegalRegulation> listLegalRegulations = doc.getLegalRegulations();

            root.put("LegalRegulationsDirectoryID", LegalRegulationsDirectoryID);
            root.put("LegalRegulationsDirectoryName", LegalRegulationsDirectoryName);
            root.put("legalregulations", listLegalRegulations);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addLegalRegulation", t.getTranslatedKey(lang, "Add Legal Regulation"));
            } else {
                root.put("addLegalRegulation", "Add Legal Regulation");
            }

            TemplateChooser tc = new TemplateChooser(root, "legalregulations");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
