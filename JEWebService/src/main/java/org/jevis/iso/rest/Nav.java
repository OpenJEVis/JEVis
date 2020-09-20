/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.Navigation;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.add.Translations;
import org.jevis.iso.classes.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/nav")
public class Nav {

    /**
     * @param httpHeaders
     * @param lang
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getObject(
            @Context HttpHeaders httpHeaders,
            @Context Request request,
            @Context UriInfo url,
            @QueryParam("site") String site,
            @DefaultValue("") @QueryParam("lang") String lang
    ) throws Exception {
        SQLDataSource ds = null;

        try {

            ds = new SQLDataSource(httpHeaders, request, url);
            ds.preload(SQLDataSource.PRELOAD.ALL_OBJECT);
            ds.preload(SQLDataSource.PRELOAD.ALL_REL);

            ISO50001 iso = new ISO50001(ds);

            Site s = iso.getOrganisation().getSite(site);

            Navigation n = new Navigation();

            n.setSiteName(s.getName());

            DocumentsDirectory d = s.getDocuments();
            n.setDocumentsDirName(d.getName());
            n.setActionPlanDirName(d.getActionPlansName());
            n.setAnnouncementDirName(d.getAnnouncementsName());
            n.setAuditDirName(d.getAuditsDirName());
            n.setEnergyTeamDirName(d.getEnergyTeamMemberName());
            n.setLegalRegulationDirName(d.getLegalRegulationsName());
            n.setManagementManualDirName(d.getManagementManualDirName());
            n.setManagementReviewDirName(d.getManagementReviewsName());
            n.setProceduralDocumentsDirName(d.getProceduralDocumentsName());
            n.setTrainingCourseDirName(d.getTrainingCoursesName());
            n.setTrainingsDirName(d.getTrainingsName());

            EnergyPlanningDirectory e = s.getenergyplanning();
            n.setEnergyPlanningDirName(e.getName());
            n.setEnergyFlowChartsDirName(e.getEnergyFlowChartDirName());
            n.setEnergySourcesDirName(e.getEnergySourcesDirName());
            n.setPerformanceDirName(e.getProductionDirName());
            n.setEquipmentRegisterDirName(e.getEquipmentRegisterDirName());

            n.setMeetingsDirName(s.getMeetingsDirName());

            MonitoringRegisterDirectory m = s.getMonitoringregister();
            n.setMonitoringRegisterDirName(m.getName());
            n.setMeasuringPointDirName(m.getMeasuringPointDirName());
            n.setMeterDirName(m.getMeterDirName());
            n.setStationDirName(m.getStationDirName());

            Map<String, Object> root = new HashMap<>();

            if (!lang.equals("")) {
                Translations t = new Translations();
                root.put("energymanagement", t.getTranslatedKey(lang, "Energy Management"));
            } else {
                root.put("energymanagement", "Energy Management");
            }

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            root.put("n", n);
            root.put("ISODirectoryID", iso.getOrganisation().getID());

            TemplateChooser tc = new TemplateChooser(root, "nav");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
