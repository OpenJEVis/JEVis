<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${EnergyFlowChartsDirName}</label>
                <ul>
					<#list listYears as year>				
						<ul>
                            <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                    class="toggle" for="item-0-${year?index}"><i
                                    class="fa fa-angle-right"></i> ${year?c}</label>
                                <ul>
									<#list energyflowcharts as efc>
									<#if efc.year == year>
									<li><a href="#"
                                           onclick="connect('./object?ID=${efc.ID?c}', '${bauth}', 'content-form')"><i
                                            class="fa fa-edit"></i> ${efc.name}</a></li>
                                    </#if>
                                    </#list>
                                </ul>
                            </li>
                        </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Energy Flow Chart', ${EnergyFlowChartsDirID?c}, 'energyflowcharts', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addEnergyFlowChart}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>