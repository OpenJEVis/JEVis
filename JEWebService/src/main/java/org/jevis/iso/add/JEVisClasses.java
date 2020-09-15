/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.iso.add;

import org.jevis.commons.ws.json.JsonJEVisClass;
import org.jevis.commons.ws.sql.SQLDataSource;

/**
 * @author <gerrit.schutz@envidatec.com>Gerrit Schutz</gerrit.schutz@envidatec.com>
 */
public class JEVisClasses {

    private final String docsStr = "Documents";
    private JsonJEVisClass OrganizationDir = null;
    private JsonJEVisClass ISO50001Dir = null;
    private JsonJEVisClass SuperiorLevelMeetingsDir = null;
    private JsonJEVisClass MeetingsDir = null;
    private JsonJEVisClass DocumentsDir = null;
    private JsonJEVisClass ActionPlanDir = null;
    private JsonJEVisClass PlannedActionsDir = null;
    private JsonJEVisClass ImplementedActionsDir = null;
    private JsonJEVisClass AnnouncementDir = null;
    private JsonJEVisClass AuditDir = null;
    private JsonJEVisClass LegalRegulationDir = null;
    private JsonJEVisClass ManagementManualDir = null;
    private JsonJEVisClass ManagementReviewDir = null;
    private JsonJEVisClass TrainingCourseDir = null;
    private JsonJEVisClass TrainingDir = null;
    private JsonJEVisClass EnergySourcesDir = null;
    private JsonJEVisClass EnergyFlowChartDir = null;
    private JsonJEVisClass PerformanceDir = null;
    private JsonJEVisClass EnergyPlanningDir = null;
    private JsonJEVisClass MonitoringRegisterDir = null;
    private JsonJEVisClass EquipmentRegisterDir = null;
    private JsonJEVisClass AirConditionEquipmentDir = null;
    private JsonJEVisClass CompressorEquipmentDir = null;
    private JsonJEVisClass CoolingEquipmentDir = null;
    private JsonJEVisClass HeatingEquipmentDir = null;
    private JsonJEVisClass LightingEquipmentDir = null;
    private JsonJEVisClass OfficeEquipmentDir = null;
    private JsonJEVisClass PantryEquipmentDir = null;
    private JsonJEVisClass ProductionEquipmentDir = null;
    private JsonJEVisClass VentilationEquipmentDir = null;
    private JsonJEVisClass MeasuringPointDir = null;
    private JsonJEVisClass ProceduralDocumentsDir = null;
    private JsonJEVisClass MeterDir = null;
    private JsonJEVisClass StationDir = null;
    private JsonJEVisClass EnergyTeamDir = null;
    //normal classes declaration
    private JsonJEVisClass InitialContact = null;
    private JsonJEVisClass Meeting = null;
    private JsonJEVisClass Site = null;
    private JsonJEVisClass ActionPlan = null;
    private JsonJEVisClass Announcement = null;
    private JsonJEVisClass ExternalAudit = null;
    private JsonJEVisClass InternalAudit = null;
    private JsonJEVisClass InternalAuditGeneral = null;
    private JsonJEVisClass InternalAuditPlan = null;
    private JsonJEVisClass InternalAuditDo = null;
    private JsonJEVisClass InternalAuditCheck = null;
    private JsonJEVisClass InternalAuditAct = null;
    private JsonJEVisClass AuditQuestion = null;
    private JsonJEVisClass AuditQuestion4100 = null;
    private JsonJEVisClass LegalRegulation = null;
    private JsonJEVisClass ManagementManual01Scope = null;
    private JsonJEVisClass ManagementManual02NormativeReferences = null;
    private JsonJEVisClass ManagementManual03TermsAndDefinitions = null;
    private JsonJEVisClass ManagementManual031TermsRelatedToTheOrganization = null;
    private JsonJEVisClass ManagementManual032TermsRelatedToTheManagementSystem = null;
    private JsonJEVisClass ManagementManual033TermsRelatedToRequirement = null;
    private JsonJEVisClass ManagementManual034TermsRelatedToPerformance = null;
    private JsonJEVisClass ManagementManual035TermsRelatedToEnergy = null;
    private JsonJEVisClass ManagementManual04ContextOfTheOrganization = null;
    private JsonJEVisClass ManagementManual040EnergyManagementSystem = null;
    private JsonJEVisClass ManagementManual041UnderstandingTheOrganizationAndItsContext = null;
    private JsonJEVisClass ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties = null;
    private JsonJEVisClass ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem = null;
    private JsonJEVisClass ManagementManual05Leadership = null;
    private JsonJEVisClass ManagementManual051LeadershipAndCommitment = null;
    private JsonJEVisClass ManagementManual052EnergyPolicy = null;
    private JsonJEVisClass ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities = null;
    private JsonJEVisClass ManagementManual06Planning = null;
    private JsonJEVisClass ManagementManual061General = null;
    private JsonJEVisClass ManagementManual062ActionsToAddressRisksAndOpportunities = null;
    private JsonJEVisClass ManagementManual063EnergyReview = null;
    private JsonJEVisClass ManagementManual064EnergyPerformanceIndicators = null;
    private JsonJEVisClass ManagementManual065EnergyBaseline = null;
    private JsonJEVisClass ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem = null;
    private JsonJEVisClass ManagementManual067PlanningForEnergyDataCollection = null;
    private JsonJEVisClass ManagementManual07Support = null;
    private JsonJEVisClass ManagementManual071Resources = null;
    private JsonJEVisClass ManagementManual072Competence = null;
    private JsonJEVisClass ManagementManual073Awareness = null;
    private JsonJEVisClass ManagementManual074Communication = null;
    private JsonJEVisClass ManagementManual075DocumentedInformation = null;
    private JsonJEVisClass ManagementManual0751General = null;
    private JsonJEVisClass ManagementManual0752CreatingAndUpdating = null;
    private JsonJEVisClass ManagementManual0753ControlOfDocumenteInformation = null;
    private JsonJEVisClass ManagementManual08Operation = null;
    private JsonJEVisClass ManagementManual081OperationalPlanningAndControl = null;
    private JsonJEVisClass ManagementManual082Design = null;
    private JsonJEVisClass ManagementManual083Procurement = null;
    private JsonJEVisClass ManagementManual09PerformanceEvaluation = null;
    private JsonJEVisClass ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS = null;
    private JsonJEVisClass ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements = null;
    private JsonJEVisClass ManagementManual093InternalEnMSAudit = null;
    private JsonJEVisClass ManagementManual094ManagementReview = null;
    private JsonJEVisClass ManagementManual10Improvement = null;
    private JsonJEVisClass ManagementManual101NonconformityAndCorrectiveAction = null;
    private JsonJEVisClass ManagementManual102ContinualImprovement = null;
    private JsonJEVisClass ManagementReview = null;
    private JsonJEVisClass TrainingCourse = null;
    private JsonJEVisClass Training = null;
    private JsonJEVisClass EnergySource = null;
    private JsonJEVisClass EnergyBills = null;
    private JsonJEVisClass EnergyConsumption = null;
    private JsonJEVisClass EnergyFlowChart = null;
    private JsonJEVisClass EnergySavingAction = null;
    private JsonJEVisClass AirConditioning = null;
    private JsonJEVisClass Compressor = null;
    private JsonJEVisClass Cooler = null;
    private JsonJEVisClass RecoolerOrCondenser = null;
    private JsonJEVisClass Heater = null;
    private JsonJEVisClass Pump = null;
    private JsonJEVisClass Lighting = null;
    private JsonJEVisClass OfficeEquipment = null;
    private JsonJEVisClass PantryEquipment = null;
    private JsonJEVisClass ProductionEquipment = null;
    private JsonJEVisClass Ventilation = null;
    private JsonJEVisClass EvaluatedOutput = null;
    private JsonJEVisClass MeasuringPoint = null;
    private JsonJEVisClass Meter = null;
    private JsonJEVisClass Station = null;
    private JsonJEVisClass MonthlyValues = null;
    private JsonJEVisClass EnergyManager = null;
    private JsonJEVisClass EnergyTeamMember = null;
    private JsonJEVisClass ProceduralDocument = null;
    private JsonJEVisClass docs = null;

