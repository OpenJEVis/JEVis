<div class="css-treeview">
    <ul>
        <li><a href="#" onclick="connect('./productionoverview?site=${siteName}', '${bauth}', 'content-form')"><i
                class="fa fa-clipboard-list"></i> ${productionoverview}</a>
        </li>
    </ul>
</div>
<nav class="vertical-navbar resticons">
    <div class="css-treeview row">
        <ul>
            <li style="padding:  4px;"><input type="checkbox" id="item-0" checked/><label
                    for="item-0">${ProductionDirName}</label>
                <ul>
					<#list listYears as year>				
					<ul>
                        <li style="padding:  4px;"><input type="checkbox" id="item-0-${year?index}"/><label
                                class="toggle" for="item-0-${year?index}"><i class="fa fa-angle-right"></i> ${year?c}
                        </label>
                            <ul>
							<#list listProduction as prod>
							<#if prod.year == year>
								<li><a href="#"
                                       onclick="connect('./object?ID=${prod.ID?c}', '${bauth}', 'content-form')"><i
                                        class="fa fa-edit"></i> ${prod.name}</a></li>
                            </#if>
                            </#list>
                            </ul>
                        </li>
                    </ul>
                    </#list>
                    <li><a class="add-item" href=""
                           onclick="createObject('Evaluated Output', ${ProductionDirID?c}, 'production', '${siteName}', '${bauth}'); return false;"><i
                            class="fa fa-plus-square"></i> ${addProduce}</a></li>
                </ul>
            </li>
        </ul>
    </div>
</nav>