/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.classes;

import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;
import org.jevis.ws.sql.SQLDataSource;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class ManagementManualDirectory {
    private long ID;
    private String name;
    private SQLDataSource ds;

    private ManagementDocument scope;

    private ManagementDocument normativeReferences;

    private ManagementDocument termsAndDefinitions;
    private ManagementDocument termsOrganization;
    private ManagementDocument termsManagementSystem;
    private ManagementDocument termsRequirements;
    private ManagementDocument termsPerformance;
    private ManagementDocument termsEnergy;

    private ManagementDocument contextOfTheOrganization;
    private ManagementDocument contextEnergyManagementSystem;
    private ManagementDocument contextUnderstandingOrganization;
    private ManagementDocument contextUnderstandingNeeds;
    private ManagementDocument contextScopeAndBoundaries;

    private ManagementDocument leadership;
    private ManagementDocument leadershipAndCommitment;
    private ManagementDocument leadershipEnergyPolicy;
    private ManagementDocument leadershipRolesResponsibilities;

    private ManagementDocument planning;
    private ManagementDocument planningGeneral;
    private ManagementDocument planningRisks;
    private ManagementDocument planningEnergyReview;
    private ManagementDocument planningEnergyPerformanceIndicators;
    private ManagementDocument planningEnergyBaseline;
    private ManagementDocument planningObjectives;
    private ManagementDocument planningEnergyDataCollection;

    private ManagementDocument support;
    private ManagementDocument supportResources;
    private ManagementDocument supportCompetence;
    private ManagementDocument supportAwareness;
    private ManagementDocument supportCommunication;
    private ManagementDocument supportDocumentedInformation;
    private ManagementDocument supportDocumentedInformationGeneral;
    private ManagementDocument supportDocumentedInformationCreatingUpdating;
    private ManagementDocument supportDocumentedInformationControl;

    private ManagementDocument operation;
    private ManagementDocument operationPlanning;
    private ManagementDocument operationDesign;
    private ManagementDocument operationProcurement;

    private ManagementDocument performanceEvaluation;
    private ManagementDocument performanceEvaluationMonitoring;
    private ManagementDocument performanceEvaluationCompliance;
    private ManagementDocument performanceEvaluationInternalAudit;
    private ManagementDocument performanceEvaluationManagementReview;

    private ManagementDocument improvement;
    private ManagementDocument improvementNonConformity;
    private ManagementDocument improvementContinualImprovement;

    public ManagementManualDirectory() {

        ID = 0L;
        name = "";
    }

    public ManagementManualDirectory(SQLDataSource ds, JsonObject input) throws Exception {
        ID = 0L;
        name = "";
        this.ID = input.getId();
        this.name = input.getName();
        this.ds = ds;

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual01Scope().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.scope = new ManagementDocument(ds, child);
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual02NormativeReferences().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.normativeReferences = new ManagementDocument(ds, child);
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual03TermsAndDefinitions().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.termsAndDefinitions = new ManagementDocument(ds, child);

                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual031TermsRelatedToTheOrganization().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.termsOrganization = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual032TermsRelatedToTheManagementSystem().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.termsManagementSystem = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual033TermsRelatedToRequirement().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.termsRequirements = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual034TermsRelatedToPerformance().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.termsPerformance = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual035TermsRelatedToEnergy().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.termsEnergy = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual04ContextOfTheOrganization().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.contextOfTheOrganization = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual040EnergyManagementSystem().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.contextEnergyManagementSystem = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual041UnderstandingTheOrganizationAndItsContext().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.contextUnderstandingOrganization = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.contextUnderstandingNeeds = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.contextScopeAndBoundaries = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual05Leadership().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.leadership = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual051LeadershipAndCommitment().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.leadershipAndCommitment = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual052EnergyPolicy().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.leadershipEnergyPolicy = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual053OrganizationRolesResponsibilitiesAndAuthorities().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.leadershipRolesResponsibilities = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual06Planning().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.planning = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual061General().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningGeneral = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual062ActionsToAddressRisksAndOpportunities().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningRisks = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual063EnergyReview().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningEnergyReview = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual064EnergyPerformanceIndicators().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningEnergyPerformanceIndicators = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual065EnergyBaseline().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningEnergyBaseline = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningObjectives = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual067PlanningForEnergyDataCollection().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.planningEnergyDataCollection = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual07Support().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.support = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual071Resources().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.supportResources = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual072Competence().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.supportCompetence = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual073Awareness().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.supportAwareness = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual074Communication().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.supportCommunication = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual075DocumentedInformation().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.supportDocumentedInformation = new ManagementDocument(ds, grandChild);
                        for (JsonObject grandGrandChild : ds.getObjects(ISO50001.getJc().getManagementManual0751General().getName(), false)) {
                            for (JsonRelationship rel : getDs().getRelationships()) {
                                if (rel.getFrom() == grandGrandChild.getId()) {
                                    if (rel.getType() == 1) {
                                        grandGrandChild.setParent(rel.getTo());
                                    }
                                }
                            }
                            if (grandGrandChild.getParent() == grandChild.getId()) {
                                this.supportDocumentedInformationGeneral = new ManagementDocument(ds, grandGrandChild);
                            }
                        }
                        for (JsonObject grandGrandChild : ds.getObjects(ISO50001.getJc().getManagementManual0752CreatingAndUpdating().getName(), false)) {
                            for (JsonRelationship rel : getDs().getRelationships()) {
                                if (rel.getFrom() == grandGrandChild.getId()) {
                                    if (rel.getType() == 1) {
                                        grandGrandChild.setParent(rel.getTo());
                                    }
                                }
                            }
                            if (grandGrandChild.getParent() == grandChild.getId()) {
                                this.supportDocumentedInformationCreatingUpdating = new ManagementDocument(ds, grandGrandChild);
                            }
                        }
                        for (JsonObject grandGrandChild : ds.getObjects(ISO50001.getJc().getManagementManual0753ControlOfDocumenteInformation().getName(), false)) {
                            for (JsonRelationship rel : getDs().getRelationships()) {
                                if (rel.getFrom() == grandGrandChild.getId()) {
                                    if (rel.getType() == 1) {
                                        grandGrandChild.setParent(rel.getTo());
                                    }
                                }
                            }
                            if (grandGrandChild.getParent() == grandChild.getId()) {
                                this.supportDocumentedInformationControl = new ManagementDocument(ds, grandGrandChild);
                            }
                        }
                    }
                }

            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual08Operation().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.operation = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual081OperationalPlanningAndControl().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.operationPlanning = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual082Design().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.operationDesign = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual083Procurement().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.operationProcurement = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual09PerformanceEvaluation().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.performanceEvaluation = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.performanceEvaluationMonitoring = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.performanceEvaluationCompliance = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual093InternalEnMSAudit().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.performanceEvaluationInternalAudit = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual094ManagementReview().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.performanceEvaluationManagementReview = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

        for (JsonObject child : ds.getObjects(ISO50001.getJc().getManagementManual10Improvement().getName(), false)) {
            for (JsonRelationship rel : getDs().getRelationships()) {
                if (rel.getFrom() == child.getId()) {
                    if (rel.getType() == 1) {
                        child.setParent(rel.getTo());
                    }
                }
            }
            if (child.getParent() == input.getId()) {
                this.improvement = new ManagementDocument(ds, child);
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual101NonconformityAndCorrectiveAction().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.improvementNonConformity = new ManagementDocument(ds, grandChild);
                    }
                }
                for (JsonObject grandChild : ds.getObjects(ISO50001.getJc().getManagementManual102ContinualImprovement().getName(), false)) {
                    for (JsonRelationship rel : getDs().getRelationships()) {
                        if (rel.getFrom() == grandChild.getId()) {
                            if (rel.getType() == 1) {
                                grandChild.setParent(rel.getTo());
                            }
                        }
                    }
                    if (grandChild.getParent() == child.getId()) {
                        this.improvementContinualImprovement = new ManagementDocument(ds, grandChild);
                    }
                }
            }
        }

    }

    public SQLDataSource getDs() {
        return ds;
    }

    public void setDs(SQLDataSource ds) {
        this.ds = ds;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ManagementDocument getScope() {
        return scope;
    }

    public void setScope(ManagementDocument scope) {
        this.scope = scope;
    }

    public ManagementDocument getNormativeReferences() {
        return normativeReferences;
    }

    public void setNormativeReferences(ManagementDocument normativeReferences) {
        this.normativeReferences = normativeReferences;
    }

    public ManagementDocument getTermsAndDefinitions() {
        return termsAndDefinitions;
    }

    public void setTermsAndDefinitions(ManagementDocument termsAndDefinitions) {
        this.termsAndDefinitions = termsAndDefinitions;
    }

    public ManagementDocument getPlanningEnergyDataCollection() {
        return planningEnergyDataCollection;
    }

    public void setPlanningEnergyDataCollection(ManagementDocument planningEnergyDataCollection) {
        this.planningEnergyDataCollection = planningEnergyDataCollection;
    }

    public ManagementDocument getTermsOrganization() {
        return termsOrganization;
    }

    public void setTermsOrganization(ManagementDocument termsOrganization) {
        this.termsOrganization = termsOrganization;
    }

    public ManagementDocument getTermsManagementSystem() {
        return termsManagementSystem;
    }

    public void setTermsManagementSystem(ManagementDocument termsManagementSystem) {
        this.termsManagementSystem = termsManagementSystem;
    }

    public ManagementDocument getTermsRequirements() {
        return termsRequirements;
    }

    public void setTermsRequirements(ManagementDocument termsRequirements) {
        this.termsRequirements = termsRequirements;
    }

    public ManagementDocument getTermsPerformance() {
        return termsPerformance;
    }

    public void setTermsPerformance(ManagementDocument termsPerformance) {
        this.termsPerformance = termsPerformance;
    }

    public ManagementDocument getTermsEnergy() {
        return termsEnergy;
    }

    public void setTermsEnergy(ManagementDocument termsEnergy) {
        this.termsEnergy = termsEnergy;
    }

    public ManagementDocument getContextOfTheOrganization() {
        return contextOfTheOrganization;
    }

    public void setContextOfTheOrganization(ManagementDocument contextOfTheOrganization) {
        this.contextOfTheOrganization = contextOfTheOrganization;
    }

    public ManagementDocument getContextEnergyManagementSystem() {
        return contextEnergyManagementSystem;
    }

    public void setContextEnergyManagementSystem(ManagementDocument contextEnergyManagementSystem) {
        this.contextEnergyManagementSystem = contextEnergyManagementSystem;
    }

    public ManagementDocument getContextUnderstandingOrganization() {
        return contextUnderstandingOrganization;
    }

    public void setContextUnderstandingOrganization(ManagementDocument contextUnderstandingOrganization) {
        this.contextUnderstandingOrganization = contextUnderstandingOrganization;
    }

    public ManagementDocument getContextUnderstandingNeeds() {
        return contextUnderstandingNeeds;
    }

    public void setContextUnderstandingNeeds(ManagementDocument contextUnderstandingNeeds) {
        this.contextUnderstandingNeeds = contextUnderstandingNeeds;
    }

    public ManagementDocument getContextScopeAndBoundaries() {
        return contextScopeAndBoundaries;
    }

    public void setContextScopeAndBoundaries(ManagementDocument contextScopeAndBoundaries) {
        this.contextScopeAndBoundaries = contextScopeAndBoundaries;
    }

    public ManagementDocument getLeadership() {
        return leadership;
    }

    public void setLeadership(ManagementDocument leadership) {
        this.leadership = leadership;
    }

    public ManagementDocument getLeadershipAndCommitment() {
        return leadershipAndCommitment;
    }

    public void setLeadershipAndCommitment(ManagementDocument leadershipAndCommitment) {
        this.leadershipAndCommitment = leadershipAndCommitment;
    }

    public ManagementDocument getLeadershipEnergyPolicy() {
        return leadershipEnergyPolicy;
    }

    public void setLeadershipEnergyPolicy(ManagementDocument leadershipEnergyPolicy) {
        this.leadershipEnergyPolicy = leadershipEnergyPolicy;
    }

    public ManagementDocument getLeadershipRolesResponsibilities() {
        return leadershipRolesResponsibilities;
    }

    public void setLeadershipRolesResponsibilities(ManagementDocument leadershipRolesResponsibilities) {
        this.leadershipRolesResponsibilities = leadershipRolesResponsibilities;
    }

    public ManagementDocument getPlanning() {
        return planning;
    }

    public void setPlanning(ManagementDocument planning) {
        this.planning = planning;
    }

    public ManagementDocument getPlanningGeneral() {
        return planningGeneral;
    }

    public void setPlanningGeneral(ManagementDocument planningGeneral) {
        this.planningGeneral = planningGeneral;
    }

    public ManagementDocument getPlanningRisks() {
        return planningRisks;
    }

    public void setPlanningRisks(ManagementDocument planningRisks) {
        this.planningRisks = planningRisks;
    }

    public ManagementDocument getPlanningEnergyReview() {
        return planningEnergyReview;
    }

    public void setPlanningEnergyReview(ManagementDocument planningEnergyReview) {
        this.planningEnergyReview = planningEnergyReview;
    }

    public ManagementDocument getPlanningEnergyPerformanceIndicators() {
        return planningEnergyPerformanceIndicators;
    }

    public void setPlanningEnergyPerformanceIndicators(ManagementDocument planningEnergyPerformanceIndicators) {
        this.planningEnergyPerformanceIndicators = planningEnergyPerformanceIndicators;
    }

    public ManagementDocument getPlanningEnergyBaseline() {
        return planningEnergyBaseline;
    }

    public void setPlanningEnergyBaseline(ManagementDocument planningEnergyBaseline) {
        this.planningEnergyBaseline = planningEnergyBaseline;
    }

    public ManagementDocument getPlanningObjectives() {
        return planningObjectives;
    }

    public void setPlanningObjectives(ManagementDocument planningObjectives) {
        this.planningObjectives = planningObjectives;
    }

    public ManagementDocument getSupport() {
        return support;
    }

    public void setSupport(ManagementDocument support) {
        this.support = support;
    }

    public ManagementDocument getSupportResources() {
        return supportResources;
    }

    public void setSupportResources(ManagementDocument supportResources) {
        this.supportResources = supportResources;
    }

    public ManagementDocument getSupportCompetence() {
        return supportCompetence;
    }

    public void setSupportCompetence(ManagementDocument supportCompetence) {
        this.supportCompetence = supportCompetence;
    }

    public ManagementDocument getSupportAwareness() {
        return supportAwareness;
    }

    public void setSupportAwareness(ManagementDocument supportAwareness) {
        this.supportAwareness = supportAwareness;
    }

    public ManagementDocument getSupportCommunication() {
        return supportCommunication;
    }

    public void setSupportCommunication(ManagementDocument supportCommunication) {
        this.supportCommunication = supportCommunication;
    }

    public ManagementDocument getSupportDocumentedInformation() {
        return supportDocumentedInformation;
    }

    public void setSupportDocumentedInformation(ManagementDocument supportDocumentedInformation) {
        this.supportDocumentedInformation = supportDocumentedInformation;
    }

    public ManagementDocument getSupportDocumentedInformationGeneral() {
        return supportDocumentedInformationGeneral;
    }

    public void setSupportDocumentedInformationGeneral(ManagementDocument supportDocumentedInformationGeneral) {
        this.supportDocumentedInformationGeneral = supportDocumentedInformationGeneral;
    }

    public ManagementDocument getSupportDocumentedInformationCreatingUpdating() {
        return supportDocumentedInformationCreatingUpdating;
    }

    public void setSupportDocumentedInformationCreatingUpdating(ManagementDocument supportDocumentedInformationCreatingUpdating) {
        this.supportDocumentedInformationCreatingUpdating = supportDocumentedInformationCreatingUpdating;
    }

    public ManagementDocument getSupportDocumentedInformationControl() {
        return supportDocumentedInformationControl;
    }

    public void setSupportDocumentedInformationControl(ManagementDocument supportDocumentedInformationControl) {
        this.supportDocumentedInformationControl = supportDocumentedInformationControl;
    }

    public ManagementDocument getOperation() {
        return operation;
    }

    public void setOperation(ManagementDocument operation) {
        this.operation = operation;
    }

    public ManagementDocument getOperationPlanning() {
        return operationPlanning;
    }

    public void setOperationPlanning(ManagementDocument operationPlanning) {
        this.operationPlanning = operationPlanning;
    }

    public ManagementDocument getOperationDesign() {
        return operationDesign;
    }

    public void setOperationDesign(ManagementDocument operationDesign) {
        this.operationDesign = operationDesign;
    }

    public ManagementDocument getOperationProcurement() {
        return operationProcurement;
    }

    public void setOperationProcurement(ManagementDocument operationProcurement) {
        this.operationProcurement = operationProcurement;
    }

    public ManagementDocument getPerformanceEvaluation() {
        return performanceEvaluation;
    }

    public void setPerformanceEvaluation(ManagementDocument performanceEvaluation) {
        this.performanceEvaluation = performanceEvaluation;
    }

    public ManagementDocument getPerformanceEvaluationMonitoring() {
        return performanceEvaluationMonitoring;
    }

    public void setPerformanceEvaluationMonitoring(ManagementDocument performanceEvaluationMonitoring) {
        this.performanceEvaluationMonitoring = performanceEvaluationMonitoring;
    }

    public ManagementDocument getPerformanceEvaluationCompliance() {
        return performanceEvaluationCompliance;
    }

    public void setPerformanceEvaluationCompliance(ManagementDocument performanceEvaluationCompliance) {
        this.performanceEvaluationCompliance = performanceEvaluationCompliance;
    }

    public ManagementDocument getPerformanceEvaluationInternalAudit() {
        return performanceEvaluationInternalAudit;
    }

    public void setPerformanceEvaluationInternalAudit(ManagementDocument performanceEvaluationInternalAudit) {
        this.performanceEvaluationInternalAudit = performanceEvaluationInternalAudit;
    }

    public ManagementDocument getPerformanceEvaluationManagementReview() {
        return performanceEvaluationManagementReview;
    }

    public void setPerformanceEvaluationManagementReview(ManagementDocument performanceEvaluationManagementReview) {
        this.performanceEvaluationManagementReview = performanceEvaluationManagementReview;
    }

    public ManagementDocument getImprovement() {
        return improvement;
    }

    public void setImprovement(ManagementDocument improvement) {
        this.improvement = improvement;
    }

    public ManagementDocument getImprovementNonConformity() {
        return improvementNonConformity;
    }

    public void setImprovementNonConformity(ManagementDocument improvementNonConformity) {
        this.improvementNonConformity = improvementNonConformity;
    }

    public ManagementDocument getImprovementContinualImprovement() {
        return improvementContinualImprovement;
    }

    public void setImprovementContinualImprovement(ManagementDocument improvementContinualImprovement) {
        this.improvementContinualImprovement = improvementContinualImprovement;
    }
}