    public JEVisClasses(SQLDataSource input) {
        String actionPlanStr = "Action Plan";
        this.ActionPlan = input.getJEVisClass(actionPlanStr);
        String actionPlanDirStr = "Action Plan Directory";
        this.ActionPlanDir = input.getJEVisClass(actionPlanDirStr);
        String airConditionEquipmentDirStr = "Air Condition Equipment Directory";
        this.AirConditionEquipmentDir = input.getJEVisClass(airConditionEquipmentDirStr);
        String announcementStr = "Announcement";
        this.Announcement = input.getJEVisClass(announcementStr);
        String announcementDirStr = "Announcement Directory";
        this.AnnouncementDir = input.getJEVisClass(announcementDirStr);
        String auditDirStr = "Audit Directory";
        this.AuditDir = input.getJEVisClass(auditDirStr);
        String auditQuestionStr = "Audit Question";
        this.AuditQuestion = input.getJEVisClass(auditQuestionStr);
        String auditQuestion4100Str = "4.1.0.0 Are scope and boundaries of EnMS defined";
        this.AuditQuestion4100 = input.getJEVisClass(auditQuestion4100Str);
        String compressorEquipmentDirStr = "Compressor Equipment Directory";
        this.CompressorEquipmentDir = input.getJEVisClass(compressorEquipmentDirStr);
        String coolingEquipmentDirStr = "Cooling Equipment Directory";
        this.CoolingEquipmentDir = input.getJEVisClass(coolingEquipmentDirStr);
        String documentsDirStr = "Documents Directory";
        this.DocumentsDir = input.getJEVisClass(documentsDirStr);
        String energyBillsStr = "Energy Bills";
        this.EnergyBills = input.getJEVisClass(energyBillsStr);
        String energyConsumptionStr = "Energy Consumption";
        this.EnergyConsumption = input.getJEVisClass(energyConsumptionStr);
        String energySourcesDirStr = "Energy Sources Directory";
        this.EnergySourcesDir = input.getJEVisClass(energySourcesDirStr);
        String energyFlowChartStr = "Energy Flow Chart";
        this.EnergyFlowChart = input.getJEVisClass(energyFlowChartStr);
        String energyFlowChartDirStr = "Energy Flow Chart Directory";
        this.EnergyFlowChartDir = input.getJEVisClass(energyFlowChartDirStr);
        String energyManagerStr = "Energy Manager";
        this.EnergyManager = input.getJEVisClass(energyManagerStr);
        String energyPlanningDirStr = "Energy Planning";
        this.EnergyPlanningDir = input.getJEVisClass(energyPlanningDirStr);
        String energySavingActionStr = "Energy Saving Action";
        this.EnergySavingAction = input.getJEVisClass(energySavingActionStr);
        String energySourceStr = "Energy Source";
        this.EnergySource = input.getJEVisClass(energySourceStr);
        String energyTeamDirStr = "Energy Team Directory";
        this.EnergyTeamDir = input.getJEVisClass(energyTeamDirStr);
        String energyTeamMemberStr = "Energy Team Member";
        this.EnergyTeamMember = input.getJEVisClass(energyTeamMemberStr);
        String equipmentRegisterDirStr = "Equipment Register";
        this.EquipmentRegisterDir = input.getJEVisClass(equipmentRegisterDirStr);
        String evaluatedOutputStr = "Evaluated Output";
        this.EvaluatedOutput = input.getJEVisClass(evaluatedOutputStr);
        String externalAuditStr = "External Audit";
        this.ExternalAudit = input.getJEVisClass(externalAuditStr);
        String heatingEquipmentDirStr = "Heating Equipment Directory";
        this.HeatingEquipmentDir = input.getJEVisClass(heatingEquipmentDirStr);
        String ISO50001DirStr = "ISO 50001 Directory";
        this.ISO50001Dir = input.getJEVisClass(ISO50001DirStr);
        String implementedActionsDirStr = "Implemented Actions Directory";
        this.ImplementedActionsDir = input.getJEVisClass(implementedActionsDirStr);
        String initialContactStr = "Initial Contact";
        this.InitialContact = input.getJEVisClass(initialContactStr);
        String internalAuditStr = "Internal Audit";
        this.InternalAudit = input.getJEVisClass(internalAuditStr);
        String internalAuditActStr = "05 Act";
        this.InternalAuditAct = input.getJEVisClass(internalAuditActStr);
        String internalAuditCheckStr = "04 Check";
        this.InternalAuditCheck = input.getJEVisClass(internalAuditCheckStr);
        String internalAuditDoStr = "03 Do";
        this.InternalAuditDo = input.getJEVisClass(internalAuditDoStr);
        String internalAuditGeneralStr = "01 General";
        this.InternalAuditGeneral = input.getJEVisClass(internalAuditGeneralStr);
        String internalAuditPlanStr = "02 Plan";
        this.InternalAuditPlan = input.getJEVisClass(internalAuditPlanStr);
        String legalRegulationStr = "Legal Regulation";
        this.LegalRegulation = input.getJEVisClass(legalRegulationStr);
        String legalRegulationDirStr = "Legal Regulation Directory";
        this.LegalRegulationDir = input.getJEVisClass(legalRegulationDirStr);
        String lightingEquipmentDirStr = "Lighting Equipment Directory";
        this.LightingEquipmentDir = input.getJEVisClass(lightingEquipmentDirStr);

        String managementManual01ScopeStr = "01 Scope";
        this.ManagementManual01Scope = input.getJEVisClass(managementManual01ScopeStr);

        String managementManual02NormativeReferencesStr = "02 Normative References";
        this.ManagementManual02NormativeReferences = input.getJEVisClass(managementManual02NormativeReferencesStr);

        String managementManual03TermsAndDefinitionsStr = "03 Terms and Definitions";
        this.ManagementManual03TermsAndDefinitions = input.getJEVisClass(managementManual03TermsAndDefinitionsStr);
        String managementManual031TermsRelatedToTheOrganizationStr = "3.1 Terms related to the Organization";
        this.ManagementManual031TermsRelatedToTheOrganization = input.getJEVisClass(managementManual031TermsRelatedToTheOrganizationStr);
        String managementManual032TermsRelatedToTheManagementSystemStr = "3.2 Terms related to the Management System";
        this.ManagementManual032TermsRelatedToTheManagementSystem = input.getJEVisClass(managementManual032TermsRelatedToTheManagementSystemStr);
        String managementManual033TermsRelatedToRequirementStr = "3.3 Terms related to Requirement";
        this.ManagementManual033TermsRelatedToRequirement = input.getJEVisClass(managementManual033TermsRelatedToRequirementStr);
        String managementManual034TermsRelatedToPerformanceStr = "3.4 Terms related to Performance";
        this.ManagementManual034TermsRelatedToPerformance = input.getJEVisClass(managementManual034TermsRelatedToPerformanceStr);
        String managementManual035TermsRelatedToEnergyStr = "3.5 Terms related to Energy";
        this.ManagementManual035TermsRelatedToEnergy = input.getJEVisClass(managementManual035TermsRelatedToEnergyStr);

        String managementManual04ContextOfTheOrganizationStr = "04 Context of the Organization";
        this.ManagementManual04ContextOfTheOrganization = input.getJEVisClass(managementManual04ContextOfTheOrganizationStr);
        String managementManual040EnergyManagementSystemStr = "4 Energy Management System";
        this.ManagementManual040EnergyManagementSystem = input.getJEVisClass(managementManual040EnergyManagementSystemStr);
        String managementManual041UnderstandingTheOrganizationAndItsContextStr = "4.1 Understanding the Organization and its Context";
        this.ManagementManual041UnderstandingTheOrganizationAndItsContext = input.getJEVisClass(managementManual041UnderstandingTheOrganizationAndItsContextStr);
        String managementManual042UnderstandingTheNeedsAndExpecationsOfInterestedPartiesStr = "4.2 Understanding the Needs and Expecations of interested parties";
        this.ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties = input.getJEVisClass(managementManual042UnderstandingTheNeedsAndExpecationsOfInterestedPartiesStr);
        String managementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystemStr = "4.3 Determining the Scope and Boundaries of the Energy Management System";
        this.ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem = input.getJEVisClass(managementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystemStr);

        String managementManual05LeadershipStr = "05 Leadership";
        this.ManagementManual05Leadership = input.getJEVisClass(managementManual05LeadershipStr);
        String managementManual051LeadershipAndCommitmentStr = "5.1 Leadership and Commitment";
        this.ManagementManual051LeadershipAndCommitment = input.getJEVisClass(managementManual051LeadershipAndCommitmentStr);
        String managementManual052EnergyPolicyStr = "5.2 Energy Policy";
        this.ManagementManual052EnergyPolicy = input.getJEVisClass(managementManual052EnergyPolicyStr);
        String managementManual053OrganizationRolesResponsibilitiesAndAuthoritiesStr = "5.3 Organization Roles, Responsibilities and Authorities";
        this.ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities = input.getJEVisClass(managementManual053OrganizationRolesResponsibilitiesAndAuthoritiesStr);

        String managementManual06PlanningStr = "06 Planning";
        this.ManagementManual06Planning = input.getJEVisClass(managementManual06PlanningStr);
        String managementManual061GeneralStr = "6.1 General";
        this.ManagementManual061General = input.getJEVisClass(managementManual061GeneralStr);
        String managementManual062ActionsToAddressRisksAndOpportunitiesStr = "6.2 Actions to Address Risks and Opportunities";
        this.ManagementManual062ActionsToAddressRisksAndOpportunities = input.getJEVisClass(managementManual062ActionsToAddressRisksAndOpportunitiesStr);
        String managementManual063EnergyReviewStr = "6.3 Energy Review";
        this.ManagementManual063EnergyReview = input.getJEVisClass(managementManual063EnergyReviewStr);
        String managementManual064EnergyPerformanceIndicatorsStr = "6.4 Energy Performance Indicators";
        this.ManagementManual064EnergyPerformanceIndicators = input.getJEVisClass(managementManual064EnergyPerformanceIndicatorsStr);
        String managementManual065EnergyBaselineStr = "6.5 Energy Baseline";
        this.ManagementManual065EnergyBaseline = input.getJEVisClass(managementManual065EnergyBaselineStr);
        String managementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThemStr = "6.6 Objectives, Energy Targets and Planning to Achieve them";
        this.ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem = input.getJEVisClass(managementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThemStr);
        String managementManual067PlanningForEnergyDataCollectionStr = "6.7 Planning for Energy Data Collection";
        this.ManagementManual067PlanningForEnergyDataCollection = input.getJEVisClass(managementManual067PlanningForEnergyDataCollectionStr);

        String managementManual07SupportStr = "07 Support";
        this.ManagementManual07Support = input.getJEVisClass(managementManual07SupportStr);
        String managementManual071ResourcesStr = "7.1 Resources";
        this.ManagementManual071Resources = input.getJEVisClass(managementManual071ResourcesStr);
        String managementManual072CompetenceStr = "7.2 Competence";
        this.ManagementManual072Competence = input.getJEVisClass(managementManual072CompetenceStr);
        String managementManual073AwarenessStr = "7.3 Awareness";
        this.ManagementManual073Awareness = input.getJEVisClass(managementManual073AwarenessStr);
        String managementManual074CommunicationStr = "7.4 Communication";
        this.ManagementManual074Communication = input.getJEVisClass(managementManual074CommunicationStr);
        String managementManual075DocumentedInformationStr = "7.5 Documented Information";
        this.ManagementManual075DocumentedInformation = input.getJEVisClass(managementManual075DocumentedInformationStr);
        String managementManual0751GeneralStr = "7.5.1 General";
        this.ManagementManual0751General = input.getJEVisClass(managementManual0751GeneralStr);
        String managementManual0752CreatingAndUpdatingStr = "7.5.2 Creating and Updating";
        this.ManagementManual0752CreatingAndUpdating = input.getJEVisClass(managementManual0752CreatingAndUpdatingStr);
        String managementManual0753ControlOfDocumenteInformationStr = "7.5.3 Control of Documented Information";
        this.ManagementManual0753ControlOfDocumenteInformation = input.getJEVisClass(managementManual0753ControlOfDocumenteInformationStr);

        String managementManual08OperationStr = "08 Operation";
        this.ManagementManual08Operation = input.getJEVisClass(managementManual08OperationStr);
        String managementManual081OperationalPlanningAndControlStr = "8.1 Operational Planning and Control";
        this.ManagementManual081OperationalPlanningAndControl = input.getJEVisClass(managementManual081OperationalPlanningAndControlStr);
        String managementManual082DesignStr = "8.2 Design";
        this.ManagementManual082Design = input.getJEVisClass(managementManual082DesignStr);
        String managementManual083ProcurementStr = "8.3 Procurement";
        this.ManagementManual083Procurement = input.getJEVisClass(managementManual083ProcurementStr);

        String managementManual09PerformanceEvaluationStr = "09 Performance Evaluation";
        this.ManagementManual09PerformanceEvaluation = input.getJEVisClass(managementManual09PerformanceEvaluationStr);
        String managementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMSStr = "9.1 Monitoring, Measurement, Analysis and Evaluation for Energy Performance and the EnMS";
        this.ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS = input.getJEVisClass(managementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMSStr);
        String managementManual092EvaluationOfComplianceWithLegalAndOtherRequirementsStr = "9.2 Evaluation of Compliance with Legal and other Requirements";
        this.ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements = input.getJEVisClass(managementManual092EvaluationOfComplianceWithLegalAndOtherRequirementsStr);
        String managementManual093InternalEnMSAuditStr = "9.3 Internal EnMS Audit";
        this.ManagementManual093InternalEnMSAudit = input.getJEVisClass(managementManual093InternalEnMSAuditStr);
        String managementManual094ManagementReviewStr = "9.4 Management Review";
        this.ManagementManual094ManagementReview = input.getJEVisClass(managementManual094ManagementReviewStr);

        String managementManual10ImprovementStr = "10 Improvement";
        this.ManagementManual10Improvement = input.getJEVisClass(managementManual10ImprovementStr);
        String managementManual101NonconformityAndCorrectiveActionStr = "10.1 Nonconformity and Corrective Action";
        this.ManagementManual101NonconformityAndCorrectiveAction = input.getJEVisClass(managementManual101NonconformityAndCorrectiveActionStr);
        String managementManual102ContinualImprovementStr = "10.2 Continual Improvement";
        this.ManagementManual102ContinualImprovement = input.getJEVisClass(managementManual102ContinualImprovementStr);

        String managementManualDirStr = "Management Manual Directory";
        this.ManagementManualDir = input.getJEVisClass(managementManualDirStr);
        String managementReviewStr = "Management Review";
        this.ManagementReview = input.getJEVisClass(managementReviewStr);
        String managementReviewDirStr = "Management Review Directory";
        this.ManagementReviewDir = input.getJEVisClass(managementReviewDirStr);
        String measuringPointStr = "Measuring Point";
        this.MeasuringPoint = input.getJEVisClass(measuringPointStr);
        String measuringPointDirStr = "Measuring Point Directory";
        this.MeasuringPointDir = input.getJEVisClass(measuringPointDirStr);
        String meetingStr = "Meeting";
        this.Meeting = input.getJEVisClass(meetingStr);
        String meetingsDirStr = "ISO 50001 Meetings Directory";
        this.MeetingsDir = input.getJEVisClass(meetingsDirStr);
        String meterStr = "Meter";
        this.Meter = input.getJEVisClass(meterStr);
        String meterDirStr = "Meter Directory";
        this.MeterDir = input.getJEVisClass(meterDirStr);
        String monitoringRegisterDirStr = "Monitoring Register";
        this.MonitoringRegisterDir = input.getJEVisClass(monitoringRegisterDirStr);
        String monthlyValuesStr = "Monthly Values";
        this.MonthlyValues = input.getJEVisClass(monthlyValuesStr);
        String officeEquipmentDirStr = "Office Equipment Directory";
        this.OfficeEquipmentDir = input.getJEVisClass(officeEquipmentDirStr);
        String organisationDirStr = "Organization";
        this.OrganizationDir = input.getJEVisClass(organisationDirStr);
        String pantryEquipmentDirStr = "Pantry Equipment Directory";
        this.PantryEquipmentDir = input.getJEVisClass(pantryEquipmentDirStr);
        String performanceDirStr = "Performance Directory";
        this.PerformanceDir = input.getJEVisClass(performanceDirStr);
        String plannedActionsDirStr = "Planned Actions Directory";
        this.PlannedActionsDir = input.getJEVisClass(plannedActionsDirStr);
        String proceduralDocumentsDirStr = "Procedural Documents Directory";
        this.ProceduralDocumentsDir = input.getJEVisClass(proceduralDocumentsDirStr);
        String proceduralDocumentStr = "Procedural Document";
        this.ProceduralDocument = input.getJEVisClass(proceduralDocumentStr);
        String productionEquipmentDirStr = "Production Equipment Directory";
        this.ProductionEquipmentDir = input.getJEVisClass(productionEquipmentDirStr);
        String siteStr = "Site";
        this.Site = input.getJEVisClass(siteStr);
        String stationStr = "Station";
        this.Station = input.getJEVisClass(stationStr);
        String stationDirStr = "Station Directory";
        this.StationDir = input.getJEVisClass(stationDirStr);
        String superiorLevelMeetingsDirStr = "Superior Level Meetings Directory";
        this.SuperiorLevelMeetingsDir = input.getJEVisClass(superiorLevelMeetingsDirStr);
        String trainingStr = "Training";
        this.Training = input.getJEVisClass(trainingStr);
        String trainingCourseStr = "Training Course";
        this.TrainingCourse = input.getJEVisClass(trainingCourseStr);
        String trainingCourseDirStr = "Training Course Directory";
        this.TrainingCourseDir = input.getJEVisClass(trainingCourseDirStr);
        String trainingDirStr = "Training Directory";
        this.TrainingDir = input.getJEVisClass(trainingDirStr);
        String ventilationEquipmentDirStr = "Ventilation Equipment Directory";
        this.VentilationEquipmentDir = input.getJEVisClass(ventilationEquipmentDirStr);

        String airConditioningStr = "Air Conditioning";
        this.AirConditioning = input.getJEVisClass(airConditioningStr);
        String compressorStr = "Compressor";
        this.Compressor = input.getJEVisClass(compressorStr);
        String coolerStr = "Cooler";
        this.Cooler = input.getJEVisClass(coolerStr);
        String recoolerOrCondenserStr = "Recooler or Condenser";
        this.RecoolerOrCondenser = input.getJEVisClass(recoolerOrCondenserStr);
        String heaterStr = "Heater";
        this.Heater = input.getJEVisClass(heaterStr);
        String pumpStr = "Pump";
        this.Pump = input.getJEVisClass(pumpStr);
        String lightingStr = "Lighting";
        this.Lighting = input.getJEVisClass(lightingStr);
        String officeEquipmentStr = "Office Equipment";
        this.OfficeEquipment = input.getJEVisClass(officeEquipmentStr);
        String pantryEquipmentStr = "Pantry Equipment";
        this.PantryEquipment = input.getJEVisClass(pantryEquipmentStr);
        String productionEquipmentStr = "Production Equipment";
        this.ProductionEquipment = input.getJEVisClass(productionEquipmentStr);
        String ventilationStr = "Ventilation";
        this.Ventilation = input.getJEVisClass(ventilationStr);

    }

