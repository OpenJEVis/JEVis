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
import org.jevis.iso.classes.DocumentsDirectory;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.Training;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/trainings")
public class Trainings {

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
            long TrainingsDirectoryID = docDir.getTrainingsDirID();
            String TrainingsDirectoryName = docDir.getTrainingsName();
            List<Training> listTrainings = docDir.getTrainings();
            List<Long> listYears = docDir.getListYearsTrainings();

            root.put("TrainingsDirectoryID", TrainingsDirectoryID);
            root.put("TrainingsDirectoryName", TrainingsDirectoryName);
            root.put("trainings", listTrainings);
            root.put("listYears", listYears);
            root.put("siteName", site);
            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("addTraining", t.getTranslatedKey(lang, "Add Training"));
            } else {
                root.put("addTraining", "Add Training");
            }

            TemplateChooser tc = new TemplateChooser(root, "trainings");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
