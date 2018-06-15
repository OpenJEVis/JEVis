<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${AuditsDirectoryName}</label>
                <ul>
                    <li style="padding:  4px;"><input type="checkbox" id="item-0-0"/><label class="toggle"
                                                                                            for="item-0-0"><i
                            class="fa fa-angle-right"></i> ${externalAudits}</label>
                        <ul>
							<#list listEAYears as eayear>				
							<ul>
                                <li style="padding:  4px;"><input type="checkbox" id="item-0-0-${eayear?index}"/><label
                                        class="toggle" for="item-0-0-${eayear?index}"><i
                                        class="fa fa-angle-right"></i> ${eayear?c}</label>
                                    <ul>
										<#list externalaudits as ea>
										<#if ea.year == eayear>
										<li><a href="#"
                                               onclick="connect('./object?ID=${ea.ID?c}', '${bauth}', 'content-form')"><i
                                                class="fa fa-edit"></i> ${ea.name}</a></li>
                                        </#if>
                                        </#list>
                                    </ul>
                                </li>
                            </ul>
                            </#list>
                            <li><a class="add-item" href=""
                                   onclick="createObject('External Audit', '${AuditsDirectoryID?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                    class="fa fa-plus-square"></i> ${addExternalAudit}</a></li>
                        </ul>
                    </li>
                    <li style="padding:  4px;"><input type="checkbox" id="item-0-1"/><label class="toggle"
                                                                                            for="item-0-1"><i
                            class="fa fa-angle-right"></i> ${internalAudits}</label>
                        <ul>
							<#list listIAYears as iayear>				
							<ul>
								<li style="padding:  4px;"><input type="checkbox" id="item-0-1-${iayear?index}"/><label
                                        class="toggle" for="item-0-1-${iayear?index}"><i
                                        class="fa fa-angle-right"></i> ${iayear?c}</label>
                                    <ul>
										<#list internalaudits as ia>
										<#if ia.year == iayear>
										<li style="padding:  4px;"><input type="checkbox"
                                                                          id="item-0-1-${iayear?index}-${ia?index}"/><label
                                                class="toggle" for="item-0-1-${iayear?index}-${ia?index}"><i
                                                class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                                                  onclick="connect('./object?ID=${ia.ID?c}', '${bauth}', 'content-form')">${ia.name}</a></label>
                                            <ul>
                                                <li style="padding:  4px;"><input type="checkbox"
                                                                                  id="item-0-1-${iayear?index}-${ia?index}-0"/><label
                                                        class="toggle" for="item-0-1-${iayear?index}-${ia?index}-0"><i
                                                        class="fa fa-angle-right"></i> ${ia.generalquestionsname}
                                                </label>
                                                    <ul>
														<#if ia.generalquestions?has_content>
                                                            <#list ia.generalquestions as gaq>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${gaq.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${gaq.name}</a></li>
                                                            </#list></#if>
                                                        <li><a class="add-item" href=""
                                                               onclick="createObject('Audit Question', '${ia.generalquestionsid?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                                                class="fa fa-plus-square"></i> ${addAuditQuestion}</a>
                                                        </li>
                                                    </ul>
                                                </li>
                                                <li style="padding:  4px;"><input type="checkbox"
                                                                                  id="item-0-1-${iayear?index}-${ia?index}-1"/><label
                                                        class="toggle" for="item-0-1-${iayear?index}-${ia?index}-1"><i
                                                        class="fa fa-angle-right"></i> ${ia.planquestionsname}</label>
                                                    <ul>
														<#if ia.planquestions?has_content>
                                                            <#list ia.planquestions as paq>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${paq.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${paq.name}</a></li>
                                                            </#list></#if>
                                                        <li><a class="add-item" href=""
                                                               onclick="createObject('Audit Question', '${ia.planquestionsid?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                                                class="fa fa-plus-square"></i> ${addAuditQuestion}</a>
                                                        </li>
                                                    </ul>
                                                </li>
                                                <li style="padding:  4px;"><input type="checkbox"
                                                                                  id="item-0-1-${iayear?index}-${ia?index}-2"/><label
                                                        class="toggle" for="item-0-1-${iayear?index}-${ia?index}-2"><i
                                                        class="fa fa-angle-right"></i> ${ia.doquestionsname}</label>
                                                    <ul>
														<#if ia.doquestions?has_content>
                                                            <#list ia.doquestions as daq>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${daq.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${daq.name}</a></li>
                                                            </#list></#if>
                                                        <li><a class="add-item" href=""
                                                               onclick="createObject('Audit Question', '${ia.doquestionsid?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                                                class="fa fa-plus-square"></i> ${addAuditQuestion}</a>
                                                        </li>
                                                    </ul>
                                                </li>
                                                <li style="padding:  4px;"><input type="checkbox"
                                                                                  id="item-0-1-${iayear?index}-${ia?index}-3"/><label
                                                        class="toggle" for="item-0-1-${iayear?index}-${ia?index}-3"><i
                                                        class="fa fa-angle-right"></i> ${ia.checkquestionsname}</label>
                                                    <ul>
                                                        <#if ia.checkquestions?has_content>
                                                            <#list ia.checkquestions as caq>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${caq.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${caq.name}</a></li>
                                                            </#list></#if>
                                                        <li><a class="add-item" href=""
                                                               onclick="createObject('Audit Question', '${ia.checkquestionsid?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                                                class="fa fa-plus-square"></i> ${addAuditQuestion}</a>
                                                        </li>
                                                    </ul>
                                                </li>
                                                <li style="padding:  4px;"><input type="checkbox"
                                                                                  id="item-0-1-${iayear?index}-${ia?index}-4"/><label
                                                        class="toggle" for="item-0-1-${iayear?index}-${ia?index}-4"><i
                                                        class="fa fa-angle-right"></i> ${ia.actquestionsname}</label>
                                                    <ul>
                                                      <#if ia.actquestions?has_content>
                                                          <#list ia.actquestions as aaq>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${aaq.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${aaq.name}</a></li>
                                                          </#list></#if>
                                                        <li><a class="add-item" href=""
                                                               onclick="createObject('Audit Question', '${ia.actquestionsid?c}', 'audits', '${siteName}', '${bauth}'); return false;"><i
                                                                class="fa fa-plus-square"></i> ${addAuditQuestion}</a>
                                                        </li>
                                                    </ul>
                                                </li>
                                                <li><a style="" href=""
                                                       onclick="removeObject(${ia.ID?c}, '${bauth}')"><i
                                                        class="fa fa-minus-square"></i> Remove Audit</a></li>
                                            </ul>
                                        </li>
                                        </#if>
                                        </#list>
                                    </ul>
                                </li>
                            </#list>
                            <li><a class="add-item" href=""
                                   onclick="createInternalAudit('${bauth}', '${AuditsDirectoryID?c}', '${siteName}'); return false;"><i
                                    class="fa fa-plus-square"></i> ${addInternalAudit}</a></li>
                        </ul>
                        </ul>
                    </li>
                </ul>
            </li>
        </ul>
    </div>
</nav>