    public JsonJEVisClass getOrganizationDir() {
        return OrganizationDir;
    }

    public void setOrganizationDir(JsonJEVisClass OrganizationDir) {
        this.OrganizationDir = OrganizationDir;
    }

    public JsonJEVisClass getISO50001Dir() {
        return ISO50001Dir;
    }

    public void setISO50001Dir(JsonJEVisClass ISO50001Dir) {
        this.ISO50001Dir = ISO50001Dir;
    }

    public JsonJEVisClass getSuperiorLevelMeetingsDir() {
        return SuperiorLevelMeetingsDir;
    }

    public void setSuperiorLevelMeetingsDir(JsonJEVisClass SuperiorLevelMeetingsDir) {
        this.SuperiorLevelMeetingsDir = SuperiorLevelMeetingsDir;
    }

    public JsonJEVisClass getMeetingsDir() {
        return MeetingsDir;
    }

    public void setMeetingsDir(JsonJEVisClass MeetingsDir) {
        this.MeetingsDir = MeetingsDir;
    }

    public JsonJEVisClass getDocumentsDir() {
        return DocumentsDir;
    }

    public void setDocumentsDir(JsonJEVisClass DocumentsDir) {
        this.DocumentsDir = DocumentsDir;
    }

    public JsonJEVisClass getEnergySourcesDir() {
        return EnergySourcesDir;
    }

