<nav class="vertical-navbar ">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${ActionPlansDirName}</label>
                <ul>
					<#list listYears as year>				
						<ul>
                            <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                    class="toggle" for="item-0-${year?index}"><i
                                    class="fa fa-angle-right"></i> ${year?c}</label>
                                <ul>
									<#list actionplans as p>
									<#if p.year == year>
									<li style="padding: 4px;"><input type="checkbox"
                                                                     id="item-0-${year?index}-${p?index}"/><label
                                            class="toggle" for="item-0-${year?index}-${p?index}"><i
                                            class="fa fa-angle-right"></i><a style="display: inline;" href="#"
                                                                             onclick="connect('./object?ID=${p.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-exclamation-triangle"></i> ${p.name}</a></label>
                                        <ul>
                                            <li style="padding:  4px;"><input type="checkbox"
                                                                              id="item-0-0-${year?index}-${p?index}"/><label
                                                    class="toggle" for="item-0-0-${year?index}-${p?index}"><i
                                                    class="fa fa-angle-right"></i> ${p.implementedactionsdirname}
                                            </label>
                                                <ul>
													<#list p.implementedactions as ia>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${ia.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${ia.name}</a></li>
                                                    </#list>
                                                    <li><i class="fa fa-plus-square"></i> <a class="add-item" href=""
                                                                                             onclick="createObject('Energy Saving Action', ${p.implementedactionsdirid?c}, 'actionplans', '${siteName}', '${bauth}'); return false;">${addImplementedAction}</a>
                                                    </li>
                                                </ul>
                                            </li>
                                            <li style="padding:  4px;"><input type="checkbox"
                                                                              id="item-0-1-${year?index}-${p?index}"/><label
                                                    class="toggle" for="item-0-1-${year?index}-${p?index}"><i
                                                    class="fa fa-angle-right"></i> ${p.plannedactionsdirname}</label>
                                                <ul>
													<#list p.plannedactions as pa>
														<li><a href="#"
                                                               onclick="connect('./object?ID=${pa.ID?c}', '${bauth}', 'content-form')"><i
                                                                class="fa fa-edit"></i> ${pa.name}</a></li>
                                                    </#list>
                                                    <li><i class="fa fa-plus-square"></i> <a class="add-item" href=""
                                                                                             onclick="createObject('Energy Saving Action', ${p.plannedactionsdirid?c}, 'actionplans', '${siteName}', '${bauth}'); return false;">${addPlannedAction}</a>
                                                    </li>
                                                </ul>
                                            </li>
                                            <li class="add-item"><a
                                                    href="actionplans/print?site=${siteName}&id=${p.ID?c}"
                                                    target="blank"><i class="fa fa-print"></i> Print</a></li>
                                        </ul>
                                    </li>
                                    </#if>
                                    </#list>
                                </ul>
                            </li>
                        </ul>
                    </#list>
                    <li><i class="fa fa-plus-square"></i> <a class="add-item" href=""
                                                             onclick="createActionPlan('${bauth}', ${ActionPlansDirectoryID?c}, '${siteName}'); return false;">${addActionPlan}</a>
                    </li>
                </ul>
            </li>
        </ul>
    </div>
</nav>