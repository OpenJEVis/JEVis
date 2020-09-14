/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.rest;

import org.jevis.commons.ws.sql.Config;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.iso.add.TemplateChooser;
import org.jevis.iso.classes.ISO50001;
import org.jevis.iso.classes.ManagementManualDirectory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
@Path("/JEWebService/v1/managementmanual")
public class ManagementManual {

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

            Map<String, Object> root = new HashMap<>();

            root.put("bauth", httpHeaders.getRequestHeader("authorization").get(0));

            ManagementManualDirectory mmd = iso.getOrganisation().getSite(site).getDocuments().getManagementManualDirectory();

            root.put("ManagementManualDirectoryName", mmd.getName());
            root.put("ManagementManualDirectoryID", mmd.getID());

            root.put("scope", mmd.getScope());

            root.put("normativeReferences", mmd.getNormativeReferences());

            root.put("termsAndDefinitions", mmd.getTermsAndDefinitions());
            root.put("termsOrganization", mmd.getTermsOrganization());
            root.put("termsManagementSystem", mmd.getTermsManagementSystem());
            root.put("termsRequirements", mmd.getTermsRequirements());
            root.put("termsPerformance", mmd.getTermsPerformance());
            root.put("termsEnergy", mmd.getTermsEnergy());

            root.put("contextOfTheOrganization", mmd.getContextOfTheOrganization());
            root.put("contextEnergyManagementSystem", mmd.getContextEnergyManagementSystem());
            root.put("contextUnderstandingOrganization", mmd.getContextUnderstandingOrganization());
            root.put("contextUnderstandingNeeds", mmd.getContextUnderstandingNeeds());
            root.put("contextScopeAndBoundaries", mmd.getContextScopeAndBoundaries());

            root.put("leadership", mmd.getLeadership());
            root.put("leadershipAndCommitment", mmd.getLeadershipAndCommitment());
            root.put("leadershipEnergyPolicy", mmd.getLeadershipEnergyPolicy());
            root.put("leadershipRolesResponsibilities", mmd.getLeadershipRolesResponsibilities());

            root.put("planning", mmd.getPlanning());
            root.put("planningGeneral", mmd.getPlanningGeneral());
            root.put("planningRisks", mmd.getPlanningRisks());
            root.put("planningEnergyReview", mmd.getPlanningEnergyReview());
            root.put("planningEnergyPerformanceIndicators", mmd.getPlanningEnergyPerformanceIndicators());
            root.put("planningEnergyBaseline", mmd.getPlanningEnergyBaseline());
            root.put("planningObjectives", mmd.getPlanningObjectives());
            root.put("planningEnergyDataCollection", mmd.getPlanningEnergyDataCollection());

            root.put("support", mmd.getSupport());
            root.put("supportResources", mmd.getSupportResources());
            root.put("supportCompetence", mmd.getSupportCompetence());
            root.put("supportAwareness", mmd.getSupportAwareness());
            root.put("supportCommunication", mmd.getSupportCommunication());
            root.put("supportDocumentedInformation", mmd.getSupportDocumentedInformation());
            root.put("supportDocumentedInformationGeneral", mmd.getSupportDocumentedInformationGeneral());
            root.put("supportDocumentedInformationCreatingUpdating", mmd.getSupportDocumentedInformationCreatingUpdating());
            root.put("supportDocumentedInformationControl", mmd.getSupportDocumentedInformationControl());

            root.put("operation", mmd.getOperation());
            root.put("operationPlanning", mmd.getOperationPlanning());
            root.put("operationDesign", mmd.getOperationDesign());
            root.put("operationProcurement", mmd.getOperationProcurement());

            root.put("performanceEvaluation", mmd.getPerformanceEvaluation());
            root.put("performanceEvaluationMonitoring", mmd.getPerformanceEvaluationMonitoring());
            root.put("performanceEvaluationCompliance", mmd.getPerformanceEvaluationCompliance());
            root.put("performanceEvaluationInternalAudit", mmd.getPerformanceEvaluationInternalAudit());
            root.put("performanceEvaluationManagementReview", mmd.getPerformanceEvaluationManagementReview());

            root.put("improvement", mmd.getImprovement());
            root.put("improvementNonConformity", mmd.getImprovementNonConformity());
            root.put("improvementContinualImprovement", mmd.getImprovementContinualImprovement());


            root.put("siteName", site);

            TemplateChooser tc = new TemplateChooser(root, "managementmanual");

            return Response.ok(tc.getOutput()).build();
        } finally {
            Config.CloseDS(ds);
        }
    }
}