    public void setEnergySourcesDir(JsonJEVisClass EnergySourcesDir) {
        this.EnergySourcesDir = EnergySourcesDir;
    }

    public JsonJEVisClass getActionPlanDir() {
        return ActionPlanDir;
    }

    public void setActionPlanDir(JsonJEVisClass ActionPlanDir) {
        this.ActionPlanDir = ActionPlanDir;
    }

    public JsonJEVisClass getPlannedActionsDir() {
        return PlannedActionsDir;
    }

    public void setPlannedActionsDir(JsonJEVisClass PlannedActionsDir) {
        this.PlannedActionsDir = PlannedActionsDir;
    }

    public JsonJEVisClass getAirConditioning() {
        return AirConditioning;
    }

    public void setAirConditioning(JsonJEVisClass AirConditioning) {
        this.AirConditioning = AirConditioning;
    }

    public JsonJEVisClass getCompressor() {
        return Compressor;
    }

    public void setCompressor(JsonJEVisClass Compressor) {
        this.Compressor = Compressor;
    }

    public JsonJEVisClass getCooler() {
        return Cooler;
    }

    public void setCooler(JsonJEVisClass Cooler) {
        this.Cooler = Cooler;
    }

    public JsonJEVisClass getRecoolerOrCondenser() {
        return RecoolerOrCondenser;
    }

    public void setRecoolerOrCondenser(JsonJEVisClass RecoolerOrCondenser) {
        this.RecoolerOrCondenser = RecoolerOrCondenser;
    }

