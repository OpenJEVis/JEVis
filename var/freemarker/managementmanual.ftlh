<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${ManagementManualDirectoryName}</label>
                <ul>
                    <li style="padding:  4px;"><input type="checkbox" id="item-0-0"/><label for="item-0-0"><a
                            style="display: inline;" href="#"
                            onclick="connect('./object?ID=${scope.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${scope.name}</a></label></li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-1"/><label for="item-0-1"><a
                            style="display: inline;" href="#"
                            onclick="connect('./object?ID=${normativeReferences.ID?c}', '${bauth}', 'content-form')"><i
                            class="fa fa-edit"></i> ${normativeReferences.name}</a></label></li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-2"/><label class="toggle"
                                                                                            for="item-0-2"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${termsAndDefinitions.ID?c}', '${bauth}', 'content-form')">${termsAndDefinitions.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${termsOrganization.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${termsOrganization.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${termsManagementSystem.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${termsManagementSystem.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${termsRequirements.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${termsRequirements.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${termsPerformance.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${termsPerformance.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${termsEnergy.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${termsEnergy.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-3"/><label class="toggle"
                                                                                            for="item-0-3"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${contextOfTheOrganization.ID?c}', '${bauth}', 'content-form')">${contextOfTheOrganization.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${contextEnergyManagementSystem.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${contextEnergyManagementSystem.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${contextUnderstandingOrganization.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${contextUnderstandingOrganization.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${contextUnderstandingNeeds.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${contextUnderstandingNeeds.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${contextScopeAndBoundaries.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${contextScopeAndBoundaries.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-4"/><label class="toggle"
                                                                                            for="item-0-4"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${leadership.ID?c}', '${bauth}', 'content-form')">${leadership.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${leadershipAndCommitment.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${leadershipAndCommitment.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${leadershipEnergyPolicy.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${leadershipEnergyPolicy.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${leadershipRolesResponsibilities.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${leadershipRolesResponsibilities.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-5"/><label class="toggle"
                                                                                            for="item-0-5"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${planning.ID?c}', '${bauth}', 'content-form')">${planning.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningGeneral.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningGeneral.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningRisks.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningRisks.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningEnergyReview.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningEnergyReview.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningEnergyPerformanceIndicators.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningEnergyPerformanceIndicators.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningEnergyBaseline.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningEnergyBaseline.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningObjectives.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningObjectives.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${planningEnergyDataCollection.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${planningEnergyDataCollection.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-6"/><label class="toggle"
                                                                                            for="item-0-6"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${support.ID?c}', '${bauth}', 'content-form')">${support.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${supportResources.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${supportResources.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${supportCompetence.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${supportCompetence.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${supportAwareness.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${supportAwareness.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${supportCommunication.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${supportCommunication.name}</a></li>

                            <li style="padding:  4px;"><input type="checkbox" id="item-0-6-0"/><label class="toggle"
                                                                                                      for="item-0-6-0"><i
                                    class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                                      onclick="connect('./object?ID=${supportDocumentedInformation.ID?c}', '${bauth}', 'content-form')">${supportDocumentedInformation.name}</a></label>
                                <ul>
                                    <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                                  onclick="connect('./object?ID=${supportDocumentedInformationGeneral.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-edit"></i> ${supportDocumentedInformationGeneral.name}</a></li>
                                    <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                                  onclick="connect('./object?ID=${supportDocumentedInformationCreatingUpdating.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-edit"></i> ${supportDocumentedInformationCreatingUpdating.name}
                                    </a></li>
                                    <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                                  onclick="connect('./object?ID=${supportDocumentedInformationControl.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-edit"></i> ${supportDocumentedInformationControl.name}</a></li>
                                </ul>
                            </li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-7"/><label class="toggle"
                                                                                            for="item-0-7"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${operation.ID?c}', '${bauth}', 'content-form')">${operation.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${operationPlanning.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${operationPlanning.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${operationDesign.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${operationDesign.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${operationProcurement.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${operationProcurement.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-8"/><label class="toggle"
                                                                                            for="item-0-8"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${performanceEvaluation.ID?c}', '${bauth}', 'content-form')">${performanceEvaluation.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${performanceEvaluationMonitoring.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${performanceEvaluationMonitoring.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${performanceEvaluationCompliance.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${performanceEvaluationCompliance.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${performanceEvaluationInternalAudit.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${performanceEvaluationInternalAudit.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${performanceEvaluationManagementReview.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${performanceEvaluationManagementReview.name}</a></li>
                        </ul>
                    </li>

                    <li style="padding:  4px;"><input type="checkbox" id="item-0-9"/><label class="toggle"
                                                                                            for="item-0-9"><i
                            class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                              onclick="connect('./object?ID=${improvement.ID?c}', '${bauth}', 'content-form')">${improvement.name}</a></label>
                        <ul>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${improvementNonConformity.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${improvementNonConformity.name}</a></li>
                            <li style="padding:  4px;"><a style="display: inline;" href="#"
                                                          onclick="connect('./object?ID=${improvementContinualImprovement.ID?c}', '${bauth}', 'content-form')"><i
                                    class="fa fa-edit"></i> ${improvementContinualImprovement.name}</a></li>
                        </ul>
                    </li>
                </ul>
            </li>
        </ul>
    </div>
</nav>