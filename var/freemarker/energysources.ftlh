<div class="css-treeview">
    <ul>
        <li><a href="#" onclick="connect('./esoverview?site=${siteName}', '${bauth}', 'content-form')"><i
                class="fa fa-clipboard-list"></i> ${esoverview}</a>
        </li>
    </ul>
</div>
<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${EnergySourcesDirName}</label>
                <ul>
					<#list energysources as es>				
					<ul>
                        <li style="padding:  4px;"><input type="checkbox" id="item-0-${es?index}"/><label class="toggle"
                                                                                                          for="item-0-${es?index}"><i
                                class="fa fa-angle-right"></i> <a style="display: inline;" href="#"
                                                                  onclick="connect('./object?ID=${es.ID?c}', '${bauth}', 'content-form')">${es.name}</a></label>
                            <ul>
								<#list es.energyconsumptions as ec>
								<ul>
                                    <li style="padding:  4px;"><input type="checkbox"
                                                                      id="item-0-${es?index}-${ec?index}"/><label
                                            class="toggle" for="item-0-${es?index}-${ec?index}"><i
                                            class="fa fa-angle-right"></i> ${ec.year?c}</label>
                                        <ul>
                                            <li><a href="#"
                                                   onclick="connect('./object?ID=${ec.ID?c}', '${bauth}', 'content-form')"><i
                                                    class="fa fa-edit"></i> ${ec.name}</a></li>
                                            <li><a href="#"
                                                   onclick="connect('./object?ID=${ec.billsID?c}', '${bauth}', 'content-form')"><i
                                                    class="fa fa-edit"></i> ${ec.nameBills}</a></li>
                                        </ul>
                                    </li>
                                </ul>
                                </#list>
                                <li><a class="add-item" href=""
                                       onclick="createObject('Energy Consumption', ${es.ID?c}, 'energysources', '${siteName}', '${bauth}'); createObject('Energy Bills', ${es.ID?c}, 'energysources', '${siteName}', '${bauth}'); return false;"><i
                                        class="fa fa-plus-square"></i> ${addEnergyConsumptionBills}</a></li>

                                <li><a style="" href="" onclick="removeObject(${es.ID?c}, '${bauth}'); return false;"><i
                                        class="fa fa-minus-square"></i> ${removeEnergySource}</a></li>
                            </ul>
                        </li>
                    </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Energy Source', ${EnergySourcesDirID?c}, 'energysources', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addEnergySource}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>