    public JsonJEVisClass getHeater() {
        return Heater;
    }

    public void setHeater(JsonJEVisClass Heater) {
        this.Heater = Heater;
    }

    public JsonJEVisClass getPump() {
        return Pump;
    }

    public void setPump(JsonJEVisClass Pump) {
        this.Pump = Pump;
    }

    public JsonJEVisClass getLighting() {
        return Lighting;
    }

    public void setLighting(JsonJEVisClass Lighting) {
        this.Lighting = Lighting;
    }

    public JsonJEVisClass getOfficeEquipment() {
        return OfficeEquipment;
    }

    public void setOfficeEquipment(JsonJEVisClass OfficeEquipment) {
        this.OfficeEquipment = OfficeEquipment;
    }

    public JsonJEVisClass getPantryEquipment() {
        return PantryEquipment;
    }

    public void setPantryEquipment(JsonJEVisClass PantryEquipment) {
        this.PantryEquipment = PantryEquipment;
    }

    public JsonJEVisClass getProductionEquipment() {
        return ProductionEquipment;
    }

    public void setProductionEquipment(JsonJEVisClass ProductionEquipment) {
        this.ProductionEquipment = ProductionEquipment;
    }

    public JsonJEVisClass getVentilation() {
        return Ventilation;
    }

    public void setVentilation(JsonJEVisClass Ventilation) {
        this.Ventilation = Ventilation;
    }

    public JsonJEVisClass getImplementedActionsDir() {
        return ImplementedActionsDir;
    }

    public void setImplementedActionsDir(JsonJEVisClass ImplementedActionsDir) {
        this.ImplementedActionsDir = ImplementedActionsDir;
    }

    public JsonJEVisClass getAnnouncementDir() {
        return AnnouncementDir;
    }

    public void setAnnouncementDir(JsonJEVisClass AnnouncementDir) {
        this.AnnouncementDir = AnnouncementDir;
    }

    public JsonJEVisClass getAuditDir() {
        return AuditDir;
    }

    public void setAuditDir(JsonJEVisClass AuditDir) {
        this.AuditDir = AuditDir;
    }

    public JsonJEVisClass getLegalRegulationDir() {
        return LegalRegulationDir;
    }

    public void setLegalRegulationDir(JsonJEVisClass LegalRegulationDir) {
        this.LegalRegulationDir = LegalRegulationDir;
    }

    public JsonJEVisClass getManagementManualDir() {
        return ManagementManualDir;
    }

    public void setManagementManualDir(JsonJEVisClass ManagementManualDir) {
        this.ManagementManualDir = ManagementManualDir;
    }

    public JsonJEVisClass getManagementReviewDir() {
        return ManagementReviewDir;
    }

    public void setManagementReviewDir(JsonJEVisClass ManagementReviewDir) {
        this.ManagementReviewDir = ManagementReviewDir;
    }

    public JsonJEVisClass getTrainingCourseDir() {
        return TrainingCourseDir;
    }

    public void setTrainingCourseDir(JsonJEVisClass TrainingCourseDir) {
        this.TrainingCourseDir = TrainingCourseDir;
    }

    public JsonJEVisClass getEnergyPlanningDir() {
        return EnergyPlanningDir;
    }

    public void setEnergyPlanningDir(JsonJEVisClass EnergyPlanningDir) {
        this.EnergyPlanningDir = EnergyPlanningDir;
    }

    public JsonJEVisClass getMonitoringRegisterDir() {
        return MonitoringRegisterDir;
    }

    public void setMonitoringRegisterDir(JsonJEVisClass MonitoringRegisterDir) {
        this.MonitoringRegisterDir = MonitoringRegisterDir;
    }

    public JsonJEVisClass getEquipmentRegisterDir() {
        return EquipmentRegisterDir;
    }

    public void setEquipmentRegisterDir(JsonJEVisClass EquipmentRegisterDir) {
        this.EquipmentRegisterDir = EquipmentRegisterDir;
    }

    public JsonJEVisClass getTrainingDir() {
        return TrainingDir;
    }

    public void setTrainingDir(JsonJEVisClass TrainingDir) {
        this.TrainingDir = TrainingDir;
    }

    public JsonJEVisClass getEnergyFlowChartDir() {
        return EnergyFlowChartDir;
    }

    public void setEnergyFlowChartDir(JsonJEVisClass EnergyFlowChartDir) {
        this.EnergyFlowChartDir = EnergyFlowChartDir;
    }

    public JsonJEVisClass getPerformanceDir() {
        return PerformanceDir;
    }

    public void setPerformanceDir(JsonJEVisClass PerformanceDir) {
        this.PerformanceDir = PerformanceDir;
    }

    public JsonJEVisClass getMeasuringPointDir() {
        return MeasuringPointDir;
    }

    public void setMeasuringPointDir(JsonJEVisClass MeasuringPointDir) {
        this.MeasuringPointDir = MeasuringPointDir;
    }

    public JsonJEVisClass getMeterDir() {
        return MeterDir;
    }

    public void setMeterDir(JsonJEVisClass MeterDir) {
        this.MeterDir = MeterDir;
    }

    public JsonJEVisClass getStationDir() {
        return StationDir;
    }

    public void setStationDir(JsonJEVisClass StationDir) {
        this.StationDir = StationDir;
    }

    public JsonJEVisClass getEnergyTeamDir() {
        return EnergyTeamDir;
    }

    public void setEnergyTeamDir(JsonJEVisClass EnergyTeamDir) {
        this.EnergyTeamDir = EnergyTeamDir;
    }

    public JsonJEVisClass getInitialContact() {
        return InitialContact;
    }

    public void setInitialContact(JsonJEVisClass InitialContact) {
        this.InitialContact = InitialContact;
    }

    public JsonJEVisClass getMeeting() {
        return Meeting;
    }

    public void setMeeting(JsonJEVisClass Meeting) {
        this.Meeting = Meeting;
    }

    public JsonJEVisClass getSite() {
        return Site;
    }

    public void setSite(JsonJEVisClass Site) {
        this.Site = Site;
    }

    public JsonJEVisClass getActionPlan() {
        return ActionPlan;
    }

    public void setActionPlan(JsonJEVisClass ActionPlan) {
        this.ActionPlan = ActionPlan;
    }

    public JsonJEVisClass getAnnouncement() {
        return Announcement;
    }

    public void setAnnouncement(JsonJEVisClass Announcement) {
        this.Announcement = Announcement;
    }

    public JsonJEVisClass getExternalAudit() {
        return ExternalAudit;
    }

    public void setExternalAudit(JsonJEVisClass ExternalAudit) {
        this.ExternalAudit = ExternalAudit;
    }

    public JsonJEVisClass getInternalAudit() {
        return InternalAudit;
    }

    public void setInternalAudit(JsonJEVisClass InternalAudit) {
        this.InternalAudit = InternalAudit;
    }

    public JsonJEVisClass getInternalAuditGeneral() {
        return InternalAuditGeneral;
    }

    public void setInternalAuditGeneral(JsonJEVisClass InternalAuditGeneral) {
        this.InternalAuditGeneral = InternalAuditGeneral;
    }

    public JsonJEVisClass getInternalAuditPlan() {
        return InternalAuditPlan;
    }

    public void setInternalAuditPlan(JsonJEVisClass InternalAuditPlan) {
        this.InternalAuditPlan = InternalAuditPlan;
    }

    public JsonJEVisClass getInternalAuditDo() {
        return InternalAuditDo;
    }

    public void setInternalAuditDo(JsonJEVisClass InternalAuditDo) {
        this.InternalAuditDo = InternalAuditDo;
    }

    public JsonJEVisClass getInternalAuditCheck() {
        return InternalAuditCheck;
    }

    public void setInternalAuditCheck(JsonJEVisClass InternalAuditCheck) {
        this.InternalAuditCheck = InternalAuditCheck;
    }

    public JsonJEVisClass getInternalAuditAct() {
        return InternalAuditAct;
    }

    public void setInternalAuditAct(JsonJEVisClass InternalAuditAct) {
        this.InternalAuditAct = InternalAuditAct;
    }

    public JsonJEVisClass getAuditQuestion() {
        return AuditQuestion;
    }

    public void setAuditQuestion(JsonJEVisClass AuditQuestion) {
        this.AuditQuestion = AuditQuestion;
    }

    public JsonJEVisClass getAuditQuestion4100() {
        return AuditQuestion4100;
    }

    public void setAuditQuestion4100(JsonJEVisClass AuditQuestion4100) {
        this.AuditQuestion4100 = AuditQuestion4100;
    }

    public JsonJEVisClass getManagementManual01Scope() {
        return ManagementManual01Scope;
    }

    public void setManagementManual01Scope(JsonJEVisClass ManagementManual01Scope) {
        this.ManagementManual01Scope = ManagementManual01Scope;
    }

    public JsonJEVisClass getManagementManual02NormativeReferences() {
        return ManagementManual02NormativeReferences;
    }

    public void setManagementManual02NormativeReferences(JsonJEVisClass ManagementManual02NormativeReferences) {
        this.ManagementManual02NormativeReferences = ManagementManual02NormativeReferences;
    }

    public JsonJEVisClass getManagementManual03TermsAndDefinitions() {
        return ManagementManual03TermsAndDefinitions;
    }

    public void setManagementManual03TermsAndDefinitions(JsonJEVisClass ManagementManual03TermsAndDefinitions) {
        this.ManagementManual03TermsAndDefinitions = ManagementManual03TermsAndDefinitions;
    }

    public JsonJEVisClass getManagementManual031TermsRelatedToTheOrganization() {
        return ManagementManual031TermsRelatedToTheOrganization;
    }

    public void setManagementManual031TermsRelatedToTheOrganization(JsonJEVisClass ManagementManual031TermsRelatedToTheOrganization) {
        this.ManagementManual031TermsRelatedToTheOrganization = ManagementManual031TermsRelatedToTheOrganization;
    }

    public JsonJEVisClass getManagementManual032TermsRelatedToTheManagementSystem() {
        return ManagementManual032TermsRelatedToTheManagementSystem;
    }

    public void setManagementManual032TermsRelatedToTheManagementSystem(JsonJEVisClass ManagementManual032TermsRelatedToTheManagementSystem) {
        this.ManagementManual032TermsRelatedToTheManagementSystem = ManagementManual032TermsRelatedToTheManagementSystem;
    }

    public JsonJEVisClass getManagementManual033TermsRelatedToRequirement() {
        return ManagementManual033TermsRelatedToRequirement;
    }

    public void setManagementManual033TermsRelatedToRequirement(JsonJEVisClass ManagementManual033TermsRelatedToRequirement) {
        this.ManagementManual033TermsRelatedToRequirement = ManagementManual033TermsRelatedToRequirement;
    }

    public JsonJEVisClass getManagementManual034TermsRelatedToPerformance() {
        return ManagementManual034TermsRelatedToPerformance;
    }

    public void setManagementManual034TermsRelatedToPerformance(JsonJEVisClass ManagementManual034TermsRelatedToPerformance) {
        this.ManagementManual034TermsRelatedToPerformance = ManagementManual034TermsRelatedToPerformance;
    }

    public JsonJEVisClass getManagementManual035TermsRelatedToEnergy() {
        return ManagementManual035TermsRelatedToEnergy;
    }

    public void setManagementManual035TermsRelatedToEnergy(JsonJEVisClass ManagementManual035TermsRelatedToEnergy) {
        this.ManagementManual035TermsRelatedToEnergy = ManagementManual035TermsRelatedToEnergy;
    }

    public JsonJEVisClass getManagementManual04ContextOfTheOrganization() {
        return ManagementManual04ContextOfTheOrganization;
    }

    public void setManagementManual04ContextOfTheOrganization(JsonJEVisClass ManagementManual04ContextOfTheOrganization) {
        this.ManagementManual04ContextOfTheOrganization = ManagementManual04ContextOfTheOrganization;
    }

    public JsonJEVisClass getManagementManual040EnergyManagementSystem() {
        return ManagementManual040EnergyManagementSystem;
    }

    public void setManagementManual040EnergyManagementSystem(JsonJEVisClass ManagementManual040EnergyManagementSystem) {
        this.ManagementManual040EnergyManagementSystem = ManagementManual040EnergyManagementSystem;
    }

    public JsonJEVisClass getManagementManual041UnderstandingTheOrganizationAndItsContext() {
        return ManagementManual041UnderstandingTheOrganizationAndItsContext;
    }

    public void setManagementManual041UnderstandingTheOrganizationAndItsContext(JsonJEVisClass ManagementManual041UnderstandingTheOrganizationAndItsContext) {
        this.ManagementManual041UnderstandingTheOrganizationAndItsContext = ManagementManual041UnderstandingTheOrganizationAndItsContext;
    }

    public JsonJEVisClass getManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties() {
        return ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties;
    }

    public void setManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties(JsonJEVisClass ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties) {
        this.ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties = ManagementManual042UnderstandingTheNeedsAndExpecationsOfInterestedParties;
    }

    public JsonJEVisClass getManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem() {
        return ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem;
    }

    public void setManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem(JsonJEVisClass ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem) {
        this.ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem = ManagementManual043DeterminingTheScopeAndBoundariesOfTheEnergyManagementSystem;
    }

    public JsonJEVisClass getManagementManual05Leadership() {
        return ManagementManual05Leadership;
    }

    public void setManagementManual05Leadership(JsonJEVisClass ManagementManual05Leadership) {
        this.ManagementManual05Leadership = ManagementManual05Leadership;
    }

    public JsonJEVisClass getManagementManual051LeadershipAndCommitment() {
        return ManagementManual051LeadershipAndCommitment;
    }

    public void setManagementManual051LeadershipAndCommitment(JsonJEVisClass ManagementManual051LeadershipAndCommitment) {
        this.ManagementManual051LeadershipAndCommitment = ManagementManual051LeadershipAndCommitment;
    }

    public JsonJEVisClass getManagementManual052EnergyPolicy() {
        return ManagementManual052EnergyPolicy;
    }

    public void setManagementManual052EnergyPolicy(JsonJEVisClass ManagementManual052EnergyPolicy) {
        this.ManagementManual052EnergyPolicy = ManagementManual052EnergyPolicy;
    }

    public JsonJEVisClass getManagementManual053OrganizationRolesResponsibilitiesAndAuthorities() {
        return ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities;
    }

    public void setManagementManual053OrganizationRolesResponsibilitiesAndAuthorities(JsonJEVisClass ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities) {
        this.ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities = ManagementManual053OrganizationRolesResponsibilitiesAndAuthorities;
    }

    public JsonJEVisClass getManagementManual06Planning() {
        return ManagementManual06Planning;
    }

    public void setManagementManual06Planning(JsonJEVisClass ManagementManual06Planning) {
        this.ManagementManual06Planning = ManagementManual06Planning;
    }

    public JsonJEVisClass getManagementManual061General() {
        return ManagementManual061General;
    }

    public void setManagementManual061General(JsonJEVisClass ManagementManual061General) {
        this.ManagementManual061General = ManagementManual061General;
    }

    public JsonJEVisClass getManagementManual062ActionsToAddressRisksAndOpportunities() {
        return ManagementManual062ActionsToAddressRisksAndOpportunities;
    }

    public void setManagementManual062ActionsToAddressRisksAndOpportunities(JsonJEVisClass ManagementManual062ActionsToAddressRisksAndOpportunities) {
        this.ManagementManual062ActionsToAddressRisksAndOpportunities = ManagementManual062ActionsToAddressRisksAndOpportunities;
    }

    public JsonJEVisClass getManagementManual063EnergyReview() {
        return ManagementManual063EnergyReview;
    }

    public void setManagementManual063EnergyReview(JsonJEVisClass ManagementManual063EnergyReview) {
        this.ManagementManual063EnergyReview = ManagementManual063EnergyReview;
    }

    public JsonJEVisClass getManagementManual064EnergyPerformanceIndicators() {
        return ManagementManual064EnergyPerformanceIndicators;
    }

    public void setManagementManual064EnergyPerformanceIndicators(JsonJEVisClass ManagementManual064EnergyPerformanceIndicators) {
        this.ManagementManual064EnergyPerformanceIndicators = ManagementManual064EnergyPerformanceIndicators;
    }

    public JsonJEVisClass getManagementManual065EnergyBaseline() {
        return ManagementManual065EnergyBaseline;
    }

    public void setManagementManual065EnergyBaseline(JsonJEVisClass ManagementManual065EnergyBaseline) {
        this.ManagementManual065EnergyBaseline = ManagementManual065EnergyBaseline;
    }

    public JsonJEVisClass getManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem() {
        return ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem;
    }

    public void setManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem(JsonJEVisClass ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem) {
        this.ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem = ManagementManual066ObjectivesEnergyTargetsAndPlanningToAchieveThem;
    }

    public JsonJEVisClass getManagementManual067PlanningForEnergyDataCollection() {
        return ManagementManual067PlanningForEnergyDataCollection;
    }

    public void setManagementManual067PlanningForEnergyDataCollection(JsonJEVisClass ManagementManual067PlanningForEnergyDataCollection) {
        this.ManagementManual067PlanningForEnergyDataCollection = ManagementManual067PlanningForEnergyDataCollection;
    }

    public JsonJEVisClass getManagementManual07Support() {
        return ManagementManual07Support;
    }

    public void setManagementManual07Support(JsonJEVisClass ManagementManual07Support) {
        this.ManagementManual07Support = ManagementManual07Support;
    }

    public JsonJEVisClass getManagementManual071Resources() {
        return ManagementManual071Resources;
    }

    public void setManagementManual071Resources(JsonJEVisClass ManagementManual071Resources) {
        this.ManagementManual071Resources = ManagementManual071Resources;
    }

    public JsonJEVisClass getManagementManual072Competence() {
        return ManagementManual072Competence;
    }

    public void setManagementManual072Competence(JsonJEVisClass ManagementManual072Competence) {
        this.ManagementManual072Competence = ManagementManual072Competence;
    }

    public JsonJEVisClass getManagementManual073Awareness() {
        return ManagementManual073Awareness;
    }

    public void setManagementManual073Awareness(JsonJEVisClass ManagementManual073Awareness) {
        this.ManagementManual073Awareness = ManagementManual073Awareness;
    }

    public JsonJEVisClass getManagementManual074Communication() {
        return ManagementManual074Communication;
    }

    public void setManagementManual074Communication(JsonJEVisClass ManagementManual074Communication) {
        this.ManagementManual074Communication = ManagementManual074Communication;
    }

    public JsonJEVisClass getManagementManual075DocumentedInformation() {
        return ManagementManual075DocumentedInformation;
    }

    public void setManagementManual075DocumentedInformation(JsonJEVisClass ManagementManual075DocumentedInformation) {
        this.ManagementManual075DocumentedInformation = ManagementManual075DocumentedInformation;
    }

    public JsonJEVisClass getManagementManual0751General() {
        return ManagementManual0751General;
    }

    public void setManagementManual0751General(JsonJEVisClass ManagementManual0751General) {
        this.ManagementManual0751General = ManagementManual0751General;
    }

    public JsonJEVisClass getManagementManual0752CreatingAndUpdating() {
        return ManagementManual0752CreatingAndUpdating;
    }

    public void setManagementManual0752CreatingAndUpdating(JsonJEVisClass ManagementManual0752CreatingAndUpdating) {
        this.ManagementManual0752CreatingAndUpdating = ManagementManual0752CreatingAndUpdating;
    }

    public JsonJEVisClass getManagementManual0753ControlOfDocumenteInformation() {
        return ManagementManual0753ControlOfDocumenteInformation;
    }

    public void setManagementManual0753ControlOfDocumenteInformation(JsonJEVisClass ManagementManual0753ControlOfDocumenteInformation) {
        this.ManagementManual0753ControlOfDocumenteInformation = ManagementManual0753ControlOfDocumenteInformation;
    }

    public JsonJEVisClass getManagementManual08Operation() {
        return ManagementManual08Operation;
    }

    public void setManagementManual08Operation(JsonJEVisClass ManagementManual08Operation) {
        this.ManagementManual08Operation = ManagementManual08Operation;
    }

    public JsonJEVisClass getManagementManual081OperationalPlanningAndControl() {
        return ManagementManual081OperationalPlanningAndControl;
    }

    public void setManagementManual081OperationalPlanningAndControl(JsonJEVisClass ManagementManual081OperationalPlanningAndControl) {
        this.ManagementManual081OperationalPlanningAndControl = ManagementManual081OperationalPlanningAndControl;
    }

    public JsonJEVisClass getManagementManual082Design() {
        return ManagementManual082Design;
    }

    public void setManagementManual082Design(JsonJEVisClass ManagementManual082Design) {
        this.ManagementManual082Design = ManagementManual082Design;
    }

    public JsonJEVisClass getManagementManual083Procurement() {
        return ManagementManual083Procurement;
    }

    public void setManagementManual083Procurement(JsonJEVisClass ManagementManual083Procurement) {
        this.ManagementManual083Procurement = ManagementManual083Procurement;
    }

    public JsonJEVisClass getManagementManual09PerformanceEvaluation() {
        return ManagementManual09PerformanceEvaluation;
    }

    public void setManagementManual09PerformanceEvaluation(JsonJEVisClass ManagementManual09PerformanceEvaluation) {
        this.ManagementManual09PerformanceEvaluation = ManagementManual09PerformanceEvaluation;
    }

    public JsonJEVisClass getManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS() {
        return ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS;
    }

    public void setManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS(JsonJEVisClass ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS) {
        this.ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS = ManagementManual091MonitoringMeasurementAnalysisAndEvaluationForEnergyPerformanceAndTheEnMS;
    }

    public JsonJEVisClass getManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements() {
        return ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements;
    }

    public void setManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements(JsonJEVisClass ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements) {
        this.ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements = ManagementManual092EvaluationOfComplianceWithLegalAndOtherRequirements;
    }

    public JsonJEVisClass getManagementManual093InternalEnMSAudit() {
        return ManagementManual093InternalEnMSAudit;
    }

    public void setManagementManual093InternalEnMSAudit(JsonJEVisClass ManagementManual093InternalEnMSAudit) {
        this.ManagementManual093InternalEnMSAudit = ManagementManual093InternalEnMSAudit;
    }

    public JsonJEVisClass getManagementManual094ManagementReview() {
        return ManagementManual094ManagementReview;
    }

    public void setManagementManual094ManagementReview(JsonJEVisClass ManagementManual094ManagementReview) {
        this.ManagementManual094ManagementReview = ManagementManual094ManagementReview;
    }

    public JsonJEVisClass getManagementManual10Improvement() {
        return ManagementManual10Improvement;
    }

    public void setManagementManual10Improvement(JsonJEVisClass ManagementManual10Improvement) {
        this.ManagementManual10Improvement = ManagementManual10Improvement;
    }

    public JsonJEVisClass getManagementManual101NonconformityAndCorrectiveAction() {
        return ManagementManual101NonconformityAndCorrectiveAction;
    }

    public void setManagementManual101NonconformityAndCorrectiveAction(JsonJEVisClass ManagementManual101NonconformityAndCorrectiveAction) {
        this.ManagementManual101NonconformityAndCorrectiveAction = ManagementManual101NonconformityAndCorrectiveAction;
    }

    public JsonJEVisClass getManagementManual102ContinualImprovement() {
        return ManagementManual102ContinualImprovement;
    }

    public void setManagementManual102ContinualImprovement(JsonJEVisClass ManagementManual102ContinualImprovement) {
        this.ManagementManual102ContinualImprovement = ManagementManual102ContinualImprovement;
    }

    public JsonJEVisClass getLegalRegulation() {
        return LegalRegulation;
    }

    public void setLegalRegulation(JsonJEVisClass LegalRegulation) {
        this.LegalRegulation = LegalRegulation;
    }

    public JsonJEVisClass getManagementReview() {
        return ManagementReview;
    }

    public void setManagementReview(JsonJEVisClass ManagementReview) {
        this.ManagementReview = ManagementReview;
    }

    public JsonJEVisClass getTrainingCourse() {
        return TrainingCourse;
    }

    public void setTrainingCourse(JsonJEVisClass TrainingCourse) {
        this.TrainingCourse = TrainingCourse;
    }

    public JsonJEVisClass getTraining() {
        return Training;
    }

    public void setTraining(JsonJEVisClass Training) {
        this.Training = Training;
    }

    public JsonJEVisClass getEnergySource() {
        return EnergySource;
    }

    public void setEnergySource(JsonJEVisClass EnergySource) {
        this.EnergySource = EnergySource;
    }

    public JsonJEVisClass getEnergyBills() {
        return EnergyBills;
    }

    public void setEnergyBills(JsonJEVisClass EnergyBills) {
        this.EnergyBills = EnergyBills;
    }

    public JsonJEVisClass getEnergyConsumption() {
        return EnergyConsumption;
    }

    public void setEnergyConsumption(JsonJEVisClass EnergyConsumption) {
        this.EnergyConsumption = EnergyConsumption;
    }

    public JsonJEVisClass getEnergyFlowChart() {
        return EnergyFlowChart;
    }

    public void setEnergyFlowChart(JsonJEVisClass EnergyFlowChart) {
        this.EnergyFlowChart = EnergyFlowChart;
    }

    public JsonJEVisClass getEnergySavingAction() {
        return EnergySavingAction;
    }

    public void setEnergySavingAction(JsonJEVisClass EnergySavingAction) {
        this.EnergySavingAction = EnergySavingAction;
    }

    public JsonJEVisClass getAirConditionEquipmentDir() {
        return AirConditionEquipmentDir;
    }

    public void setAirConditionEquipmentDir(JsonJEVisClass AirConditionEquipmentDir) {
        this.AirConditionEquipmentDir = AirConditionEquipmentDir;
    }

    public JsonJEVisClass getCompressorEquipmentDir() {
        return CompressorEquipmentDir;
    }

    public void setCompressorEquipmentDir(JsonJEVisClass CompressorEquipmentDir) {
        this.CompressorEquipmentDir = CompressorEquipmentDir;
    }

    public JsonJEVisClass getCoolingEquipmentDir() {
        return CoolingEquipmentDir;
    }

    public void setCoolingEquipmentDir(JsonJEVisClass CoolingEquipmentDir) {
        this.CoolingEquipmentDir = CoolingEquipmentDir;
    }

    public JsonJEVisClass getHeatingEquipmentDir() {
        return HeatingEquipmentDir;
    }

    public void setHeatingEquipmentDir(JsonJEVisClass HeatingEquipmentDir) {
        this.HeatingEquipmentDir = HeatingEquipmentDir;
    }

    public JsonJEVisClass getLightingEquipmentDir() {
        return LightingEquipmentDir;
    }

    public void setLightingEquipmentDir(JsonJEVisClass LightingEquipmentDir) {
        this.LightingEquipmentDir = LightingEquipmentDir;
    }

    public JsonJEVisClass getOfficeEquipmentDir() {
        return OfficeEquipmentDir;
    }

    public void setOfficeEquipmentDir(JsonJEVisClass OfficeEquipmentDir) {
        this.OfficeEquipmentDir = OfficeEquipmentDir;
    }

    public JsonJEVisClass getPantryEquipmentDir() {
        return PantryEquipmentDir;
    }

    public void setPantryEquipmentDir(JsonJEVisClass PantryEquipmentDir) {
        this.PantryEquipmentDir = PantryEquipmentDir;
    }

    public JsonJEVisClass getProductionEquipmentDir() {
        return ProductionEquipmentDir;
    }

    public void setProductionEquipmentDir(JsonJEVisClass ProductionEquipmentDir) {
        this.ProductionEquipmentDir = ProductionEquipmentDir;
    }

    public JsonJEVisClass getVentilationEquipmentDir() {
        return VentilationEquipmentDir;
    }

    public void setVentilationEquipmentDir(JsonJEVisClass VentilationEquipmentDir) {
        this.VentilationEquipmentDir = VentilationEquipmentDir;
    }

    public JsonJEVisClass getProceduralDocument() {
        return ProceduralDocument;
    }

    public void setProceduralDocument(JsonJEVisClass ProceduralDocument) {
        this.ProceduralDocument = ProceduralDocument;
    }

    public JsonJEVisClass getEvaluatedOutput() {
        return EvaluatedOutput;
    }

    public void setEvaluatedOutput(JsonJEVisClass EvaluatedOutput) {
        this.EvaluatedOutput = EvaluatedOutput;
    }

    public JsonJEVisClass getMeasuringPoint() {
        return MeasuringPoint;
    }

    public void setMeasuringPoint(JsonJEVisClass MeasuringPoint) {
        this.MeasuringPoint = MeasuringPoint;
    }

    public JsonJEVisClass getMeter() {
        return Meter;
    }

    public void setMeter(JsonJEVisClass Meter) {
        this.Meter = Meter;
    }

    public JsonJEVisClass getProceduralDocumentsDir() {
        return ProceduralDocumentsDir;
    }

    public void setProceduralDocumentsDir(JsonJEVisClass ProceduralDocumentsDir) {
        this.ProceduralDocumentsDir = ProceduralDocumentsDir;
    }

    public JsonJEVisClass getStation() {
        return Station;
    }

    public void setStation(JsonJEVisClass Station) {
        this.Station = Station;
    }

    public JsonJEVisClass getMonthlyValues() {
        return MonthlyValues;
    }

    public void setMonthlyValues(JsonJEVisClass MonthlyValues) {
        this.MonthlyValues = MonthlyValues;
    }

    public JsonJEVisClass getEnergyManager() {
        return EnergyManager;
    }

    public void setEnergyManager(JsonJEVisClass EnergyManager) {
        this.EnergyManager = EnergyManager;
    }

    public JsonJEVisClass getEnergyTeamMember() {
        return EnergyTeamMember;
    }

    public void setEnergyTeamMember(JsonJEVisClass EnergyTeamMember) {
        this.EnergyTeamMember = EnergyTeamMember;
    }

    public JsonJEVisClass getDocs() {
        return docs;
    }

    public void setDocs(JsonJEVisClass docs) {
        this.docs = docs;
    }

}
